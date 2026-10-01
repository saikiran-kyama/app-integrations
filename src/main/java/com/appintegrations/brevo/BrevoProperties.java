package com.appintegrations.brevo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Brevo (Sendinblue) email and SMS service.
 *
 * <p>Configure in application.yaml:
 *
 * <pre>
 * integrations:
 *   brevo:
 *     enabled: true
 *     api-key: your-api-key
 *     sender:
 *       email: noreply@example.com
 *       name: Your App Name
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "integrations.brevo")
public class BrevoProperties {

  /** Whether Brevo integration is enabled. */
  private boolean enabled = true;

  /** Brevo API key for authentication. */
  private String apiKey;

  /** Base URL for Brevo API. */
  private String apiUrl = "https://api.brevo.com/v3";

  /** Default sender configuration. */
  private Sender sender = new Sender();

  /** Connection timeout in milliseconds. */
  private int connectTimeout = 5000;

  /** Read timeout in milliseconds. */
  private int readTimeout = 10000;

  /** SMS configuration. */
  private Sms sms = new Sms();

  /** SMTP configuration for direct SMTP sending. */
  private Smtp smtp = new Smtp();

  @Data
  public static class Sender {
    private String email;
    private String name;
  }

  @Data
  public static class Sms {
    /** Whether SMS sending is enabled. */
    private boolean enabled = true;

    /** Default sender name for SMS (max 11 characters for alphanumeric). */
    private String sender = "AuthService";

    /** SMS API endpoint. */
    private String apiUrl = "https://api.brevo.com/v3/transactionalSMS/sms";
  }

  @Data
  public static class Smtp {
    /** Whether SMTP is enabled. */
    private boolean enabled = true;

    /** SMTP host. */
    private String host = "smtp-relay.brevo.com";

    /** SMTP port. */
    private int port = 587;

    /** SMTP login. */
    private String login;

    /** SMTP key from Brevo. */
    private String smtpKey;

    /** Use TLS. */
    private boolean tls = true;
  }

  /** Webhook configuration. */
  private Webhook webhook = new Webhook();

  /** Webhook configuration for receiving delivery status updates. */
  @Data
  public static class Webhook {
    /** Whether webhook processing is enabled. */
    private boolean enabled = true;

    /**
     * Webhook endpoint path. This is the path where your application will receive Brevo webhooks.
     * You need to configure this URL in Brevo dashboard.
     */
    private String path = "/webhooks/brevo";

    /**
     * Secret key for validating webhook signatures (optional). Set this in Brevo webhook
     * configuration to verify webhook authenticity.
     */
    private String secret;
  }
}
