package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from MSG91 Email Send API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91EmailResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** Response message. */
  @JsonProperty("message")
  private String message;

  /** Request ID for tracking. */
  @JsonProperty("request_id")
  private String requestId;

  /** Error code (if error). */
  @JsonProperty("code")
  private String code;

  /** Additional data. */
  @JsonProperty("data")
  private Object data;

  /** Errors object. */
  @JsonProperty("errors")
  private Object errors;

  /** Whether there was an error. */
  @JsonProperty("hasError")
  private Boolean hasError;

  // Rate limit information from response headers

  /** Maximum number of allowed requests. */
  private Integer rateLimitLimit;

  /** Number of remaining requests in the current window. */
  private Integer rateLimitRemaining;

  /** Time in seconds until the rate limit resets. */
  private Long rateLimitReset;

  /** Check if the request was successful. */
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(status) || (hasError != null && !hasError);
  }

  /** Check if there was an error. */
  public boolean isError() {
    return "error".equalsIgnoreCase(status) || (hasError != null && hasError);
  }

  /** Get error message if failed. */
  public String getErrorMessage() {
    if (isError()) {
      return message;
    }
    return null;
  }
}
