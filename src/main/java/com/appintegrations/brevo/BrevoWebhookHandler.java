package com.appintegrations.brevo;

import com.appintegrations.brevo.dto.BrevoWebhookEvent;

/**
 * Handler interface for processing Brevo webhook events.
 *
 * <p>Implement this interface in your application to handle email and SMS delivery status updates
 * from Brevo.
 *
 * <p>Example implementation:
 *
 * <pre>
 * &#64;Component
 * public class MyBrevoWebhookHandler implements BrevoWebhookHandler {
 *
 *   &#64;Override
 *   public void onDelivered(BrevoWebhookEvent event) {
 *     log.info("Email delivered to {}", event.getEmail());
 *     // Update your database
 *   }
 *
 *   &#64;Override
 *   public void onBounced(BrevoWebhookEvent event) {
 *     log.warn("Email bounced for {}: {}", event.getEmail(), event.getReason());
 *     // Mark contact as invalid
 *   }
 * }
 * </pre>
 */
public interface BrevoWebhookHandler {

  /**
   * Called when an email/SMS is sent by Brevo (not yet delivered).
   *
   * @param event The webhook event details
   */
  default void onSent(BrevoWebhookEvent event) {}

  /**
   * Called when an email is successfully delivered.
   *
   * @param event The webhook event details
   */
  default void onDelivered(BrevoWebhookEvent event) {}

  /**
   * Called when an email is opened by the recipient.
   *
   * @param event The webhook event details
   */
  default void onOpened(BrevoWebhookEvent event) {}

  /**
   * Called when a link in the email is clicked.
   *
   * @param event The webhook event details
   */
  default void onClicked(BrevoWebhookEvent event) {}

  /**
   * Called when an email bounces (soft or hard bounce).
   *
   * @param event The webhook event details
   */
  default void onBounced(BrevoWebhookEvent event) {}

  /**
   * Called when a recipient unsubscribes.
   *
   * @param event The webhook event details
   */
  default void onUnsubscribed(BrevoWebhookEvent event) {}

  /**
   * Called when a recipient marks the email as spam.
   *
   * @param event The webhook event details
   */
  default void onComplaint(BrevoWebhookEvent event) {}

  /**
   * Called when there's an error sending the email.
   *
   * @param event The webhook event details
   */
  default void onError(BrevoWebhookEvent event) {}

  /**
   * Called when email is deferred (temporary failure, will retry).
   *
   * @param event The webhook event details
   */
  default void onDeferred(BrevoWebhookEvent event) {}

  /**
   * Called for any event not handled by specific methods.
   *
   * @param event The webhook event details
   */
  default void onOtherEvent(BrevoWebhookEvent event) {}
}
