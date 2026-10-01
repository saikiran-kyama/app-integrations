package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single SMS log entry from MSG91 Report API.
 *
 * <p>Example response entry:
 *
 * <pre>
 * {
 *     "requestDate": "2026-05-07 12:48:56",
 *     "status": "Failed",
 *     "deliveryDate": "2026-05-07",
 *     "deliveryTime": "12:48:57",
 *     "telNum": "918309619653",
 *     "sentDateTime": "2026-05-07 12:48:56"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91SmsLogEntry {

  /** Date and time when the SMS request was made. */
  @JsonProperty("requestDate")
  private String requestDate;

  /** Delivery status (e.g., "Delivered", "Failed", "Pending"). */
  @JsonProperty("status")
  private String status;

  /** Date when the SMS was delivered. */
  @JsonProperty("deliveryDate")
  private String deliveryDate;

  /** Time when the SMS was delivered. */
  @JsonProperty("deliveryTime")
  private String deliveryTime;

  /** Recipient phone number. */
  @JsonProperty("telNum")
  private String telNum;

  /** Date and time when the SMS was sent. */
  @JsonProperty("sentDateTime")
  private String sentDateTime;

  /**
   * Check if the SMS was delivered successfully.
   *
   * @return true if status indicates delivery
   */
  public boolean isDelivered() {
    return "Delivered".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status);
  }

  /**
   * Check if the SMS delivery failed.
   *
   * @return true if status indicates failure
   */
  public boolean isFailed() {
    return "Failed".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
  }

  /**
   * Normalize status to uppercase for consistent storage.
   *
   * @return uppercase status string
   */
  public String getNormalizedStatus() {
    return status != null ? status.toUpperCase() : null;
  }
}
