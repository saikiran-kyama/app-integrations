package com.appintegrations.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from WhatsApp Cloud API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessageResponse {

  /** Messaging product (always "whatsapp"). */
  @JsonProperty("messaging_product")
  private String messagingProduct;

  /** List of contacts the message was sent to. */
  @JsonProperty("contacts")
  private List<Contact> contacts;

  /** List of messages sent. */
  @JsonProperty("messages")
  private List<Message> messages;

  /** Error details (if any). */
  @JsonProperty("error")
  private Error error;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Contact {
    @JsonProperty("input")
    private String input;

    @JsonProperty("wa_id")
    private String waId;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Message {
    @JsonProperty("id")
    private String id;

    @JsonProperty("message_status")
    private String messageStatus;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Error {
    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("error_subcode")
    private Integer errorSubcode;

    @JsonProperty("fbtrace_id")
    private String fbtraceId;
  }

  /** Rate limit data extracted from X-Business-Use-Case-Usage and X-App-Usage headers. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RateLimitInfo {
    /** Percentage of allowed calls used (0-100). */
    @JsonProperty("call_count")
    private int callCount;

    /** Percentage of allowed CPU time used (0-100). */
    @JsonProperty("total_cputime")
    private int totalCpuTime;

    /** Percentage of allowed total time used (0-100). */
    @JsonProperty("total_time")
    private int totalTime;

    /** Percentage of messaging tier quota consumed (0-100). */
    @JsonProperty("quota_usage")
    private int quotaUsage;

    /** Whether the quota has been exceeded. */
    @JsonProperty("quota_exceeded")
    private boolean quotaExceeded;

    /** Use-case type, e.g. "MESSAGES" (from X-Business-Use-Case-Usage). */
    @JsonProperty("type")
    private String type;
  }

  /** App-level rate limits from the X-App-Usage header. */
  private RateLimitInfo appUsage;

  /** Per-phone-number rate limits from the X-Business-Use-Case-Usage header. */
  private RateLimitInfo businessUsage;

  /** All HTTP response headers returned by the WhatsApp API. */
  private Map<String, List<String>> headers;

  /** Check if the message was sent successfully. */
  public boolean isSuccess() {
    return error == null && messages != null && !messages.isEmpty();
  }

  /** Check if there was an error. */
  public boolean isError() {
    return error != null;
  }

  /** Get the message ID if successful. */
  public String getMessageId() {
    if (messages != null && !messages.isEmpty()) {
      return messages.get(0).getId();
    }
    return null;
  }

  /** Get error message if failed. */
  public String getErrorMessage() {
    if (error != null) {
      return error.getMessage();
    }
    return null;
  }
}
