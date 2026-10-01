package com.appintegrations.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for WhatsApp Cloud API - Text Message. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppMessageRequest {

  /** Messaging product (always "whatsapp"). */
  @JsonProperty("messaging_product")
  @Builder.Default
  private String messagingProduct = "whatsapp";

  /** Recipient type (individual or broadcast). */
  @JsonProperty("recipient_type")
  @Builder.Default
  private String recipientType = "individual";

  /** Recipient phone number with country code (e.g., "919876543210"). */
  @JsonProperty("to")
  private String to;

  /** Message type: text, template, image, document, etc. */
  @JsonProperty("type")
  private String type;

  /** Text message content. */
  @JsonProperty("text")
  private TextContent text;

  /** Template message content. */
  @JsonProperty("template")
  private TemplateContent template;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TextContent {
    /** Preview URL in the message. */
    @JsonProperty("preview_url")
    private Boolean previewUrl;

    /** The text message body. */
    @JsonProperty("body")
    private String body;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TemplateContent {
    /** Template name (as registered in WhatsApp Business Manager). */
    @JsonProperty("name")
    private String name;

    /** Template language. */
    @JsonProperty("language")
    private Language language;

    /** Template components (header, body, buttons). */
    @JsonProperty("components")
    private List<Component> components;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Language {
    @JsonProperty("code")
    private String code;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Component {
    @JsonProperty("type")
    private String type; // "header", "body", "button"

    @JsonProperty("parameters")
    private List<Parameter> parameters;

    @JsonProperty("sub_type")
    private String subType; // For buttons: "quick_reply", "url"

    @JsonProperty("index")
    private Integer index; // Button index
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Parameter {
    @JsonProperty("type")
    private String type; // "text", "image", "document", "currency", "date_time"

    @JsonProperty("text")
    private String text;

    @JsonProperty("image")
    private MediaObject image;

    @JsonProperty("document")
    private MediaObject document;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MediaObject {
    @JsonProperty("link")
    private String link;

    @JsonProperty("id")
    private String id; // Media ID if uploaded to WhatsApp

    @JsonProperty("filename")
    private String filename;
  }

  /** Create a text message request. */
  public static WhatsAppMessageRequest textMessage(String to, String body) {
    return WhatsAppMessageRequest.builder()
        .to(to)
        .type("text")
        .text(TextContent.builder().body(body).build())
        .build();
  }

  /** Create a template message request. */
  public static WhatsAppMessageRequest templateMessage(
      String to, String templateName, String languageCode, List<Component> components) {
    return WhatsAppMessageRequest.builder()
        .to(to)
        .type("template")
        .template(
            TemplateContent.builder()
                .name(templateName)
                .language(Language.builder().code(languageCode).build())
                .components(components)
                .build())
        .build();
  }
}
