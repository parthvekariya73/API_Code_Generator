package com.apiCodeGenerator.common.apputil.utils.mailutil;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@Configuration("mailUtilConfig")
@EnableAsync
@EnableScheduling // This is important for the @Scheduled method in MailService
public class MailConfig {

    @Value("${mail.host}")
    private String host;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.from}")
    private String fromMail;

    @Value("${mail.fromname}")
    private String fromName;

    @Value("${mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable;

    @Value("${mail.smtp.ssl.trust:*}")
    private String sslTrust;

    @Bean("mailSenderUtil")
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.ssl.trust", sslTrust);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.debug", "false");

        return mailSender;
    }

    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        velocityEngine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        velocityEngine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");
        velocityEngine.init();
        return velocityEngine;
    }

    @Bean
    public MailProperties mailProperties() {
        return new MailProperties(fromMail, fromName);
    }
}