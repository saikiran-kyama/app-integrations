package com.appintegrations.firebase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from Firebase Cloud Messaging (FCM) HTTP v1 API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageResponse {

  /** The identifier of the message sent. Format: projects/{project_id}/messages/{message_id} */
  @JsonProperty("name")
  private String name;

  /** Error details (if any). */
  @JsonProperty("error")
  private Error error;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Error {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;

    @JsonProperty("details")
    private Object details;
  }

  /** Check if the message was sent successfully. */
  public boolean isSuccess() {
    return error == null && name != null && !name.isBlank();
  }

  /** Check if there was an error. */
  public boolean isError() {
    return error != null;
  }

  /** Get the message ID from the full name. */
  public String getMessageId() {
    if (name != null && name.contains("/")) {
      return name.substring(name.lastIndexOf('/') + 1);
    }
    return name;
  }

  /** Get error message if failed. */
  public String getErrorMessage() {
    if (error != null) {
      return error.getMessage();
    }
    return null;
  }
}
