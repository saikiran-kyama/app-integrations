package com.appintegrations.firebase;

import com.appintegrations.firebase.dto.FcmMessageRequest;
import com.appintegrations.firebase.dto.FcmMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending push notifications via Firebase Cloud Messaging (FCM).
 *
 * <p>Usage example:
 *
 * <pre>
 * // Send a notification to a device token
 * firebasePushService.sendNotification(deviceToken, "Hello!", "Welcome to our app");
 *
 * // Send a data message
 * firebasePushService.sendDataMessage(deviceToken, Map.of("action", "open_page", "page_id", "123"));
 *
 * // Send to a topic
 * firebasePushService.sendToTopic("news", "Breaking News", "New article available");
 * </pre>
 *
 * @see <a href="https://firebase.google.com/docs/cloud-messaging/send-message">FCM Send
 *     Messages</a>
 */
@Slf4j
public class FirebasePushService {

  private final FirebaseProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private String accessToken;
  private long tokenExpiryTime;

  public FirebasePushService(FirebaseProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
            .build();
  }

  /** Check if Firebase is enabled and configured. */
  public boolean isEnabled() {
    return properties.isEnabled()
        && properties.getProjectId() != null
        && !properties.getProjectId().isBlank()
        && (hasCredentialsFile() || hasCredentialsJson());
  }

  private boolean hasCredentialsFile() {
    return properties.getCredentialsFile() != null && !properties.getCredentialsFile().isBlank();
  }

  private boolean hasCredentialsJson() {
    return properties.getCredentialsJson() != null && !properties.getCredentialsJson().isBlank();
  }

  /**
   * Send a notification to a device.
   *
   * @param deviceToken FCM device registration token
   * @param title Notification title
   * @param body Notification body
   * @return Response from FCM
   */
  public FcmMessageResponse sendNotification(String deviceToken, String title, String body) {
    FcmMessageRequest request = FcmMessageRequest.toToken(deviceToken, title, body);
    return sendMessage(request);
  }

  /**
   * Send a notification with image.
   *
   * @param deviceToken FCM device registration token
   * @param title Notification title
   * @param body Notification body
   * @param imageUrl URL of the image to display
   * @return Response from FCM
   */
  public FcmMessageResponse sendNotification(
      String deviceToken, String title, String body, String imageUrl) {
    FcmMessageRequest request =
        FcmMessageRequest.builder()
            .message(
                FcmMessageRequest.Message.builder()
                    .token(deviceToken)
                    .notification(
                        FcmMessageRequest.Notification.builder()
                            .title(title)
                            .body(body)
                            .image(imageUrl)
                            .build())
                    .build())
            .build();
    return sendMessage(request);
  }

  /**
   * Send a data-only message (silent notification).
   *
   * @param deviceToken FCM device registration token
   * @param data Key-value data payload
   * @return Response from FCM
   */
  public FcmMessageResponse sendDataMessage(String deviceToken, Map<String, String> data) {
    FcmMessageRequest request = FcmMessageRequest.dataToToken(deviceToken, data);
    return sendMessage(request);
  }

  /**
   * Send a notification to a topic.
   *
   * @param topic Topic name (without "/topics/" prefix)
   * @param title Notification title
   * @param body Notification body
   * @return Response from FCM
   */
  public FcmMessageResponse sendToTopic(String topic, String title, String body) {
    FcmMessageRequest request = FcmMessageRequest.toTopic(topic, title, body);
    return sendMessage(request);
  }

  /**
   * Send a message with full request customization.
   *
   * @param request The FCM message request
   * @return Response from FCM
   */
  public FcmMessageResponse sendMessage(FcmMessageRequest request) {
    if (!isEnabled()) {
      log.warn("Firebase is not enabled. Skipping push notification.");
      FcmMessageResponse response = new FcmMessageResponse();
      response.setError(new FcmMessageResponse.Error());
      response.getError().setMessage("Firebase integration is disabled");
      return response;
    }

    try {
      String token = getAccessToken();
      if (token == null) {
        FcmMessageResponse response = new FcmMessageResponse();
        response.setError(new FcmMessageResponse.Error());
        response.getError().setMessage("Failed to obtain access token");
        return response;
      }

      String jsonBody = objectMapper.writeValueAsString(request);
      String endpoint =
          String.format(
              "%s/projects/%s/messages:send", properties.getApiUrl(), properties.getProjectId());

      log.info(
          "FCM Request - Token: {}..., Topic: {}",
          request.getMessage().getToken() != null
              ? request
                  .getMessage()
                  .getToken()
                  .substring(0, Math.min(20, request.getMessage().getToken().length()))
              : "N/A",
          request.getMessage().getTopic());
      log.debug("FCM Request Body: {}", jsonBody);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(endpoint))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + token)
              .timeout(Duration.ofMillis(properties.getReadTimeout()))
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      log.info("FCM Response - Status: {}", httpResponse.statusCode());
      log.debug("FCM Response Body: {}", httpResponse.body());

      FcmMessageResponse response = parseResponse(httpResponse);

      if (response.isSuccess()) {
        log.info("Push notification sent successfully. MessageId: {}", response.getMessageId());
      } else {
        log.error("Failed to send push notification. Error: {}", response.getErrorMessage());
      }

      return response;
    } catch (Exception e) {
      log.error("Error sending push notification: {}", e.getMessage(), e);
      FcmMessageResponse errorResponse = new FcmMessageResponse();
      errorResponse.setError(new FcmMessageResponse.Error());
      errorResponse.getError().setMessage("Failed to send notification: " + e.getMessage());
      return errorResponse;
    }
  }

  /** Send a notification asynchronously. */
  public CompletableFuture<FcmMessageResponse> sendNotificationAsync(
      String deviceToken, String title, String body) {
    return CompletableFuture.supplyAsync(() -> sendNotification(deviceToken, title, body));
  }

  /** Send to topic asynchronously. */
  public CompletableFuture<FcmMessageResponse> sendToTopicAsync(
      String topic, String title, String body) {
    return CompletableFuture.supplyAsync(() -> sendToTopic(topic, title, body));
  }

  /**
   * Get OAuth2 access token for FCM. Note: In production, use Google Auth Library for proper token
   * management. This is a simplified implementation.
   */
  private synchronized String getAccessToken() {
    // Check if we have a valid cached token
    if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
      return accessToken;
    }

    // TODO: Implement proper OAuth2 token generation using service account credentials
    // For now, log a warning and return null
    // In production, use: GoogleCredentials.fromStream(inputStream).createScoped(scopes)
    log.warn(
        "Firebase OAuth2 token generation not implemented. "
            + "Please use Google Auth Library in production.");

    // For testing, you can set the token manually via properties
    // or implement the full OAuth2 flow

    return null;
  }

  private FcmMessageResponse parseResponse(HttpResponse<String> httpResponse) {
    try {
      return objectMapper.readValue(httpResponse.body(), FcmMessageResponse.class);
    } catch (Exception e) {
      log.warn("Failed to parse FCM response: {}", httpResponse.body());
      FcmMessageResponse response = new FcmMessageResponse();
      response.setError(new FcmMessageResponse.Error());
      response.getError().setMessage(httpResponse.body());
      return response;
    }
  }
}
