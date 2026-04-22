package com.apiCodeGenerator.common.apputil.dropdown.dynamic.repository;

import com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response.DynamicDropdownResponse;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.enums.DynamicDropdownType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DynamicDropdownRepository {

    private final JdbcTemplate jdbcTemplate;
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    //    @Cacheable(value = "dropdownCache", key = "#type.type + '_' + (#parentId != null ? #parentId : 'all') + '_' + (#search != null ? #search : '') + '_' + (#filters != null ? #filters.hashCode() : '')",
//            unless = "#result.isEmpty()")
    public List<DynamicDropdownResponse> getDropdown(
            DynamicDropdownType type,
            UUID parentId,
            String search,
            Map<String, Object> filters) {

        QueryInfo queryInfo = buildQueryWithFilters(type, parentId, search, filters);
        log.debug("Executing query: {} with params: {}", queryInfo.sql, queryInfo.params);

        return jdbcTemplate.query(
                queryInfo.sql,
                ps -> {
                    for (int i = 0; i < queryInfo.params.size(); i++) {
                        ps.setObject(i + 1, queryInfo.params.get(i));
                    }
                },
                new DynamicRowMapper(type.getResponseKeys())
        );
    }

    @Cacheable(value = "dropdownPaginationCache", key = "#type.type + '_' + (#parentId != null ? #parentId : 'all') + '_' + (#search != null ? #search : '') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            unless = "#result.isEmpty()")
    public Page<DynamicDropdownResponse> getPaginatedDropdown(
            DynamicDropdownType type,
            UUID parentId,
            String search,
            Map<String, Object> filters,
            Pageable pageable) {

        // Get valid columns for this type
        Set<String> validColumns = getValidColumnsForType(type);

        // Sanitize filters - remove invalid columns
        Map<String, Object> sanitizedFilters = sanitizeFilters(filters, validColumns);

        // Build count query with parameters
        QueryInfo countInfo = buildCountQuery(type, parentId, search, sanitizedFilters);
        Integer total = jdbcTemplate.queryForObject(
                countInfo.sql,
                Integer.class,
                countInfo.params.toArray()
        );

        // Build paginated query with parameters
        QueryInfo dataInfo = buildPaginatedQuery(type, parentId, search, sanitizedFilters, pageable);

        List<DynamicDropdownResponse> content =  jdbcTemplate.query(
                dataInfo.sql,
                ps -> {
                    for (int i = 0; i < dataInfo.params.size(); i++) {
                        ps.setObject(i + 1, dataInfo.params.get(i));
                    }
                },
                new DynamicRowMapper(type.getResponseKeys())
        );

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Cacheable(value = "dropdownHierarchyCache", key = "#type.type + '_' + #parentColumn")
    public List<DynamicDropdownResponse> getHierarchicalDropdown(DynamicDropdownType type, String parentColumn) {
        String sql = buildHierarchicalQuery(type, parentColumn);
        log.debug("Executing hierarchical query: {}", sql);

        return jdbcTemplate.query(sql, new DynamicRowMapper(type.getResponseKeys()));
    }

    @CacheEvict(value = {"dropdownCache", "dropdownPaginationCache", "dropdownHierarchyCache"}, allEntries = true)
    public void clearCache() {
        log.info("Clearing all dropdown caches");
    }

    /**
     * Get valid column names for this dropdown type
     */
    private Set<String> getValidColumnsForType(DynamicDropdownType type) {
        Set<String> validColumns = new HashSet<>();

        // Add columns from the enum
        for (DynamicDropdownType.ColumnConfig col : type.getColumns()) {
            String colName = col.getColumnName();
            // Extract base column name if it has table alias
            if (colName.contains(".")) {
                colName = colName.substring(colName.indexOf(".") + 1);
            }
            validColumns.add(colName);
        }

        // Add common columns that might exist in all tables
        validColumns.add("status");
        validColumns.add("is_active");
        validColumns.add("id");
        validColumns.add("parent_id");

        return validColumns;
    }

    /**
     * Remove invalid filters and log warnings
     */
    private Map<String, Object> sanitizeFilters(Map<String, Object> filters, Set<String> validColumns) {
        if (filters == null || filters.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> sanitized = new HashMap<>();

        filters.forEach((key, value) -> {
            // Sanitize key - allow only alphanumeric and underscore
            String sanitizedKey = key.replaceAll("[^a-zA-Z0-9_]", "");

            if (value != null && validColumns.contains(sanitizedKey)) {
                sanitized.put(sanitizedKey, value);
            } else {
                log.warn("Ignoring invalid filter column: '{}' for dropdown type. Valid columns: {}",
                        key, validColumns);
            }
        });

        return sanitized;
    }

    /**
     * Query info holder with SQL and parameters
     */
    @lombok.Value
    private static class QueryInfo {
        String sql;
        List<Object> params;
    }

    /**
     * Build query with filters using parameterized statements
     */
    private QueryInfo buildQueryWithFilters(DynamicDropdownType type, UUID parentId,
                                            String search, Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT ").append(type.getSelectColumns())
                .append(" FROM ").append(type.getFullTableName());

        // Add joins if any
        if (type.getJoinClause() != null && !type.getJoinClause().isEmpty()) {
            sql.append(" ").append(type.getJoinClause());
        }

        // Build WHERE clause
        List<String> whereConditions = new ArrayList<>();

        // Add default where clause from enum (static, no parameters)
        if (type.getWhereClause() != null && !type.getWhereClause().isEmpty()) {
            whereConditions.add(type.getWhereClause());
        }

        // Add parent filter if applicable
        if (parentId != null) {
            whereConditions.add(getParentColumnName(type) + " = ?");
            params.add(parentId);
        }

        // Add search filter
        if (search != null && !search.trim().isEmpty()) {
            SearchInfo searchInfo = buildSearchCondition(type, search);
            whereConditions.add(searchInfo.condition);
            params.addAll(searchInfo.params);
        }

        // Add custom filters (only valid ones)
        if (filters != null && !filters.isEmpty()) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    whereConditions.add(key + " = ?");
                    params.add(value);
                }
            });
        }

        // Append WHERE clause if conditions exist
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        // Add ORDER BY
        if (type.getOrderBy() != null && !type.getOrderBy().isEmpty()) {
            sql.append(" ORDER BY ").append(type.getOrderBy());
        }
        System.out.println("Final SQL: " + sql.toString());
        return new QueryInfo(sql.toString(), params);
    }

    /**
     * Build count query with parameters
     */
    private QueryInfo buildCountQuery(DynamicDropdownType type, UUID parentId,
                                      String search, Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) FROM ").append(type.getFullTableName());

        // Add joins if needed for filtering
        if (needsJoinForFilters(type, filters, search)) {
            if (type.getJoinClause() != null && !type.getJoinClause().isEmpty()) {
                sql.append(" ").append(type.getJoinClause());
            }
        }

        // Build WHERE clause
        List<String> whereConditions = new ArrayList<>();

        if (type.getWhereClause() != null && !type.getWhereClause().isEmpty()) {
            whereConditions.add(type.getWhereClause());
        }

        if (parentId != null) {
            whereConditions.add(getParentColumnName(type) + " = ?");
            params.add(parentId);
        }

        if (search != null && !search.trim().isEmpty()) {
            SearchInfo searchInfo = buildSearchCondition(type, search);
            whereConditions.add(searchInfo.condition);
            params.addAll(searchInfo.params);
        }

        if (filters != null && !filters.isEmpty()) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    whereConditions.add(key + " = ?");
                    params.add(value);
                }
            });
        }

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        return new QueryInfo(sql.toString(), params);
    }

    /**
     * Build paginated query with parameters
     */
    private QueryInfo buildPaginatedQuery(DynamicDropdownType type, UUID parentId,
                                          String search, Map<String, Object> filters,
                                          Pageable pageable) {
        QueryInfo baseQuery = buildQueryWithFilters(type, parentId, search, filters);

        String paginatedSql = baseQuery.sql + " LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(baseQuery.params);
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        return new QueryInfo(paginatedSql, params);
    }

    /**
     * Check if joins are needed for filters
     */
    private boolean needsJoinForFilters(DynamicDropdownType type, Map<String, Object> filters, String search) {
        if (type.getJoinClause() == null || type.getJoinClause().isEmpty()) {
            return false;
        }

        // Check if any filter column requires a join
        if (filters != null) {
            for (String key : filters.keySet()) {
                if (key.contains(".")) {
                    return true;
                }
            }
        }

        // Check if search requires join (if search columns have table aliases)
        if (search != null && !search.trim().isEmpty()) {
            for (int i = 0; i < Math.min(3, type.getColumns().size()); i++) {
                String colName = type.getColumns().get(i).getColumnName();
                if (colName.contains(".")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Search info holder
     */
    @lombok.Value
    private static class SearchInfo {
        String condition;
        List<Object> params;
    }

    /**
     * Build search condition with parameters
     */
    private SearchInfo buildSearchCondition(DynamicDropdownType type, String search) {
        List<String> searchConditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String searchPattern = "%" + search.toLowerCase() + "%";

        // Search across text columns (usually the name column at index 1)
        for (int i = 0; i < Math.min(3, type.getColumns().size()); i++) {
            DynamicDropdownType.ColumnConfig col = type.getColumns().get(i);
            if (col.getDataType() == String.class) {
                String colName = col.getColumnName();
                searchConditions.add("LOWER(" + colName + "::text) LIKE LOWER(?)");
                params.add(searchPattern);
            }
        }

        // If no string columns found, use the second column as fallback
        if (searchConditions.isEmpty() && type.getColumns().size() > 1) {
            String colName = type.getColumns().get(1).getColumnName();
            searchConditions.add("LOWER(" + colName + "::text) LIKE LOWER(?)");
            params.add(searchPattern);
        }

        String condition = "(" + String.join(" OR ", searchConditions) + ")";
        return new SearchInfo(condition, params);
    }

    /**
     * Get parent column name - make this configurable
     */
    private String getParentColumnName(DynamicDropdownType type) {
        // Handle specific parent column names for different types
        switch (type) {
            case CITY:
                return "s.state_uuid";  // City's parent is state_id
            case STATE:
                return "c.country_uuid"; // State's parent is country_id
            case PINCODE:
                return "c.city_uuid";    // Pincode's parent is city_id
            case PATCH_LIST:
                return "c.city_uuid";   // Patch List's parent is patch_id
            default:
                return "parent_id";    // Default fallback
        }
    }

    /**
     * Build hierarchical query
     */
    private String buildHierarchicalQuery(DynamicDropdownType type, String parentColumn) {
        return String.format(
                "WITH RECURSIVE hierarchy AS (" +
                        "  SELECT %s, 0 as level FROM %s WHERE %s IS NULL AND %s" +
                        "  UNION ALL" +
                        "  SELECT child.%s, parent.level + 1 FROM %s child" +
                        "  INNER JOIN hierarchy parent ON child.%s = parent.%s" +
                        "  WHERE %s" +
                        ") SELECT * FROM hierarchy ORDER BY level, %s",
                type.getSelectColumns(), type.getFullTableName(), parentColumn, type.getWhereClause(),
                type.getSelectColumns(), type.getFullTableName(), parentColumn,
                type.getColumns().get(0).getColumnName(), type.getWhereClause(),
                type.getOrderBy()
        );
    }

    // Dynamic Row Mapper
    private static class DynamicRowMapper implements RowMapper<DynamicDropdownResponse> {
        private final List<String> responseKeys;

        DynamicRowMapper(List<String> responseKeys) {
            this.responseKeys = responseKeys;
        }

        @Override
        public DynamicDropdownResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResultSetMetaData metaData = rs.getMetaData();
            DynamicDropdownResponse response = new DynamicDropdownResponse();

            for (int i = 0; i < responseKeys.size() && i < metaData.getColumnCount(); i++) {
                String key = responseKeys.get(i);
                Object value = rs.getObject(i + 1);
                response.setAttribute(key, value);
            }

            return response;
        }
    }
}
