package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Brevo webhook event payload.
 *
 * <p>Brevo sends webhook events for various email and SMS activities:
 *
 * <ul>
 *   <li>Email events: sent, delivered, opened, clicked, soft_bounce, hard_bounce, invalid_email,
 *       deferred, complaint, unsubscribed, blocked, error
 *   <li>SMS events: sent, delivered, soft_bounce, hard_bounce, error
 * </ul>
 *
 * @see <a href="https://developers.brevo.com/docs/transactional-webhooks">Brevo Webhooks</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrevoWebhookEvent {

  /**
   * Event type (e.g., "delivered", "opened", "clicked", "soft_bounce", "hard_bounce",
   * "unsubscribed", "complaint", "blocked", "spam", "invalid_email", "deferred", "error").
   */
  @JsonProperty("event")
  private String event;

  /** Recipient email address. */
  @JsonProperty("email")
  private String email;

  /** Brevo message ID. */
  @JsonProperty("message-id")
  private String messageId;

  /** Timestamp of the event (Unix timestamp). */
  @JsonProperty("ts")
  private Long timestamp;

  /** Timestamp in epoch milliseconds. */
  @JsonProperty("ts_epoch")
  private Long timestampEpoch;

  /** Date string. */
  @JsonProperty("date")
  private String date;

  /** Subject of the email. */
  @JsonProperty("subject")
  private String subject;

  /** Sender email. */
  @JsonProperty("sender_email")
  private String senderEmail;

  /** Tag associated with the email. */
  @JsonProperty("tag")
  private String tag;

  /** Tags associated with the email. */
  @JsonProperty("tags")
  private List<String> tags;

  /** Custom headers sent with the email. */
  @JsonProperty("X-Mailin-custom")
  private String customHeaders;

  /** Template ID if a template was used. */
  @JsonProperty("template_id")
  private Long templateId;

  // Click event specific fields

  /** URL that was clicked (for click events). */
  @JsonProperty("link")
  private String link;

  /** IP address of the recipient (for open/click events). */
  @JsonProperty("ip")
  private String ip;

  // Bounce/Error specific fields

  /** Reason for bounce or error. */
  @JsonProperty("reason")
  private String reason;

  /** SMTP reply from receiving server. */
  @JsonProperty("ts_event")
  private Long eventTimestamp;

  // SMS specific fields

  /** Phone number (for SMS events). */
  @JsonProperty("phone")
  private String phone;

  /** SMS message content. */
  @JsonProperty("content")
  private String content;

  // Additional metadata

  /** Campaign ID if applicable. */
  @JsonProperty("camp_id")
  private Long campaignId;

  /** List ID if applicable. */
  @JsonProperty("list_id")
  private List<Long> listIds;

  /**
   * Returns the event timestamp as an Instant.
   *
   * @return Event timestamp or null if not available
   */
  public Instant getEventInstant() {
    if (timestampEpoch != null) {
      return Instant.ofEpochMilli(timestampEpoch);
    }
    if (timestamp != null) {
      return Instant.ofEpochSecond(timestamp);
    }
    return null;
  }

  /**
   * Checks if this is a successful delivery event.
   *
   * @return true if the email was delivered
   */
  public boolean isDelivered() {
    return "delivered".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is an open event.
   *
   * @return true if the email was opened
   */
  public boolean isOpened() {
    return "opened".equalsIgnoreCase(event) || "unique_opened".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is a click event.
   *
   * @return true if a link was clicked
   */
  public boolean isClicked() {
    return "clicked".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is a bounce event (soft or hard).
   *
   * @return true if the email bounced
   */
  public boolean isBounce() {
    return "soft_bounce".equalsIgnoreCase(event)
        || "hard_bounce".equalsIgnoreCase(event)
        || "blocked".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is a hard bounce (permanent failure).
   *
   * @return true if this is a hard bounce
   */
  public boolean isHardBounce() {
    return "hard_bounce".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is an unsubscribe event.
   *
   * @return true if the recipient unsubscribed
   */
  public boolean isUnsubscribed() {
    return "unsubscribed".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is a complaint (spam report) event.
   *
   * @return true if the email was marked as spam
   */
  public boolean isComplaint() {
    return "complaint".equalsIgnoreCase(event) || "spam".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is an error event.
   *
   * @return true if there was an error
   */
  public boolean isError() {
    return "error".equalsIgnoreCase(event)
        || "invalid_email".equalsIgnoreCase(event)
        || "blocked".equalsIgnoreCase(event);
  }

  /**
   * Checks if this is an SMS event (has phone number, no email).
   *
   * @return true if this is an SMS event
   */
  public boolean isSmsEvent() {
    return phone != null && !phone.isBlank() && (email == null || email.isBlank());
  }

  /**
   * Checks if this is an email event (has email).
   *
   * @return true if this is an email event
   */
  public boolean isEmailEvent() {
    return email != null && !email.isBlank();
  }

  /**
   * Returns the recipient identifier (phone for SMS, email for email).
   *
   * @return recipient identifier
   */
  public String getRecipient() {
    if (isSmsEvent()) {
      return phone;
    }
    return email;
  }
}
