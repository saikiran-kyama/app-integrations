package com.appintegrations.msg91.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for MSG91 SMS and Email services.
 *
 * <p>Configure in application.yaml:
 *
 * <pre>
 * integrations:
 *   msg91:
 *     enabled: true
 *     auth-key: your-auth-key
 *     api-url: https://control.msg91.com/api/v5
 *     default-sender-id: YOURID
 *     email:
 *       domain: domain.mailer91.com
 *       sender-address: noreply@example.com
 *       sender-name: Your App Name
 * </pre>
 *
 * @see <a href="https://docs.msg91.com">MSG91 API Documentation</a>
 */
@Data
@ConfigurationProperties(prefix = "integrations.msg91")
public class Msg91Properties {

  /** Whether MSG91 integration is enabled. */
  private boolean enabled = true;

  /** MSG91 authentication key. */
  private String authKey;

  /** Base URL for MSG91 API. */
  private String apiUrl = "https://control.msg91.com/api/v5";

  /** Default sender ID for SMS. */
  private String defaultSenderId;

  /** Connection timeout in milliseconds. */
  private int connectTimeout = 5000;

  /** Read timeout in milliseconds. */
  private int readTimeout = 10000;

  /** Email configuration. */
  private Email email = new Email();

  @Data
  public static class Email {
    /** Email domain (e.g., "domain.mailer91.com"). */
    private String domain;

    /** Default sender email address. */
    private String senderAddress;

    /** Default sender name. */
    private String senderName;
  }

  /** Get email domain. */
  public String getEmailDomain() {
    return email.getDomain();
  }

  /** Get email sender address. */
  public String getEmailSenderAddress() {
    return email.getSenderAddress();
  }

  /** Get email sender name. */
  public String getEmailSenderName() {
    return email.getSenderName();
  }
}
