package com.appintegrations.firebase;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Firebase Cloud Messaging (FCM).
 *
 * <p>Configure in application.yaml:
 *
 * <pre>
 * integrations:
 *   firebase:
 *     enabled: true
 *     project-id: your-firebase-project-id
 *     credentials-file: /path/to/service-account.json
 *     # OR use inline credentials
 *     credentials-json: '{"type": "service_account", ...}'
 * </pre>
 *
 * @see <a href="https://firebase.google.com/docs/cloud-messaging">Firebase Cloud Messaging</a>
 */
@Data
@ConfigurationProperties(prefix = "integrations.firebase")
public class FirebaseProperties {

  /** Whether Firebase integration is enabled. */
  private boolean enabled = false;

  /** Firebase project ID. */
  private String projectId;

  /** Path to the service account JSON credentials file. */
  private String credentialsFile;

  /** Inline service account JSON credentials (alternative to file). */
  private String credentialsJson;

  /** FCM API endpoint (v1 HTTP API). */
  private String apiUrl = "https://fcm.googleapis.com/v1";

  /** Connection timeout in milliseconds. */
  private int connectTimeout = 5000;

  /** Read timeout in milliseconds. */
  private int readTimeout = 10000;

  /** Default notification icon. */
  private String defaultIcon;

  /** Default notification click action. */
  private String defaultClickAction;

  /** Default notification channel ID (Android). */
  private String defaultChannelId;
}
