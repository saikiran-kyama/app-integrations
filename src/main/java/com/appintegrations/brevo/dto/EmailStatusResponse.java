package com.appintegrations.brevo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response containing email delivery status information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusResponse {

  /** The message ID of the email. */
  private String messageId;

  /**
   * The delivery status. Possible values: PENDING, SENT, DELIVERED, OPENED, CLICKED, HARD_BOUNCED,
   * SOFT_BOUNCED, BLOCKED, SPAM, UNSUBSCRIBED, DEFERRED, ERROR, UNKNOWN.
   */
  private String status;

  /** The raw event type from Brevo. */
  private String event;

  /** The recipient email address. */
  private String email;

  /** The timestamp of the event. */
  private String eventDate;

  /** Additional message or error description. */
  private String message;

  /** Check if the email was successfully delivered. */
  public boolean isDelivered() {
    return "DELIVERED".equals(status);
  }

  /** Check if the email had a delivery error. */
  public boolean hasError() {
    return "ERROR".equals(status)
        || "HARD_BOUNCED".equals(status)
        || "SOFT_BOUNCED".equals(status)
        || "BLOCKED".equals(status);
  }

  /** Check if the email is still pending delivery. */
  public boolean isPending() {
    return "PENDING".equals(status) || "SENT".equals(status) || "DEFERRED".equals(status);
  }
}
