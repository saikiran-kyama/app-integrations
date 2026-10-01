package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 SMS Send API.
 *
 * <p>Example success response:
 *
 * <pre>
 * {
 *     "message": "3664436c74756c5673715550",
 *     "type": "success"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91SmsResponse {

  /** Response type (success/error). */
  @JsonProperty("type")
  private String type;

  /** Message - contains request ID on success or error message on failure. */
  @JsonProperty("message")
  private String message;

  /** Error code (if error). */
  @JsonProperty("code")
  private String code;

  /** Request ID for tracking (extracted from message on success). */
  private String requestId;

  // Rate limit information from response headers

  /** Maximum number of allowed requests. */
  private Integer rateLimitLimit;

  /** Number of remaining requests in the current window. */
  private Integer rateLimitRemaining;

  /** Time in seconds until the rate limit resets. */
  private Long rateLimitReset;

  /** Check if the request was successful. */
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(type);
  }

  /** Check if there was an error. */
  public boolean isError() {
    return "error".equalsIgnoreCase(type);
  }

  /**
   * Get the request ID. On success, the message field contains the request ID.
   *
   * @return Request ID if success, null otherwise
   */
  public String getRequestId() {
    if (requestId != null) {
      return requestId;
    }
    if (isSuccess() && message != null) {
      return message;
    }
    return null;
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
  public static Msg91SmsResponse error(String errorMessage) {
    return Msg91SmsResponse.builder().type("error").message(errorMessage).build();
  }

  /**
   * Creates a success response.
   *
   * @param requestId Request ID
   * @return Success response
   */
  public static Msg91SmsResponse success(String requestId) {
    return Msg91SmsResponse.builder()
        .type("success")
        .message(requestId)
        .requestId(requestId)
        .build();
  }
}
