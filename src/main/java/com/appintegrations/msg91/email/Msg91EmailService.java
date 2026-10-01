package com.appintegrations.msg91.email;

import com.appintegrations.msg91.email.dto.Msg91CreateEmailTemplateRequest;
import com.appintegrations.msg91.email.dto.Msg91CreateEmailTemplateResponse;
import com.appintegrations.msg91.email.dto.Msg91EmailLogsResponse;
import com.appintegrations.msg91.email.dto.Msg91EmailRecipientGroup;
import com.appintegrations.msg91.email.dto.Msg91EmailRequest;
import com.appintegrations.msg91.email.dto.Msg91EmailResponse;
import com.appintegrations.msg91.email.dto.Msg91EmailSender;
import com.appintegrations.msg91.email.dto.Msg91EmailTemplateListResponse;
import com.appintegrations.msg91.email.dto.Msg91EmailTemplateVersionResponse;
import com.appintegrations.msg91.sms.Msg91Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending emails and managing email templates via MSG91 Email API.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Send a simple email with template
 * msg91EmailService.sendEmail(
 *     "recipient@example.com",
 *     "Recipient Name",
 *     "template_slug",
 *     Map.of("VAR1", "value1", "VAR2", "value2")
 * );
 *
 * // Send with full request customization
 * Msg91EmailRequest request = Msg91EmailRequest.builder()
 *     .from(Msg91EmailSender.of("sender@domain.com", "Sender Name"))
 *     .domain("domain.mailer91.com")
 *     .templateId("template_slug")
 *     .recipients(List.of(
 *         Msg91EmailRecipientGroup.singleRecipient(
 *             "recipient@example.com",
 *             "Recipient Name",
 *             Map.of("VAR1", "value1")
 *         )
 *     ))
 *     .build();
 * msg91EmailService.sendEmail(request);
 *
 * // Create an email template
 * Msg91CreateEmailTemplateResponse response = msg91EmailService.createTemplate(
 *     "Welcome Email",
 *     "Welcome {{first_name}}!",
 *     "&lt;html&gt;&lt;body&gt;Hello {{first_name}}!&lt;/body&gt;&lt;/html&gt;"
 * );
 *
 * // Get list of templates with pagination
 * Msg91EmailTemplateListResponse templates = msg91EmailService.getTemplates(1, 10, true);
 *
 * // Get template version status
 * Msg91EmailTemplateVersionResponse versionStatus = msg91EmailService.getTemplateVersion(73900L, true);
 * if (versionStatus.isApproved()) {
 *     // Template is approved and ready to use
 * }
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/send-email">MSG91 Email API</a>
 */
@Slf4j
public class Msg91EmailService {

  private static final String EMAIL_SEND_ENDPOINT = "/email/send";
  private static final String EMAIL_TEMPLATES_ENDPOINT = "/email/templates";
  private static final String EMAIL_TEMPLATE_VERSIONS_ENDPOINT = "/email/template-versions";
  private static final String EMAIL_LOGS_ENDPOINT = "/report/logs/mail";

  private final Msg91Properties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public Msg91EmailService(Msg91Properties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
    log.info("MSG91 Email Service initialized");
  }

  /** Check if MSG91 Email is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getAuthKey() != null
        && !properties.getAuthKey().isBlank();
  }

  // ==================== Send Email Methods ====================

  /**
   * Send email to a single recipient using a template.
   *
   * @param toEmail Recipient email address
   * @param toName Recipient name
   * @param templateId Template ID/slug from MSG91 dashboard
   * @param variables Template variable values
   * @return Response with status information
   */
  public Msg91EmailResponse sendEmail(
      String toEmail, String toName, String templateId, Map<String, String> variables) {
    return sendEmail(toEmail, toName, templateId, variables, null, null);
  }

  /**
   * Send email to a single recipient using a template with custom sender.
   *
   * @param toEmail Recipient email address
   * @param toName Recipient name
   * @param templateId Template ID/slug from MSG91 dashboard
   * @param variables Template variable values
   * @param from Sender information (uses default if null)
   * @param domain Domain name (uses default if null)
   * @return Response with status information
   */
  public Msg91EmailResponse sendEmail(
      String toEmail,
      String toName,
      String templateId,
      Map<String, String> variables,
      Msg91EmailSender from,
      String domain) {
    Msg91EmailRequest request =
        Msg91EmailRequest.singleRecipient(
            toEmail,
            toName,
            from != null ? from : getDefaultSender(),
            domain != null ? domain : properties.getEmailDomain(),
            templateId,
            variables);
    return sendEmail(request);
  }

  /**
   * Send email to multiple recipient groups.
   *
   * @param recipients List of recipient groups with variables
   * @param templateId Template ID/slug
   * @return Response with status information
   */
  public Msg91EmailResponse sendBulkEmail(
      List<Msg91EmailRecipientGroup> recipients, String templateId) {
    Msg91EmailRequest request =
        Msg91EmailRequest.builder()
            .from(getDefaultSender())
            .domain(properties.getEmailDomain())
            .templateId(templateId)
            .recipients(recipients)
            .build();
    return sendEmail(request);
  }

  /**
   * Send email with full request customization.
   *
   * @param request The email request
   * @return Response with status information
   */
  public Msg91EmailResponse sendEmail(Msg91EmailRequest request) {
    if (!isEnabled()) {
      log.warn("MSG91 Email is not enabled. Skipping email send.");
      return Msg91EmailResponse.builder()
          .status("error")
          .message("MSG91 Email is disabled")
          .build();
    }

    try {
      // Apply defaults if not set
      if (request.getFrom() == null) {
        request.setFrom(getDefaultSender());
      }
      if (request.getDomain() == null) {
        request.setDomain(properties.getEmailDomain());
      }

      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint = properties.getApiUrl() + EMAIL_SEND_ENDPOINT;

      log.info(
          "Sending email via MSG91 - Template: {}, Recipients: {}",
          request.getTemplateId(),
          request.getRecipients() != null ? request.getRecipients().size() : 0);
      log.debug("MSG91 Email Request body: {}", jsonBody);

      HttpRequest httpRequest = buildPostRequest(endpoint, jsonBody);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Email API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Email API Response body: {}", httpResponse.body());

      Msg91EmailResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91EmailResponse.class);
      extractRateLimitHeaders(httpResponse, response);

      if (response.isSuccess()) {
        log.info("Email sent successfully via MSG91. RequestId: {}", response.getRequestId());
      } else {
        log.error("Failed to send email via MSG91. Error: {}", response.getMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error sending email via MSG91: {}", e.getMessage(), e);
      return Msg91EmailResponse.builder()
          .status("error")
          .message("Failed to send email: " + e.getMessage())
          .build();
    }
  }

  /**
   * Send email asynchronously.
   *
   * @param toEmail Recipient email address
   * @param toName Recipient name
   * @param templateId Template ID/slug
   * @param variables Template variable values
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91EmailResponse> sendEmailAsync(
      String toEmail, String toName, String templateId, Map<String, String> variables) {
    return CompletableFuture.supplyAsync(() -> sendEmail(toEmail, toName, templateId, variables));
  }

  // ==================== Template Methods ====================

  /**
   * Create a new email template.
   *
   * @param name Template name
   * @param subject Email subject (supports variables like {{first_name}})
   * @param body HTML body (supports variables)
   * @return Response with template ID and version information
   */
  public Msg91CreateEmailTemplateResponse createTemplate(String name, String subject, String body) {
    Msg91CreateEmailTemplateRequest request =
        Msg91CreateEmailTemplateRequest.htmlTemplate(name, subject, body);
    return createTemplate(request);
  }

  /**
   * Create a new email template with full customization.
   *
   * @param request The template creation request
   * @return Response with template ID and version information
   */
  public Msg91CreateEmailTemplateResponse createTemplate(Msg91CreateEmailTemplateRequest request) {
    if (!isEnabled()) {
      log.warn("MSG91 Email is not enabled. Skipping template creation.");
      return Msg91CreateEmailTemplateResponse.builder().status("error").hasError(true).build();
    }

    try {
      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint = properties.getApiUrl() + EMAIL_TEMPLATES_ENDPOINT;

      log.info("Creating email template via MSG91 - Name: {}", request.getName());
      log.debug("MSG91 Create Template Request body: {}", jsonBody);

      HttpRequest httpRequest = buildPostRequest(endpoint, jsonBody);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Create Template API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Create Template API Response body: {}", httpResponse.body());

      Msg91CreateEmailTemplateResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91CreateEmailTemplateResponse.class);

      if (response.isSuccess()) {
        log.info(
            "Template created successfully via MSG91. TemplateId: {}, Slug: {}",
            response.getTemplateId(),
            response.getTemplateSlug());
      } else {
        log.error("Failed to create template via MSG91. Error: {}", response.getErrors());
      }

      return response;
    } catch (Exception e) {
      log.error("Error creating template via MSG91: {}", e.getMessage(), e);
      return Msg91CreateEmailTemplateResponse.builder().status("error").hasError(true).build();
    }
  }

  /**
   * Create template asynchronously.
   *
   * @param name Template name
   * @param subject Email subject
   * @param body HTML body
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91CreateEmailTemplateResponse> createTemplateAsync(
      String name, String subject, String body) {
    return CompletableFuture.supplyAsync(() -> createTemplate(name, subject, body));
  }

  // ==================== Get Templates (Email Logs) Methods ====================

  /**
   * Get list of email templates with default pagination.
   *
   * @return Response with paginated template list
   */
  public Msg91EmailTemplateListResponse getTemplates() {
    return getTemplates(1, 10, true);
  }

  /**
   * Get list of email templates with pagination.
   *
   * @param page Page number (1-based)
   * @param perPage Number of items per page
   * @param withVersions Whether to include template versions
   * @return Response with paginated template list
   */
  public Msg91EmailTemplateListResponse getTemplates(int page, int perPage, boolean withVersions) {
    if (!isEnabled()) {
      log.warn("MSG91 Email is not enabled. Skipping get templates.");
      return Msg91EmailTemplateListResponse.builder().status("error").hasError(true).build();
    }

    try {
      StringBuilder urlBuilder =
          new StringBuilder(properties.getApiUrl())
              .append(EMAIL_TEMPLATES_ENDPOINT)
              .append("?page=")
              .append(page)
              .append("&per_page=")
              .append(perPage);

      if (withVersions) {
        urlBuilder.append("&with=versions");
      }

      String endpoint = urlBuilder.toString();

      log.info("Getting email templates via MSG91 - Page: {}, PerPage: {}", page, perPage);

      HttpRequest httpRequest = buildGetRequest(endpoint);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Get Templates API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Get Templates API Response body: {}", httpResponse.body());

      Msg91EmailTemplateListResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91EmailTemplateListResponse.class);

      if (response.isSuccess()) {
        log.info(
            "Templates retrieved successfully via MSG91. Total: {}, Page: {}/{}",
            response.getTotal(),
            response.getCurrentPage(),
            response.getLastPage());
      } else {
        log.error("Failed to get templates via MSG91. Error: {}", response.getErrors());
      }

      return response;
    } catch (Exception e) {
      log.error("Error getting templates via MSG91: {}", e.getMessage(), e);
      return Msg91EmailTemplateListResponse.builder().status("error").hasError(true).build();
    }
  }

  /**
   * Get templates asynchronously.
   *
   * @param page Page number
   * @param perPage Items per page
   * @param withVersions Include versions
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91EmailTemplateListResponse> getTemplatesAsync(
      int page, int perPage, boolean withVersions) {
    return CompletableFuture.supplyAsync(() -> getTemplates(page, perPage, withVersions));
  }

  // ==================== Get Template Version Status Methods ====================

  /**
   * Get template version status by version ID.
   *
   * @param versionId The template version ID
   * @return Response with version status information
   */
  public Msg91EmailTemplateVersionResponse getTemplateVersion(Long versionId) {
    return getTemplateVersion(versionId, false);
  }

  /**
   * Get template version status by version ID.
   *
   * @param versionId The template version ID
   * @param withTemplate Whether to include parent template information
   * @return Response with version status information
   */
  public Msg91EmailTemplateVersionResponse getTemplateVersion(
      Long versionId, boolean withTemplate) {
    if (!isEnabled()) {
      log.warn("MSG91 Email is not enabled. Skipping get template version.");
      return Msg91EmailTemplateVersionResponse.builder().status("error").hasError(true).build();
    }

    try {
      StringBuilder urlBuilder =
          new StringBuilder(properties.getApiUrl())
              .append(EMAIL_TEMPLATE_VERSIONS_ENDPOINT)
              .append("/")
              .append(versionId);

      if (withTemplate) {
        urlBuilder.append("?with=template");
      }

      String endpoint = urlBuilder.toString();

      log.info("Getting template version status via MSG91 - VersionId: {}", versionId);

      HttpRequest httpRequest = buildGetRequest(endpoint);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Get Template Version API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Get Template Version API Response body: {}", httpResponse.body());

      Msg91EmailTemplateVersionResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91EmailTemplateVersionResponse.class);

      if (response.isSuccess()) {
        log.info(
            "Template version retrieved successfully via MSG91. VersionId: {}, Status: {}",
            response.getVersionId(),
            response.getStatusName());
      } else {
        log.error("Failed to get template version via MSG91. Error: {}", response.getErrors());
      }

      return response;
    } catch (Exception e) {
      log.error("Error getting template version via MSG91: {}", e.getMessage(), e);
      return Msg91EmailTemplateVersionResponse.builder().status("error").hasError(true).build();
    }
  }

  /**
   * Get template version asynchronously.
   *
   * @param versionId The version ID
   * @param withTemplate Include template info
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91EmailTemplateVersionResponse> getTemplateVersionAsync(
      Long versionId, boolean withTemplate) {
    return CompletableFuture.supplyAsync(() -> getTemplateVersion(versionId, withTemplate));
  }

  /**
   * Check if a template version is approved and ready to use.
   *
   * @param versionId The template version ID
   * @return true if approved, false otherwise
   */
  public boolean isTemplateApproved(Long versionId) {
    Msg91EmailTemplateVersionResponse response = getTemplateVersion(versionId, false);
    return response.isApproved();
  }

  // ==================== Email Logs Methods ====================

  /**
   * Get email send logs for a date range with default pagination.
   *
   * @param startDate Start date in yyyy-MM-dd format
   * @param endDate End date in yyyy-MM-dd format
   * @return Response with list of email log records
   */
  public Msg91EmailLogsResponse getEmailLogs(String startDate, String endDate) {
    return getEmailLogs(startDate, endDate, null, null, null, null);
  }

  /**
   * Get email send logs for a date range with optional filters.
   *
   * @param startDate Start date in yyyy-MM-dd format
   * @param endDate End date in yyyy-MM-dd format
   * @param recipientEmail Filter by recipient email (optional)
   * @param status Filter by status e.g. "Delivered", "Rejected" (optional)
   * @param templateName Filter by template name (optional)
   * @param domain Filter by domain (optional)
   * @return Response with list of email log records
   */
  public Msg91EmailLogsResponse getEmailLogs(
      String startDate,
      String endDate,
      String recipientEmail,
      String status,
      String templateName,
      String domain) {
    if (!isEnabled()) {
      log.warn("MSG91 Email is not enabled. Skipping get email logs.");
      return Msg91EmailLogsResponse.builder().build();
    }

    try {
      StringBuilder urlBuilder =
          new StringBuilder(properties.getApiUrl())
              .append(EMAIL_LOGS_ENDPOINT)
              .append("?startDate=")
              .append(startDate)
              .append("&endDate=")
              .append(endDate);

      if (recipientEmail != null && !recipientEmail.isBlank()) {
        urlBuilder.append("&recipientEmail=").append(recipientEmail);
      }
      if (status != null && !status.isBlank()) {
        urlBuilder.append("&status=").append(status);
      }
      if (templateName != null && !templateName.isBlank()) {
        urlBuilder.append("&templateName=").append(templateName);
      }
      if (domain != null && !domain.isBlank()) {
        urlBuilder.append("&domain=").append(domain);
      }

      String endpoint = urlBuilder.toString();

      log.info("Getting email logs via MSG91 - StartDate: {}, EndDate: {}", startDate, endDate);

      HttpRequest httpRequest = buildGetRequest(endpoint);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Email Logs API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Email Logs API Response body: {}", httpResponse.body());

      Msg91EmailLogsResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91EmailLogsResponse.class);

      log.info("Email logs retrieved successfully via MSG91. Total: {}", response.getTotal());

      return response;
    } catch (Exception e) {
      log.error("Error getting email logs via MSG91: {}", e.getMessage(), e);
      return Msg91EmailLogsResponse.builder().build();
    }
  }

  /**
   * Get email logs asynchronously.
   *
   * @param startDate Start date in yyyy-MM-dd format
   * @param endDate End date in yyyy-MM-dd format
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91EmailLogsResponse> getEmailLogsAsync(
      String startDate, String endDate) {
    return CompletableFuture.supplyAsync(() -> getEmailLogs(startDate, endDate));
  }

  // ==================== Helper Methods ====================

  private Msg91EmailSender getDefaultSender() {
    return Msg91EmailSender.builder()
        .name(properties.getEmailSenderName())
        .email(properties.getEmailSenderAddress())
        .build();
  }

  private HttpRequest buildPostRequest(String endpoint, String jsonBody) {
    return HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/json")
        .header("accept", "application/json")
        .header("authkey", properties.getAuthKey())
        .timeout(Duration.ofMillis(properties.getReadTimeout()))
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();
  }

  private HttpRequest buildGetRequest(String endpoint) {
    return HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("accept", "application/json")
        .header("authkey", properties.getAuthKey())
        .timeout(Duration.ofMillis(properties.getReadTimeout()))
        .GET()
        .build();
  }

  private void extractRateLimitHeaders(HttpResponse<?> httpResponse, Msg91EmailResponse response) {
    try {
      httpResponse
          .headers()
          .firstValue("X-RateLimit-Limit")
          .ifPresent(v -> response.setRateLimitLimit(Integer.parseInt(v)));
      httpResponse
          .headers()
          .firstValue("X-RateLimit-Remaining")
          .ifPresent(v -> response.setRateLimitRemaining(Integer.parseInt(v)));
      httpResponse
          .headers()
          .firstValue("X-RateLimit-Reset")
          .ifPresent(v -> response.setRateLimitReset(Long.parseLong(v)));
    } catch (NumberFormatException e) {
      log.debug("Failed to parse rate limit headers: {}", e.getMessage());
    }
  }
}
