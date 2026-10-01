package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 SMS Add Template API.
 *
 * <p>Note: MSG91 Add Template API returns different responses based on success/failure. This class
 * handles both scenarios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91CreateSmsTemplateResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** Response message. */
  @JsonProperty("message")
  private String message;

  /** Response type (success/error). */
  @JsonProperty("type")
  private String type;

  /** Template ID (returned on success). */
  @JsonProperty("template_id")
  private String templateId;

  /** Error code (if error). */
  @JsonProperty("code")
  private String code;

  /** Errors object. */
  @JsonProperty("errors")
  private Object errors;

  /** Whether there was an error. */
  @JsonProperty("hasError")
  private Boolean hasError;

  /** Check if the request was successful. */
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(status)
        || "success".equalsIgnoreCase(type)
        || (hasError != null && !hasError);
  }

  /** Check if there was an error. */
  public boolean isError() {
    return "error".equalsIgnoreCase(status)
        || "error".equalsIgnoreCase(type)
        || (hasError != null && hasError);
  }

  /**
   * Get error message if failed.
   *
   * @return Error message if failed, null otherwise
   */
  public String getErrorMessage() {
    if (isError()) {
      return message;
    }
    return null;
  }

  /**
   * Creates an error response.
   *
   * @param errorMessage Error message
   * @return Error response
   */
  public static Msg91CreateSmsTemplateResponse error(String errorMessage) {
    return Msg91CreateSmsTemplateResponse.builder()
        .status("error")
        .type("error")
        .message(errorMessage)
        .hasError(true)
        .build();
  }
}
