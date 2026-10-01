package com.appintegrations.firebase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for Firebase Cloud Messaging (FCM) HTTP v1 API.
 *
 * @see <a href="https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages">FCM
 *     API</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FcmMessageRequest {

  /** The message payload. */
  @JsonProperty("message")
  private Message message;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Message {
    /** Registration token to send a message to. */
    @JsonProperty("token")
    private String token;

    /** Topic name to send a message to. */
    @JsonProperty("topic")
    private String topic;

    /** Condition to send a message to. */
    @JsonProperty("condition")
    private String condition;

    /** Notification payload. */
    @JsonProperty("notification")
    private Notification notification;

    /** Arbitrary key/value payload (data message). */
    @JsonProperty("data")
    private Map<String, String> data;

    /** Android-specific options. */
    @JsonProperty("android")
    private AndroidConfig android;

    /** Apple Push Notification Service specific options. */
    @JsonProperty("apns")
    private ApnsConfig apns;

    /** Web push protocol options. */
    @JsonProperty("webpush")
    private WebpushConfig webpush;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Notification {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("image")
    private String image;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AndroidConfig {
    @JsonProperty("collapse_key")
    private String collapseKey;

    @JsonProperty("priority")
    private String priority; // "normal" or "high"

    @JsonProperty("ttl")
    private String ttl;

    @JsonProperty("notification")
    private AndroidNotification notification;

    @JsonProperty("data")
    private Map<String, String> data;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class AndroidNotification {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("color")
    private String color;

    @JsonProperty("sound")
    private String sound;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("click_action")
    private String clickAction;

    @JsonProperty("channel_id")
    private String channelId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ApnsConfig {
    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("payload")
    private ApnsPayload payload;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ApnsPayload {
    @JsonProperty("aps")
    private Aps aps;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Aps {
    @JsonProperty("alert")
    private Object alert; // Can be string or ApsAlert object

    @JsonProperty("badge")
    private Integer badge;

    @JsonProperty("sound")
    private String sound;

    @JsonProperty("content-available")
    private Integer contentAvailable;

    @JsonProperty("category")
    private String category;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WebpushConfig {
    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("notification")
    private WebpushNotification notification;

    @JsonProperty("fcm_options")
    private WebpushFcmOptions fcmOptions;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WebpushNotification {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("icon")
    private String icon;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class WebpushFcmOptions {
    @JsonProperty("link")
    private String link;
  }

  /** Create a simple notification message to a device token. */
  public static FcmMessageRequest toToken(String token, String title, String body) {
    return FcmMessageRequest.builder()
        .message(
            Message.builder()
                .token(token)
                .notification(Notification.builder().title(title).body(body).build())
                .build())
        .build();
  }

  /** Create a data-only message to a device token. */
  public static FcmMessageRequest dataToToken(String token, Map<String, String> data) {
    return FcmMessageRequest.builder()
        .message(Message.builder().token(token).data(data).build())
        .build();
  }

  /** Create a notification message to a topic. */
  public static FcmMessageRequest toTopic(String topic, String title, String body) {
    return FcmMessageRequest.builder()
        .message(
            Message.builder()
                .topic(topic)
                .notification(Notification.builder().title(title).body(body).build())
                .build())
        .build();
  }
}
