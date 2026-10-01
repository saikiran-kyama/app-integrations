package com.appintegrations.msg91.sms;

import com.appintegrations.msg91.sms.dto.Msg91SmsLogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for retrieving SMS reports and analytics via MSG91 API.
 *
 * <p>This service provides methods to fetch delivery reports, SMS logs, and analytics data.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // Get SMS status for a specific request ID
 * Msg91SmsLogResponse response = msg91ReportService.getSmsStatus(requestId, startDate, endDate);
 * if (response.isSuccess() && response.getFirstEntry() != null) {
 *     String status = response.getFirstEntry().getStatus();
 * }
 *
 * // Get SMS status for today (using default date range)
 * Msg91SmsLogResponse response = msg91ReportService.getSmsStatus(requestId);
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/reports">MSG91 Reports API</a>
 */
@Slf4j
public class Msg91ReportService {

  private static final String SMS_LOGS_ENDPOINT = "/report/logs/p/sms";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final Msg91Properties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public Msg91ReportService(Msg91Properties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
    log.info("MSG91 Report Service initialized");
  }

  /** Check if MSG91 is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getAuthKey() != null
        && !properties.getAuthKey().isBlank();
  }

  // ==================== Report Methods ====================

  /**
   * Get SMS delivery status for a specific request ID using default date range (last 7 days).
   *
   * @param requestId The request ID (provider_message_id) to look up
   * @return Msg91SmsLogResponse containing delivery status
   */
  public Msg91SmsLogResponse getSmsStatus(String requestId) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(2); // MSG91 allows max 3 days
    return getSmsStatus(
        requestId, startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
  }

  /**
   * Get SMS delivery status for a specific request ID.
   *
   * @param requestId The request ID (provider_message_id) to look up
   * @param startDate Start date in yyyy-MM-dd format
   * @param endDate End date in yyyy-MM-dd format
   * @return Msg91SmsLogResponse containing delivery status
   */
  public Msg91SmsLogResponse getSmsStatus(String requestId, String startDate, String endDate) {
    if (!isEnabled()) {
      log.warn("MSG91 is not enabled. Skipping SMS status check.");
      return Msg91SmsLogResponse.error("MSG91 is disabled");
    }

    if (requestId == null || requestId.isBlank()) {
      log.warn("Request ID is required for SMS status lookup");
      return Msg91SmsLogResponse.error("Request ID is required");
    }

    try {
      String endpoint =
          String.format(
              "%s%s?startDate=%s&endDate=%s&requestId=%s",
              properties.getApiUrl(), SMS_LOGS_ENDPOINT, startDate, endDate, requestId);

      log.debug(
          "Fetching SMS status from MSG91 - RequestId: {}, DateRange: {} to {}",
          requestId,
          startDate,
          endDate);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("accept", "application/json")
              .header("authkey", properties.getAuthKey())
              .POST(HttpRequest.BodyPublishers.noBody())
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.debug("MSG91 SMS Status API Response - Status: {}", httpResponse.statusCode());
      log.debug("MSG91 SMS Status API Response body: {}", httpResponse.body());

      if (httpResponse.statusCode() != 200) {
        log.error("MSG91 SMS Status API returned error status: {}", httpResponse.statusCode());
        return Msg91SmsLogResponse.error(
            "API returned status " + httpResponse.statusCode() + ": " + httpResponse.body());
      }

      Msg91SmsLogResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91SmsLogResponse.class);

      if (response.isSuccess()) {
        String status = response.getFirstStatus();
        log.info("SMS status retrieved for requestId {}: {}", requestId, status);
      } else {
        log.warn(
            "Failed to get SMS status for requestId {}: {}", requestId, response.getErrorMessage());
      }

      return response;
    } catch (Exception e) {
      log.error(
          "Error fetching SMS status from MSG91 for requestId {}: {}",
          requestId,
          e.getMessage(),
          e);
      return Msg91SmsLogResponse.error("Failed to fetch SMS status: " + e.getMessage());
    }
  }

  /**
   * Get SMS delivery status using specific auth key (for multi-tenant scenarios).
   *
   * @param authKey The MSG91 auth key to use
   * @param requestId The request ID (provider_message_id) to look up
   * @param startDate Start date in yyyy-MM-dd format
   * @param endDate End date in yyyy-MM-dd format
   * @return Msg91SmsLogResponse containing delivery status
   */
  public Msg91SmsLogResponse getSmsStatusWithAuthKey(
      String authKey, String requestId, String startDate, String endDate) {

    if (authKey == null || authKey.isBlank()) {
      log.warn("Auth key is required for SMS status lookup");
      return Msg91SmsLogResponse.error("Auth key is required");
    }

    if (requestId == null || requestId.isBlank()) {
      log.warn("Request ID is required for SMS status lookup");
      return Msg91SmsLogResponse.error("Request ID is required");
    }

    try {
      String endpoint =
          String.format(
              "%s%s?startDate=%s&endDate=%s&requestId=%s",
              properties.getApiUrl(), SMS_LOGS_ENDPOINT, startDate, endDate, requestId);

      log.info(
          "Fetching SMS status from MSG91 - Endpoint: {}, RequestId: {}, DateRange: {} to {}",
          endpoint,
          requestId,
          startDate,
          endDate);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("accept", "application/json")
              .header("authkey", authKey)
              .POST(HttpRequest.BodyPublishers.noBody())
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info(
          "MSG91 SMS Status API Response - Status: {}, Body: {}",
          httpResponse.statusCode(),
          httpResponse.body());

      if (httpResponse.statusCode() != 200) {
        log.error("MSG91 SMS Status API returned error status: {}", httpResponse.statusCode());
        return Msg91SmsLogResponse.error(
            "API returned status " + httpResponse.statusCode() + ": " + httpResponse.body());
      }

      Msg91SmsLogResponse response =
          objectMapper.readValue(httpResponse.body(), Msg91SmsLogResponse.class);

      log.info(
          "Parsed response - data: {}, metadata: {}, type: {}",
          response.getData(),
          response.getMetadata(),
          response.getType());

      if (response.isSuccess()) {
        String status = response.getFirstStatus();
        log.info("SMS status retrieved for requestId {}: {}", requestId, status);
      }

      return response;
    } catch (Exception e) {
      log.error(
          "Error fetching SMS status from MSG91 for requestId {}: {}",
          requestId,
          e.getMessage(),
          e);
      return Msg91SmsLogResponse.error("Failed to fetch SMS status: " + e.getMessage());
    }
  }

  /** Get MSG91 properties for advanced configurations. */
  public Msg91Properties getProperties() {
    return properties;
  }
}
