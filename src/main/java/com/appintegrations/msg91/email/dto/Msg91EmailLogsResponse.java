package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 Email Logs (Report) API.
 *
 * @see <a href="https://docs.msg91.com/reference/email-logs">MSG91 Email Logs API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91EmailLogsResponse {

  /** List of email log records. */
  @JsonProperty("data")
  private List<EmailLogRecord> data;

  /** Metadata including total count. */
  @JsonProperty("metadata")
  private Metadata metadata;

  /** Get total number of records. */
  public Integer getTotal() {
    return metadata != null ? metadata.getTotal() : null;
  }

  /** Individual email log record. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class EmailLogRecord {

    /** Timestamp when the email was created/sent. */
    @JsonProperty("createdAt")
    private String createdAt;

    /** Unique request ID for tracking. */
    @JsonProperty("requestId")
    private String requestId;

    /** Email subject line. */
    @JsonProperty("subject")
    private String subject;

    /** Sending domain. */
    @JsonProperty("domain")
    private String domain;

    /** Sender email address. */
    @JsonProperty("senderEmail")
    private String senderEmail;

    /** Recipient email address. */
    @JsonProperty("recipientEmail")
    private String recipientEmail;

    /** Template name used. */
    @JsonProperty("templateName")
    private String templateName;

    /** Mail type (e.g., "OTP", "Transactional"). */
    @JsonProperty("mailType")
    private String mailType;

    /** Whether sent via SMTP (0 = API, 1 = SMTP). */
    @JsonProperty("isSmtp")
    private Integer isSmtp;

    /** CC recipients as JSON string. */
    @JsonProperty("cc")
    private String cc;

    /** BCC recipients as JSON string. */
    @JsonProperty("bcc")
    private String bcc;

    /** Send-to recipients as JSON string. */
    @JsonProperty("sendTo")
    private String sendTo;

    /** Reply-to as JSON string. */
    @JsonProperty("rto")
    private String rto;

    /** Attachments as JSON string. */
    @JsonProperty("attc")
    private String attc;

    /** Inline reply-to. */
    @JsonProperty("irto")
    private String irto;

    /** Message ID (SMTP message-id header). */
    @JsonProperty("msgId")
    private String msgId;

    /** Template version ID. */
    @JsonProperty("tvid")
    private String tvid;

    /** Variables used as JSON string (e.g., "{\"VAR1\":\"value\"}"). */
    @JsonProperty("vbls")
    private String vbls;

    /** Campaign name (if sent via campaign). */
    @JsonProperty("campaignName")
    private String campaignName;

    /** Campaign request ID. */
    @JsonProperty("campaignRequestId")
    private String campaignRequestId;

    /** Plugin source. */
    @JsonProperty("pluginSource")
    private String pluginSource;

    /** Mailer request ID. */
    @JsonProperty("mailerRequestId")
    private String mailerRequestId;

    /** Node ID. */
    @JsonProperty("nodeId")
    private String nodeId;

    /** Company ID. */
    @JsonProperty("companyId")
    private String companyId;

    /** UUID. */
    @JsonProperty("UUID")
    private String uuid;

    /** Campaign request ID (alternate). */
    @JsonProperty("CRQID")
    private String crqid;

    /** Delivery status (e.g., "Delivered", "Rejected", "Pending"). */
    @JsonProperty("status")
    private String status;

    /** Timestamp of last status update. */
    @JsonProperty("statusUpdatedAt")
    private String statusUpdatedAt;

    /** Description of the status. */
    @JsonProperty("description")
    private String description;

    /** Failure reason (if rejected). */
    @JsonProperty("failureReason")
    private String failureReason;

    /** Spam score. */
    @JsonProperty("spamScore")
    private Double spamScore;

    /** Delivery timeline events as list of JSON strings. */
    @JsonProperty("timeline")
    private List<String> timeline;

    /** Number of times the email was opened. */
    @JsonProperty("opened")
    private Integer opened;

    /** Number of unsubscribes. */
    @JsonProperty("unsubscribed")
    private Integer unsubscribed;

    /** Number of clicks. */
    @JsonProperty("clicked")
    private Integer clicked;

    /** Number of spam complaints. */
    @JsonProperty("complaints")
    private Integer complaints;

    /** Engagement events as list of JSON strings. */
    @JsonProperty("events")
    private List<String> events;

    /** Service type (e.g., "mail"). */
    @JsonProperty("service")
    private String service;

    /** Source (e.g., "API"). */
    @JsonProperty("source")
    private String source;

    /** Check if the email was delivered. */
    public boolean isDelivered() {
      return "Delivered".equalsIgnoreCase(status);
    }

    /** Check if the email was rejected. */
    public boolean isRejected() {
      return "Rejected".equalsIgnoreCase(status);
    }

    /** Check if the email was opened at least once. */
    public boolean wasOpened() {
      return opened != null && opened > 0;
    }
  }

  /** Response metadata. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Metadata {

    /** Total number of log records. */
    @JsonProperty("total")
    private Integer total;
  }
}
