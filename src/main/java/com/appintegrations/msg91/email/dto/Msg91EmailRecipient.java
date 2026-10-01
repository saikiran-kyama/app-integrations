package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email recipient with optional name for MSG91 Email API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91EmailRecipient {

  /** Recipient's name. */
  @JsonProperty("name")
  private String name;

  /** Recipient's email address. */
  @JsonProperty("email")
  private String email;

  /**
   * Creates a recipient with email only.
   *
   * @param email The email address
   * @return Configured recipient
   */
  public static Msg91EmailRecipient of(String email) {
    return Msg91EmailRecipient.builder().email(email).build();
  }

  /**
   * Creates a recipient with name and email.
   *
   * @param email The email address
   * @param name The recipient name
   * @return Configured recipient
   */
  public static Msg91EmailRecipient of(String email, String name) {
    return Msg91EmailRecipient.builder().email(email).name(name).build();
  }
}
