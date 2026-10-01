package com.appintegrations.brevo;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.ApiResponse;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import brevoModel.CreateSmtpEmail;
import brevoModel.GetEmailEventReport;
import brevoModel.GetEmailEventReportEvents;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailBcc;
import brevoModel.SendSmtpEmailCc;
import brevoModel.SendSmtpEmailReplyTo;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import com.appintegrations.brevo.dto.BrevoEmailRequest;
import com.appintegrations.brevo.dto.BrevoEmailResponse;
import com.appintegrations.brevo.dto.EmailRecipient;
import com.appintegrations.brevo.dto.EmailStatusResponse;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Service for sending emails using Brevo SDK.
 *
 * <p>Usage example:
 *
 * <pre>
 * brevoEmailService.sendEmail(
 *     "user@example.com",
 *     "Welcome!",
 *     "&lt;h1&gt;Hello&lt;/h1&gt;"
 * );
 * </pre>
 */
@Slf4j
public class BrevoEmailService {

  private final BrevoProperties properties;
  private TransactionalEmailsApi emailsApi;
  private JavaMailSender mailSender;

  public BrevoEmailService(BrevoProperties properties) {
    this.properties = properties;
  }

  /** Initialize the Brevo SDK client. */
  @PostConstruct
  public void init() {
    if (isEnabled()) {
      ApiClient defaultClient = Configuration.getDefaultApiClient();
      ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
      apiKey.setApiKey(properties.getApiKey());
      this.emailsApi = new TransactionalEmailsApi();
      log.info("Brevo Email SDK initialized successfully");
    }
  }

  /** Check if Brevo API is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getApiKey() != null
        && !properties.getApiKey().isBlank();
  }

  /** Check if SMTP is enabled and configured. */
  public boolean isSmtpEnabled() {
    BrevoProperties.Smtp smtp = properties.getSmtp();
    return smtp != null
        && smtp.isEnabled()
        && smtp.getHost() != null
        && !smtp.getHost().isBlank()
        && smtp.getLogin() != null
        && !smtp.getLogin().isBlank()
        && smtp.getSmtpKey() != null
        && !smtp.getSmtpKey().isBlank();
  }

  /** Send a simple email using Brevo SDK. */
  public BrevoEmailResponse sendEmail(String toEmail, String subject, String htmlContent) {
    return sendEmail(toEmail, null, subject, htmlContent);
  }

  /** Send an email with recipient name using Brevo SDK. */
  public BrevoEmailResponse sendEmail(
      String toEmail, String toName, String subject, String htmlContent) {
    BrevoEmailRequest request =
        BrevoEmailRequest.builder()
            .sender(getDefaultSender())
            .to(List.of(EmailRecipient.of(toEmail, toName)))
            .subject(subject)
            .htmlContent(htmlContent)
            .build();

    return sendEmail(request);
  }

  /** Send an email using Brevo SDK with full request customization. */
  public BrevoEmailResponse sendEmail(BrevoEmailRequest request) {
    if (!isEnabled()) {
      log.warn("Brevo is not enabled or not configured. Skipping email send.");
      BrevoEmailResponse response = new BrevoEmailResponse();
      response.setMessage("Brevo is disabled");
      return response;
    }

    if (request.getSender() == null) {
      request.setSender(getDefaultSender());
    }

    try {
      SendSmtpEmail email = buildSendSmtpEmail(request);

      log.info(
          "Sending email via Brevo SDK - To: {}, Subject: {}",
          request.getTo().get(0).getEmail(),
          request.getSubject());

      ApiResponse<CreateSmtpEmail> apiResponse = emailsApi.sendTransacEmailWithHttpInfo(email);
      CreateSmtpEmail result = apiResponse.getData();

      log.info("Email sent successfully via Brevo SDK. MessageId: {}", result.getMessageId());

      BrevoEmailResponse response = new BrevoEmailResponse();
      response.setMessageId(result.getMessageId());
      // Extract rate limit headers
      extractRateLimitHeaders(apiResponse.getHeaders(), response);
      return response;

    } catch (ApiException e) {
      log.error(
          "Brevo SDK API error sending email. Code: {}, Body: {}",
          e.getCode(),
          e.getResponseBody(),
          e);
      BrevoEmailResponse errorResponse = new BrevoEmailResponse();
      errorResponse.setCode(String.valueOf(e.getCode()));
      errorResponse.setMessage("Brevo API error: " + e.getResponseBody());
      // Extract rate limit headers from ApiException (429 responses still carry headers)
      if (e.getResponseHeaders() != null) {
        extractRateLimitHeaders(e.getResponseHeaders(), errorResponse);
      }
      return errorResponse;
    } catch (Exception e) {
      log.error("Error sending email via Brevo SDK: {}", e.getMessage(), e);
      BrevoEmailResponse errorResponse = new BrevoEmailResponse();
      errorResponse.setMessage("Failed to send email: " + e.getMessage());
      return errorResponse;
    }
  }

  /** Send an email asynchronously using Brevo SDK. */
  public CompletableFuture<BrevoEmailResponse> sendEmailAsync(
      String toEmail, String subject, String htmlContent) {
    return CompletableFuture.supplyAsync(() -> sendEmail(toEmail, subject, htmlContent));
  }

  /** Send an email via SMTP (direct JavaMail). */
  public BrevoEmailResponse sendEmailViaSmtp(BrevoEmailRequest request) {
    if (!isSmtpEnabled()) {
      log.warn("Brevo SMTP is not enabled. Skipping email send.");
      BrevoEmailResponse response = new BrevoEmailResponse();
      response.setMessage("SMTP is disabled or not configured");
      return response;
    }

    try {
      JavaMailSender sender = getMailSender();
      MimeMessage message = ((JavaMailSenderImpl) sender).createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      EmailRecipient fromSender =
          request.getSender() != null ? request.getSender() : getDefaultSender();
      helper.setFrom(
          fromSender.getEmail(), fromSender.getName() != null ? fromSender.getName() : "");

      if (request.getTo() != null && !request.getTo().isEmpty()) {
        helper.setTo(request.getTo().get(0).getEmail());
      }

      helper.setSubject(request.getSubject());

      if (request.getHtmlContent() != null) {
        helper.setText(
            request.getTextContent() != null ? request.getTextContent() : "",
            request.getHtmlContent());
      } else if (request.getTextContent() != null) {
        helper.setText(request.getTextContent(), false);
      }

      if (request.getCc() != null && !request.getCc().isEmpty()) {
        String[] ccEmails =
            request.getCc().stream().map(EmailRecipient::getEmail).toArray(String[]::new);
        helper.setCc(ccEmails);
      }

      if (request.getBcc() != null && !request.getBcc().isEmpty()) {
        String[] bccEmails =
            request.getBcc().stream().map(EmailRecipient::getEmail).toArray(String[]::new);
        helper.setBcc(bccEmails);
      }

      if (request.getReplyTo() != null) {
        helper.setReplyTo(request.getReplyTo().getEmail());
      }

      sender.send(message);

      String messageId = "smtp-" + System.currentTimeMillis();
      log.info(
          "Email sent via SMTP to: {}. MessageId: {}",
          request.getTo().get(0).getEmail(),
          messageId);

      BrevoEmailResponse response = new BrevoEmailResponse();
      response.setMessageId(messageId);
      return response;

    } catch (MessagingException e) {
      log.error("SMTP error: {}", e.getMessage(), e);
      BrevoEmailResponse errorResponse = new BrevoEmailResponse();
      errorResponse.setMessage("SMTP error: " + e.getMessage());
      return errorResponse;
    } catch (Exception e) {
      log.error("Error sending email via SMTP: {}", e.getMessage(), e);
      BrevoEmailResponse errorResponse = new BrevoEmailResponse();
      errorResponse.setMessage("Failed to send email: " + e.getMessage());
      return errorResponse;
    }
  }

  /**
   * Get email status by message ID using Brevo SDK.
   *
   * @param messageId the messageId returned from sendEmail
   * @return EmailStatusResponse containing the delivery status
   */
  public EmailStatusResponse getEmailStatus(String messageId) {
    if (!isEnabled()) {
      log.warn("Brevo is not enabled. Cannot get email status.");
      return EmailStatusResponse.builder()
          .messageId(messageId)
          .status("UNKNOWN")
          .message("Brevo is disabled")
          .build();
    }

    try {
      log.info("Getting email status for messageId: {}", messageId);

      // Use getEmailEventReport with messageId filter
      GetEmailEventReport report =
          emailsApi.getEmailEventReport(
              null, // limit
              null, // offset
              null, // startDate
              null, // endDate
              null, // days
              null, // email
              null, // event
              null, // tags
              messageId, // messageId
              null, // templateId
              null // sort
              );

      if (report.getEvents() == null || report.getEvents().isEmpty()) {
        log.info("No events found for messageId: {}", messageId);
        return EmailStatusResponse.builder()
            .messageId(messageId)
            .status("PENDING")
            .message("No events found yet")
            .build();
      }

      // Get the most recent event
      GetEmailEventReportEvents latestEvent = report.getEvents().get(0);

      String eventStr = latestEvent.getEvent() != null ? latestEvent.getEvent().getValue() : null;
      String status = mapEventToStatus(eventStr);
      String email = latestEvent.getEmail();
      String date = latestEvent.getDate();

      log.info("Email status for messageId {}: event={}, status={}", messageId, eventStr, status);

      return EmailStatusResponse.builder()
          .messageId(messageId)
          .status(status)
          .event(eventStr)
          .email(email)
          .eventDate(date)
          .message("Status retrieved successfully")
          .build();

    } catch (ApiException e) {
      log.error(
          "Brevo SDK API error getting email status. Code: {}, Body: {}",
          e.getCode(),
          e.getResponseBody(),
          e);
      return EmailStatusResponse.builder()
          .messageId(messageId)
          .status("ERROR")
          .message("Brevo API error: " + e.getResponseBody())
          .build();
    } catch (Exception e) {
      log.error("Error getting email status via Brevo SDK: {}", e.getMessage(), e);
      return EmailStatusResponse.builder()
          .messageId(messageId)
          .status("ERROR")
          .message("Failed to get status: " + e.getMessage())
          .build();
    }
  }

  /** Map Brevo event types to standard status. */
  private String mapEventToStatus(String event) {
    if (event == null) {
      return "UNKNOWN";
    }
    return switch (event.toLowerCase()) {
      case "delivered" -> "DELIVERED";
      case "opened" -> "OPENED";
      case "clicks" -> "CLICKED";
      case "hardBounces", "hard_bounces" -> "HARD_BOUNCED";
      case "softBounces", "soft_bounces" -> "SOFT_BOUNCED";
      case "blocked" -> "BLOCKED";
      case "spam" -> "SPAM";
      case "unsubscribed" -> "UNSUBSCRIBED";
      case "requests" -> "SENT";
      case "deferred" -> "DEFERRED";
      default -> event.toUpperCase();
    };
  }

  /**
   * Extract Brevo rate-limit headers from an API response and populate the DTO. Headers:
   * x-sib-ratelimit-limit, x-sib-ratelimit-remaining, x-sib-ratelimit-reset
   */
  private void extractRateLimitHeaders(
      java.util.Map<String, java.util.List<String>> headers, BrevoEmailResponse response) {
    if (headers == null) return;
    headers.forEach(
        (key, values) -> {
          if (values == null || values.isEmpty()) return;
          String value = values.get(0);
          switch (key.toLowerCase()) {
            case "x-sib-ratelimit-limit" -> response.setRateLimitLimit(value);
            case "x-sib-ratelimit-remaining" -> response.setRateLimitRemaining(value);
            case "x-sib-ratelimit-reset" -> response.setRateLimitReset(value);
            default -> {}
          }
        });
    log.debug(
        "[RATELIMIT HEADERS] limit={} remaining={} reset={}",
        response.getRateLimitLimit(),
        response.getRateLimitRemaining(),
        response.getRateLimitReset());
  }

  /** Build SendSmtpEmail from BrevoEmailRequest. */
  private SendSmtpEmail buildSendSmtpEmail(BrevoEmailRequest request) {
    SendSmtpEmail email = new SendSmtpEmail();

    // Set sender
    SendSmtpEmailSender sender = new SendSmtpEmailSender();
    sender.setEmail(request.getSender().getEmail());
    sender.setName(request.getSender().getName());
    email.setSender(sender);

    // Set recipients
    List<SendSmtpEmailTo> toList =
        request.getTo().stream()
            .map(
                r -> {
                  SendSmtpEmailTo to = new SendSmtpEmailTo();
                  to.setEmail(r.getEmail());
                  to.setName(r.getName());
                  return to;
                })
            .collect(Collectors.toList());
    email.setTo(toList);

    // Set subject and content
    email.setSubject(request.getSubject());
    email.setHtmlContent(request.getHtmlContent());
    if (request.getTextContent() != null) {
      email.setTextContent(request.getTextContent());
    }

    // Set CC if present
    if (request.getCc() != null && !request.getCc().isEmpty()) {
      List<SendSmtpEmailCc> ccList =
          request.getCc().stream()
              .map(
                  r -> {
                    SendSmtpEmailCc cc = new SendSmtpEmailCc();
                    cc.setEmail(r.getEmail());
                    cc.setName(r.getName());
                    return cc;
                  })
              .collect(Collectors.toList());
      email.setCc(ccList);
    }

    // Set BCC if present
    if (request.getBcc() != null && !request.getBcc().isEmpty()) {
      List<SendSmtpEmailBcc> bccList =
          request.getBcc().stream()
              .map(
                  r -> {
                    SendSmtpEmailBcc bcc = new SendSmtpEmailBcc();
                    bcc.setEmail(r.getEmail());
                    bcc.setName(r.getName());
                    return bcc;
                  })
              .collect(Collectors.toList());
      email.setBcc(bccList);
    }

    // Set reply-to if present
    if (request.getReplyTo() != null) {
      SendSmtpEmailReplyTo replyTo = new SendSmtpEmailReplyTo();
      replyTo.setEmail(request.getReplyTo().getEmail());
      replyTo.setName(request.getReplyTo().getName());
      email.setReplyTo(replyTo);
    }

    // Set tags if present
    if (request.getTags() != null && !request.getTags().isEmpty()) {
      email.setTags(request.getTags());
    }

    // Set template ID if present
    if (request.getTemplateId() != null) {
      email.setTemplateId(request.getTemplateId());
    }

    return email;
  }

  private EmailRecipient getDefaultSender() {
    return EmailRecipient.builder()
        .email(properties.getSender().getEmail())
        .name(properties.getSender().getName())
        .build();
  }

  private synchronized JavaMailSender getMailSender() {
    if (mailSender == null) {
      JavaMailSenderImpl sender = new JavaMailSenderImpl();
      BrevoProperties.Smtp smtp = properties.getSmtp();

      sender.setHost(smtp.getHost());
      sender.setPort(smtp.getPort());
      sender.setUsername(smtp.getLogin());
      sender.setPassword(smtp.getSmtpKey());

      var props = sender.getJavaMailProperties();
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.auth", "true");
      if (smtp.isTls()) {
        props.put("mail.smtp.starttls.enable", "true");
      }
      props.put("mail.debug", "false");

      mailSender = sender;
    }
    return mailSender;
  }
}
