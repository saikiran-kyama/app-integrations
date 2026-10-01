package com.appintegrations.msg91.sms;

import com.appintegrations.msg91.sms.dto.Msg91SmsRecipient;
import com.appintegrations.msg91.sms.dto.Msg91SmsRequest;
import com.appintegrations.msg91.sms.dto.Msg91SmsResponse;
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
 * Service for sending SMS via MSG91 Flow API.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Send SMS to a single recipient
 * msg91SmsService.sendSms("918309496713", "templateId123");
 *
 * // Send SMS with template variables
 * msg91SmsService.sendSms(
 *     "918309496713",
 *     "templateId123",
 *     Map.of("VAR1", "12345")
 * );
 *
 * // Send to multiple recipients
 * msg91SmsService.sendBulkSms(
 *     List.of("918309496713", "919876543210"),
 *     "templateId123"
 * );
 *
 * // Send with full request customization
 * Msg91SmsRequest request = Msg91SmsRequest.builder()
 *     .templateId("templateId123")
 *     .recipients(List.of(
 *         Msg91SmsRecipient.of("918309496713", Map.of("VAR1", "value1")),
 *         Msg91SmsRecipient.of("919876543210", Map.of("VAR1", "value2"))
 *     ))
 *     .shortUrl(1)
 *     .build();
 * msg91SmsService.sendSms(request);
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/send-sms">MSG91 Send SMS API</a>
 */
@Slf4j
public class Msg91SmsService {

  private static final String SMS_FLOW_ENDPOINT = "/flow";

  private final Msg91Properties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public Msg91SmsService(Msg91Properties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
    log.info("MSG91 SMS Service initialized");
  }

  /** Check if MSG91 SMS is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getAuthKey() != null
        && !properties.getAuthKey().isBlank();
  }

  // ==================== Send SMS Methods ====================

  /**
   * Send SMS to a single recipient using a template.
   *
   * @param mobile Mobile number with country code (e.g., "918309496713")
   * @param templateId Template ID from MSG91 dashboard
   * @return Response with status information
   */
  public Msg91SmsResponse sendSms(String mobile, String templateId) {
    return sendSms(mobile, templateId, null);
  }

  /**
   * Send SMS to a single recipient using a template with variables.
   *
   * @param mobile Mobile number with country code (e.g., "918309496713")
   * @param templateId Template ID from MSG91 dashboard
   * @param variables Template variable values (VAR1, VAR2, etc.)
   * @return Response with status information
   */
  public Msg91SmsResponse sendSms(String mobile, String templateId, Map<String, String> variables) {
    Msg91SmsRequest request =
        variables != null
            ? Msg91SmsRequest.singleRecipient(mobile, templateId, variables)
            : Msg91SmsRequest.singleRecipient(mobile, templateId);
    return sendSms(request);
  }

  /**
   * Send SMS to multiple recipients using a template.
   *
   * @param mobiles List of mobile numbers with country code
   * @param templateId Template ID from MSG91 dashboard
   * @return Response with status information
   */
  public Msg91SmsResponse sendBulkSms(List<String> mobiles, String templateId) {
    Msg91SmsRequest request = Msg91SmsRequest.bulkRecipients(mobiles, templateId);
    return sendSms(request);
  }

  /**
   * Send SMS to multiple recipients with individual variables.
   *
   * @param recipients List of recipients with their variables
   * @param templateId Template ID from MSG91 dashboard
   * @return Response with status information
   */
  public Msg91SmsResponse sendBulkSmsWithRecipients(
      List<Msg91SmsRecipient> recipients, String templateId) {
    Msg91SmsRequest request =
        Msg91SmsRequest.builder().templateId(templateId).recipients(recipients).build();
    return sendSms(request);
  }

  /**
   * Send SMS with full request customization.
   *
   * @param request The SMS request
   * @return Response with status information
   */
  public Msg91SmsResponse sendSms(Msg91SmsRequest request) {
    if (!isEnabled()) {
      log.warn("MSG91 SMS is not enabled. Skipping SMS send.");
      return Msg91SmsResponse.error("MSG91 SMS is disabled");
    }

    try {
      String jsonBody = buildRequestBody(request);
      String endpoint = properties.getApiUrl() + SMS_FLOW_ENDPOINT;

      log.info(
          "Sending SMS via MSG91 - Template: {}, Recipients: {}",
          request.getTemplateId(),
          request.getRecipients() != null ? request.getRecipients().size() : 0);
      log.debug("MSG91 SMS Request body: {}", jsonBody);

      HttpRequest httpRequest = buildPostRequest(endpoint, jsonBody);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 SMS API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 SMS API Response body: {}", httpResponse.body());

      Msg91SmsResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91SmsResponse.class);
      extractRateLimitHeaders(httpResponse, response);

      if (response.isSuccess()) {
        log.info("SMS sent successfully via MSG91. RequestId: {}", response.getRequestId());
      } else {
        log.error("Failed to send SMS via MSG91. Error: {}", response.getMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error sending SMS via MSG91: {}", e.getMessage(), e);
      return Msg91SmsResponse.error("Failed to send SMS: " + e.getMessage());
    }
  }

  /**
   * Send SMS asynchronously.
   *
   * @param mobile Mobile number with country code
   * @param templateId Template ID from MSG91 dashboard
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91SmsResponse> sendSmsAsync(String mobile, String templateId) {
    return CompletableFuture.supplyAsync(() -> sendSms(mobile, templateId));
  }

  /**
   * Send SMS asynchronously with variables.
   *
   * @param mobile Mobile number with country code
   * @param templateId Template ID from MSG91 dashboard
   * @param variables Template variable values
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91SmsResponse> sendSmsAsync(
      String mobile, String templateId, Map<String, String> variables) {
    return CompletableFuture.supplyAsync(() -> sendSms(mobile, templateId, variables));
  }

  /**
   * Send bulk SMS asynchronously.
   *
   * @param mobiles List of mobile numbers
   * @param templateId Template ID from MSG91 dashboard
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91SmsResponse> sendBulkSmsAsync(
      List<String> mobiles, String templateId) {
    return CompletableFuture.supplyAsync(() -> sendBulkSms(mobiles, templateId));
  }

  // ==================== Helper Methods ====================

  private String buildRequestBody(Msg91SmsRequest request) throws Exception {
    // MSG91 expects variables as top-level properties in recipient objects
    // We need to flatten the variables map into the JSON
    if (request.getRecipients() != null) {
      for (Msg91SmsRecipient recipient : request.getRecipients()) {
        if (recipient.getVariables() != null && !recipient.getVariables().isEmpty()) {
          // Variables will be serialized via @JsonAnySetter/@JsonAnyGetter pattern
          // The ObjectMapper will handle this
        }
      }
    }
    return objectMapper.writeValueAsString(request);
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

  private void extractRateLimitHeaders(HttpResponse<?> httpResponse, Msg91SmsResponse response) {
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
