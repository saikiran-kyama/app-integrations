package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MSG91 SMS Get Template Versions API.
 *
 * <p>Usage example:
 *
 * <pre>
 * Msg91SmsTemplateDetailsRequest request = Msg91SmsTemplateDetailsRequest.builder()
 *     .templateId("69f1abd698ee82f2110719d5")
 *     .build();
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/get-template-versions">MSG91 Get Template Versions
 *     API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91SmsTemplateDetailsRequest {

  /** Template ID to get details for. */
  @JsonProperty("template_id")
  private String templateId;

  /**
   * Creates a request for a specific template.
   *
   * @param templateId Template ID
   * @return Configured request
   */
  public static Msg91SmsTemplateDetailsRequest of(String templateId) {
    return Msg91SmsTemplateDetailsRequest.builder().templateId(templateId).build();
  }
}
