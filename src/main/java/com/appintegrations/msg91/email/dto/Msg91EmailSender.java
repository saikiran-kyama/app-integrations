package com.appintegrations.msg91.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email sender details for MSG91 Email API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91EmailSender {

  /** Sender's display name. */
  @JsonProperty("name")
  private String name;

  /** Sender's email address. */
  @JsonProperty("email")
  private String email;

  /**
   * Creates a sender with email only.
   *
   * @param email The email address
   * @return Configured sender
   */
  public static Msg91EmailSender of(String email) {
    return Msg91EmailSender.builder().email(email).build();
  }

  /**
   * Creates a sender with name and email.
   *
   * @param email The email address
   * @param name The sender name
   * @return Configured sender
   */
  public static Msg91EmailSender of(String email, String name) {
    return Msg91EmailSender.builder().email(email).name(name).build();
  }
}
