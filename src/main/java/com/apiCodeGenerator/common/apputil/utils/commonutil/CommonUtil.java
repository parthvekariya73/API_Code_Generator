package com.apiCodeGenerator.common.apputil.utils.commonutil;

import com.apiCodeGenerator.common.apputil.response.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    private static final Object LOCK = new Object();
    private static BigDecimal lastTxnNumber = BigDecimal.valueOf(0L);

    /** TRANSACTION UTILITY **/
    public static BigDecimal generateUniqueTxnNumber() {
        LocalDateTime dateTime = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(dateTime.getYear() % 100); // last 2 digits of year
        sb.append(String.format("%02d%02d%02d%02d%02d", dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
        sb.append(String.format("%07d", dateTime.getNano() / 1000));

        try {
            TimeUnit.MILLISECONDS.sleep(1); // ensure uniqueness in fast loops
        } catch (InterruptedException ignored) {}

        BigDecimal txnNumber = new BigDecimal(sb.toString());

        synchronized (LOCK) {
            if (txnNumber.compareTo(lastTxnNumber) <= 0) {
                lastTxnNumber = lastTxnNumber.add(BigDecimal.ONE);
            } else {
                lastTxnNumber = txnNumber;
            }
            return lastTxnNumber;
        }
    }

    public static String getRequestId(HttpServletRequest request) {
        return (String) request.getAttribute("X-Request-ID");
    }

    public static Meta getMetaData(int page, int size, Page<?> pageObject) {
        return  Meta.builder()
                .page(page)
                .size(size)
                .totalElements((int) pageObject.getTotalElements())
                .totalPages(pageObject.getTotalPages())
                .hasNext(pageObject.hasNext())
                .hasPrevious(pageObject.hasPrevious())
                .build();
    }

    public static UUID getStringToUuid(String strUuid) {
        return UUID.fromString(strUuid);
    }
}
