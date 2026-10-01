package com.appintegrations.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned by the Graph API when fetching template status.
 *
 * <p>Example response:
 *
 * <pre>
 * {
 *     "name": "whatsapp_template_5_50131",
 *     "status": "APPROVED",
 *     "category": "MARKETING",
 *     "language": "en_US",
 *     "id": "1940863766533570"
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppTemplateStatusResponse {

  /** Unique template ID assigned by Meta. */
  @JsonProperty("id")
  private String id;

  /** Name of the template. */
  @JsonProperty("name")
  private String name;

  /**
   * Review status of the template.
   *
   * <p>Possible values: {@code PENDING}, {@code APPROVED}, {@code REJECTED}, {@code PAUSED}, {@code
   * DISABLED}, {@code IN_APPEAL}.
   */
  @JsonProperty("status")
  private String status;

  /**
   * Template category.
   *
   * <p>Possible values: {@code UTILITY}, {@code MARKETING}, {@code AUTHENTICATION}.
   */
  @JsonProperty("category")
  private String category;

  /** Language code of the template (e.g., {@code en_US}). */
  @JsonProperty("language")
  private String language;

  /** Error details present when the API returns an error envelope. */
  @JsonProperty("error")
  private Error error;

  // -------------------------------------------------------------------------
  // Inner classes
  // -------------------------------------------------------------------------

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Error {

    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("error_subcode")
    private Integer errorSubcode;

    @JsonProperty("fbtrace_id")
    private String fbtraceId;

    @JsonProperty("is_transient")
    private Boolean isTransient;

    @JsonProperty("error_user_title")
    private String errorUserTitle;

    @JsonProperty("error_user_msg")
    private String errorUserMsg;
  }

  /** Returns {@code true} when the template status was successfully retrieved (id is present). */
  public boolean isSuccess() {
    return error == null && id != null && !id.isBlank();
  }

  /** Returns the error message, preferring the user-friendly message if available. */
  public String getErrorMessage() {
    if (error == null) {
      return null;
    }
    // Prefer user-friendly message if available
    if (error.getErrorUserMsg() != null && !error.getErrorUserMsg().isBlank()) {
      return error.getErrorUserMsg();
    }
    return error.getMessage();
  }
}
