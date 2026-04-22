package com.apiCodeGenerator.common.apputil.dropdown.dynamic.enums;


import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

enum DropdownSource {
    MASTER_TABLE,
    DYNAMIC_TABLE
}

@Getter
public enum DynamicDropdownType {

    USER("users", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("userUuid", "user_uuid", UUID.class),
            ColumnConfig.of("fullName", "full_name", String.class),
            ColumnConfig.of("email", "email", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_users");
            setWhereClause("status = 1");
            setOrderBy("full_name");
        }
    },

    USER_TYPE("user-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("userTypeUuid", "user_type_uuid", UUID.class),
            ColumnConfig.of("userTypeCode", "user_type_code", String.class),
            ColumnConfig.of("userTypeName", "user_type_name", String.class),
            ColumnConfig.of("description", "description", String.class)
    ){
        {
            setSchema("master");
            setTable("mst_user_types");
            setWhereClause("status = 1");
            setOrderBy("user_type_name");
        }
    },

    DESIGNATION("designation", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("designationUuid", "designation_uuid", UUID.class),
            ColumnConfig.of("userTypeId", "user_type_id", UUID.class),
            ColumnConfig.of("designationCode", "designation_code", String.class),
            ColumnConfig.of("designationName", "designation_name", String.class),
            ColumnConfig.of("description", "description", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_designations");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("designation_name");
        }
    },

    DOCTOR_CATEGORY("doctor-category", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("doctorCategoryUuid", "doctor_category_uuid", UUID.class),
            ColumnConfig.of("doctorCategoryCode", "doctor_category_code", String.class),
            ColumnConfig.of("doctorCategoryName", "doctor_category_name", String.class),
            ColumnConfig.of("description", "description", String.class),
            ColumnConfig.of("priorityScore", "priority_score", Integer.class)
    ) {
        {
            setSchema("master");
            setTable("mst_doctor_category");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("priority_score NULLS LAST, doctor_category_name");
        }
    },

    SPECIALIZATION("specialization", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("specializationUuid", "specialization_uuid", UUID.class),
            ColumnConfig.of("specializationCode", "specialization_code", String.class),
            ColumnConfig.of("specializationName", "specialization_name", String.class),
            ColumnConfig.of("description", "description", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_specialization");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("specialization_name");
        }
    },

    SESSION_TYPE("session-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("sessionTypeUuid", "session_type_uuid", UUID.class),
            ColumnConfig.of("sessionTypeCode", "session_type_code", String.class),
            ColumnConfig.of("sessionTypeName", "session_type_name", String.class),
            ColumnConfig.of("displayOrder", "display_order", Integer.class)
    ) {
        {
            setSchema("master");
            setTable("mst_session_type");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("display_order NULLS LAST, session_type_name");
        }
    },

    DAY_OF_WEEK("day-of-week", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("dayOfWeekUuid", "day_of_week_uuid", UUID.class),
            ColumnConfig.of("dayCode", "day_code", String.class),
            ColumnConfig.of("dayName", "day_name", String.class),
            ColumnConfig.of("daySequence", "day_sequence", Integer.class),
            ColumnConfig.of("isWeekend", "is_weekend", Boolean.class)
    ) {
        {
            setSchema("master");
            setTable("mst_day_of_week");
            setWhereClause("status = 1");
            setOrderBy("day_sequence");
        }
    },

    PRODUCT_CATEGORY("product-category", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("productCategoryUuid", "product_category_uuid", UUID.class),
            ColumnConfig.of("parentId", "parent_id", UUID.class),
            ColumnConfig.of("categoryName", "name", String.class),
            ColumnConfig.of("slug", "slug", String.class),
            ColumnConfig.of("displayOrder", "display_order", Integer.class)
    ) {
        {
            setSchema("catalog");
            setTable("product_categories");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("display_order, name");
        }
    },

    DOSAGE_FORM("dosage-form", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("dosageFormUuid", "dosage_form_uuid", UUID.class),
            ColumnConfig.of("dosageFormCode", "dosage_form_code", String.class),
            ColumnConfig.of("dosageFormName", "dosage_form_name", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("master");
            setTable("mst_dosage_forms");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, dosage_form_name");
        }
    },

    DRUG_TYPE("drug-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("drugTypeUuid", "drug_type_uuid", UUID.class),
            ColumnConfig.of("drugTypeCode", "drug_type_code", String.class),
            ColumnConfig.of("drugTypeName", "drug_type_name", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("master");
            setTable("mst_drug_types");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, drug_type_name");
        }
    },

    WAREHOUSE_TYPE("warehouse-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("warehouseTypeUuid", "warehouse_type_uuid", UUID.class),
            ColumnConfig.of("warehouseTypeCode", "warehouse_type_code", String.class),
            ColumnConfig.of("warehouseTypeName", "warehouse_type_name", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("master");
            setTable("mst_warehouse_type");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, warehouse_type_name");
        }
    },

    PROPERTY_GROUP("property-group", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("documentTypeUuid", "property_group_uuid", UUID.class),
            ColumnConfig.of("documentTypeCode", "property_group_code", String.class),
            ColumnConfig.of("documentTypeName", "property_group_name", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_property_group");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, property_group_name");
        }
    },

    PHARMACY_CATEGORY("pharmacy-category", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("pharmacyCategoryUuid", "pharmacy_category_uuid", UUID.class),
            ColumnConfig.of("pharmacyCategoryCode", "pharmacy_category_code", String.class),
            ColumnConfig.of("pharmacyCategoryName", "pharmacy_category_name", String.class),
            ColumnConfig.of("description", "remark", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("pharmacy");
            setTable("mst_pharmacy_category");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, pharmacy_category_name");
        }
    },

    PHARMACY_TYPE("pharmacy-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("pharmacyTypeUuid", "pharmacy_type_uuid", UUID.class),
            ColumnConfig.of("pharmacyTypeCode", "pharmacy_type_code", String.class),
            ColumnConfig.of("pharmacyTypeName", "pharmacy_type_name", String.class),
            ColumnConfig.of("description", "remark", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("pharmacy");
            setTable("mst_pharmacy_type");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, pharmacy_type_name");
        }
    },

    DISTRIBUTOR_TYPE("distributor-type", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("distributorTypeUuid", "distributor_type_uuid", UUID.class),
            ColumnConfig.of("distributorTypeCode", "distributor_type_code", String.class),
            ColumnConfig.of("distributorTypeName", "distributor_type_name", String.class),
            ColumnConfig.of("description", "remark", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("distributor");
            setTable("mst_distributor_type");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, distributor_type_name");
        }
    },

    DISTRIBUTOR_CATEGORY("distributor-category", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("distributorCategoryUuid", "distributor_category_uuid", UUID.class),
            ColumnConfig.of("distributorCategoryCode", "distributor_category_code", String.class),
            ColumnConfig.of("distributorCategoryName", "distributor_category_name", String.class),
            ColumnConfig.of("description", "remark", String.class),
            ColumnConfig.of("sortOrder", "sort_order", Integer.class)
    ) {
        {
            setSchema("distributor");
            setTable("mst_distributor_category");
            setWhereClause("status = 1 AND deleted_at IS NULL");
            setOrderBy("sort_order, distributor_category_name");
        }
    },

    PATCH_LIST("patch-list", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("patchName", "p.patch_name", String.class),
            ColumnConfig.of("patchUuid", "p.patch_uuid", UUID.class ),
            ColumnConfig.of("patchCode","p.patch_code", String.class))
            {
                {
                    setSchema("master");
                    setTable("mst_patch p");
                    setJoinClause("LEFT JOIN master.mst_city c ON c.city_id = p.city_id AND c.status = 1");
                    setWhereClause("p.status = 1");
                    setOrderBy("p.patch_name");
                }
            },

    COUNTRY("country", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("countryUuid", "country_uuid", UUID.class),
            ColumnConfig.of("countryName", "country_name", String.class),
            ColumnConfig.of("countryCode", "country_code", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_country");
            setWhereClause("status = 1");
            setOrderBy("country_name");
        }
    },

    STATE("state", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("stateUuid", "s.state_uuid", UUID.class),
            ColumnConfig.of("stateName", "s.state_name", String.class),
            ColumnConfig.of("stateCode", "s.state_code", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_state s");
            setJoinClause("JOIN master.mst_country c ON c.country_id = s.country_id AND c.status = 1");
            setWhereClause("s.status = 1");
            setOrderBy("s.state_name");
        }
    },

    CITY("city", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("cityUuid", "c.city_uuid", UUID.class),
            ColumnConfig.of("cityName", "c.city_name", String.class),
            ColumnConfig.of("cityCode", "c.city_code", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_city c");
            setJoinClause("JOIN master.mst_state s ON s.state_id = c.state_id AND s.status = 1");
            setWhereClause("c.status = 1");
            setOrderBy("c.city_name");
        }
    },

    PINCODE("pincode", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("pinCodeUuid", "p.pincode_uuid", UUID.class),
            ColumnConfig.of("pinCode", "p.pincode", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_pincode p");
            setJoinClause("LEFT JOIN master.mst_city c ON c.city_id = p.city_id AND c.status = 1 ");
            setWhereClause("p.status = 1");
            setOrderBy("p.pincode");
        }
    },

    SERVICE_CHANNEL("service-channel", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("serviceChannelUuid", "service_channel_uuid", UUID.class),
            ColumnConfig.of("serviceChannelCode", "service_channel_code", String.class),
            ColumnConfig.of("serviceChannelName", "service_channel_name", String.class),
            ColumnConfig.of("description", "description", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_service_channels");
            setWhereClause("status = 1");
            setOrderBy("service_channel_name");
        }
    },

    DELIVERY_TYPES("delivery-types", DropdownSource.DYNAMIC_TABLE,
            ColumnConfig.of("deliveryTypeUuid", "delivery_type_uuid", UUID.class),
            ColumnConfig.of("deliveryTypeCode", "delivery_type_code", String.class),
            ColumnConfig.of("deliveryTypeName", "delivery_type_name", String.class),
            ColumnConfig.of("description", "description", String.class)
    ) {
        {
            setSchema("master");
            setTable("mst_delivery_types");
            setWhereClause("status = 1");
            setOrderBy("delivery_type_name");
        }
    };

    // --- Enum Fields and Methods ---

    private final String type;
    private final DropdownSource source;
    private final List<ColumnConfig> columns;
    private String schema;
    private String table;
    private String joinClause;
    private String whereClause = "status = 1";
    private String orderBy;

    // Cache for fast lookup
    private static final Map<String, DynamicDropdownType> TYPE_MAP = new ConcurrentHashMap<>();

    static {
        Arrays.stream(values()).forEach(type -> TYPE_MAP.put(type.type, type));
    }

    DynamicDropdownType(String type, DropdownSource source, ColumnConfig... columns) {
        this.type = type;
        this.source = source;
        this.columns = Collections.unmodifiableList(Arrays.asList(columns));
        if (columns.length > 1) {
            this.orderBy = columns[1].getColumnName(); // Default order by name column
        }
    }

    public static DynamicDropdownType fromType(String type) {
        DynamicDropdownType dropdownType = TYPE_MAP.get(type);
        if (dropdownType == null) {
            throw new IllegalArgumentException("Invalid dropdown type: " + type);
        }
        return dropdownType;
    }

    protected void setSchema(String schema) {
        this.schema = schema;
    }

    protected void setTable(String table) {
        this.table = table;
    }

    protected void setJoinClause(String joinClause) {
        this.joinClause = joinClause;
    }

    protected void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    protected void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public DynamicDropdownType withSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public DynamicDropdownType withTable(String table) {
        this.table = table;
        return this;
    }

    public DynamicDropdownType withWhereClause(String whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    public DynamicDropdownType withOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public String getFullTableName() {
        if (source == DropdownSource.MASTER_TABLE) {
            return "system.sys_common_master";
        }
        return schema + "." + table;
    }

    public String getJoinClause() {
        return joinClause != null ? joinClause : "";
    }

    public String getSelectColumns() {
        return columns.stream()
                .map(ColumnConfig::getColumnName)
                .collect(Collectors.joining(", "));
    }

    public List<String> getResponseKeys() {
        return columns.stream()
                .map(ColumnConfig::getResponseKey)
                .collect(Collectors.toList());
    }

    // Build complete SQL query
    public String buildQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(getSelectColumns())
                .append(" FROM ").append(getFullTableName());

        if (joinClause != null && !joinClause.isEmpty()) {
            sql.append(" ").append(joinClause);
        }

        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        return sql.toString();
    }

    // --- Public Nested Helper Class ---
    @Getter
    public static class ColumnConfig {
        private final String responseKey;
        private final String columnName;
        private final Class<?> dataType;
        private final boolean nullable;
        private final Object defaultValue;

        private ColumnConfig(String responseKey, String columnName, Class<?> dataType,
                             boolean nullable, Object defaultValue) {
            this.responseKey = responseKey;
            this.columnName = columnName;
            this.dataType = dataType;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
        }

        public static ColumnConfig of(String responseKey, String columnName, Class<?> dataType) {
            return new ColumnConfig(responseKey, columnName, dataType, true, null);
        }

        public static ColumnConfig of(String responseKey, String columnName, Class<?> dataType,
                                      Object defaultValue) {
            return new ColumnConfig(responseKey, columnName, dataType, false, defaultValue);
        }

        public static ColumnConfig of(String responseKey, String columnName, Class<?> dataType,
                                      boolean nullable) {
            return new ColumnConfig(responseKey, columnName, dataType, nullable, null);
        }
    }
}



