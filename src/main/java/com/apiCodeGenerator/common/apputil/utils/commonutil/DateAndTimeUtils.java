package com.apiCodeGenerator.common.apputil.utils.commonutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.format.datetime.DateFormatter;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.UUID;

@Configuration
public class DateAndTimeUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(DateAndTimeUtils.class);

    private static final char[] hexArray = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    @Autowired
    private Environment env;

    private SimpleDateFormat dateFormate;

    private SimpleDateFormat timeFormate;

    private SimpleDateFormat dateTimeFormate;

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    DateFormatter dateFormatter() {
        return new DateFormatter();
    }

    /**
     * Using This SimpleDateFormat Convert Date to Date Format
     *
     * @return java.text.SimpleDateFormat
     * @throws Exception
     */
    @Bean(name = { "dateFormat" })
    SimpleDateFormat getSimpleDateFormatObject() throws Exception {
        String dateFormat = env.getProperty("common.dataformat");
        if (null == dateFormate) {
            dateFormate = (null != dateFormat && dateFormat.trim().length() > 0) ? new SimpleDateFormat(dateFormat) : null;
        }
        return dateFormate;
    }

    /**
     * Using This SimpleDateFormat Convert Date to Time Format
     *
     * @return java.text.SimpleDateFormat
     * @throws Exception
     */
    @Bean(name = { "timeFormat" })
    SimpleDateFormat getSimpleTimeFormatObject() throws Exception {
        String dateFormat = env.getProperty("common.timeformat");
        if (null == timeFormate) {
            timeFormate = (null != dateFormat && dateFormat.trim().length() > 0) ? new SimpleDateFormat(dateFormat) : null;
        }
        return timeFormate;
    }

    /**
     * Using This SimpleDateFormat Convert Date to Date And Time Format
     *
     * @return java.text.SimpleDateFormat
     * @throws Exception
     */
    @Bean(name = { "dateTimeFormat" })
    SimpleDateFormat getSimpleDateTimeFormatObject() throws Exception {
        String dateFormat = env.getProperty("common.datetimeformat");
        if (null == dateTimeFormate) {
            dateTimeFormate = (null != dateFormat && dateFormat.trim().length() > 0) ? new SimpleDateFormat(dateFormat) : null;
        }
        return dateTimeFormate;
    }

    /**
     * Convert SQL Date to Java Util Date
     *
     * @param date java.sql.Date
     * @return java.util.Date
     */
    public Date convertToJavaDate(java.sql.Date date) {
        return new Date(date.getTime());
    }

    /**
     * Convert Time stamp to Date
     *
     * @param timestamp java.sql.Timestamp
     * @return java.util.Date
     */
    public Date convertToJavaDate(java.sql.Timestamp timestamp) {
        return new Date(timestamp.getTime());
    }

    /**
     * Convert Time to Date
     *
     * @param time java.sql.Time
     * @return java.util.Date
     */
    public Date convertToJavaDate(java.sql.Time time) {
        return new Date(time.getTime());
    }

    /**
     * Convert Date To LocalDate
     *
     * @param date java.util.Date
     * @return java.time.LocalDate
     */
    public LocalDate convertToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Convert Date To LocalDateTime
     *
     * @param date java.util.Date
     * @return java.time.LocalDateTime
     */
    public LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Two Date Difference In Period (Days,Months,Years)
     *
     * @param firstDate
     * @param secondDate
     * @return java.time.Period
     */
    public Period getPeriodUsingLocalDate(Date firstDate, Date secondDate) {
        LocalDate first = convertToLocalDate(firstDate);
        LocalDate second = convertToLocalDate(secondDate);
        return Period.between(first, second);
    }

    /**
     * Two Date Difference In Duration (Days,Hours,Milliseconds,Minutes,Nanoseconds)
     *
     * @param firstDate
     * @param secondDate
     * @return java.time.Duration
     */
    public Duration getDurationUsingLocalDate(Date firstDate, Date secondDate) {
        LocalDate first = convertToLocalDate(firstDate);
        LocalDate second = convertToLocalDate(secondDate);
        return Duration.between(first, second);
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String getUUID() {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final String UUID_WITH_TIMESTAMP = UUID.randomUUID().toString() + System.currentTimeMillis();
            sha256.update(UUID_WITH_TIMESTAMP.getBytes());
            return bytesToHex(sha256.digest());
        } catch (Exception e) {
            LOGGER.error("{}", e);
        }
        return null;
    }

    public static Long getdatenumber() {
        Long datenumber = null;
        try {
            String pattern = "yyyyMMdd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            datenumber = Long.parseLong(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datenumber;
    }

    public static Long getInputdatenumber(Date _date) {
        Long datenumber = null;
        try {
            String pattern = "yyyyMMdd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(_date);
            datenumber = Long.parseLong(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datenumber;
    }

    public static int getdatenumberInt() {
        int datenumber = 0;
        try {
            String pattern = "yyyyMMdd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            datenumber = Integer.parseInt(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datenumber;
    }

    public static Long getdatenumberFromDate(String Date,String Formate) {
        Long datenumber = null;
        try {
            String pattern = "yyyyMMdd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Formate);
            Date date = simpleDateFormat.parse(Date);
            simpleDateFormat = new SimpleDateFormat(pattern);
            String newdate = simpleDateFormat.format(date);
            datenumber = Long.parseLong(newdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datenumber;
    }
}
