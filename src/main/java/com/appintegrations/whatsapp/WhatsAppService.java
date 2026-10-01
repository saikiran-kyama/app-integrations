package com.appintegrations.whatsapp;

import com.appintegrations.whatsapp.dto.WhatsAppMessageRequest;
import com.appintegrations.whatsapp.dto.WhatsAppMessageResponse;
import com.appintegrations.whatsapp.dto.WhatsAppTemplateRequest;
import com.appintegrations.whatsapp.dto.WhatsAppTemplateResponse;
import com.appintegrations.whatsapp.dto.WhatsAppTemplateStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending messages via WhatsApp Business Cloud API.
 *
 * <p>Usage example:
 *
 * <pre>
 * // Send a text message
 * whatsAppService.sendTextMessage("+919876543210", "Hello from our app!");
 *
 * // Send a template message
 * whatsAppService.sendTemplateMessage("+919876543210", "hello_world", "en");
 * </pre>
 *
 * @see <a href=
 *     "https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-messages">WhatsApp Cloud
 *     API</a>
 */
@Slf4j
public class WhatsAppService {

  private final WhatsAppProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public WhatsAppService(WhatsAppProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
  }

  /** Check if WhatsApp is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getAccessToken() != null
        && !properties.getAccessToken().isBlank()
        && properties.getPhoneNumberId() != null
        && !properties.getPhoneNumberId().isBlank();
  }

  /**
   * Send a text message.
   *
   * @param to Recipient phone number with country code (e.g., "919876543210", without +)
   * @param message Text message content
   * @return Response from WhatsApp API
   */
  public WhatsAppMessageResponse sendTextMessage(String to, String message) {
    WhatsAppMessageRequest request =
        WhatsAppMessageRequest.textMessage(normalizePhoneNumber(to), message);
    return sendMessage(request);
  }

  /**
   * Send a template message.
   *
   * @param to Recipient phone number
   * @param templateName Template name as registered in WhatsApp Business Manager
   * @param languageCode Language code (e.g., "en", "en_US")
   * @return Response from WhatsApp API
   */
  public WhatsAppMessageResponse sendTemplateMessage(
      String to, String templateName, String languageCode) {
    return sendTemplateMessage(to, templateName, languageCode, null);
  }

  /**
   * Send a template message with body parameters.
   *
   * <p>Use this method when your template has placeholders like {{1}}, {{2}}, etc. The parameters
   * are substituted in order.
   *
   * <p>Example usage:
   *
   * <pre>
   * // Template: "Hi {{1}}, your order {{2}} has been shipped."
   * whatsAppService.sendTemplateMessage("919876543210", "order_update", "en_US",
   *     List.of("Sai", "ORD12345"));
   * </pre>
   *
   * @param to Recipient phone number
   * @param templateName Template name
   * @param languageCode Language code
   * @param bodyParameters List of text values to substitute for {{1}}, {{2}}, etc.
   * @return Response from WhatsApp API with rate limit info
   */
  public WhatsAppMessageResponse sendTemplateMessage(
      String to, String templateName, String languageCode, List<String> bodyParameters) {
    List<WhatsAppMessageRequest.Component> components = null;

    if (bodyParameters != null && !bodyParameters.isEmpty()) {
      List<WhatsAppMessageRequest.Parameter> params =
          bodyParameters.stream()
              .map(
                  text ->
                      WhatsAppMessageRequest.Parameter.builder().type("text").text(text).build())
              .toList();

      components =
          List.of(
              WhatsAppMessageRequest.Component.builder().type("body").parameters(params).build());
    }

    return sendTemplateMessageWithComponents(to, templateName, languageCode, components);
  }

  /**
   * Send a template message with full component customization.
   *
   * <p>Use this method when you need to specify header, body, and/or button components with various
   * parameter types (text, image, document, etc.).
   *
   * @param to Recipient phone number
   * @param templateName Template name
   * @param languageCode Language code
   * @param components Template components with parameters
   * @return Response from WhatsApp API with rate limit info
   */
  public WhatsAppMessageResponse sendTemplateMessageWithComponents(
      String to,
      String templateName,
      String languageCode,
      List<WhatsAppMessageRequest.Component> components) {
    WhatsAppMessageRequest request =
        WhatsAppMessageRequest.templateMessage(
            normalizePhoneNumber(to),
            templateName,
            languageCode != null ? languageCode : properties.getDefaultLanguage(),
            components);
    return sendMessage(request);
  }

  /**
   * Send a WhatsApp message with full request customization.
   *
   * @param request The message request
   * @return Response from WhatsApp API
   */
  public WhatsAppMessageResponse sendMessage(WhatsAppMessageRequest request) {
    if (!isEnabled()) {
      log.warn("WhatsApp integration is not enabled. Skipping message send.");
      WhatsAppMessageResponse response = new WhatsAppMessageResponse();
      response.setError(new WhatsAppMessageResponse.Error());
      response.getError().setMessage("WhatsApp integration is disabled");
      return response;
    }

    try {
      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint =
          String.format("%s/%s/messages", properties.getApiUrl(), properties.getPhoneNumberId());

      log.info("WhatsApp Request - To: {}, Type: {}", request.getTo(), request.getType());
      log.info("[WHATSAPP API REQUEST] Full body: {}", jsonBody);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + properties.getAccessToken())
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("WhatsApp API Response - Status: {}", httpResponse.statusCode());
      log.info("[WHATSAPP API RESPONSE] Body: {}", httpResponse.body());

      WhatsAppMessageResponse response = parseResponse(httpResponse);
      response.setHeaders(httpResponse.headers().map());
      parseRateLimitHeaders(httpResponse, response);

      if (response.isSuccess()) {
        log.info("WhatsApp message sent successfully. MessageId: {}", response.getMessageId());
      } else {
        log.error("Failed to send WhatsApp message. Error: {}", response.getErrorMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error sending WhatsApp message: {}", e.getMessage(), e);
      WhatsAppMessageResponse errorResponse = new WhatsAppMessageResponse();
      errorResponse.setError(new WhatsAppMessageResponse.Error());
      errorResponse.getError().setMessage("Failed to send message: " + e.getMessage());
      return errorResponse;
    }
  }

  /**
   * Create a new WhatsApp message template.
   *
   * <p>Sends a POST request to {@code /{apiUrl}/{wabaId}/message_templates} where {@code wabaId} is
   * the Business Account ID configured via {@code integrations.whatsapp.business-account-id}.
   *
   * <p>Example usage:
   *
   * <pre>
   * WhatsAppTemplateRequest request = WhatsAppTemplateRequest
   *     .builder().name("order_update_v1").language("en_US").category(
   *         "UTILITY")
   *     .components(List.of(WhatsAppTemplateRequest.Component.builder().type("BODY")
   *         .text("Hi {{1}}, your order {{2}} has been shipped.")
   *         .example(WhatsAppTemplateRequest.Example.builder()
   *             .bodyText(List.of(List.of("Sai", "ORD12345"))).build())
   *         .build()))
   *     .build();
   *
   * WhatsAppTemplateResponse response = whatsAppService.createTemplate(request);
   * </pre>
   *
   * @param request Template definition
   * @return API response containing the new template {@code id} and {@code status}
   */
  public WhatsAppTemplateResponse createTemplate(WhatsAppTemplateRequest request) {
    if (!isEnabled()) {
      log.warn("WhatsApp integration is not enabled. Skipping template creation.");
      WhatsAppTemplateResponse response = new WhatsAppTemplateResponse();
      response.setError(new WhatsAppTemplateResponse.Error());
      response.getError().setMessage("WhatsApp integration is disabled");
      return response;
    }

    String wabaId = properties.getBusinessAccountId();
    if (wabaId == null || wabaId.isBlank()) {
      log.error("WhatsApp business-account-id is not configured.");
      WhatsAppTemplateResponse response = new WhatsAppTemplateResponse();
      response.setError(new WhatsAppTemplateResponse.Error());
      response.getError().setMessage("integrations.whatsapp.business-account-id is not configured");
      return response;
    }

    try {
      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint = String.format("%s/%s/message_templates", properties.getApiUrl(), wabaId);

      log.info(
          "WhatsApp createTemplate – name: {}, language: {}, category: {}",
          request.getName(),
          request.getLanguage(),
          request.getCategory());
      log.debug("WhatsApp createTemplate request body: {}", jsonBody);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + properties.getAccessToken())
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("WhatsApp createTemplate API response – status: {}", httpResponse.statusCode());
      log.debug("WhatsApp createTemplate response body: {}", httpResponse.body());

      WhatsAppTemplateResponse response =
          objectMapper.readValue(httpResponse.body(), WhatsAppTemplateResponse.class);

      if (response.isSuccess()) {
        log.info(
            "WhatsApp template created successfully. TemplateId: {}, Status: {}",
            response.getId(),
            response.getStatus());
      } else {
        log.error("Failed to create WhatsApp template. Error: {}", response.getErrorMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error creating WhatsApp template: {}", e.getMessage(), e);
      WhatsAppTemplateResponse errorResponse = new WhatsAppTemplateResponse();
      errorResponse.setError(new WhatsAppTemplateResponse.Error());
      errorResponse.getError().setMessage("Failed to create template: " + e.getMessage());
      return errorResponse;
    }
  }

  /**
   * Fetch the status of a WhatsApp message template by its ID.
   *
   * <p>Sends a GET request to {@code /{apiUrl}/{templateId}?fields=name,status,category,language}
   * to retrieve the current status and metadata of a template.
   *
   * <p>Example usage:
   *
   * <pre>
   * WhatsAppTemplateStatusResponse response = whatsAppService.getTemplateStatus("1940863766533570");
   * if (response.isSuccess()) {
   *   System.out.println("Template: " + response.getName());
   *   System.out.println("Status: " + response.getStatus());
   *   System.out.println("Category: " + response.getCategory());
   *   System.out.println("Language: " + response.getLanguage());
   * }
   * </pre>
   *
   * @param templateId The unique template ID assigned by Meta
   * @return API response containing the template name, status, category, language, and id
   */
  public WhatsAppTemplateStatusResponse getTemplateStatus(String templateId) {
    if (!isEnabled()) {
      log.warn("WhatsApp integration is not enabled. Skipping template status fetch.");
      WhatsAppTemplateStatusResponse response = new WhatsAppTemplateStatusResponse();
      response.setError(new WhatsAppTemplateStatusResponse.Error());
      response.getError().setMessage("WhatsApp integration is disabled");
      return response;
    }

    if (templateId == null || templateId.isBlank()) {
      log.error("Template ID is required to fetch template status.");
      WhatsAppTemplateStatusResponse response = new WhatsAppTemplateStatusResponse();
      response.setError(new WhatsAppTemplateStatusResponse.Error());
      response.getError().setMessage("Template ID is required");
      return response;
    }

    try {
      String endpoint =
          String.format(
              "%s/%s?fields=name,status,category,language", properties.getApiUrl(), templateId);

      log.info("WhatsApp getTemplateStatus – templateId: {}", templateId);
      log.debug("WhatsApp getTemplateStatus endpoint: {}", endpoint);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("Authorization", "Bearer " + properties.getAccessToken())
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .GET()
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("WhatsApp getTemplateStatus API response – status: {}", httpResponse.statusCode());
      log.debug("WhatsApp getTemplateStatus response body: {}", httpResponse.body());

      WhatsAppTemplateStatusResponse response =
          objectMapper.readValue(httpResponse.body(), WhatsAppTemplateStatusResponse.class);

      if (response.isSuccess()) {
        log.info(
            "WhatsApp template status fetched successfully. TemplateId: {}, Name: {}, Status: {}",
            response.getId(),
            response.getName(),
            response.getStatus());
      } else {
        log.error(
            "Failed to fetch WhatsApp template status. Error: {}", response.getErrorMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error fetching WhatsApp template status: {}", e.getMessage(), e);
      WhatsAppTemplateStatusResponse errorResponse = new WhatsAppTemplateStatusResponse();
      errorResponse.setError(new WhatsAppTemplateStatusResponse.Error());
      errorResponse.getError().setMessage("Failed to fetch template status: " + e.getMessage());
      return errorResponse;
    }
  }

  /** Send a text message asynchronously. */
  public CompletableFuture<WhatsAppMessageResponse> sendTextMessageAsync(
      String to, String message) {
    return CompletableFuture.supplyAsync(() -> sendTextMessage(to, message));
  }

  /** Send a template message asynchronously. */
  public CompletableFuture<WhatsAppMessageResponse> sendTemplateMessageAsync(
      String to, String templateName, String languageCode) {
    return CompletableFuture.supplyAsync(() -> sendTemplateMessage(to, templateName, languageCode));
  }

  /**
   * Normalize phone number - remove + prefix if present. WhatsApp API expects phone numbers without
   * the + prefix.
   */
  private String normalizePhoneNumber(String phoneNumber) {
    if (phoneNumber == null) {
      return null;
    }
    return phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;
  }

  private WhatsAppMessageResponse parseResponse(HttpResponse<String> httpResponse) {
    try {
      return objectMapper.readValue(httpResponse.body(), WhatsAppMessageResponse.class);
    } catch (Exception e) {
      log.warn("Failed to parse WhatsApp response: {}", httpResponse.body());
      WhatsAppMessageResponse response = new WhatsAppMessageResponse();
      response.setError(new WhatsAppMessageResponse.Error());
      response.getError().setMessage(httpResponse.body());
      return response;
    }
  }

  private void parseRateLimitHeaders(
      HttpResponse<String> httpResponse, WhatsAppMessageResponse response) {
    // X-App-Usage: {"call_count":1,"total_cputime":1,"total_time":1}
    Optional<String> appUsageHeader = httpResponse.headers().firstValue("X-App-Usage");
    appUsageHeader.ifPresent(
        header -> {
          try {
            WhatsAppMessageResponse.RateLimitInfo info =
                objectMapper.readValue(header, WhatsAppMessageResponse.RateLimitInfo.class);
            response.setAppUsage(info);
            log.info(
                "App rate limits – calls: {}%, CPU time: {}%, total time: {}%",
                info.getCallCount(), info.getTotalCpuTime(), info.getTotalTime());
          } catch (Exception e) {
            log.warn("Failed to parse X-App-Usage header: {}", header);
          }
        });

    // X-Business-Use-Case-Usage: {"<phoneNumberId>":[{...}]}
    Optional<String> bizUsageHeader =
        httpResponse.headers().firstValue("X-Business-Use-Case-Usage");
    bizUsageHeader.ifPresent(
        header -> {
          try {
            JsonNode root = objectMapper.readTree(header);
            JsonNode firstEntry = root.fields().hasNext() ? root.fields().next().getValue() : null;
            if (firstEntry != null && firstEntry.isArray() && firstEntry.size() > 0) {
              WhatsAppMessageResponse.RateLimitInfo info =
                  objectMapper.treeToValue(
                      firstEntry.get(0), WhatsAppMessageResponse.RateLimitInfo.class);
              response.setBusinessUsage(info);
              log.info(
                  "Business rate limits [{}] – messages: {}%, calls: {}%, CPU time: {}%, total time: {}%, quota exceeded: {}",
                  info.getType(),
                  info.getQuotaUsage(),
                  info.getCallCount(),
                  info.getTotalCpuTime(),
                  info.getTotalTime(),
                  info.isQuotaExceeded());
            }
          } catch (Exception e) {
            log.warn("Failed to parse X-Business-Use-Case-Usage header: {}", header);
          }
        });
  }
}
