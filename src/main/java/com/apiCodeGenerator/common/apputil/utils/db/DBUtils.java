package com.apiCodeGenerator.common.apputil.utils.db;

import com.apiCodeGenerator.common.apputil.exception.custom.CustomDBUtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Component
public class DBUtils {

    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    private final DataSource dataSource;
//    private final Set<String> allowedTables;
//    private final Set<String> allowedColumns;


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DBUtils(DataSource dataSource, Set<String> allowedTables, Set<String> allowedColumns, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.dataSource = dataSource;
//        this.allowedTables = allowedTables;
//        this.allowedColumns = allowedColumns;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // Public methods

    public List<Map<String, Object>> getResults(String query, List<Object> params) {
        try (Connection conn = dataSource.getConnection()) {
            return execute(conn, query, params);
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

/*    public <T> List<T> getResults(String query, List<Object> params, Class<T> classType) {
        try {
            List<Map<String, Object>> resultSet;
            try (Connection conn = dataSource.getConnection()) {
                resultSet = execute(conn, query, params);
            }
            return MapperUtils.convert(resultSet, classType);
        } catch (Exception e) {
            log.error("Error mapping result: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }*/

    public <T> List<T> getResult(String query, Map<String, Object> paramMap, Class<T> classType) {
        try {
            return namedParameterJdbcTemplate.query(query.toString(), paramMap, new BeanPropertyRowMapper<>(classType));
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public <T> List<T> getResult(
            String query,
            Map<String, Object> paramMap,
            RowMapper<T> mapper
    ) {
        return namedParameterJdbcTemplate.query(query, paramMap, mapper);
    }

    public List<Map<String, Object>> getResult(String query, Map<String, Object> paramMap) {
        try {
            return namedParameterJdbcTemplate.query(
                    query,
                    paramMap,
                    new org.springframework.jdbc.core.ColumnMapRowMapper()
            );
        } catch (Exception e) {
            log.error("Error executing map query: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public <T> List<T> getSingleColumnResult(String query, Map<String, Object> paramMap, Class<T> classType) {
        try {
            return namedParameterJdbcTemplate.query(
                    query,
                    new MapSqlParameterSource(paramMap),
                    new SingleColumnRowMapper<>(classType)
            );
        } catch (Exception e) {
            log.error("Error executing single column query: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public int getCount(String query, Map<String, Object> paramMap) {
        try {
            return namedParameterJdbcTemplate.queryForObject(query.toString(), paramMap, Integer.class);
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return 0;
        }
    }

    public Map<String, Object> getResult(String query, List<Object> params) throws CustomDBUtilsException {
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> resultSet = execute(conn, query, params);
            if (resultSet.isEmpty()) return Collections.emptyMap();
            if (resultSet.size() > 1) throw new CustomDBUtilsException("Multiple results found for query");
            return resultSet.get(0);
        } catch (CustomDBUtilsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching result: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /*public <T> T getResult(String query, List<Object> params, Class<T> classType) throws CustomDBUtilsException {
        try (Connection conn = dataSource.getConnection()) {
            List<Map<String, Object>> resultSet = execute(conn, query, params);

            if (resultSet.isEmpty()) return null;
            if (resultSet.size() > 1) throw new CustomDBUtilsException("Multiple results found for query");

            return MapperUtils.convert(resultSet.get(0), classType);
        } catch (CustomDBUtilsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching result: {}", e.getMessage(), e);
            return null;
        }
    }*/

/*    public <T> T findById(String tableName, String primaryKeyColumn, Object primaryKeyValue, Class<T> classType) throws CustomDBUtilsException {
        validateTableAndColumns(tableName, primaryKeyColumn);

        String query = "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        return getResult(query, List.of(primaryKeyValue), classType);
    }

/*    public <T> T findByIds(String tableName, String[] primaryKeyColumns, Object[] primaryKeyValues, Class<T> classType) throws CustomDBUtilsException {
        validateTableAndColumns(tableName, primaryKeyColumns);

        StringBuilder whereClause = buildWhereClause(primaryKeyColumns);
        String query = "SELECT * FROM " + tableName + whereClause;

        return getResult(query, Arrays.asList(primaryKeyValues), classType);
    }*/

    public int getCount(String countQuery, List<Object> params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(countQuery)) {

            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            log.error("Error fetching count: {}", e.getMessage(), e);
            return 0;
        }
    }

    // Core execution logic

    private List<Map<String, Object>> execute(Connection conn, String query, List<Object> params) {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    resultList.add(row);
                }
                return resultList;
            }
        } catch (SQLException e) {
            log.error("\nWhile Exicuting : \n[{}] \nError fetching Result : {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }

    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    // Validation

//    private void validateTableAndColumns(String tableName, String... columns) {
//        if (!allowedTables.contains(tableName)) {
//            throw new IllegalArgumentException("Table not allowed: " + tableName);
//        }
//        for (String col : columns) {
//            if (!allowedColumns.contains(col)) {
//                throw new IllegalArgumentException("Column not allowed: " + col);
//            }
//        }
//    }

    // Helper

//    private StringBuilder buildWhereClause(String[] columns) {
//        String clause = Arrays.stream(columns)
//                .map(col -> col + " = ?")
//                .collect(Collectors.joining(" AND "));
//        return new StringBuilder(" WHERE ").append(clause);
//    }

    public List<Map<String, Object>> returnResultSet(String query, Map<String, Object> keysAndValues) throws Exception {
        Assert.notNull(query, "Sql Query Should Not Null");
        if (query.trim().isEmpty()) {
            throw new IllegalArgumentException("Sql Query Should Not Empty");
        }
        if (keysAndValues == null) {
            keysAndValues = Collections.emptyMap();
        }
        if (log.isDebugEnabled()) {
            log.debug("Executing query: {}", query);
        }
        // Executes query with named parameters and returns list of maps (each map is a row)
        return namedParameterJdbcTemplate.queryForList(query, keysAndValues);
    }

    public List<Object[]> getResults(String query, Map<String, Object> keysAndValues) throws Exception {
        Assert.notNull(query, "Sql Query Should Not Null");
        if (query.trim().isEmpty()) {
            throw new IllegalArgumentException("Sql Query Should Not Empty");
        }
        if (keysAndValues == null) {
            keysAndValues = Collections.emptyMap();
        }
        if (log.isDebugEnabled()) {
            log.debug("Executing query: {}", query);
        }
        // Executes query with named parameters and returns list of maps (each map is a row)
        return namedParameterJdbcTemplate.query(query, keysAndValues, (rs, rowNum) -> {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1); // JDBC column indexes start at 1
            }
            return row;
        });
    }

    public List<Map<String, Object>> getResultsMap(String query, Map<String, Object> keysAndValues) throws Exception {

        Assert.notNull(query, "Sql Query Should Not Null");

        if (query.trim().isEmpty()) {
            throw new IllegalArgumentException("Sql Query Should Not Empty");
        }

        if (keysAndValues == null) {
            keysAndValues = Collections.emptyMap();
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing query: {}", query);
        }

        return namedParameterJdbcTemplate.query(query, keysAndValues, (rs, rowNum) -> {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Map<String, Object> row = new HashMap<>(columnCount);

            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }

            return row;
        });
    }


}