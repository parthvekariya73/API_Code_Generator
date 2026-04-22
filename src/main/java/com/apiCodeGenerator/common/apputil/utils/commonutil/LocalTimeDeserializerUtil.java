package com.apiCodeGenerator.common.apputil.utils.commonutil;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalTimeDeserializerUtil extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER_WITH_AM_PM =
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.ENGLISH);

    private static final DateTimeFormatter FORMATTER_24HR =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getText().trim();
        if (value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, FORMATTER_WITH_AM_PM);
        } catch (Exception ignored) {}

        try {
            return LocalDateTime.parse(value, FORMATTER_24HR);
        } catch (Exception ignored) {}

//        throw new IllegalArgumentException("Invalid datetime format: " + value);
        throw ctxt.weirdStringException(
                value,
                LocalDateTime.class,
                "Expected formats: yyyy-MM-dd hh:mm a OR yyyy-MM-dd HH:mm:ss"
        );
    }
}

