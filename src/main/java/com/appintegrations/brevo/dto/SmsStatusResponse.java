package com.appintegrations.brevo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response containing SMS delivery status information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsStatusResponse {

  /** The message ID of the SMS. */
  private String messageId;

  /** The recipient phone number. */
  private String phoneNumber;

  /**
   * The delivery status. Possible values: PENDING, SENT, ACCEPTED, DELIVERED, UNDELIVERED,
   * REJECTED, SOFT_BOUNCED, HARD_BOUNCED, BLOCKED, ERROR, UNKNOWN.
   */
  private String status;

  /** The raw event type from Brevo. */
  private String event;

  /** The tag associated with the SMS. */
  private String tag;

  /** The timestamp of the event. */
  private String eventDate;

  /** Additional message or error description. */
  private String message;

  /** Check if the SMS was successfully delivered. */
  public boolean isDelivered() {
    return "DELIVERED".equals(status);
  }

  /** Check if the SMS had a delivery error. */
  public boolean hasError() {
    return "ERROR".equals(status)
        || "UNDELIVERED".equals(status)
        || "REJECTED".equals(status)
        || "HARD_BOUNCED".equals(status)
        || "SOFT_BOUNCED".equals(status)
        || "BLOCKED".equals(status);
  }

  /** Check if the SMS is still pending delivery. */
  public boolean isPending() {
    return "PENDING".equals(status) || "SENT".equals(status) || "ACCEPTED".equals(status);
  }
}
