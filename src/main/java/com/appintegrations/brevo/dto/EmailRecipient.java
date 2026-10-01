package com.appintegrations.brevo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents an email recipient for Brevo API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRecipient {

  @JsonProperty("email")
  private String email;

  @JsonProperty("name")
  private String name;

  /** Create a recipient with just an email. */
  public static EmailRecipient of(String email) {
    return EmailRecipient.builder().email(email).build();
  }

  /** Create a recipient with email and name. */
  public static EmailRecipient of(String email, String name) {
    return EmailRecipient.builder().email(email).name(name).build();
  }
}
