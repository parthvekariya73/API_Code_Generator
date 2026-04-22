package com.apiCodeGenerator.common.apputil.utils.mailutil;

import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class MailUtil {

    private final JavaMailSenderImpl javaMailSender;
    private final VelocityEngine velocityEngine;
    private final MailProperties mailProperties;

    public MailUtil(JavaMailSenderImpl mailSender,
                    VelocityEngine velocityEngine,
                    MailProperties mailProperties) {
        this.javaMailSender = mailSender;
        this.velocityEngine = velocityEngine;
        this.mailProperties = mailProperties;
    }

    @Async
    public void sendEmail(Set<String> recipients, String templatePath,
                          VelocityContext model, String subject,
                          Set<String> ccRecipients, Set<String> bccRecipients,
                          List<MultipartFile> attachments) {
        for (String recipient : recipients) {
            try {
                if (recipient == null || recipient.isBlank()) {
                    log.info("Skipping blank recipient in bulk template send.");
                    continue;
                }

                // Merge template with model
                StringWriter stringWriter = new StringWriter();

                if(templatePath != null){
                    velocityEngine.mergeTemplate(templatePath, "UTF-8", model, stringWriter);
                }


                // Create message
                MimeMessageHelper message = new MimeMessageHelper(
                        javaMailSender.createMimeMessage(), true, "UTF-8");

                message.setTo(recipient);
                message.setFrom(new InternetAddress(
                        mailProperties.fromMail(),
                        mailProperties.fromName()));
                message.setSubject(subject);
                message.setText(stringWriter.toString(), true);

                // Add CC recipients if provided
                if (ccRecipients != null && !ccRecipients.isEmpty()) {
                    String[] ccArray = ccRecipients.stream()
                            .filter(cc -> cc != null && !cc.isBlank())
                            .toArray(String[]::new);
                    if (ccArray.length > 0) {
                        message.setCc(ccArray);
                    }
                }

                if (bccRecipients != null && !bccRecipients.isEmpty()) {
                    String[] bccArray = bccRecipients.stream()
                            .filter(cc -> cc != null && !cc.isBlank())
                            .toArray(String[]::new);
                    if (bccArray.length > 0) {
                        message.setBcc(bccArray);
                    }
                }

                // Add MultipartFile attachments
                if (attachments != null && !attachments.isEmpty()) {
                    for (MultipartFile file : attachments) {
                        if (file != null && !file.isEmpty()) {
                            ByteArrayResource resource = new ByteArrayResource(file.getBytes());
                            message.addAttachment(file.getOriginalFilename(), resource);
                            log.debug("Attached file: {} ({} bytes)",
                                    file.getOriginalFilename(), file.getSize());
                        }
                    }
                }

                javaMailSender.send(message.getMimeMessage());
                log.info("Template email sent successfully to: {} with {} attachments",
                        recipient, attachments != null ? attachments.size() : 0);

            } catch (Exception e) {
                log.error("Failed to send template email to: {}", recipient, e);
            }
        }
    }


    // Add to MailUtil.java
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            if (to == null || to.isBlank()) {
                log.warn("Cannot send HTML email to blank recipient");
                return;
            }

            MimeMessageHelper helper = new MimeMessageHelper(
                    javaMailSender.createMimeMessage(), true, "UTF-8");

            helper.setFrom(new InternetAddress(mailProperties.fromMail(), mailProperties.fromName()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content

            javaMailSender.send(helper.getMimeMessage());
            log.info("HTML Email sent to {} | Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendEmail(Set<String> recipients, String templatePath,
                          VelocityContext model, String subject) {
        sendEmail(recipients, templatePath, model, subject, null, null, null);
    }

    @Async
    public void sendEmail(Set<String> recipients, String templatePath,
                          VelocityContext model, String subject,
                          Set<String> ccRecipients, Set<String> bccRecipients) {
        sendEmail(recipients, templatePath, model, subject, ccRecipients, bccRecipients, null);
    }

    @Async
    @Deprecated
    public void sendEmailWithFileAttachments(Set<String> recipients, String templatePath,
                                             VelocityContext model, String subject,
                                             Set<String> ccRecipients, Set<File> attachments) {
        for (String recipient : recipients) {
            try {
                if (recipient == null || recipient.isBlank()) {
                    log.warn("Skipping blank recipient in bulk template send.");
                    continue;
                }

                // Merge template with model
                StringWriter stringWriter = new StringWriter();
                velocityEngine.mergeTemplate(templatePath, "UTF-8", model, stringWriter);

                // Create message
                MimeMessageHelper message = new MimeMessageHelper(
                        javaMailSender.createMimeMessage(), true, "UTF-8");

                message.setTo(recipient);
                message.setFrom(new InternetAddress(
                        mailProperties.fromMail(),
                        mailProperties.fromName()));
                message.setSubject(subject);
                message.setText(stringWriter.toString(), true);

                // Add CC recipients
                if (ccRecipients != null && !ccRecipients.isEmpty()) {
                    String[] ccArray = ccRecipients.stream()
                            .filter(cc -> cc != null && !cc.isBlank())
                            .toArray(String[]::new);
                    if (ccArray.length > 0) {
                        message.setCc(ccArray);
                    }
                }

                // Add File attachments
                if (attachments != null && !attachments.isEmpty()) {
                    for (File file : attachments) {
                        if (file != null && file.exists()) {
                            FileSystemResource resource = new FileSystemResource(file);
                            message.addAttachment(file.getName(), resource);
                        }
                    }
                }

                javaMailSender.send(message.getMimeMessage());
                log.info("Template email sent successfully to: {}", recipient);

            } catch (Exception e) {
                log.error("Failed to send template email to: {}", recipient, e);
            }
        }
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body,
                                boolean isHtml, List<MultipartFile> attachments) {
        try {
            if (to == null || to.isBlank()) {
                log.warn("Cannot send simple email to blank recipient");
                return;
            }

            MimeMessageHelper message = new MimeMessageHelper(
                    javaMailSender.createMimeMessage(), true, "UTF-8");

            message.setTo(to);
            message.setFrom(new InternetAddress(
                    mailProperties.fromMail(),
                    mailProperties.fromName()));
            message.setSubject(subject);
            message.setText(body, isHtml);

            // Add MultipartFile attachments
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile file : attachments) {
                    if (file != null && !file.isEmpty()) {
                        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
                        message.addAttachment(file.getOriginalFilename(), resource);
                        log.debug("Attached file: {} ({} bytes)",
                                file.getOriginalFilename(), file.getSize());
                    }
                }
            }

            javaMailSender.send(message.getMimeMessage());
            log.info("Simple email sent successfully to: {} with {} attachments",
                    to, attachments != null ? attachments.size() : 0);

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body, boolean isHtml) {
        sendSimpleEmail(to, subject, body, isHtml, null);
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        sendSimpleEmail(to, subject, body, false, null);
    }

    @Async
    public void sendSimpleBulkEmail(Set<String> recipients, String subject,
                                    String body, boolean isHtml,
                                    List<MultipartFile> attachments) {
        for (String recipient : recipients) {
            sendSimpleEmail(recipient, subject, body, isHtml, attachments);
        }
        log.info("Simple bulk email process finished for {} recipients with {} attachments.",
                recipients.size(), attachments != null ? attachments.size() : 0);
    }

    @Async
    public void sendSimpleBulkEmail(Set<String> recipients, String subject,
                                    String body, boolean isHtml) {
        sendSimpleBulkEmail(recipients, subject, body, isHtml, null);
    }

    @Async
    public void sendEmailToSingle(String recipient, String templatePath,
                                  VelocityContext model, String subject,
                                  List<MultipartFile> attachments) {

        if (recipient == null || recipient.isBlank()) {
            log.warn("Cannot send email to single recipient: recipient address is null or blank.");
            return;
        }

        sendEmail(Set.of(recipient), templatePath, model, subject, null, null, attachments);
    }

    @Async
    public void sendEmailToSingle(String recipient, String templatePath,
                                  VelocityContext model, String subject) {
        sendEmailToSingle(recipient, templatePath, model, subject, null);
    }

    public boolean validateAttachments(List<MultipartFile> attachments,
                                       long maxSizePerFile, int maxFileCount) {
        if (attachments == null || attachments.isEmpty()) {
            return true; // No attachments is valid
        }

        // Check file count
        if (attachments.size() > maxFileCount) {
            log.warn("Too many attachments: {} (max: {})", attachments.size(), maxFileCount);
            return false;
        }

        // Check individual file sizes
        for (MultipartFile file : attachments) {
            if (file != null && file.getSize() > maxSizePerFile) {
                log.warn("File too large: {} ({} bytes, max: {} bytes)",
                        file.getOriginalFilename(), file.getSize(), maxSizePerFile);
                return false;
            }
        }

        return true;
    }

    public long getTotalAttachmentSize(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return 0;
        }

        return attachments.stream()
                .filter(file -> file != null)
                .mapToLong(MultipartFile::getSize)
                .sum();
    }

    public List<String> getAttachmentFileNames(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        return attachments.stream()
                .filter(file -> file != null)
                .map(MultipartFile::getOriginalFilename)
                .toList();
    }
    public boolean hasAttachments(List<MultipartFile> attachments) {
        return attachments != null && !attachments.isEmpty() &&
                attachments.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    public int getAttachmentCount(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return 0;
        }

        return (int) attachments.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();
    }

    public String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public void logAttachmentDetails(List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            log.debug("No attachments");
            return;
        }

        log.debug("Attachment details:");
        for (int i = 0; i < attachments.size(); i++) {
            MultipartFile file = attachments.get(i);
            if (file != null && !file.isEmpty()) {
                log.debug("  [{}] {} - {} ({})",
                        i + 1,
                        file.getOriginalFilename(),
                        formatFileSize(file.getSize()),
                        file.getContentType());
            }
        }
        log.debug("Total size: {}", formatFileSize(getTotalAttachmentSize(attachments)));
    }











    // Added By Parth Ajudiya
    @Async
    public void sendEmail(Set<String> recipients,
                          Set<String> ccRecipients,
                          Set<String> bccRecipients,
                          String subject,
                          String  body,
                          List<MultipartFile> attachments) {


        String [] recipientsArray = recipients.toArray(new String[0]);
        try {

            VelocityContext context = new VelocityContext();
            context.put("subject", subject);
            context.put("companyName", "Market-Access");
            context.put("year", 2026);

            // HTML body from frontend
            context.put("htmlBody", body);


            // Merge template with model
            StringWriter stringWriter = new StringWriter();

            String templatePath = "templates/mail-base/email-base.vm";
            velocityEngine.mergeTemplate(templatePath, "UTF-8", context, stringWriter);


            // Create message
            MimeMessageHelper message = new MimeMessageHelper(
                    javaMailSender.createMimeMessage(), true, "UTF-8");

            message.setTo(recipientsArray);
            message.setFrom(new InternetAddress(
                    mailProperties.fromMail(),
                    mailProperties.fromName()));
            message.setSubject(subject);
            message.setText(stringWriter.toString(), true);

            // Add CC recipients if provided
            if (ccRecipients != null && !ccRecipients.isEmpty()) {
                String[] ccArray = ccRecipients.stream()
                        .filter(cc -> cc != null && !cc.isBlank())
                        .toArray(String[]::new);
                if (ccArray.length > 0) {
                    message.setCc(ccArray);
                }
            }

            if (bccRecipients != null && !bccRecipients.isEmpty()) {
                String[] bccArray = bccRecipients.stream()
                        .filter(cc -> cc != null && !cc.isBlank())
                        .toArray(String[]::new);
                if (bccArray.length > 0) {
                    message.setBcc(bccArray);
                }
            }

            // Add MultipartFile attachments
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile file : attachments) {
                    if (file != null && !file.isEmpty()) {
                        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
                        message.addAttachment(file.getOriginalFilename(), resource);
                        log.debug("Attached file: {} ({} bytes)",
                                file.getOriginalFilename(), file.getSize());
                    }
                }
            }

            javaMailSender.send(message.getMimeMessage());
            log.info("Template email sent successfully to: {} with {} attachments",
                    "client", attachments != null ? attachments.size() : 0);

        } catch (Exception e) {
            log.error("Failed to send template email to: {}", "client", e);
        }
    }
}

