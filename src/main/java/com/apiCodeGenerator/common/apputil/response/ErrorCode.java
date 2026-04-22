package com.apiCodeGenerator.common.apputil.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_UUID("ERR_00", HttpStatus.BAD_REQUEST, "Invalid UUID"),
    VALIDATION_ERROR("ERR_01", HttpStatus.BAD_REQUEST, "Validation failed"),
    RESOURCE_NOT_FOUND("ERR_02", HttpStatus.NOT_FOUND, "Requested resource not found"),
    DUPLICATE_RESOURCE("ERR_03", HttpStatus.CONFLICT, "Resource already exists"),
    INTERNAL_SERVER_ERROR("ERR_04", HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong"),

    ERROR_INVALID_STATUS_NAME("ERR_05", HttpStatus.BAD_REQUEST, "Invalid status name", "status", "Invalid status name"),
    ERROR_INVALID_STATUS_CODE("ERR_06", HttpStatus.BAD_REQUEST, "Invalid status code", "status", "Invalid status code"),
    ERROR_INVALID_STATUS_DELETED("ERR_07", HttpStatus.BAD_REQUEST, "You don't set data deleted", "status", "Invalid status"),

    // ─────────────────────── 1. PRODUCT CATEGORY ───────────────────────
    PRODUCT_CATEGORY_REQUEST_CANNOT_BE_NULL("PROD_CAT_101", HttpStatus.BAD_REQUEST, "Product Category Cannot be Null"),
    PRODUCT_CATEGORY_SLUG_ALREADY_EXISTS("PROD_CAT_102", HttpStatus.BAD_REQUEST, "Product Category Slug Already Exists"),
    PRODUCT_PARENT_CATEGORY_NOT_FOUND("PROD_CAT_103", HttpStatus.BAD_REQUEST, "Parent Category Not Found", "parentProductCategoryUUID", "Parent category UUID does not exist"),
    PRODUCT_CATEGORY_NOT_FOUND("PROD_CAT_104", HttpStatus.BAD_REQUEST, "Product Category Not Found"),
    PRODUCT_CATEGORY_CANNOT_BE_OWN_PARENT("PROD_CAT_105", HttpStatus.BAD_REQUEST, "Category cannot be its own parent", "parentProductCategoryUUID", "Category cannot be its own parent"),
    PRODUCT_CATEGORY_CIRCULAR_REFERENCE("PROD_CAT_106", HttpStatus.BAD_REQUEST, "Circular reference detected", "parentProductCategoryUUID", "Circular reference detected"),
    PRODUCT_CATEGORY_DELETE_FAILED("PROD_CAT_107", HttpStatus.BAD_REQUEST, "Failed to delete product category"),
    PRODUCT_CATEGORY_STATUS_UPDATE_FAILED("PROD_CAT_108", HttpStatus.BAD_REQUEST, "Failed to update product category status"),
    PRODUCT_CATEGORY_NAME_REQUIRED("PROD_CAT_109", HttpStatus.BAD_REQUEST, "Product category name is required", "productCategoryName", "Product category name is required"),
    PRODUCT_CATEGORY_SLUG_REQUIRED("PROD_CAT_110", HttpStatus.BAD_REQUEST, "Product category slug is required", "productCategorySlug", "Product category slug is required"),

    // ─────────────────────── 2. DELIVERY PARTNER ───────────────────────
    DELIVERY_PARTNER_REQUEST_CANNOT_BE_NULL("DLP_101", HttpStatus.BAD_REQUEST, "Delivery partner request cannot be null"),
    DELIVERY_PARTNER_NOT_FOUND("DLP_102", HttpStatus.BAD_REQUEST, "Delivery partner not found"),
    DELIVERY_PARTNER_NAME_ALREADY_EXISTS("DLP_103", HttpStatus.BAD_REQUEST, "Delivery partner name already exists"),
    DELIVERY_PARTNER_DELETE_FAILED("DLP_104", HttpStatus.BAD_REQUEST, "Failed to delete delivery partner"),
    DELIVERY_PARTNER_STATUS_UPDATE_FAILED("DLP_105", HttpStatus.BAD_REQUEST, "Failed to update delivery partner status"),
    DELIVERY_PARTNER_COMPANY_TYPE_NOT_FOUND("DLP_106", HttpStatus.BAD_REQUEST, "Company type not found"),
    DELIVERY_PARTNER_CATEGORY_NOT_FOUND("DLP_107", HttpStatus.BAD_REQUEST, "Partner category not found"),
    DELIVERY_PARTNER_GST_REQUIRED_FOR_COMPANY_TYPE("DLP_108", HttpStatus.BAD_REQUEST, "GST is required for selected company type", "gstNumber", "GST is required for selected company type"),
    DELIVERY_PARTNER_BANKS_REQUIRED("DLP_109", HttpStatus.BAD_REQUEST, "At least one bank record is required"),
    DELIVERY_PARTNER_SINGLE_PRIMARY_BANK_ALLOWED("DLP_110", HttpStatus.BAD_REQUEST, "Only one primary bank is allowed per partner"),
    DELIVERY_PARTNER_CONTACTS_REQUIRED("DLP_111", HttpStatus.BAD_REQUEST, "At least one contact record is required"),

    // ─────────────────────── 3. User ───────────────────────
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_101", HttpStatus.BAD_REQUEST, "User email already exist."),
    USER_EMPLOYEE_CODE_ALREADY_EXISTS("USER_EMAIL_102", HttpStatus.BAD_REQUEST, "User employee code already exist."),

    // ─────────────────────── 4. MR ───────────────────────
    MR_NOT_FOUND("MR_102", HttpStatus.BAD_REQUEST, "Mr details not found."),
    MR_STATUS_UPDATE_FAILED("PROD_CAT_108", HttpStatus.BAD_REQUEST, "Failed to update MR status"),

    // ─────────────────────── 5. User Type───────────────────────
    USER_TYPE_ID_NOT_EXIST("USER_TYPE_ID_101", HttpStatus.BAD_REQUEST, "User type is not exist."),

    // ─────────────────────── COUNTRY ───────────────────────
    COUNTRY_NOT_FOUND("COUNTRY_201", HttpStatus.BAD_REQUEST, "Country not found"),
    // ─────────────────────── COUNTRY ───────────────────────

//    ERROR_INVALID_URL_TYPE("ERR_21", HttpStatus.NOT_FOUND, "Invalid URL type"),
//    ERROR_INVALID_SCAN_FREQUENCY("ERR_22", HttpStatus.NOT_FOUND, "Invalid scan frequency"),
//    ERROR_INVALID_SCRAPING_METHOD("ERR_30", HttpStatus.NOT_FOUND, "Invalid scraping method"),

//    MAH_ALREADY_CODE_EXISTS("ERR_23", HttpStatus.CONFLICT, "MAH already exists", "mah code", "mah code already exists"),
//    MAH_ALREADY_EMAIL_EXISTS("ERR_23", HttpStatus.CONFLICT, "MAH already exists", "contact email", "email already exists"),
//    MAH_NOT_FOUND("ERR_24", HttpStatus.NOT_FOUND, "MAH not found"),
//    MAH_ALREADY_DELETED("ERR_25", HttpStatus.BAD_REQUEST, "MAH already deleted"),

//    PRODUCT_ALREADY_EXISTS("ERR_61", HttpStatus.CONFLICT, "Product already exists", "Product", "Product already exists"),
//    PRODUCT_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "Product not found"),
//    PRODUCT_ALREADY_DELETED("ERR_28", HttpStatus.BAD_REQUEST, "Product already deleted"),

    // ─────────────────────── STATE ───────────────────────
//    STATE_NOT_FOUND("STATE_201", HttpStatus.BAD_REQUEST, "State not found"),
//    CATEGORY_NOT_FOUND("ERR_28", HttpStatus.BAD_REQUEST, "category already deleted"),

    // ─────────────────────── CITY ───────────────────────
//    CITY_NOT_FOUND("CITY_201", HttpStatus.BAD_REQUEST, "City not found"),

    // ─────────────────────── PATCH ───────────────────────
//    PATCH_REQUEST_CANNOT_BE_NULL("PATCH_201", HttpStatus.BAD_REQUEST, "Patch request cannot be null"),
//    PATCH_NOT_FOUND("PATCH_202", HttpStatus.BAD_REQUEST, "Patch not found"),
//    PATCH_CODE_ALREADY_EXISTS("PATCH_203", HttpStatus.BAD_REQUEST, "Patch code already exists"),
//    PATCH_CREATE_FAILED("PATCH_204", HttpStatus.BAD_REQUEST, "Failed to createPatch patch"),
//    PATCH_UPDATE_FAILED("PATCH_205", HttpStatus.BAD_REQUEST, "Failed to updatePatch patch"),
//    PATCH_DELETE_FAILED("PATCH_206", HttpStatus.BAD_REQUEST, "Patch already deleted"),
//    PATCH_STATUS_UPDATE_FAILED("PATCH_207", HttpStatus.BAD_REQUEST, "Failed to updatePatch patch status"),
//    PATCH_NAME_REQUIRED("PATCH_208", HttpStatus.BAD_REQUEST, "Patch name is required", "patchName", "Patch name is required"),
//    PATCH_CODE_REQUIRED("PATCH_209", HttpStatus.BAD_REQUEST, "Patch code is required", "patchCode", "Patch code is required"),
//    PATCH_CITY_REQUIRED("PATCH_210", HttpStatus.BAD_REQUEST, "City is required", "cityUUID", "City UUID is required"),
//    PATCH_STATE_REQUIRED("PATCH_211", HttpStatus.BAD_REQUEST, "State is required", "stateUUID", "State UUID is required"),
//    PATCH_COUNTRY_REQUIRED("PATCH_212", HttpStatus.BAD_REQUEST, "Country is required", "countryUUID", "Country UUID is required"),

    // ─────────────────────── COMMON VALIDATION FOR COUNTRY, STATE, CITY, PATCH  ───────────────────────
//    INVALID_STATE_COUNTRY_MAPPING("COMMON_201", HttpStatus.BAD_REQUEST, "State does not belong to country"),
//    INVALID_CITY_STATE_MAPPING("COMMON_202", HttpStatus.BAD_REQUEST, "City does not belong to state"),
//    PRODUCT_TYPE_NOT_FOUND("ERR_29", HttpStatus.NOT_FOUND, "Product Type not found"),

//    REGULATORY_AUTHORITY_CODE_EXISTS("ERR_31", HttpStatus.CONFLICT, "Already Regulatory Authority Code"),
//    REGULATORY_AUTHORITY_NAME_EXISTS("ERR_32", HttpStatus.CONFLICT, "Already Regulatory Authority Name"),
//    REGULATORY_AUTHORITY_DELETE_FAILED("ERR_33", HttpStatus.CONFLICT, " Regulatory Authority Deleted fail"),

//    FREQUENCY_ALREADY_EXIST("ERR_34", HttpStatus.CONFLICT, "Already frequency exist"),
//    ALREADY_EXIST_SCRAPING_METHOD("ERR_35", HttpStatus.CONFLICT, "Already Scraping method exist"),
//    FREQUENCY_NOT_FOUND("ERR_36", HttpStatus.CONFLICT, "Frequency not found"),
//    SCRAPING_METHOD_NOT_FOUND("ERR_37", HttpStatus.CONFLICT, " Scarping method not found"),

//    AUTHORITY_TYPE_NOT_FOUND("ERR_38", HttpStatus.CONFLICT, " Authority type not found"),
//    INVALID_DROPDOWN_TYPE("ERR_39", HttpStatus.CONFLICT, "Invalid DropDown Type"),
//    PRIORITY_NOT_FOUND("ERR_40", HttpStatus.NOT_FOUND, "Priority Not Found"),

//    INSTRUCTION_REQUIRED("INS_REQ_400", HttpStatus.BAD_REQUEST, "Instruction Required"),
//    PRIORITY_REQUIRED("PRI_REQ_400", HttpStatus.BAD_REQUEST, "Priority Required"),
//    DEPARTMENT_REQUIRED("DEP_REQ_400", HttpStatus.BAD_REQUEST, "Department Required"),
//    DUE_DATE_REQUIRED("DEP_REQ_400", HttpStatus.BAD_REQUEST, "Due Date Required"),

//    ASSIGN_PRODUCT_REQUIRED("ASS_PR_400", HttpStatus.BAD_REQUEST, "Assign Product Required"),
//    PLAN_OF_ACTIONS_REQUIRED("PLA_OF_AC_400", HttpStatus.BAD_REQUEST, "Plan of Actions Required"),
//    ACTIONS_REQUIRED("ACT_REQ_400", HttpStatus.BAD_REQUEST, "Actions Required"),
//    COMMENT_REQUIRED("COM_REQ_400", HttpStatus.BAD_REQUEST, "Comment Required"),

//    MAH_AUTHORITY_ACCESS_NOT_FOUND("MAH_AUTH_ACC_404", HttpStatus.NOT_FOUND,"MAH Authority access mapping not found"),
//    MAH_AUTHORITY_ACCESS_ALREADY_EXISTS("MAH_AUTH_ACC_409", HttpStatus.CONFLICT,"MAH Authority access mapping already exists"),
//    MAH_AUTHORITY_ACCESS_DELETE_FAILED("MAH_AUTH_ACC_500", HttpStatus.INTERNAL_SERVER_ERROR,"Failed to delete MAH Authority access mapping"),

//    RIUPDATE_NOT_FOUND("RI_01", HttpStatus.NOT_FOUND, "RI Update Not Found"),
//    ACTION_TASK_NOT_FOUND("ACTION_404", HttpStatus.NOT_FOUND, "Action task not found"),
//    ACTION_ALREADY_COMPLETED("ACTION_409", HttpStatus.CONFLICT, "Action is already completed"),
//    ACTION_NOT_ASSIGNED("ACTION_400", HttpStatus.BAD_REQUEST,"Action is not assigned to any user"),
//    ACTION_DELETED("ACTION_410", HttpStatus.GONE,"Action has been deleted"),
//    SUBMISSION_DATE_REQUIRED("ACTION_400", HttpStatus.BAD_REQUEST, "Submission Date is required"),
//    WORKFLOW_CONTEXT_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "WorkFlow Status Not Found (Internal Server Error)"),
//    COMMENT_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "Comment Not Found"),
//    WORKFLOW_INSTANCE_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "Workflow instance not found"),
//    WORKFLOW_TRANSITION_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "No transition configured"),
//    WORKFLOW_TRANSITION_FAILED("ACTION_500", HttpStatus.INTERNAL_SERVER_ERROR, "Workflow Transition Failed"),
//    SUBMIT_ACTION_REQUIRED("ACTION_400", HttpStatus.BAD_REQUEST, "Submit Action is required"),
//    ROLE_NO_ACCESS_TO_STAGE("ACTION_401", HttpStatus.UNAUTHORIZED, "You do not Submit this RI Update (Change the Role)"),
//    ROLE_NO_ACCESS_TO_COMPLETE_ACTION("ACTION_401", HttpStatus.UNAUTHORIZED, "You do not Complete this Action Task (Change the Role)"),
//    RI_SUMMARY_NOT_FOUND("RI_404", HttpStatus.NOT_FOUND, "RI Summary Not Found"),
//    NOT_ALL_ACTIONS_COMPLETED("RI_400", HttpStatus.BAD_REQUEST, "All Actions are not completed"),
//    RI_UPDATE_ALREADY_CLOSED("RI_409", HttpStatus.CONFLICT, "This Ri Update is Already Closed"),
//    ROLE_NO_ACCESS_TO_CLOSE_RI_UPDATE("Ri_401", HttpStatus.UNAUTHORIZED, "You do not Close This Ri Update (Change the Role)"),
//    CLOSURE_COMMENT_REQUIRED("Ri_400", HttpStatus.BAD_REQUEST, "Closure Comment is Required"),
//    INVENTORY_ALREADY_EXISTS("ERR_26", HttpStatus.CONFLICT, "Invetory Already exists", "Invetory ", "Invetory  already exists"),

//    SHIPPING_EXISTS("ERR_26", HttpStatus.CONFLICT, "shipping already exists", "shipping", "shipping  already exists"),
//    SEO_SLUG_ALREADY_EXISTS("ERR_29", HttpStatus.CONFLICT, "slug already exists", "Slug ", "Slug already exists"),
//    VISIBLITY_ALREADY_EXISTS("ERR_29", HttpStatus.CONFLICT, "Visiblity already exists", "Visiblity ", "Visiblity already exists"),

//    CLASSIFICATION_ALREADY_EXISTS("ERR_34",HttpStatus.CONFLICT ,"classification already exists","classification","classification already exists"),
//    BATCH_NUMBER_ALREADY_EXIST("ERR_35", HttpStatus.CONFLICT, "Batch Number Already exist","Batch ", "batch already exists"),
//    MANUFACTURER_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "manufacturer not found","manufacturer ", "manufacturer not found"),
//    BATCH_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "Batch not found","batch  ", "batch not found"),
//    PHARMA_SPACE_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "pharmaspac not found","pharmaspac ", "pharmaspac not found"),

    ERROR_INVALID_URL_TYPE("ERR_21", HttpStatus.NOT_FOUND, "Invalid URL type"),
    ERROR_INVALID_SCAN_FREQUENCY("ERR_22", HttpStatus.NOT_FOUND, "Invalid scan frequency"),
    ERROR_INVALID_SCRAPING_METHOD("ERR_30", HttpStatus.NOT_FOUND, "Invalid scraping method"),

    MAH_ALREADY_CODE_EXISTS("ERR_23", HttpStatus.CONFLICT, "MAH already exists", "mah code", "mah code already exists"),
    MAH_ALREADY_EMAIL_EXISTS("ERR_23", HttpStatus.CONFLICT, "MAH already exists", "contact email", "email already exists"),
    MAH_NOT_FOUND("ERR_24", HttpStatus.NOT_FOUND, "MAH not found"),
    MAH_ALREADY_DELETED("ERR_25", HttpStatus.BAD_REQUEST, "MAH already deleted"),

    PRODUCT_ALREADY_EXISTS("ERR_61", HttpStatus.CONFLICT, "Product already exists", "Product", "Product already exists"),
    PRODUCT_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_ALREADY_DELETED("ERR_28", HttpStatus.BAD_REQUEST, "Product already deleted"),

    // ─────────────────────── STATE ───────────────────────
    STATE_NOT_FOUND("STATE_201", HttpStatus.BAD_REQUEST, "State not found"),
    CATEGORY_NOT_FOUND("ERR_28", HttpStatus.BAD_REQUEST, "category already deleted"),

    // ─────────────────────── CITY ───────────────────────
    CITY_NOT_FOUND("CITY_201", HttpStatus.BAD_REQUEST, "City not found"),

    // ─────────────────────── PATCH ───────────────────────
    PATCH_REQUEST_CANNOT_BE_NULL("PATCH_201", HttpStatus.BAD_REQUEST, "Patch request cannot be null"),
    PATCH_NOT_FOUND("PATCH_202", HttpStatus.NOT_FOUND, "Patch not found"),
    PATCH_CODE_ALREADY_EXISTS("PATCH_203", HttpStatus.BAD_REQUEST, "Patch code already exists"),
    PATCH_CREATE_FAILED("PATCH_204", HttpStatus.BAD_REQUEST, "Failed to createPatch patch"),
    PATCH_UPDATE_FAILED("PATCH_205", HttpStatus.BAD_REQUEST, "Failed to updatePatch patch"),
    PATCH_DELETE_FAILED("PATCH_206", HttpStatus.BAD_REQUEST, "Patch already deleted"),
    PATCH_STATUS_UPDATE_FAILED("PATCH_207", HttpStatus.BAD_REQUEST, "Failed to updatePatch patch status"),
    PATCH_NAME_REQUIRED("PATCH_208", HttpStatus.BAD_REQUEST, "Patch name is required", "patchName", "Patch name is required"),
    PATCH_CODE_REQUIRED("PATCH_209", HttpStatus.BAD_REQUEST, "Patch code is required", "patchCode", "Patch code is required"),
    PATCH_CITY_REQUIRED("PATCH_210", HttpStatus.BAD_REQUEST, "City is required", "cityUUID", "City UUID is required"),
    PATCH_STATE_REQUIRED("PATCH_211", HttpStatus.BAD_REQUEST, "State is required", "stateUUID", "State UUID is required"),
    PATCH_COUNTRY_REQUIRED("PATCH_212", HttpStatus.BAD_REQUEST, "Country is required", "countryUUID", "Country UUID is required"),
    PATCH_CODE_GENERATION_FAILED("PATCH_213", HttpStatus.BAD_REQUEST, "Failed to generate patch code"),
    PATCH_CODE_ALREADY_ASSIGN("PATCH_409", HttpStatus.CONFLICT, "Patch already assign"),


    // ─────────────────────── COMMON VALIDATION FOR COUNTRY, STATE, CITY, PATCH  ───────────────────────
    INVALID_STATE_COUNTRY_MAPPING("COMMON_201", HttpStatus.BAD_REQUEST, "State does not belong to country"),
    INVALID_CITY_STATE_MAPPING("COMMON_202", HttpStatus.BAD_REQUEST, "City does not belong to state"),
    PRODUCT_TYPE_NOT_FOUND("ERR_29", HttpStatus.NOT_FOUND, "Product Type not found"),

    REGULATORY_AUTHORITY_CODE_EXISTS("ERR_31", HttpStatus.CONFLICT, "Already Regulatory Authority Code"),
    REGULATORY_AUTHORITY_NAME_EXISTS("ERR_32", HttpStatus.CONFLICT, "Already Regulatory Authority Name"),
    REGULATORY_AUTHORITY_DELETE_FAILED("ERR_33", HttpStatus.CONFLICT, " Regulatory Authority Deleted fail"),

    FREQUENCY_ALREADY_EXIST("ERR_34", HttpStatus.CONFLICT, "Already frequency exist"),
    ALREADY_EXIST_SCRAPING_METHOD("ERR_35", HttpStatus.CONFLICT, "Already Scraping method exist"),
    FREQUENCY_NOT_FOUND("ERR_36", HttpStatus.CONFLICT, "Frequency not found"),
    SCRAPING_METHOD_NOT_FOUND("ERR_37", HttpStatus.CONFLICT, " Scarping method not found"),

    AUTHORITY_TYPE_NOT_FOUND("ERR_38", HttpStatus.CONFLICT, " Authority type not found"),
    INVALID_DROPDOWN_TYPE("ERR_39", HttpStatus.CONFLICT, "Invalid DropDown Type"),
    PRIORITY_NOT_FOUND("ERR_40", HttpStatus.NOT_FOUND, "Priority Not Found"),

    INSTRUCTION_REQUIRED("INS_REQ_400", HttpStatus.BAD_REQUEST, "Instruction Required"),
    PRIORITY_REQUIRED("PRI_REQ_400", HttpStatus.BAD_REQUEST, "Priority Required"),
    DEPARTMENT_REQUIRED("DEP_REQ_400", HttpStatus.BAD_REQUEST, "Department Required"),
    DUE_DATE_REQUIRED("DEP_REQ_400", HttpStatus.BAD_REQUEST, "Due Date Required"),

    ASSIGN_PRODUCT_REQUIRED("ASS_PR_400", HttpStatus.BAD_REQUEST, "Assign Product Required"),
    PLAN_OF_ACTIONS_REQUIRED("PLA_OF_AC_400", HttpStatus.BAD_REQUEST, "Plan of Actions Required"),
    ACTIONS_REQUIRED("ACT_REQ_400", HttpStatus.BAD_REQUEST, "Actions Required"),
    COMMENT_REQUIRED("COM_REQ_400", HttpStatus.BAD_REQUEST, "Comment Required"),

    MAH_AUTHORITY_ACCESS_NOT_FOUND("MAH_AUTH_ACC_404", HttpStatus.NOT_FOUND,"MAH Authority access mapping not found"),
    MAH_AUTHORITY_ACCESS_ALREADY_EXISTS("MAH_AUTH_ACC_409", HttpStatus.CONFLICT,"MAH Authority access mapping already exists"),
    MAH_AUTHORITY_ACCESS_DELETE_FAILED("MAH_AUTH_ACC_500", HttpStatus.INTERNAL_SERVER_ERROR,"Failed to delete MAH Authority access mapping"),

    RIUPDATE_NOT_FOUND("RI_01", HttpStatus.NOT_FOUND, "RI Update Not Found"),
    ACTION_TASK_NOT_FOUND("ACTION_404", HttpStatus.NOT_FOUND, "Action task not found"),
    ACTION_ALREADY_COMPLETED("ACTION_409", HttpStatus.CONFLICT, "Action is already completed"),
    ACTION_NOT_ASSIGNED("ACTION_400", HttpStatus.BAD_REQUEST,"Action is not assigned to any user"),
    ACTION_DELETED("ACTION_410", HttpStatus.GONE,"Action has been deleted"),
    SUBMISSION_DATE_REQUIRED("ACTION_400", HttpStatus.BAD_REQUEST, "Submission Date is required"),
    WORKFLOW_CONTEXT_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "WorkFlow Status Not Found (Internal Server Error)"),
    COMMENT_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "Comment Not Found"),
    WORKFLOW_INSTANCE_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "Workflow instance not found"),
    WORKFLOW_TRANSITION_NOT_FOUND("ACTION_409", HttpStatus.NOT_FOUND, "No transition configured"),
    WORKFLOW_TRANSITION_FAILED("ACTION_500", HttpStatus.INTERNAL_SERVER_ERROR, "Workflow Transition Failed"),
    SUBMIT_ACTION_REQUIRED("ACTION_400", HttpStatus.BAD_REQUEST, "Submit Action is required"),
    ROLE_NO_ACCESS_TO_STAGE("ACTION_401", HttpStatus.UNAUTHORIZED, "You do not Submit this RI Update (Change the Role)"),
    ROLE_NO_ACCESS_TO_COMPLETE_ACTION("ACTION_401", HttpStatus.UNAUTHORIZED, "You do not Complete this Action Task (Change the Role)"),
    RI_SUMMARY_NOT_FOUND("RI_404", HttpStatus.NOT_FOUND, "RI Summary Not Found"),
    NOT_ALL_ACTIONS_COMPLETED("RI_400", HttpStatus.BAD_REQUEST, "All Actions are not completed"),
    RI_UPDATE_ALREADY_CLOSED("RI_409", HttpStatus.CONFLICT, "This Ri Update is Already Closed"),
    ROLE_NO_ACCESS_TO_CLOSE_RI_UPDATE("Ri_401", HttpStatus.UNAUTHORIZED, "You do not Close This Ri Update (Change the Role)"),
    CLOSURE_COMMENT_REQUIRED("Ri_400", HttpStatus.BAD_REQUEST, "Closure Comment is Required"),
    INVENTORY_ALREADY_EXISTS("ERR_26", HttpStatus.CONFLICT, "Invetory Already exists", "Invetory ", "Invetory  already exists"),

    SHIPPING_EXISTS("ERR_26", HttpStatus.CONFLICT, "shipping already exists", "shipping", "shipping  already exists"),
    SEO_SLUG_ALREADY_EXISTS("ERR_29", HttpStatus.CONFLICT, "slug already exists", "Slug ", "Slug already exists"),
    VISIBLITY_ALREADY_EXISTS("ERR_29", HttpStatus.CONFLICT, "Visiblity already exists", "Visiblity ", "Visiblity already exists"),

    CLASSIFICATION_ALREADY_EXISTS("ERR_34",HttpStatus.CONFLICT ,"classification already exists","classification","classification already exists"),
    BATCH_NUMBER_ALREADY_EXIST("ERR_35", HttpStatus.CONFLICT, "Batch Number Already exist","Batch ", "batch already exists"),
    MANUFACTURER_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "manufacturer not found","manufacturer ", "manufacturer not found"),
    BATCH_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "Batch not found","batch  ", "batch not found"),
    PHARMA_SPACE_NOT_FOUND("ERR_27", HttpStatus.NOT_FOUND, "pharmaspac not found","pharmaspac ", "pharmaspac not found"),


    // ─────────────────────── 2. Designation ───────────────────────
    DESIGNATION_ID_NOT_EXIST("DESIGNATION_101", HttpStatus.BAD_REQUEST, "Designation id not exist."),
    INVALID_PAGE_NUMBER("INVALID_PAGE_NUMBER", HttpStatus.BAD_REQUEST , "Invalid Page Number" ),

    // ─────────────────────── Pharmacy ───────────────────────
    PHARMACY_NOT_FOUND("PHARMACY_404", HttpStatus.NOT_FOUND, "Pharmacy not found"),
    PHARMACY_NAME_EXISTS("PHARMACY_409", HttpStatus.CONFLICT, "Pharmacy name already exists"),
    PHARMACY_LICENSE_EXISTS("PHARMACY_410", HttpStatus.CONFLICT, "License number already exists"),
    PHARMACY_DOCUMENT_NOT_FOUND("PHARMACY_DOCUMENT_404", HttpStatus.NOT_FOUND, "Pharmacy document not found"),

    SERVICE_CHANNEL_NOT_FOUND("SERVICE_CHANNEL_404", HttpStatus.NOT_FOUND, "Service channel not found"),
    DELIVERY_TYPE_NOT_FOUND("DELIVERY_TYPE_404", HttpStatus.NOT_FOUND, "Delivery type not found"),
    PHARMACY_SERVICEABLE_PINCODE_NOT_FOUND("PHARMACY_SERVICEABLE_PINCODE_404", HttpStatus.NOT_FOUND, "Pharmacy serviceable pin code not found"),
    PINCODE_NOT_FOUND("PINCODE_404", HttpStatus.NOT_FOUND, "PinCode not found"),
    DUPLICATE_PINCODE_SERVICE_CHANNEL("PHARMACY_SERVICEABLE_PINCODE_409", HttpStatus.CONFLICT, "Duplicate service channel for the same pin code"),
    INVALID_DELIVERY_WINDOW("PHARMACY_SERVICEABLE_PINCODE_400", HttpStatus.BAD_REQUEST, "Invalid delivery window configuration"),
    INVALID_DELIVERY_HOURS("PHARMACY_SERVICEABLE_PINCODE_400", HttpStatus.BAD_REQUEST, "Min delivery hours cannot be greater than max delivery hours"),
    INVALID_DELIVERY_TIME_WINDOW("PHARMACY_SERVICEABLE_PINCODE_400", HttpStatus.BAD_REQUEST, "Delivery start time must be before end time"),

    PHARMACY_BANK_NOT_FOUND("PHARMACY_BANK_404", HttpStatus.NOT_FOUND, "Pharmacy bank details not found"),
    ACCOUNT_NUMBER_ALREADY_EXISTS("PHARMACY_BANK_409", HttpStatus.CONFLICT, "Account number already exists for another bank record"),
    INVALID_IFSC_CODE("PHARMACY_BANK_410", HttpStatus.BAD_REQUEST, "Invalid IFSC code format"),
    BANK_NAME_TOO_LONG("PHARMACY_BANK_411", HttpStatus.BAD_REQUEST, "Bank name cannot exceed 150 characters"),
    ACCOUNT_NUMBER_TOO_LONG("PHARMACY_BANK_412", HttpStatus.BAD_REQUEST, "Account number cannot exceed 30 characters"),
    ACCOUNT_HOLDER_NAME_TOO_LONG("PHARMACY_BANK_413", HttpStatus.BAD_REQUEST, "Account holder name cannot exceed 30 characters"),
    DUPLICATE_ACCOUNT_NUMBER("PHARMACY_BANK_414", HttpStatus.CONFLICT, "Duplicate account number for the same pharmacy"),
    MULTIPLE_PRIMARY_BANKS("PHARMACY_BANK_415", HttpStatus.CONFLICT, "Multiple primary bank records for the same pharmacy"),

    WORKING_HOURS_NOT_FOUND("WORKING_HOURS_404", HttpStatus.NOT_FOUND, "Working hours record not found"),
    DUPLICATE_DAY_OF_WEEK("WORKING_HOURS_400", HttpStatus.BAD_REQUEST, "Duplicate day of week in request"),
    DAY_OF_WEEK_NOT_FOUND("WORKING_HOURS_404", HttpStatus.NOT_FOUND, "Day of week not found"),
    MISSING_WEEKDAYS("WORKING_HOURS_400", HttpStatus.BAD_REQUEST, "At least one working day must be provided"),
    WORKING_HOURS_CONFLICT("WORKING_HOURS_409", HttpStatus.CONFLICT, "Working hours for the day of week already exist"),
    TIME_REQUIRED_FOR_WORKING_DAY("WORKING_HOURS_400", HttpStatus.BAD_REQUEST, "Opening and closing times are required for working days"),
    INVALID_TIME_RANGE("WORKING_HOURS_400", HttpStatus.BAD_REQUEST, "Opening time must be before closing time"),

    CAPABILITY_NOT_FOUND("CAPABILITY_404", HttpStatus.NOT_FOUND, "Capability record not found"),
    PROPERTY_NOT_FOUND("PROPERTY_404", HttpStatus.NOT_FOUND, "Property not found"),
    DUPLICATE_PROPERTY("CAPABILITY_400", HttpStatus.BAD_REQUEST, "Same Properties are not allowed"),
    PROPERTY_VALUE_TOO_LONG("CAPABILITY_400", HttpStatus.BAD_REQUEST, "Property value exceeds maximum length"),
    UOM_TOO_LONG("CAPABILITY_400", HttpStatus.BAD_REQUEST, "UOM exceeds maximum length"),
    REMARK_TOO_LONG("CAPABILITY_400", HttpStatus.BAD_REQUEST, "Remark exceeds maximum length"),
    INVALID_VERIFICATION_DATE("CAPABILITY_400", HttpStatus.BAD_REQUEST, "Verification date cannot be in the future");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    // optional
    private final String field;
    private final String issue;

    // Constructor without field issue
    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this(code, httpStatus, message, null, null);
    }

    // Constructor with field issue
    ErrorCode(String code, HttpStatus httpStatus, String message,
              String field, String issue) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
        this.field = field;
        this.issue = issue;
    }

    public boolean hasFieldIssue() {
        return field != null && issue != null;
    }
}