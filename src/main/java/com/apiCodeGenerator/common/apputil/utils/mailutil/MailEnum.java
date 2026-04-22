package com.apiCodeGenerator.common.apputil.utils.mailutil;

public enum MailEnum {

    HOST("host"),
    PORT("port"),
    USER_NAME("username"),
    PASSWORD("password"),
    FROM_NAME("fromname"),
    FROM_MAIL("from"),
    SMTP_AUTH("mail.smtp.auth"),
    STARTTLS_ENABLE("mail.smtp.starttls.enable"),
    SSL_TRUST("mail.smtp.ssl.trust");

    private final String value;

    MailEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

