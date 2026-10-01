package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from MSG91 Email Template Version API. Used to get status of a template version by
 * version ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91EmailTemplateVersionResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** Version data. */
  @JsonProperty("data")
  private VersionData data;

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

  /** Get the version ID. */
  public Long getVersionId() {
    return data != null ? data.getId() : null;
  }

  /** Get the template ID. */
  public Long getTemplateId() {
    return data != null ? data.getTemplateId() : null;
  }

  /** Get the status ID. */
  public Integer getStatusId() {
    return data != null ? data.getStatusId() : null;
  }

  /**
   * Get the human-readable status name.
   *
   * @return Status name (Pending, Approved, Rejected, or Unknown)
   */
  public String getStatusName() {
    if (data == null || data.getStatusId() == null) {
      return "Unknown";
    }
    return switch (data.getStatusId()) {
      case 1 -> "Pending";
      case 2 -> "Approved";
      case 3 -> "Rejected";
      default -> "Unknown";
    };
  }

  /** Check if the template version is approved. */
  public boolean isApproved() {
    return data != null && data.getStatusId() != null && data.getStatusId() == 2;
  }

  /** Check if the template version is pending. */
  public boolean isPending() {
    return data != null && data.getStatusId() != null && data.getStatusId() == 1;
  }

  /** Check if the template version is rejected. */
  public boolean isRejected() {
    return data != null && data.getStatusId() != null && data.getStatusId() == 3;
  }

  /** Template version data. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VersionData {

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

    /** Attached files. */
    @JsonProperty("files")
    private Object files;

    /** Metadata. */
    @JsonProperty("meta")
    private Map<String, Object> meta;

    /** Mail type ID. */
    @JsonProperty("mail_type_id")
    private Integer mailTypeId;

    /** Thread ID. */
    @JsonProperty("thread_id")
    private Long threadId;

    /** Request type. */
    @JsonProperty("request_type")
    private String requestType;

    /** Status ID (1=Pending, 2=Approved, 3=Rejected). */
    @JsonProperty("status_id")
    private Integer statusId;

    /** Whether this version is active. */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** Whether this is a draft. */
    @JsonProperty("is_draft")
    private Integer isDraft;

    /** Editor ID. */
    @JsonProperty("editor_id")
    private Integer editorId;

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

    /** Deletion timestamp. */
    @JsonProperty("deleted_at")
    private String deletedAt;

    /** Reason ID. */
    @JsonProperty("reason_id")
    private Integer reasonId;

    /** Parent template information (when with=template is specified). */
    @JsonProperty("template")
    private TemplateInfo template;

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

    /** Check if the template version is approved. */
    public boolean isApproved() {
      return statusId != null && statusId == 2;
    }

    /** Check if the template version is pending. */
    public boolean isPending() {
      return statusId != null && statusId == 1;
    }

    /** Check if the template version is rejected. */
    public boolean isRejected() {
      return statusId != null && statusId == 3;
    }
  }

  /** Parent template information. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TemplateInfo {

    /** Template ID. */
    @JsonProperty("id")
    private Long id;

    /** User ID. */
    @JsonProperty("user_id")
    private Long userId;

    /** Template name. */
    @JsonProperty("name")
    private String name;

    /** Template slug. */
    @JsonProperty("slug")
    private String slug;

    /** Intelligent send flag. */
    @JsonProperty("intelligent_send")
    private Integer intelligentSend;

    /** Initial limit. */
    @JsonProperty("initial_limit")
    private Integer initialLimit;

    /** Creation timestamp. */
    @JsonProperty("created_at")
    private String createdAt;

    /** Last update timestamp. */
    @JsonProperty("updated_at")
    private String updatedAt;

    /** Deletion timestamp. */
    @JsonProperty("deleted_at")
    private String deletedAt;

    /** Number of versions. */
    @JsonProperty("versions_count")
    private Integer versionsCount;
  }
}
