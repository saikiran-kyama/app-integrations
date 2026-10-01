package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Recipient information for MSG91 SMS request.
 *
 * <p>Usage example:
 *
 * <pre>
 * // Simple recipient
 * Msg91SmsRecipient recipient = Msg91SmsRecipient.of("918309496713");
 *
 * // Recipient with variables
 * Msg91SmsRecipient recipient = Msg91SmsRecipient.of(
 *     "918309496713",
 *     Map.of("VAR1", "12345", "VAR2", "John")
 * );
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91SmsRecipient {

  /** Mobile number with country code (e.g., "918309496713"). */
  @JsonProperty("mobiles")
  private String mobiles;

  /**
   * Template variables (VAR1, VAR2, etc.). These are serialized as top-level properties in the
   * JSON.
   */
  @JsonIgnore @Builder.Default private Map<String, String> variables = new HashMap<>();

  /**
   * Get variables map for JSON serialization. This allows variables to be serialized as top-level
   * properties.
   */
  @JsonAnyGetter
  public Map<String, String> getVariablesForSerialization() {
    return variables;
  }

  /**
   * Creates a recipient with just mobile number.
   *
   * @param mobile Mobile number with country code
   * @return Configured recipient
   */
  public static Msg91SmsRecipient of(String mobile) {
    return Msg91SmsRecipient.builder().mobiles(mobile).build();
  }

  /**
   * Creates a recipient with mobile number and variables.
   *
   * @param mobile Mobile number with country code
   * @param variables Template variable values (VAR1, VAR2, etc.)
   * @return Configured recipient
   */
  public static Msg91SmsRecipient of(String mobile, Map<String, String> variables) {
    return Msg91SmsRecipient.builder()
        .mobiles(mobile)
        .variables(variables != null ? new HashMap<>(variables) : new HashMap<>())
        .build();
  }

  /**
   * Add a variable to this recipient.
   *
   * @param key Variable key (e.g., "VAR1")
   * @param value Variable value
   * @return This recipient for chaining
   */
  public Msg91SmsRecipient addVariable(String key, String value) {
    if (variables == null) {
      variables = new HashMap<>();
    }
    variables.put(key, value);
    return this;
  }

  /** Handle dynamic variable properties during JSON deserialization. */
  @JsonAnySetter
  public void setVariable(String key, Object value) {
    if (variables == null) {
      variables = new HashMap<>();
    }
    if (value != null) {
      variables.put(key, value.toString());
    }
  }
}
