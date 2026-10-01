package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MSG91 Email Template Create API.
 *
 * <p>Usage example:
 *
 * <pre>
 * Msg91CreateEmailTemplateRequest request = Msg91CreateEmailTemplateRequest.builder()
 *     .name("Welcome Email")
 *     .subject("Welcome {{first_name}} 👋")
 *     .body("&lt;html&gt;&lt;body&gt;Hello {{first_name}}!&lt;/body&gt;&lt;/html&gt;")
 *     .build();
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/create-template">MSG91 Create Email Template
 *     API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91CreateEmailTemplateRequest {

  /** Template name. */
  @JsonProperty("name")
  private String name;

  /** Email subject line (supports template variables like {{first_name}}). */
  @JsonProperty("subject")
  private String subject;

  /** HTML body content (supports template variables). */
  @JsonProperty("body")
  private String body;

  /** Plain text version of the email body. */
  @JsonProperty("text_plain")
  private String textPlain;

  /** AMP HTML content for dynamic emails. */
  @JsonProperty("amp_html")
  private String ampHtml;

  /** Template syntax type (default: "handlebars"). */
  @JsonProperty("template_syntax")
  private String templateSyntax;

  /** Template description. */
  @JsonProperty("description")
  private String description;

  /**
   * Creates a simple HTML template request.
   *
   * @param name Template name
   * @param subject Email subject
   * @param body HTML body
   * @return Configured request
   */
  public static Msg91CreateEmailTemplateRequest htmlTemplate(
      String name, String subject, String body) {
    return Msg91CreateEmailTemplateRequest.builder().name(name).subject(subject).body(body).build();
  }
}
