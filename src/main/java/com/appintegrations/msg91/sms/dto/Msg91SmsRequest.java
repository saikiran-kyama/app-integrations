package com.appintegrations.msg91.sms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MSG91 SMS Send API (Flow API).
 *
 * <p>Usage example:
 *
 * <pre>
 * Msg91SmsRequest request = Msg91SmsRequest.builder()
 *     .templateId("69f07fcbc1e2b3204b090763")
 *     .recipients(List.of(
 *         Msg91SmsRecipient.of("918309496713"),
 *         Msg91SmsRecipient.of("919876543210", Map.of("VAR1", "value1"))
 *     ))
 *     .build();
 * </pre>
 *
 * @see <a href="https://docs.msg91.com/reference/send-sms">MSG91 Send SMS API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Msg91SmsRequest {

  /** Template ID from MSG91 dashboard. */
  @JsonProperty("template_id")
  private String templateId;

  /** Short URL (1 for enabled, 0 for disabled). */
  @JsonProperty("short_url")
  private Integer shortUrl;

  /** Realtime callback URL for delivery reports. */
  @JsonProperty("realTimeResponse")
  private String realTimeResponse;

  /** List of recipients with their mobile numbers and variables. */
  @JsonProperty("recipients")
  private List<Msg91SmsRecipient> recipients;

  /**
   * Creates a simple SMS request for a single recipient.
   *
   * @param mobile Mobile number with country code (e.g., "918309496713")
   * @param templateId Template ID from MSG91 dashboard
   * @return Configured request
   */
  public static Msg91SmsRequest singleRecipient(String mobile, String templateId) {
    return Msg91SmsRequest.builder()
        .templateId(templateId)
        .recipients(List.of(Msg91SmsRecipient.of(mobile)))
        .build();
  }

  /**
   * Creates a simple SMS request for a single recipient with variables.
   *
   * @param mobile Mobile number with country code (e.g., "918309496713")
   * @param templateId Template ID from MSG91 dashboard
   * @param variables Template variable values
   * @return Configured request
   */
  public static Msg91SmsRequest singleRecipient(
      String mobile, String templateId, Map<String, String> variables) {
    return Msg91SmsRequest.builder()
        .templateId(templateId)
        .recipients(List.of(Msg91SmsRecipient.of(mobile, variables)))
        .build();
  }

  /**
   * Creates a bulk SMS request for multiple recipients.
   *
   * @param mobiles List of mobile numbers with country code
   * @param templateId Template ID from MSG91 dashboard
   * @return Configured request
   */
  public static Msg91SmsRequest bulkRecipients(List<String> mobiles, String templateId) {
    List<Msg91SmsRecipient> recipients = mobiles.stream().map(Msg91SmsRecipient::of).toList();
    return Msg91SmsRequest.builder().templateId(templateId).recipients(recipients).build();
  }
}
