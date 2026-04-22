package com.apiCodeGenerator.dynamicMaster.v2.generator.engine;

import com.apiCodeGenerator.dynamicMaster.v2.generator.config.FieldConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.ModuleConfig;
import com.apiCodeGenerator.dynamicMaster.v2.generator.config.UniqueConstraint;
import com.apiCodeGenerator.dynamicMaster.v2.generator.core.NamingUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 SchemaParserService — SQL DDL to ModuleConfig
 * ============================================================
 * Takes a raw CREATE TABLE SQL script and converts it into a
 * draft ModuleConfig object.
 *
 * This allows developers to start from a DB schema and get a
 * code-gen JSON automatically.
 */
@Slf4j
@Service
public class SchemaParserService {

    /**
     * Parse a CREATE TABLE statement into a ModuleConfig.
     *
     * @param sql The SQL DDL string
     * @return A populated ModuleConfig draft
     */
    public ModuleConfig parseSql(String sql) throws Exception {
        log.info("Parsing SQL DDL for module generation...");
        
        Statement stmt = CCJSqlParserUtil.parse(sql);
        if (!(stmt instanceof CreateTable createTable)) {
            throw new IllegalArgumentException("Only CREATE TABLE statements are supported.");
        }

        ModuleConfig config = new ModuleConfig();
        
        // 1. Table & Schema Name
        String fullTableName = unquote(createTable.getTable().getName());
        String schemaName = createTable.getTable().getSchemaName();
        config.setTableName(fullTableName);
        config.setSchemaName(schemaName != null ? unquote(schemaName) : "public");
        
        // 2. Module & Package Guessing
        String rawName = stripPrefix(fullTableName);
        config.setModuleName(rawName);
        config.setPackageName("com.apiCodeGenerator." + rawName.replace("_", "."));
        
        // 3. Columns to Fields
        List<FieldConfig> fields = new ArrayList<>();
        List<ColumnDefinition> colDefs = createTable.getColumnDefinitions();
        
        for (ColumnDefinition col : colDefs) {
            String colName = unquote(col.getColumnName());
            
            // Skip automated fields - they will be added by the generator based on config
            if (isAutomatedField(colName)) continue;
            
            FieldConfig field = new FieldConfig();
            field.setColumnName(colName);
            field.setName(NamingUtil.decapitalize(NamingUtil.toPascalCase(colName)));
            
            // Type Mapping
            String dbType = col.getColDataType().getDataType();
            field.setType(mapSqlToJavaType(dbType));
            
            // Constraints
            field.setNullable(!isNotNull(col));
            field.setUnique(isUnique(col));
            
            // Primary Key Check
            if (isPrimaryKey(col, createTable, colName)) {
                config.setPrimaryKey(field.getName());
                config.setPrimaryKeyType(field.getType());
                // If it's PK, we don't put it in the "fields" list as generator handles it separately
                continue;
            }

            // UUID Field Guess
            if (colName.toLowerCase().contains("uuid")) {
                config.setUuidField(field.getName());
                continue;
            }

            fields.add(field);
        }
        
        config.setFields(fields);

        // 4. Extract Key/Unique constraints from table-level definitions
        if (createTable.getIndexes() != null) {
            for (Index index : createTable.getIndexes()) {
                if ("UNIQUE".equalsIgnoreCase(index.getType())) {
                    UniqueConstraint uc = new UniqueConstraint();
                    uc.setName(index.getName() != null ? unquote(index.getName()) : "uq_" + config.getTableName() + "_" + System.currentTimeMillis());
                    uc.setFields(index.getColumnsNames().stream()
                            .map(this::unquote)
                            .map(c -> NamingUtil.decapitalize(NamingUtil.toPascalCase(c)))
                            .collect(Collectors.toList()));
                    uc.setMessage(NamingUtil.toPascalCase(config.getModuleName()) + " already exists with these values.");
                    config.getUniqueConstraints().add(uc);
                }
            }
        }

        log.info("Successfully parsed SQL for module: {}", config.getModuleName());
        return config;
    }

    private String unquote(String name) {
        if (name == null) return null;
        return name.replace("\"", "").replace("`", "").replace("[", "").replace("]", "");
    }

    private String stripPrefix(String tableName) {
        if (tableName.startsWith("mst_")) return tableName.substring(4);
        if (tableName.startsWith("txn_")) return tableName.substring(4);
        if (tableName.startsWith("sys_")) return tableName.substring(4);
        return tableName;
    }

    private boolean isAutomatedField(String colName) {
        String lower = colName.toLowerCase();
        return List.of("status", "created_at", "updated_at", "deleted_at", "created_by", 
                "updated_by", "deleted_by", "audit_tracker_id").contains(lower);
    }

    private String mapSqlToJavaType(String dbType) {
        if (dbType == null) return "String";
        String type = dbType.toUpperCase();
        
        if (type.contains("VARCHAR") || type.contains("TEXT") || type.contains("CHAR") || type.contains("JSON") || type.contains("CLOB")) return "String";
        if (type.contains("BIGINT") || type.contains("BIGSERIAL") || type.contains("INT8")) return "Long";
        if (type.contains("SMALLINT") || type.contains("SMALLSERIAL") || type.contains("INT2")) return "Short";
        if (type.contains("INT") || type.contains("INTEGER") || type.contains("SERIAL") || type.contains("INT4") || type.contains("SERIAL4")) return "Integer";
        if (type.contains("BOOLEAN") || type.contains("BIT") || type.contains("BOOL")) return "Boolean";
        if (type.contains("DECIMAL") || type.contains("NUMERIC") || type.contains("MONEY")) return "BigDecimal";
        if (type.contains("DOUBLE") || type.contains("FLOAT8")) return "Double";
        if (type.contains("REAL") || type.contains("FLOAT4") || type.contains("FLOAT")) return "Float";
        if (type.contains("TIMESTAMP") || type.contains("DATETIME")) return "LocalDateTime";
        if (type.contains("TIME") && !type.contains("TIMESTAMP")) return "LocalTime";
        if (type.contains("DATE")) return "LocalDate";
        if (type.contains("UUID")) return "UUID";
        if (type.contains("BYTEA") || type.contains("BLOB") || type.contains("BINARY")) return "byte[]";
        
        return "String"; // Default
    }

    private boolean isNotNull(ColumnDefinition col) {
        if (col.getColumnSpecs() == null) return false;
        String specs = String.join(" ", col.getColumnSpecs()).toUpperCase();
        return specs.contains("NOT NULL");
    }

    private boolean isUnique(ColumnDefinition col) {
        if (col.getColumnSpecs() == null) return false;
        String specs = String.join(" ", col.getColumnSpecs()).toUpperCase();
        return specs.contains("UNIQUE");
    }

    private boolean isPrimaryKey(ColumnDefinition col, CreateTable createTable, String unquotedColName) {
        // Check inline spec
        if (col.getColumnSpecs() != null) {
            String specs = String.join(" ", col.getColumnSpecs()).toUpperCase();
            if (specs.contains("PRIMARY KEY")) return true;
        }
        
        // Check table-level constraint
        if (createTable.getIndexes() != null) {
             for (Index index : createTable.getIndexes()) {
                 if ("PRIMARY KEY".equalsIgnoreCase(index.getType())) {
                     for (String cName : index.getColumnsNames()) {
                         if (unquote(cName).equalsIgnoreCase(unquotedColName)) {
                             return true;
                         }
                     }
                 }
             }
        }
        return false;
    }
}
