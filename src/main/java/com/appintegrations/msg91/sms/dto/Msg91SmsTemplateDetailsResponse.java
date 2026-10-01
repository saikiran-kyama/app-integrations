package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 SMS Get Template Versions API.
 *
 * <p>Example response:
 *
 * <pre>
 * {
 *     "data": [
 *         {
 *             "id": "403896",
 *             "user_id": "510516",
 *             "template_id": "69f1abd698ee82f2110719d5",
 *             "template_name": "Server Request",
 *             "template_data": "Dear Customer, your service request ID is ##VAR1##...",
 *             "DLT_ID": "1107177736243421169",
 *             "sender_id": "BSBRNS",
 *             "version": "v1.0",
 *             "status": "1",
 *             "active_status": "1",
 *             "sms_type": "NORMAL",
 *             "dlt_verified": "10",
 *             "dlt_reason": "",
 *             "reject_reason": ""
 *         }
 *     ],
 *     "status": "success",
 *     "hasError": false,
 *     "errors": []
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91SmsTemplateDetailsResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** List of template version details. */
  @JsonProperty("data")
  private List<TemplateVersion> data;

  /** Whether there was an error. */
  @JsonProperty("hasError")
  private Boolean hasError;

  /** Errors list. */
  @JsonProperty("errors")
  private List<Object> errors;

  /** Check if the request was successful. */
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(status) || (hasError != null && !hasError);
  }

  /** Check if there was an error. */
  public boolean isError() {
    return "error".equalsIgnoreCase(status) || (hasError != null && hasError);
  }

  /** Get the first (usually only) template version. */
  public TemplateVersion getTemplateVersion() {
    return data != null && !data.isEmpty() ? data.get(0) : null;
  }

  /** Get template name from first version. */
  public String getTemplateName() {
    TemplateVersion version = getTemplateVersion();
    return version != null ? version.getTemplateName() : null;
  }

  /** Get template content from first version. */
  public String getTemplateData() {
    TemplateVersion version = getTemplateVersion();
    return version != null ? version.getTemplateData() : null;
  }

  /** Check if template is active. */
  public boolean isActive() {
    TemplateVersion version = getTemplateVersion();
    return version != null && "1".equals(version.getActiveStatus());
  }

  /** Check if template is DLT verified. */
  public boolean isDltVerified() {
    TemplateVersion version = getTemplateVersion();
    return version != null && "10".equals(version.getDltVerified());
  }

  /** Template version details. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TemplateVersion {

    /** Version record ID. */
    @JsonProperty("id")
    private String id;

    /** User ID. */
    @JsonProperty("user_id")
    private String userId;

    /** Template ID. */
    @JsonProperty("template_id")
    private String templateId;

    /** Template name. */
    @JsonProperty("template_name")
    private String templateName;

    /** Template content with variables. */
    @JsonProperty("template_data")
    private String templateData;

    /** DLT (Distributed Ledger Technology) template ID. */
    @JsonProperty("DLT_ID")
    private String dltId;

    /** Sender ID. */
    @JsonProperty("sender_id")
    private String senderId;

    /** Template version (e.g., "v1.0"). */
    @JsonProperty("version")
    private String version;

    /** Status (1 = approved, 0 = pending). */
    @JsonProperty("status")
    private String status;

    /** Active status (1 = active, 0 = inactive). */
    @JsonProperty("active_status")
    private String activeStatus;

    /** SMS type (NORMAL/UNICODE). */
    @JsonProperty("sms_type")
    private String smsType;

    /** DLT verification status (10 = verified). */
    @JsonProperty("dlt_verified")
    private String dltVerified;

    /** DLT rejection reason if not verified. */
    @JsonProperty("dlt_reason")
    private String dltReason;

    /** Rejection reason if template was rejected. */
    @JsonProperty("reject_reason")
    private String rejectReason;

    /** Check if template is approved. */
    public boolean isApproved() {
      return "1".equals(status);
    }

    /** Check if template is active. */
    public boolean isActive() {
      return "1".equals(activeStatus);
    }

    /** Check if DLT is verified. */
    public boolean isDltVerified() {
      return "10".equals(dltVerified);
    }
  }

  /**
   * Creates an error response.
   *
   * @param errorMessage Error message
   * @return Error response
   */
  public static Msg91SmsTemplateDetailsResponse error(String errorMessage) {
    return Msg91SmsTemplateDetailsResponse.builder().status("error").hasError(true).build();
  }
}
