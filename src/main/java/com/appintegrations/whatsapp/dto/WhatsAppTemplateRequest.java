package com.appintegrations.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for creating a WhatsApp message template via the Graph API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppTemplateRequest {

  /** Unique template name (snake_case, e.g. "order_update_v1"). */
  @JsonProperty("name")
  private String name;

  /** BCP‑47 language code (e.g. "en_US"). */
  @JsonProperty("language")
  private String language;

  /**
   * Template category.
   *
   * <p>Accepted values: {@code AUTHENTICATION}, {@code MARKETING}, {@code UTILITY}.
   */
  @JsonProperty("category")
  private String category;

  /** List of template components (HEADER, BODY, FOOTER, BUTTONS). */
  @JsonProperty("components")
  private List<Component> components;

  // -------------------------------------------------------------------------
  // Inner classes
  // -------------------------------------------------------------------------

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Component {

    /**
     * Component type.
     *
     * <p>Accepted values: {@code HEADER}, {@code BODY}, {@code FOOTER}, {@code BUTTONS}.
     */
    @JsonProperty("type")
    private String type;

    /** Template text with placeholders such as {@code {{1}}}, {@code {{2}}}. */
    @JsonProperty("text")
    private String text;

    /** Format used for HEADER components (TEXT, IMAGE, DOCUMENT, VIDEO). */
    @JsonProperty("format")
    private String format;

    /** Example values used to populate placeholders during template review. */
    @JsonProperty("example")
    private Example example;

    /** Button definitions for BUTTONS components. */
    @JsonProperty("buttons")
    private List<Button> buttons;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Example {

    /**
     * Sample values for BODY placeholders.
     *
     * <p>Outer list represents the message instance; inner list contains values for each positional
     * placeholder in order.
     *
     * <pre>
     * "body_text": [["Sai", "ORD12345"]]
     * </pre>
     */
    @JsonProperty("body_text")
    private List<List<String>> bodyText;

    /**
     * Sample URL(s) for HEADER components that use a media placeholder (IMAGE / DOCUMENT / VIDEO).
     */
    @JsonProperty("header_handle")
    private List<String> headerHandle;

    /** Sample text for a HEADER with TEXT format. */
    @JsonProperty("header_text")
    private List<String> headerText;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Button {

    /**
     * Button type.
     *
     * <p>Accepted values: {@code QUICK_REPLY}, {@code URL}, {@code PHONE_NUMBER}, {@code OTP},
     * {@code CATALOG}.
     */
    @JsonProperty("type")
    private String type;

    /** Button display text. */
    @JsonProperty("text")
    private String text;

    /** URL for URL-type buttons. May contain a {@code {{1}}} placeholder. */
    @JsonProperty("url")
    private String url;

    /** Phone number for PHONE_NUMBER-type buttons (E.164 format). */
    @JsonProperty("phone_number")
    private String phoneNumber;
  }
}
