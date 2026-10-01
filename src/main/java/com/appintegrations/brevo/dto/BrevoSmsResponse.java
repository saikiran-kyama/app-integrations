package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from Brevo Transactional SMS API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrevoSmsResponse {

  @JsonProperty("messageId")
  private Long messageId;

  @JsonProperty("smsCount")
  private Integer smsCount;

  @JsonProperty("usedCredits")
  private Double usedCredits;

  @JsonProperty("remainingCredits")
  private Double remainingCredits;

  @JsonProperty("reference")
  private String reference;

  @JsonProperty("code")
  private String code;

  @JsonProperty("message")
  private String message;

  private String rateLimitLimit;
  private String rateLimitRemaining;
  private String rateLimitReset;

  public boolean isSuccess() {
    return messageId != null;
  }

  public boolean isError() {
    return code != null || (message != null && messageId == null);
  }

  public String getMessageIdAsString() {
    return messageId != null ? String.valueOf(messageId) : null;
  }
}
