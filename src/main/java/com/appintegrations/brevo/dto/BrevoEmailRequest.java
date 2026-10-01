package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for Brevo Transactional Email API.
 *
 * @see <a href="https://developers.brevo.com/reference/sendtransacemail">Brevo API Docs</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrevoEmailRequest {

  @JsonProperty("sender")
  private EmailRecipient sender;

  @JsonProperty("to")
  private List<EmailRecipient> to;

  @JsonProperty("subject")
  private String subject;

  @JsonProperty("htmlContent")
  private String htmlContent;

  @JsonProperty("textContent")
  private String textContent;

  @JsonProperty("cc")
  private List<EmailRecipient> cc;

  @JsonProperty("bcc")
  private List<EmailRecipient> bcc;

  @JsonProperty("replyTo")
  private EmailRecipient replyTo;

  @JsonProperty("templateId")
  private Long templateId;

  @JsonProperty("params")
  private Object params;

  @JsonProperty("headers")
  private Object headers;

  @JsonProperty("tags")
  private List<String> tags;
}
