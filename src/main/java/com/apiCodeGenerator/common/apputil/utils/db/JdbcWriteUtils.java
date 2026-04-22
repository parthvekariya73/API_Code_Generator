package com.apiCodeGenerator.common.apputil.utils.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class JdbcWriteUtils {

    private static final Logger log = LoggerFactory.getLogger(JdbcWriteUtils.class);

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcWriteUtils(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // Insert / Save single row
    public int save(String sql, Object... params) {
        try {
            log.debug("Executing save: {} with params: {}", sql, Arrays.toString(params));
            return update(sql, params);
        } catch (Exception e) {
            log.error("Error executing save: {}", e.getMessage(), e);
            throw e;
        }
    }

    public int save(String query, Object paramsObject) {
        try {
            log.debug("Executing save: {} with params: {}", query, paramsObject);
            SqlParameterSource sqlParameterSource;
            if (paramsObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> paramMap = (Map<String, ?>) paramsObject;
                sqlParameterSource = new MapSqlParameterSource(paramMap);
            } else {
                sqlParameterSource = new BeanPropertySqlParameterSource(paramsObject);
            }
            return namedParameterJdbcTemplate.update(query, sqlParameterSource);
        } catch (Exception e) {
            log.error("Error executing save: {}", e.getMessage(), e);
            throw e;
        }
    }

    public int saveAndReturnKey(String query, Object paramsObject) {
        try {
            log.debug("Executing saveAndReturnKeyAsInt: {} with params: {}", query, paramsObject);
            SqlParameterSource sqlParameterSource;
            if (paramsObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> paramMap = (Map<String, ?>) paramsObject;
                sqlParameterSource = new MapSqlParameterSource(paramMap);
            } else {
                sqlParameterSource = new BeanPropertySqlParameterSource(paramsObject);
            }
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(query, sqlParameterSource, keyHolder, new String[]{"id"});
            Number generatedKey = keyHolder.getKey();
            if (generatedKey != null) {
                int id = generatedKey.intValue();
                log.debug("Generated ID: {}", id);
                return id;
            } else {
                throw new IllegalStateException("No key generated during insert.");
            }
        } catch (Exception e) {
            log.error("Error executing saveAndReturnKeyAsInt: {}", e.getMessage(), e);
            throw e;
        }
    }

    public int[] saveBatch(String query, List<?> paramsList) {
        try {
            log.debug("Executing batch save: {} with {} records", query, paramsList.size());
            SqlParameterSource[] batchParams = paramsList.stream()
                    .map(param -> {
                        if (param instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, ?> paramMap = (Map<String, ?>) param;
                            return new MapSqlParameterSource(paramMap);
                        } else {
                            return new BeanPropertySqlParameterSource(param);
                        }
                    })
                    .toArray(SqlParameterSource[]::new);

            return namedParameterJdbcTemplate.batchUpdate(query, batchParams);
        } catch (Exception e) {
            log.error("Error executing batch save: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Insert / Save multiple rows
    public int[] saveAll(String sql, List<Object[]> batchParams) {
        try {
            log.debug("Executing saveAll: {} with batch size: {}", sql, batchParams.size());
            return updateAll(sql, batchParams);
        } catch (Exception e) {
            log.error("Error executing saveAll: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Update single row
    public int update(String sql, Object... params) {
        try {
            log.debug("Executing update: {} with params: {}", sql, Arrays.toString(params));
            return jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            log.error("Error executing update: {}", e.getMessage(), e);
            throw e;
        }
    }

    public int update(String query, Object paramObject) {
        try {
            log.debug("Executing update: {} with params: {}", query, paramObject);
            SqlParameterSource sqlParameterSource;
            if (paramObject == null) {
                sqlParameterSource = new MapSqlParameterSource();
            } else if (paramObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> paramMap = (Map<String, ?>) paramObject;
                sqlParameterSource = new MapSqlParameterSource(paramMap);
            } else {
                sqlParameterSource = new BeanPropertySqlParameterSource(paramObject);
            }
            return namedParameterJdbcTemplate.update(query, sqlParameterSource);
        } catch (Exception e) {
            log.error("Error executing update: {}", e.getMessage(), e);
            throw e;
        }
    }

    public int[] updateBatch(List<String> queries, List<Object> paramObjects) {
        try {
            if (queries.size() != paramObjects.size()) {
                throw new IllegalArgumentException("Queries and parameter lists must be the same size.");
            }

            List<Integer> results = new ArrayList<>();
            for (int i = 0; i < queries.size(); i++) {
                String query = queries.get(i);
                Object paramObject = paramObjects.get(i);

                log.debug("Executing batch update: {} with params: {}", query, paramObject);

                SqlParameterSource sqlParameterSource;
                if (paramObject == null) {
                    sqlParameterSource = new MapSqlParameterSource();
                } else if (paramObject instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, ?> paramMap = (Map<String, ?>) paramObject;
                    sqlParameterSource = new MapSqlParameterSource(paramMap);
                } else {
                    sqlParameterSource = new BeanPropertySqlParameterSource(paramObject);
                }

                int updatedRows = namedParameterJdbcTemplate.update(query, sqlParameterSource);
                results.add(updatedRows);
            }

            return results.stream().mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            log.error("Error executing batch update: {}", e.getMessage(), e);
            throw e;
        }
    }


    // Update multiple rows (batch)
    public int[] updateAll(String sql, List<Object[]> batchParams) {
        try {
            log.debug("Executing updateAll: {} with batch size: {}", sql, batchParams.size());
            return jdbcTemplate.batchUpdate(sql, batchParams);
        } catch (Exception e) {
            log.error("Error executing updateAll: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Delete
    public int delete(String sql, Object... params) {
        try {
            log.debug("Executing delete: {} with params: {}", sql, Arrays.toString(params));
            return update(sql, params);
        } catch (Exception e) {
            log.error("Error executing delete: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Save and return generated ID (e.g., auto-increment primary key)
    public Number saveAndReturnId(String sql, Object... params) {
        try {
            log.debug("Executing saveAndReturnId: {} with params: {}", sql, Arrays.toString(params));
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps;
            }, keyHolder);

            return keyHolder.getKey();
        } catch (Exception e) {
            log.error("Error executing saveAndReturnId: {}", e.getMessage(), e);
            throw e;
        }
    }
}


