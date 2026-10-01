package com.appintegrations.brevo;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.ApiResponse;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalSmsApi;
import brevoModel.GetSmsEventReport;
import brevoModel.GetSmsEventReportEvents;
import brevoModel.SendSms;
import brevoModel.SendTransacSms;
import com.appintegrations.brevo.dto.BrevoSmsRequest;
import com.appintegrations.brevo.dto.BrevoSmsResponse;
import com.appintegrations.brevo.dto.SmsStatusResponse;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending SMS using Brevo SDK.
 *
 * <p>Usage example:
 *
 * <pre>
 * brevoSmsService.sendSms("+919876543210", "Your OTP is 123456");
 * </pre>
 */
@Slf4j
public class BrevoSmsService {

  private final BrevoProperties properties;
  private TransactionalSmsApi smsApi;

  public BrevoSmsService(BrevoProperties properties) {
    this.properties = properties;
  }

  /** Initialize the Brevo SMS SDK client. */
  @PostConstruct
  public void init() {
    if (isEnabled()) {
      ApiClient defaultClient = Configuration.getDefaultApiClient();
      ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
      apiKey.setApiKey(properties.getApiKey());
      this.smsApi = new TransactionalSmsApi();
      log.info("Brevo SMS SDK initialized successfully");
    }
  }

  /** Check if SMS sending is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getSms().isEnabled()
        && properties.getApiKey() != null
        && !properties.getApiKey().isBlank();
  }

  /** Send a simple SMS using Brevo SDK. */
  public BrevoSmsResponse sendSms(String phoneNumber, String content) {
    return sendSms(phoneNumber, content, null);
  }

  /** Send an SMS with a custom tag using Brevo SDK. */
  public BrevoSmsResponse sendSms(String phoneNumber, String content, String tag) {
    BrevoSmsRequest request =
        BrevoSmsRequest.builder()
            .sender(properties.getSms().getSender())
            .recipient(phoneNumber)
            .content(content)
            .tag(tag)
            .type("transactional")
            .build();

    return sendSms(request);
  }

  /** Send an SMS with full request customization using Brevo SDK. */
  public BrevoSmsResponse sendSms(BrevoSmsRequest request) {
    if (!isEnabled()) {
      log.warn("Brevo SMS is not enabled. Skipping SMS send.");
      BrevoSmsResponse response = new BrevoSmsResponse();
      response.setMessage("Brevo SMS is disabled");
      return response;
    }

    if (request.getSender() == null || request.getSender().isBlank()) {
      request.setSender(properties.getSms().getSender());
    }

    try {
      log.info(
          "Sending SMS via Brevo SDK - Sender: {}, Recipient: {}",
          request.getSender(),
          request.getRecipient());

      SendTransacSms smsRequest = new SendTransacSms();
      smsRequest.setSender(request.getSender());
      smsRequest.setRecipient(request.getRecipient());
      smsRequest.setContent(request.getContent());
      smsRequest.setType(SendTransacSms.TypeEnum.TRANSACTIONAL);
      if (request.getTag() != null) {
        smsRequest.setTag(request.getTag());
      }
      if (request.getWebUrl() != null) {
        smsRequest.setWebUrl(request.getWebUrl());
      }

      ApiResponse<SendSms> apiResponse = smsApi.sendTransacSmsWithHttpInfo(smsRequest);
      SendSms result = apiResponse.getData();

      log.info(
          "SMS sent successfully via Brevo SDK. MessageId: {}, SmsCount: {}, UsedCredits: {}",
          result.getMessageId(),
          result.getSmsCount(),
          result.getUsedCredits());

      BrevoSmsResponse response = new BrevoSmsResponse();
      response.setMessageId(result.getMessageId());
      response.setSmsCount(result.getSmsCount() != null ? result.getSmsCount().intValue() : null);
      response.setUsedCredits(
          result.getUsedCredits() != null ? result.getUsedCredits().doubleValue() : null);
      response.setRemainingCredits(
          result.getRemainingCredits() != null ? result.getRemainingCredits().doubleValue() : null);
      // Extract rate limit headers
      extractRateLimitHeaders(apiResponse.getHeaders(), response);
      return response;

    } catch (ApiException e) {
      log.error("Brevo SMS SDK API error. Code: {}, Body: {}", e.getCode(), e.getResponseBody(), e);
      BrevoSmsResponse errorResponse = new BrevoSmsResponse();
      errorResponse.setCode(String.valueOf(e.getCode()));
      errorResponse.setMessage("Brevo SMS API error: " + e.getResponseBody());
      // Extract rate limit headers from 429 error responses
      if (e.getResponseHeaders() != null) {
        extractRateLimitHeaders(e.getResponseHeaders(), errorResponse);
      }
      return errorResponse;
    } catch (Exception e) {
      log.error("Error sending SMS via Brevo SDK: {}", e.getMessage(), e);
      BrevoSmsResponse errorResponse = new BrevoSmsResponse();
      errorResponse.setMessage("Failed to send SMS: " + e.getMessage());
      return errorResponse;
    }
  }

  /** Send an SMS asynchronously using Brevo SDK. */
  public CompletableFuture<BrevoSmsResponse> sendSmsAsync(String phoneNumber, String content) {
    return CompletableFuture.supplyAsync(() -> sendSms(phoneNumber, content));
  }

  /** Send an OTP SMS using Brevo SDK. */
  public BrevoSmsResponse sendOtp(String phoneNumber, String otp) {
    String content = "Your verification code is: " + otp + ". This code expires in 10 minutes.";
    return sendSms(phoneNumber, content, "OTP");
  }

  /**
   * Get SMS delivery status by phone number using Brevo SDK.
   *
   * @param phoneNumber the phone number to check status for
   * @return SmsStatusResponse containing the delivery status
   */
  public SmsStatusResponse getSmsStatus(String phoneNumber) {
    return getSmsStatus(phoneNumber, null, null);
  }

  /**
   * Get SMS delivery status by phone number with date range using Brevo SDK.
   *
   * @param phoneNumber the phone number to check status for
   * @param startDate optional start date for filtering
   * @param endDate optional end date for filtering
   * @return SmsStatusResponse containing the delivery status
   */
  public SmsStatusResponse getSmsStatus(
      String phoneNumber, LocalDate startDate, LocalDate endDate) {
    if (!isEnabled()) {
      log.warn("Brevo SMS is not enabled. Cannot get SMS status.");
      return SmsStatusResponse.builder()
          .phoneNumber(phoneNumber)
          .status("UNKNOWN")
          .message("Brevo SMS is disabled")
          .build();
    }

    try {
      log.info("Getting SMS status for phoneNumber: {}", phoneNumber);

      // Use getSmsEvents to get individual SMS event reports
      // Parameters: limit, startDate, endDate, offset, days, phoneNumber, event, tags, sort
      GetSmsEventReport report =
          smsApi.getSmsEvents(
              10L, // limit
              startDate != null ? startDate.toString() : null, // startDate
              endDate != null ? endDate.toString() : null, // endDate
              null, // offset
              null, // days
              phoneNumber, // phoneNumber
              null, // event
              null, // tags
              "desc" // sort (most recent first)
              );

      if (report.getEvents() == null || report.getEvents().isEmpty()) {
        log.info("No SMS events found for phoneNumber: {}", phoneNumber);
        return SmsStatusResponse.builder()
            .phoneNumber(phoneNumber)
            .status("PENDING")
            .message("No SMS events found")
            .build();
      }

      // Get the most recent event
      GetSmsEventReportEvents latestEvent = report.getEvents().get(0);

      String eventStr = latestEvent.getEvent() != null ? latestEvent.getEvent().getValue() : null;
      String status = mapSmsEventToStatus(eventStr);
      String messageId = latestEvent.getMessageId();

      log.info("SMS status for phoneNumber {}: event={}, status={}", phoneNumber, eventStr, status);

      return SmsStatusResponse.builder()
          .phoneNumber(phoneNumber)
          .messageId(messageId)
          .status(status)
          .event(eventStr)
          .tag(latestEvent.getTag())
          .eventDate(latestEvent.getDate())
          .message("Status retrieved successfully")
          .build();

    } catch (ApiException e) {
      log.error(
          "Brevo SMS SDK API error getting SMS status. Code: {}, Body: {}",
          e.getCode(),
          e.getResponseBody(),
          e);
      return SmsStatusResponse.builder()
          .phoneNumber(phoneNumber)
          .status("ERROR")
          .message("Brevo SMS API error: " + e.getResponseBody())
          .build();
    } catch (Exception e) {
      log.error("Error getting SMS status via Brevo SDK: {}", e.getMessage(), e);
      return SmsStatusResponse.builder()
          .phoneNumber(phoneNumber)
          .status("ERROR")
          .message("Failed to get status: " + e.getMessage())
          .build();
    }
  }

  /**
   * Get SMS delivery status by message ID using Brevo SDK. Brevo API does not support direct
   * messageId filtering, so events are paginated and matched locally using exact string comparison
   * (trim on both sides).
   *
   * @param messageId the message ID returned from sendSms
   * @return SmsStatusResponse containing the delivery status
   */
  public SmsStatusResponse getSmsStatusByMessageId(String messageId) {
    if (!isEnabled()) {
      log.warn("Brevo SMS is not enabled. Cannot get SMS status.");
      return SmsStatusResponse.builder()
          .messageId(messageId)
          .status("UNKNOWN")
          .message("Brevo SMS is disabled")
          .build();
    }

    try {
      log.info("Getting SMS status for messageId: {}", messageId);

      String trimmedId = messageId.trim();
      long limit = 100L;
      long offset = 0L;
      List<GetSmsEventReportEvents> allEvents = new ArrayList<>();

      // Paginate through ALL events — Brevo does not support filtering by messageId
      while (true) {
        GetSmsEventReport page =
            smsApi.getSmsEvents(
                limit, // limit (max 100)
                null, // startDate   — no date filter
                null, // endDate     — no date filter
                offset, // offset for pagination
                null, // days        — no date filter
                null, // phoneNumber
                null, // event
                null, // tags
                "desc" // sort (required by API)
                );

        if (page.getEvents() == null || page.getEvents().isEmpty()) {
          break;
        }

        allEvents.addAll(page.getEvents());

        // Early exit if messageId found in this page
        boolean found =
            page.getEvents().stream()
                .anyMatch(
                    e -> trimmedId.equals(e.getMessageId() != null ? e.getMessageId().trim() : ""));

        if (found || page.getEvents().size() < limit) {
          break;
        }

        offset += limit;
      }

      // Match all collected events by exact string comparison (same as demo)
      String matchedStatus = null;
      String matchedDate = null;
      String matchedPhone = null;
      String matchedTag = null;

      for (GetSmsEventReportEvents event : allEvents) {
        String eventMsgId = event.getMessageId() != null ? event.getMessageId().trim() : null;
        if (eventMsgId != null && eventMsgId.equals(trimmedId)) {
          matchedPhone = event.getPhoneNumber();
          matchedDate = event.getDate();
          matchedTag = event.getTag();
          matchedStatus = event.getEvent() != null ? event.getEvent().getValue() : null;
        }
      }

      if (matchedStatus != null) {
        log.info(
            "SMS status for messageId {}: event={}, mapped={}",
            messageId,
            matchedStatus,
            mapSmsEventToStatus(matchedStatus));
        return SmsStatusResponse.builder()
            .messageId(messageId)
            .phoneNumber(matchedPhone)
            .status(mapSmsEventToStatus(matchedStatus))
            .event(matchedStatus)
            .tag(matchedTag)
            .eventDate(matchedDate)
            .message("Status retrieved successfully")
            .build();
      }

      // Diagnostic: log all messageIds returned by Brevo to help troubleshoot mismatches
      String allIds =
          allEvents.stream()
              .map(GetSmsEventReportEvents::getMessageId)
              .filter(id -> id != null)
              .distinct()
              .collect(Collectors.joining(", "));
      log.warn(
          "messageId [{}] not found in {} events. All event messageIds: [{}]",
          messageId,
          allEvents.size(),
          allIds);

      return SmsStatusResponse.builder()
          .messageId(messageId)
          .status("PENDING")
          .message(
              "No match. Searched messageId=["
                  + messageId
                  + "]. Brevo event messageIds ("
                  + allEvents.size()
                  + " events): ["
                  + allIds
                  + "]")
          .build();

    } catch (ApiException e) {
      log.error(
          "Brevo SMS SDK API error getting SMS status. Code: {}, Body: {}",
          e.getCode(),
          e.getResponseBody(),
          e);
      return SmsStatusResponse.builder()
          .messageId(messageId)
          .status("ERROR")
          .message("Brevo SMS API error: " + e.getResponseBody())
          .build();
    } catch (Exception e) {
      log.error("Error getting SMS status via Brevo SDK: {}", e.getMessage(), e);
      return SmsStatusResponse.builder()
          .messageId(messageId)
          .status("ERROR")
          .message("Failed to get status: " + e.getMessage())
          .build();
    }
  }

  /** Map Brevo SMS event types to standard status. */
  private String mapSmsEventToStatus(String event) {
    if (event == null) {
      return "UNKNOWN";
    }
    return switch (event.toLowerCase()) {
      case "delivered" -> "DELIVERED";
      case "sent" -> "SENT";
      case "accepted" -> "ACCEPTED";
      case "undelivered" -> "UNDELIVERED";
      case "rejected" -> "REJECTED";
      case "unknown" -> "UNKNOWN";
      case "softbounces", "soft_bounces" -> "SOFT_BOUNCED";
      case "hardbounces", "hard_bounces" -> "HARD_BOUNCED";
      case "blocked" -> "BLOCKED";
      default -> event.toUpperCase();
    };
  }

  /**
   * Extract Brevo rate-limit headers from an API response and populate the DTO. Headers:
   * x-sib-ratelimit-limit, x-sib-ratelimit-remaining, x-sib-ratelimit-reset
   */
  private void extractRateLimitHeaders(
      java.util.Map<String, java.util.List<String>> headers, BrevoSmsResponse response) {
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
}
