package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email attachment for MSG91 Email API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91EmailAttachment {

  /** File name. */
  @JsonProperty("filename")
  private String filename;

  /** Base64 encoded file content. */
  @JsonProperty("content")
  private String content;

  /** MIME type (e.g., "application/pdf"). */
  @JsonProperty("content_type")
  private String contentType;

  /**
   * Creates an attachment.
   *
   * @param filename The file name
   * @param content Base64 encoded content
   * @param contentType MIME type
   * @return Configured attachment
   */
  public static Msg91EmailAttachment of(String filename, String content, String contentType) {
    return Msg91EmailAttachment.builder()
        .filename(filename)
        .content(content)
        .contentType(contentType)
        .build();
  }
}
