package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Recipient group containing recipients and template variables for MSG91 Email API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91EmailRecipientGroup {

  /** List of primary recipients (To). */
  @JsonProperty("to")
  private List<Msg91EmailRecipient> to;

  /** List of CC recipients. */
  @JsonProperty("cc")
  private List<Msg91EmailRecipient> cc;

  /** List of BCC recipients. */
  @JsonProperty("bcc")
  private List<Msg91EmailRecipient> bcc;

  /** Template variables for this recipient group. */
  @JsonProperty("variables")
  private Map<String, String> variables;

  /**
   * Creates a simple recipient group with single recipient.
   *
   * @param email The email address
   * @param name The recipient name
   * @param variables Template variables
   * @return Configured recipient group
   */
  public static Msg91EmailRecipientGroup singleRecipient(
      String email, String name, Map<String, String> variables) {
    return Msg91EmailRecipientGroup.builder()
        .to(List.of(Msg91EmailRecipient.of(email, name)))
        .variables(variables)
        .build();
  }

  /**
   * Creates a simple recipient group with single recipient (no name).
   *
   * @param email The email address
   * @param variables Template variables
   * @return Configured recipient group
   */
  public static Msg91EmailRecipientGroup singleRecipient(
      String email, Map<String, String> variables) {
    return Msg91EmailRecipientGroup.builder()
        .to(List.of(Msg91EmailRecipient.of(email)))
        .variables(variables)
        .build();
  }
}
