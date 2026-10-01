package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response from MSG91 Email Templates List API (Email Logs). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Msg91EmailTemplateListResponse {

  /** Response status (success/error). */
  @JsonProperty("status")
  private String status;

  /** Paginated data. */
  @JsonProperty("data")
  private PaginatedData data;

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

  /** Get the list of templates. */
  public List<TemplateItem> getTemplates() {
    return data != null ? data.getData() : null;
  }

  /** Get total number of templates. */
  public Integer getTotal() {
    return data != null ? data.getTotal() : null;
  }

  /** Get current page number. */
  public Integer getCurrentPage() {
    return data != null ? data.getCurrentPage() : null;
  }

  /** Get last page number. */
  public Integer getLastPage() {
    return data != null ? data.getLastPage() : null;
  }

  /** Paginated data container. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PaginatedData {

    /** Current page number. */
    @JsonProperty("current_page")
    private Integer currentPage;

    /** List of templates. */
    @JsonProperty("data")
    private List<TemplateItem> data;

    /** First page URL. */
    @JsonProperty("first_page_url")
    private String firstPageUrl;

    /** Starting record number. */
    @JsonProperty("from")
    private Integer from;

    /** Last page number. */
    @JsonProperty("last_page")
    private Integer lastPage;

    /** Last page URL. */
    @JsonProperty("last_page_url")
    private String lastPageUrl;

    /** Next page URL. */
    @JsonProperty("next_page_url")
    private String nextPageUrl;

    /** API path. */
    @JsonProperty("path")
    private String path;

    /** Items per page. */
    @JsonProperty("per_page")
    private Integer perPage;

    /** Previous page URL. */
    @JsonProperty("prev_page_url")
    private String prevPageUrl;

    /** Ending record number. */
    @JsonProperty("to")
    private Integer to;

    /** Total number of records. */
    @JsonProperty("total")
    private Integer total;

    /** Pagination links. */
    @JsonProperty("links")
    private List<PaginationLink> links;
  }

  /** Pagination link. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PaginationLink {

    /** Link URL. */
    @JsonProperty("url")
    private String url;

    /** Link label. */
    @JsonProperty("label")
    private String label;

    /** Whether this is the active page. */
    @JsonProperty("active")
    private Boolean active;
  }

  /** Template item in the list. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TemplateItem {

    /** Template ID. */
    @JsonProperty("id")
    private Long id;

    /** Template name. */
    @JsonProperty("name")
    private String name;

    /** User ID. */
    @JsonProperty("user_id")
    private Long userId;

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

    /** Template versions. */
    @JsonProperty("versions")
    private List<Msg91CreateEmailTemplateResponse.TemplateVersion> versions;
  }
}
