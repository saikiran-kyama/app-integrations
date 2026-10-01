package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from MSG91 Email Template Create API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91CreateEmailTemplateResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** Template data. */
  @JsonProperty("data")
  private TemplateData data;

  /** Errors object. */
  @JsonProperty("errors")
  private Object errors;

  /** Whether there was an error. */
  @JsonProperty("hasError")
  private Boolean hasError;

  /** Check if the request was successful. */
  public boolean isSuccess() {
    return "success".equalsIgnoreCase(status) || (hasError != null && !hasError);
  }

  /** Get the template ID. */
  public Long getTemplateId() {
    return data != null ? data.getId() : null;
  }

  /** Get the template slug (used as template_id in send email). */
  public String getTemplateSlug() {
    return data != null ? data.getSlug() : null;
  }

  /** Get the first version ID. */
  public Long getFirstVersionId() {
    if (data != null && data.getVersions() != null && !data.getVersions().isEmpty()) {
      return data.getVersions().get(0).getId();
    }
    return null;
  }

  /** Template data. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TemplateData {

    /** Template ID. */
    @JsonProperty("id")
    private Long id;

    /** Template name. */
    @JsonProperty("name")
    private String name;

    /** Template slug (URL-friendly name). */
    @JsonProperty("slug")
    private String slug;

    /** User ID. */
    @JsonProperty("user_id")
    private Long userId;

    /** Creation timestamp. */
    @JsonProperty("created_at")
    private String createdAt;

    /** Last update timestamp. */
    @JsonProperty("updated_at")
    private String updatedAt;

    /** Template versions. */
    @JsonProperty("versions")
    private List<TemplateVersion> versions;
  }

  /** Template version data. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TemplateVersion {

    /** Version ID. */
    @JsonProperty("id")
    private Long id;

    /** Template ID. */
    @JsonProperty("template_id")
    private Long templateId;

    /** Version name (e.g., "v1.0"). */
    @JsonProperty("name")
    private String name;

    /** Version slug. */
    @JsonProperty("slug")
    private String slug;

    /** Email subject. */
    @JsonProperty("subject")
    private String subject;

    /** HTML body. */
    @JsonProperty("body")
    private String body;

    /** Plain text body. */
    @JsonProperty("text_plain")
    private String textPlain;

    /** AMP HTML content. */
    @JsonProperty("amp_html")
    private String ampHtml;

    /** Preview link URL. */
    @JsonProperty("preview_link")
    private String previewLink;

    /** Template variables extracted from content. */
    @JsonProperty("variables")
    private List<String> variables;

    /** Status ID (1=Pending, 2=Approved, 3=Rejected). */
    @JsonProperty("status_id")
    private Integer statusId;

    /** Whether this version is active. */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** Whether this is a draft. */
    @JsonProperty("is_draft")
    private Integer isDraft;

    /** Template syntax type. */
    @JsonProperty("template_syntax")
    private String templateSyntax;

    /** Description. */
    @JsonProperty("description")
    private String description;

    /** Creation timestamp. */
    @JsonProperty("created_at")
    private String createdAt;

    /** Last update timestamp. */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Get the human-readable status name.
     *
     * @return Status name (Pending, Approved, Rejected, or Unknown)
     */
    public String getStatusName() {
      if (statusId == null) {
        return "Unknown";
      }
      return switch (statusId) {
        case 1 -> "Pending";
        case 2 -> "Approved";
        case 3 -> "Rejected";
        default -> "Unknown";
      };
    }

    /** Check if the template is approved. */
    public boolean isApproved() {
      return statusId != null && statusId == 2;
    }

    /** Check if the template is pending. */
    public boolean isPending() {
      return statusId != null && statusId == 1;
    }

    /** Check if the template is rejected. */
    public boolean isRejected() {
      return statusId != null && statusId == 3;
    }
  }
}
