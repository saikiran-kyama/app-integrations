package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MSG91 SMS Add Template API.
 *
 * <p>Usage example:
 *
 * <pre>
 * Msg91CreateSmsTemplateRequest request = Msg91CreateSmsTemplateRequest.builder()
 *     .template("Dear Customer, your service request ID is ##VAR1##. Our team will contact you shortly.")
 *     .senderId("BSBRNS")
 *     .templateName("Service Request")
 *     .dltTemplateId("1107177736243421169")
 *     .smsType("NORMAL")
 *     .build();
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/add-template">MSG91 Add SMS Template API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91CreateSmsTemplateRequest {

  /** Template content with variables. Variables use ##VAR1##, ##VAR2## format. */
  @JsonProperty("template")
  private String template;

  /** Sender ID (6 characters for promotional, alphanumeric for transactional). */
  @JsonProperty("sender_id")
  private String senderId;

  /** Template name for identification. */
  @JsonProperty("template_name")
  private String templateName;

  /** DLT (Distributed Ledger Technology) template ID as required by TRAI regulations. */
  @JsonProperty("dlt_template_id")
  private String dltTemplateId;

  /** SMS type. Valid values: "NORMAL", "UNICODE". Use UNICODE for non-English characters. */
  @JsonProperty("smsType")
  @Builder.Default
  private String smsType = "NORMAL";

  /**
   * Creates a simple template request.
   *
   * @param templateName Template name
   * @param template Template content with variables
   * @param senderId Sender ID
   * @param dltTemplateId DLT template ID
   * @return Configured request
   */
  public static Msg91CreateSmsTemplateRequest of(
      String templateName, String template, String senderId, String dltTemplateId) {
    return Msg91CreateSmsTemplateRequest.builder()
        .templateName(templateName)
        .template(template)
        .senderId(senderId)
        .dltTemplateId(dltTemplateId)
        .smsType("NORMAL")
        .build();
  }

  /**
   * Creates a unicode template request for non-English content.
   *
   * @param templateName Template name
   * @param template Template content with variables
   * @param senderId Sender ID
   * @param dltTemplateId DLT template ID
   * @return Configured request
   */
  public static Msg91CreateSmsTemplateRequest unicode(
      String templateName, String template, String senderId, String dltTemplateId) {
    return Msg91CreateSmsTemplateRequest.builder()
        .templateName(templateName)
        .template(template)
        .senderId(senderId)
        .dltTemplateId(dltTemplateId)
        .smsType("UNICODE")
        .build();
  }
}
