package com.appintegrations.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for WhatsApp Business Cloud API (Meta).
 *
 * <p>Configure in application.yaml:
 *
 * <pre>
 * integrations:
 *   whatsapp:
 *     enabled: true
 *     access-token: your-access-token
 *     phone-number-id: your-phone-number-id
 *     business-account-id: your-business-account-id
 * </pre>
 *
 * @see <a href= "https://developers.facebook.com/docs/whatsapp/cloud-api">WhatsApp Cloud API</a>
 */
@Data
@ConfigurationProperties(prefix = "integrations.whatsapp")
public class WhatsAppProperties {

  /** Whether WhatsApp integration is enabled. */
  private boolean enabled = false;

  /** Meta Cloud API base URL. */
  private String apiUrl = "https://graph.facebook.com/v18.0";

  /** Access token from Meta Developer Portal. */
  private String accessToken;

  /** Phone Number ID from WhatsApp Business Manager. */
  private String phoneNumberId;

  /** Business Account ID (optional, for some API calls). */
  private String businessAccountId;

  /** Connection timeout in milliseconds. */
  private int connectTimeout = 30000;

  /** Read timeout in milliseconds. */
  private int readTimeout = 60000;

  /** Default template language. */
  private String defaultLanguage = "en";
}
