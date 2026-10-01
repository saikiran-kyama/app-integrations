package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MSG91 Email Send API.
 *
 * <p>Usage example:
 *
 * <pre>
 * Msg91EmailRequest request = Msg91EmailRequest.builder()
 *     .from(Msg91EmailSender.of("sender@domain.com", "Sender Name"))
 *     .domain("domain.mailer91.com")
 *     .templateId("template_slug")
 *     .recipients(List.of(
 *         Msg91EmailRecipientGroup.singleRecipient(
 *             "recipient@example.com",
 *             "Recipient Name",
 *             Map.of("VAR1", "value1")
 *         )
 *     ))
 *     .build();
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/send-email">MSG91 Send Email API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91EmailRequest {

  /** List of recipient groups with variables. */
  @JsonProperty("recipients")
  private List<Msg91EmailRecipientGroup> recipients;

  /** Sender information. */
  @JsonProperty("from")
  private Msg91EmailSender from;

  /** Domain name (e.g., "domain.mailer91.com"). */
  @JsonProperty("domain")
  private String domain;

  /** Template ID/slug from MSG91 dashboard. */
  @JsonProperty("template_id")
  private String templateId;

  /** Reply-to email address. */
  @JsonProperty("reply_to")
  private Msg91EmailRecipient replyTo;

  /** Attachments (optional). */
  @JsonProperty("attachments")
  private List<Msg91EmailAttachment> attachments;

  /**
   * Creates a simple email request for a single recipient.
   *
   * @param toEmail Recipient email address
   * @param toName Recipient name (optional)
   * @param from Sender information
   * @param domain Domain name
   * @param templateId Template ID/slug
   * @param variables Template variables
   * @return Configured request
   */
  public static Msg91EmailRequest singleRecipient(
      String toEmail,
      String toName,
      Msg91EmailSender from,
      String domain,
      String templateId,
      Map<String, String> variables) {
    return Msg91EmailRequest.builder()
        .from(from)
        .domain(domain)
        .templateId(templateId)
        .recipients(List.of(Msg91EmailRecipientGroup.singleRecipient(toEmail, toName, variables)))
        .build();
  }

  /**
   * Creates a simple email request for a single recipient without name.
   *
   * @param toEmail Recipient email address
   * @param from Sender information
   * @param domain Domain name
   * @param templateId Template ID/slug
   * @param variables Template variables
   * @return Configured request
   */
  public static Msg91EmailRequest singleRecipient(
      String toEmail,
      Msg91EmailSender from,
      String domain,
      String templateId,
      Map<String, String> variables) {
    return Msg91EmailRequest.builder()
        .from(from)
        .domain(domain)
        .templateId(templateId)
        .recipients(List.of(Msg91EmailRecipientGroup.singleRecipient(toEmail, variables)))
        .build();
  }
}
