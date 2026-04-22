package com.apiCodeGenerator.common.apputil.utils.commonutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Common {

    public static final Logger log = LoggerFactory.getLogger(Common.class);
//    private static final String REGEX_COMMA_WITH_NUMBER = "([0-9]+([,])(.)*)+";

    private static final Object LOCK = new Object();
    private static BigInteger lastTxnNumber = BigInteger.valueOf(0L);

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");



    /** STRING UTIL **/

    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String BlankReturn(String str) {
        if(isBlank(str)) {
            return null;
        }
        return str;
    }

    public static boolean areAllNotBlank(String... values) {
        if (values == null) return false;
        for (String val : values) {
            if (isBlank(val)) return false;
        }
        return true;
    }

    public static boolean areAllNotNull(Object... values) {
        if (values == null) return false;
        for (Object obj : values) {
            if (obj == null || isBlank(String.valueOf(obj))) return false;
        }
        return true;
    }

    public static String safeTrim(String str) {
        return (str != null && !str.trim().isEmpty()) ? str.trim() : null;
    }

    public static String safeToString(Object obj) {
        return (obj != null) ? obj.toString() : "";
    }

    public static Integer safeToInteger(String str) {
        try {
            return isNotBlank(str) ? Integer.parseInt(str.trim()) : null;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer format: {}", str);
            return null;
        }
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("\\d+(\\.\\d+)?");
    }

//    public static String encryptValue(String val) {
//        if (val.matches(REGEX_COMMA_WITH_NUMBER)) {
//            return Arrays.stream(val.split(","))
//                    .map(String::trim)
//                    .map(Encryptor::encryptText)
//                    .collect(Collectors.joining(","));
//        }
//        return Common.isNotBlank(val) ? Encryptor.encryptText(val) : "";
//    }

    //    /**
//	 * Checks for null or empty for multiple values.
//	 *
//	 * @param values variable number of strings
//	 * @return true if all strings are valid, false otherwise
//	 */
    public static boolean validateFields(Object... fields) {
        if (fields == null || fields.length == 0)
            return false;
        for (Object field : fields) {
            if (field == null)
                return false;
            if (field instanceof String str && str.trim().isEmpty())
                return false;
        }
        return true;
    }


    /** DATE UTIL **/

    public static boolean isValidDateFormat(String str) {
        return str != null && str.trim().matches("\\d{2}/\\d{2}/\\d{4}");
    }

    public static Timestamp parseToTimestamp(String dateStr) {
        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Date date = format.parse(dateStr);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            log.warn("Invalid date: {}", dateStr, e);
            return null;
        }
    }

    public static Date parseToDate(String dateStr) {
        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            return format.parse(dateStr);
        } catch (Exception e) {
            log.warn("Invalid date: {}", dateStr, e);
            return null;
        }
    }

    public static String convertDateToCompactFormat(String dateStr) {
        try {
            DateFormat input = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat output = new SimpleDateFormat("yyyyMMdd");
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            log.warn("Failed to convert date: {}", dateStr, e);
            return null;
        }
    }



    /** TRANSACTION UTILITY **/

    public static BigInteger generateUniqueTxnNumber() {
        LocalDateTime dateTime = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(dateTime.getYear() % 100); // last 2 digits of year
        sb.append(String.format("%02d%02d%02d%02d%02d", dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
        sb.append(String.format("%07d", dateTime.getNano() / 1000));

        try {
            TimeUnit.MILLISECONDS.sleep(1); // ensure uniqueness in fast loops
        } catch (InterruptedException ignored) {}

        BigInteger txnNumber = new BigInteger(sb.toString());

        synchronized (LOCK) {
            if (txnNumber.compareTo(lastTxnNumber) <= 0) {
                lastTxnNumber = lastTxnNumber.add(BigInteger.ONE);
            } else {
                lastTxnNumber = txnNumber;
            }
            return lastTxnNumber;
        }
    }



    /** JSON / OBJECT UTIL **/

    @SuppressWarnings("unchecked")
    public static String mergeOldFieldsFromNewJson(String oldJsonStr, String newJsonStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> oldMap = isNotBlank(oldJsonStr)
                    ? mapper.readValue(oldJsonStr, Map.class)
                    : new HashMap<>();

            Map<String, Object> newMap = mapper.readValue(newJsonStr, Map.class);

            Map<String, Object> result = new HashMap<>();
            for (String key : newMap.keySet()) {
                result.put(key, oldMap.get(key));
            }

            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Failed to merge JSON fields", e);
            return null;
        }
    }

    public static ObjectNode removeFieldsFromJson(ObjectNode node, String... fields) {
        if (node != null && fields != null) {
            for (String field : fields) {
                node.remove(field);
            }
        }
        return node;
    }



    /** QUERY STRING UTILITY **/

    public static void appendSearchConditions(StringBuilder query, String searchParam, String... columns) {
        if (columns == null || columns.length == 0) return;

        query.append(" AND (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) query.append(" OR ");
            query.append("LOWER(")
                    .append(columns[i].trim())
                    .append(") LIKE LOWER(CONCAT('%', :")
                    .append(searchParam)
                    .append(", '%'))");
        }
        query.append(")");
    }

    public static void appendLikeFilter(StringBuilder query, String searchValue, String column) {
        if (isNotBlank(searchValue) && isNotBlank(column)) {
            query.append(" AND ")
                    .append(column)
                    .append(" LIKE ('%")
                    .append(searchValue)
                    .append("%') ");
        }
    }

    public static void appendFindInSetFilter(StringBuilder query, String searchValue, String column) {
        if (isNotBlank(searchValue) && isNotBlank(column)) {
            query.append(" AND FIND_IN_SET(")
                    .append("'").append(searchValue).append("', ")
                    .append(column)
                    .append(") > 0 ");
        }
    }

    public static Map<String, String> getValidationErrors(BindingResult result){
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }

    public static String getDateString(Date date) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return formattedDate;
    }

    public static boolean isNullOrEmptyOrStringNull(String str) {
        return str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("null");
    }

    public static String getEnvironmentName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Linux";
        } else if (os.contains("mac")) {
            return "Mac";
        } else if (os.contains("sunos")) {
            return "Solaris";
        } else {
            return "Unknown";
        }
    }

    public static Map<String, String> convertRangeToMap(String range) {
        Map<String, String> result = new HashMap<>();
        result.put("from", "");
        result.put("to", "");

        if (range == null || range.trim().isEmpty()) {
            return result;
        }

        try {
            // Split by "to" or "-"
            String[] parts = range.split("\\s+(to|-)\\s+");
            if (parts.length != 2) {
                return result;
            }

            LocalDate startDate = LocalDate.parse(parts[0].trim(), INPUT_FORMAT);
            LocalDate endDate = LocalDate.parse(parts[1].trim(), INPUT_FORMAT);

            result.put("from", OUTPUT_FORMAT.format(startDate));
            result.put("to", OUTPUT_FORMAT.format(endDate));

        } catch (Exception e) {
            return null; // or return result if you prefer safe empty values
        }

        return result;
    }

//    public static String getUserStatusByCode(Short status, LocalDateTime lockUntil) {
//        // 1. If account is locked by time
//        if (lockUntil != null && lockUntil.isAfter(LocalDateTime.now())) {
//            return UserStatus.LOCKED.getDisplayName();  // Or "locked"
//        }
//        // 2. Now use status mapping
//        return switch (status) {
//            case 1 -> UserStatus.ACTIVE.getDisplayName();
//            case 0 -> UserStatus.INACTIVE.getDisplayName();
//            case 9 -> UserStatus.DELETED.getDisplayName();
//            default -> "";
//        };
//    }

    public static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


}
