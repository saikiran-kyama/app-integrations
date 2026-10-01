package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for Brevo Transactional SMS API.
 *
 * @see <a href="https://developers.brevo.com/reference/sendtransacsms">Brevo SMS API Docs</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrevoSmsRequest {

  @JsonProperty("sender")
  private String sender;

  @JsonProperty("recipient")
  private String recipient;

  @JsonProperty("content")
  private String content;

  @JsonProperty("type")
  @Builder.Default
  private String type = "transactional";

  @JsonProperty("tag")
  private String tag;

  @JsonProperty("webUrl")
  private String webUrl;

  @JsonProperty("organisationPrefix")
  private String organisationPrefix;
}
