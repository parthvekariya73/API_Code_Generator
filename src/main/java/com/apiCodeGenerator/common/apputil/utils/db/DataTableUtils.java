package com.apiCodeGenerator.common.apputil.utils.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataTableUtils {
    // @formatter:off
    private static final Logger log = LoggerFactory.getLogger(DataTableUtils.class);
    private static final int NO_PAGING = -1;
//    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    //    private static final String REGEX_COMMA_WITH_NUMBER = "([0-9]+([,])(.)*)+";
    private final DBUtils dbUtils;

    private String[] columnSpecification;
    private String[] sortColumnArr;
    private Long start;
    private Long length;
    private String search;
    private String orderBy;

    public DataTableUtils(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public String getOrderBy() {
        return orderBy;
    }


    // version 2.0
    // support multiple order by and inner order like  Ex. order by FIELD(column, val1, val2)
    public DataTableResponse fetch(
            HttpServletRequest request,
            String sqlQueryBase,
            String countQueryBase,
            String whereClause,
            List<Object> paramValues,
            Map<String, String> customOrderMap,
            String[] columnsToEncrypt,
            String... columnSpecification
    ) {
        try {
            validateColumns(columnSpecification);
            initializeColumnSpec(columnSpecification);

            if (!extractDataTableRequest(request, customOrderMap)) {
                throw new IllegalStateException("Invalid DataTable request params.");
            }
            return fetchData(sqlQueryBase, countQueryBase, whereClause, paramValues, columnsToEncrypt, columnSpecification);
        } catch (Exception e) {
            log.error("Error in DataTable fetch logic", e);
            return new DataTableResponse(0, 0, Collections.emptyList());
        }
    }


    // version 1.0
    public DataTableResponse fetch(
            HttpServletRequest request,
            String sqlQueryBase,
            String countQueryBase,
            String whereClause,
            List<Object> paramValues,
            String[] columnsToEncrypt,
            String... columnSpecification
    ) {
        try {
            validateColumns(columnSpecification);
            initializeColumnSpec(columnSpecification);

            if (!extractDataTableRequest(request, new HashMap<>())) {
                throw new IllegalStateException("Invalid DataTable request params.");
            }
            return fetchData(sqlQueryBase, countQueryBase, whereClause, paramValues, columnsToEncrypt, columnSpecification);
        } catch (Exception e) {
            log.error("Error in DataTable fetch logic", e);
            return new DataTableResponse(0, 0, Collections.emptyList());
        }
    }


    private DataTableResponse fetchData(
            String sqlQueryBase,
            String countQueryBase,
            String whereClause,
            List<Object> paramValues,
            String[] columnsToEncrypt,
            String... columnSpecification
    ) throws JsonProcessingException {

        StringBuilder dynamicWhere = buildWhereClause(whereClause, columnSpecification, paramValues);
        String finalQuery = sqlQueryBase + dynamicWhere + " " + orderBy;
        String pagedQuery = applyPaging(finalQuery, paramValues);

        int totalFiltered = dbUtils.getCount(countQueryBase + dynamicWhere, getCountParams(paramValues));
        List<Map<String, Object>> resultList = dbUtils.getResults(pagedQuery, paramValues);
        if (log.isDebugEnabled()) {
            log.debug("Executing query: {}", pagedQuery);
        }
        return new DataTableResponse(
                totalFiltered,
                totalFiltered,
                resultList
//                MapperUtils.readTree(convert(resultList, columnsToEncrypt, columnSpecification))
        );
    }


    private boolean extractDataTableRequest(HttpServletRequest request, Map<String, String> customOrderMap) {
        if (request == null) {
            throw new NullPointerException("Request can't be null.");
        }

        Enumeration<String> parameterNames = request.getParameterNames();
        if (!parameterNames.hasMoreElements()) {
            return false;
        }

        this.start = Long.parseLong(request.getParameter("start"));
        this.length = Long.parseLong(request.getParameter("length"));
        this.search = request.getParameter("search[value]") == null
                ? request.getParameter("search")
                : request.getParameter("search[value]");

        List<String> orderClauses = orderClausesAppend(request, customOrderMap);

        if (!orderClauses.isEmpty()) {
            this.orderBy = "ORDER BY " + String.join(", ", orderClauses);
        } else {
            this.orderBy = "ORDER BY " + columnSpecification[0] + " " + "desc";
        }
        return true;
    }


    private void validateColumns(String[] columnSpecification) {
        if (columnSpecification == null || columnSpecification.length == 0) {
            throw new IllegalArgumentException("Columns must not be empty.");
        }
    }

    private void initializeColumnSpec(String[] columnSpecification) {
        this.columnSpecification = Arrays.stream(columnSpecification)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        this.sortColumnArr = this.columnSpecification;
    }

    private StringBuilder buildWhereClause(String whereClause, String[] columnSpec, List<Object> paramValues) {
        StringBuilder clause = new StringBuilder( (whereClause != null && !whereClause.isBlank()) ? whereClause : " WHERE 1=1" );
        if (search != null && !search.trim().isEmpty()) {
            clause.append(" AND (");
            for (int i = 0; i < columnSpec.length; i++) {
                clause.append(columnSpec[i]).append(" LIKE ?");
                paramValues.add("%" + search + "%");
                if (i < columnSpec.length - 1) clause.append(" OR ");
            }
            clause.append(")");
        }
        return clause;
    }


    private String applyPaging(String finalQuery, List<Object> paramValues) {
        if (length != NO_PAGING) {
            paramValues.add(length);
            paramValues.add(start);
            return finalQuery + " LIMIT ? OFFSET ?";
        }
        return finalQuery;
    }


    private List<Object> getCountParams(List<Object> paramValues) {
        if (length == NO_PAGING) return paramValues;
        return paramValues.subList(0, paramValues.size() - 2);
    }


    private List<String> orderClausesAppend(HttpServletRequest request, Map<String, String> customOrderMap) {
        List<String> orderClauses = new ArrayList<>();
        int orderIndex = 0;
        while (true) {
            String sortColIndexStr = request.getParameter("order[" + orderIndex + "][column]");
            String sortDirection = request.getParameter("order[" + orderIndex + "][dir]");

            if (sortColIndexStr == null) break;

            try {
                int sortColumn = Integer.parseInt(sortColIndexStr);
                String baseColumn = (sortColumnArr != null && sortColumn < sortColumnArr.length)
                        ? sortColumnArr[sortColumn]
                        : columnSpecification[0];
                String columnExpr = customOrderMap.getOrDefault(baseColumn, baseColumn);
                String dir = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";
                orderClauses.add(columnExpr + " " + dir.toUpperCase());
            } catch (Exception e) {}
            orderIndex++;
        }
        return orderClauses;
    }

}




