package com.appintegrations.msg91.sms;

import com.appintegrations.msg91.sms.dto.Msg91CreateSmsTemplateRequest;
import com.appintegrations.msg91.sms.dto.Msg91CreateSmsTemplateResponse;
import com.appintegrations.msg91.sms.dto.Msg91SmsTemplateDetailsRequest;
import com.appintegrations.msg91.sms.dto.Msg91SmsTemplateDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing SMS templates via MSG91 API.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Add a new SMS template
 * Msg91CreateSmsTemplateResponse response = msg91TemplateService.addTemplate(
 *     "Service Request",
 *     "Dear Customer, your service request ID is ##VAR1##. Our team will contact you shortly.",
 *     "BSBRNS",
 *     "1107177736243421169"
 * );
 *
 * // Get template details
 * Msg91SmsTemplateDetailsResponse details = msg91TemplateService.getTemplateDetails(
 *     "69f1abd698ee82f2110719d5"
 * );
 *
 * // Check if template is active
 * if (details.isActive() && details.isDltVerified()) {
 *     // Template is ready to use
 * }
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/add-template">MSG91 Add Template API</a>
 * @see <a href="https://docs.msg91.com/reference/get-template-versions">MSG91 Get Template Versions
 *     API</a>
 */
@Slf4j
public class Msg91TemplateService {

  private static final String ADD_TEMPLATE_ENDPOINT = "/sms/addTemplate";
  private static final String GET_TEMPLATE_VERSIONS_ENDPOINT = "/sms/getTemplateVersions";

  private final Msg91Properties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public Msg91TemplateService(Msg91Properties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
    log.info("MSG91 Template Service initialized");
  }

  /** Check if MSG91 is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getAuthKey() != null
        && !properties.getAuthKey().isBlank();
  }

  // ==================== Add Template Methods ====================

  /**
   * Add a new SMS template.
   *
   * @param templateName Template name for identification
   * @param template Template content with variables (##VAR1##, ##VAR2##, etc.)
   * @param senderId Sender ID (6 characters for promotional, alphanumeric for transactional)
   * @param dltTemplateId DLT template ID as required by TRAI regulations
   * @return Response with template creation status
   */
  public Msg91CreateSmsTemplateResponse addTemplate(
      String templateName, String template, String senderId, String dltTemplateId) {
    return addTemplate(templateName, template, senderId, dltTemplateId, "NORMAL");
  }

  /**
   * Add a new SMS template with specified SMS type.
   *
   * @param templateName Template name for identification
   * @param template Template content with variables (##VAR1##, ##VAR2##, etc.)
   * @param senderId Sender ID
   * @param dltTemplateId DLT template ID
   * @param smsType SMS type ("NORMAL" or "UNICODE")
   * @return Response with template creation status
   */
  public Msg91CreateSmsTemplateResponse addTemplate(
      String templateName, String template, String senderId, String dltTemplateId, String smsType) {
    Msg91CreateSmsTemplateRequest request =
        Msg91CreateSmsTemplateRequest.builder()
            .templateName(templateName)
            .template(template)
            .senderId(senderId)
            .dltTemplateId(dltTemplateId)
            .smsType(smsType)
            .build();
    return addTemplate(request);
  }

  /**
   * Add a new SMS template with full request customization.
   *
   * @param request The template creation request
   * @return Response with template creation status
   */
  public Msg91CreateSmsTemplateResponse addTemplate(Msg91CreateSmsTemplateRequest request) {
    if (!isEnabled()) {
      log.warn("MSG91 is not enabled. Skipping template creation.");
      return Msg91CreateSmsTemplateResponse.error("MSG91 is disabled");
    }

    try {
      String endpoint = properties.getApiUrl() + ADD_TEMPLATE_ENDPOINT;

      log.info(
          "Adding SMS template via MSG91 - Name: {}, SenderId: {}",
          request.getTemplateName(),
          request.getSenderId());

      // Build form data (the API uses multipart form data)
      String formData = buildFormData(request);
      log.debug("MSG91 Add Template Request form data: {}", formData);

      HttpRequest httpRequest = buildFormPostRequest(endpoint, formData);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Add Template API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Add Template API Response body: {}", httpResponse.body());

      Msg91CreateSmsTemplateResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91CreateSmsTemplateResponse.class);

      if (response.isSuccess()) {
        log.info("Template added successfully via MSG91. TemplateId: {}", response.getTemplateId());
      } else {
        log.error(
            "Failed to add template via MSG91. Error: {}",
            response.getErrorMessage() != null ? response.getErrorMessage() : response.getErrors());
      }

      return response;
    } catch (Exception e) {
      log.error("Error adding template via MSG91: {}", e.getMessage(), e);
      return Msg91CreateSmsTemplateResponse.error("Failed to add template: " + e.getMessage());
    }
  }

  /**
   * Add template asynchronously.
   *
   * @param templateName Template name
   * @param template Template content
   * @param senderId Sender ID
   * @param dltTemplateId DLT template ID
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91CreateSmsTemplateResponse> addTemplateAsync(
      String templateName, String template, String senderId, String dltTemplateId) {
    return CompletableFuture.supplyAsync(
        () -> addTemplate(templateName, template, senderId, dltTemplateId));
  }

  // ==================== Get Template Details Methods ====================

  /**
   * Get SMS template details/versions.
   *
   * @param templateId Template ID to get details for
   * @return Response with template version details
   */
  public Msg91SmsTemplateDetailsResponse getTemplateDetails(String templateId) {
    Msg91SmsTemplateDetailsRequest request = Msg91SmsTemplateDetailsRequest.of(templateId);
    return getTemplateDetails(request);
  }

  /**
   * Get SMS template details with full request customization.
   *
   * @param request The template details request
   * @return Response with template version details
   */
  public Msg91SmsTemplateDetailsResponse getTemplateDetails(
      Msg91SmsTemplateDetailsRequest request) {
    if (!isEnabled()) {
      log.warn("MSG91 is not enabled. Skipping get template details.");
      return Msg91SmsTemplateDetailsResponse.error("MSG91 is disabled");
    }

    try {
      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint = properties.getApiUrl() + GET_TEMPLATE_VERSIONS_ENDPOINT;

      log.info("Getting SMS template details via MSG91 - TemplateId: {}", request.getTemplateId());
      log.debug("MSG91 Get Template Details Request body: {}", jsonBody);

      HttpRequest httpRequest = buildPostRequest(endpoint, jsonBody);

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("MSG91 Get Template Details API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 Get Template Details API Response body: {}", httpResponse.body());

      Msg91SmsTemplateDetailsResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91SmsTemplateDetailsResponse.class);

      if (response.isSuccess()) {
        log.info(
            "Template details retrieved successfully via MSG91. TemplateName: {}, Active: {}, DltVerified: {}",
            response.getTemplateName(),
            response.isActive(),
            response.isDltVerified());
      } else {
        log.error("Failed to get template details via MSG91. Error: {}", response.getErrors());
      }

      return response;
    } catch (Exception e) {
      log.error("Error getting template details via MSG91: {}", e.getMessage(), e);
      return Msg91SmsTemplateDetailsResponse.error(
          "Failed to get template details: " + e.getMessage());
    }
  }

  /**
   * Get template details asynchronously.
   *
   * @param templateId Template ID
   * @return CompletableFuture with response
   */
  public CompletableFuture<Msg91SmsTemplateDetailsResponse> getTemplateDetailsAsync(
      String templateId) {
    return CompletableFuture.supplyAsync(() -> getTemplateDetails(templateId));
  }

  /**
   * Check if a template is active and DLT verified.
   *
   * @param templateId Template ID to check
   * @return true if template is active and DLT verified, false otherwise
   */
  public boolean isTemplateReady(String templateId) {
    Msg91SmsTemplateDetailsResponse response = getTemplateDetails(templateId);
    return response.isSuccess() && response.isActive() && response.isDltVerified();
  }

  // ==================== Helper Methods ====================

  private String buildFormData(Msg91CreateSmsTemplateRequest request) {
    Map<String, String> formParams = new LinkedHashMap<>();
    if (request.getTemplate() != null) {
      formParams.put("template", request.getTemplate());
    }
    if (request.getSenderId() != null) {
      formParams.put("sender_id", request.getSenderId());
    }
    if (request.getTemplateName() != null) {
      formParams.put("template_name", request.getTemplateName());
    }
    if (request.getDltTemplateId() != null) {
      formParams.put("dlt_template_id", request.getDltTemplateId());
    }
    if (request.getSmsType() != null) {
      formParams.put("smsType", request.getSmsType());
    }

    return formParams.entrySet().stream()
        .map(
            e ->
                URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }

  private HttpRequest buildFormPostRequest(String endpoint, String formData) {
    return HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("accept", "application/json")
        .header("authkey", properties.getAuthKey())
        .timeout(Duration.ofMillis(properties.getReadTimeout()))
        .POST(HttpRequest.BodyPublishers.ofString(formData))
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
}
