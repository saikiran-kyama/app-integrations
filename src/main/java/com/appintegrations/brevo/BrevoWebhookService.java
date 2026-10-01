package com.appintegrations.brevo;

import com.appintegrations.brevo.dto.BrevoWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for processing Brevo webhook events.
 *
 * <p>This service parses incoming webhook payloads and dispatches them to registered {@link
 * BrevoWebhookHandler} implementations.
 *
 * <p>To use this service:
 *
 * <ol>
 *   <li>Create a controller endpoint that receives POST requests from Brevo
 *   <li>Parse the request body and call {@link #processWebhook(BrevoWebhookEvent)}
 *   <li>Implement {@link BrevoWebhookHandler} in your application to handle events
 * </ol>
 *
 * <p>Example controller:
 *
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/webhooks")
 * public class WebhookController {
 *
 *   private final BrevoWebhookService webhookService;
 *
 *   &#64;PostMapping("/brevo")
 *   public ResponseEntity&lt;Void&gt; handleBrevoWebhook(&#64;RequestBody BrevoWebhookEvent event) {
 *     webhookService.processWebhook(event);
 *     return ResponseEntity.ok().build();
 *   }
 * }
 * </pre>
 */
@Service
@Slf4j
public class BrevoWebhookService {

  private final ObjectMapper objectMapper;
  private final List<BrevoWebhookHandler> handlers;
  private final BrevoProperties properties;

  /**
   * Constructor.
   *
   * @param objectMapper JSON mapper
   * @param handlers List of webhook handlers (can be empty)
   * @param properties Brevo configuration
   */
  @Autowired
  public BrevoWebhookService(
      ObjectMapper objectMapper,
      @Autowired(required = false) List<BrevoWebhookHandler> handlers,
      BrevoProperties properties) {
    this.objectMapper = objectMapper;
    this.handlers = handlers != null ? handlers : List.of();
    this.properties = properties;

    if (this.handlers.isEmpty()) {
      log.info(
          "No BrevoWebhookHandler implementations found. "
              + "Implement BrevoWebhookHandler to process webhook events.");
    } else {
      log.info("Found {} BrevoWebhookHandler implementation(s)", this.handlers.size());
    }
  }

  /**
   * Process a webhook event from Brevo.
   *
   * @param event The parsed webhook event
   */
  public void processWebhook(BrevoWebhookEvent event) {
    if (event == null) {
      log.warn("Received null webhook event");
      return;
    }

    log.info(
        "Processing Brevo webhook: event={}, email={}, messageId={}",
        event.getEvent(),
        event.getEmail(),
        event.getMessageId());

    if (handlers.isEmpty()) {
      log.debug("No handlers configured for webhook events");
      return;
    }

    String eventType = Optional.ofNullable(event.getEvent()).orElse("").toLowerCase();

    for (BrevoWebhookHandler handler : handlers) {
      try {
        switch (eventType) {
          case "sent" -> handler.onSent(event);
          case "delivered" -> handler.onDelivered(event);
          case "opened", "unique_opened" -> handler.onOpened(event);
          case "clicked" -> handler.onClicked(event);
          case "soft_bounce", "hard_bounce", "blocked" -> handler.onBounced(event);
          case "unsubscribed" -> handler.onUnsubscribed(event);
          case "complaint", "spam" -> handler.onComplaint(event);
          case "error", "invalid_email" -> handler.onError(event);
          case "deferred" -> handler.onDeferred(event);
          default -> handler.onOtherEvent(event);
        }
      } catch (Exception e) {
        log.error(
            "Error in webhook handler {} processing event {}: {}",
            handler.getClass().getSimpleName(),
            eventType,
            e.getMessage(),
            e);
      }
    }
  }

  /**
   * Parse a raw JSON webhook payload into an event object.
   *
   * @param jsonPayload Raw JSON string from Brevo
   * @return Parsed event or null if parsing fails
   */
  public BrevoWebhookEvent parseWebhook(String jsonPayload) {
    if (jsonPayload == null || jsonPayload.isBlank()) {
      log.warn("Received empty webhook payload");
      return null;
    }

    try {
      return objectMapper.readValue(jsonPayload, BrevoWebhookEvent.class);
    } catch (Exception e) {
      log.error("Failed to parse Brevo webhook payload: {}", e.getMessage());
      log.debug("Raw payload: {}", jsonPayload);
      return null;
    }
  }

  /**
   * Parse and process a raw JSON webhook payload.
   *
   * @param jsonPayload Raw JSON string from Brevo
   */
  public void processRawWebhook(String jsonPayload) {
    BrevoWebhookEvent event = parseWebhook(jsonPayload);
    if (event != null) {
      processWebhook(event);
    }
  }
}
