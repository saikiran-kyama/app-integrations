package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from Brevo Transactional Email API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrevoEmailResponse {

  @JsonProperty("messageId")
  private String messageId;

  @JsonProperty("code")
  private String code;

  @JsonProperty("message")
  private String message;

  private String rateLimitLimit;
  private String rateLimitRemaining;
  private String rateLimitReset;

  public boolean isSuccess() {
    return messageId != null && !messageId.isBlank();
  }

  public boolean isError() {
    return code != null || (message != null && messageId == null);
  }
}
