package com.appintegrations;

import com.appintegrations.brevo.BrevoEmailService;
import com.appintegrations.brevo.BrevoProperties;
import com.appintegrations.brevo.BrevoSmsService;
import com.appintegrations.brevo.BrevoWebhookHandler;
import com.appintegrations.brevo.BrevoWebhookService;
import com.appintegrations.firebase.FirebaseProperties;
import com.appintegrations.firebase.FirebasePushService;
import com.appintegrations.msg91.email.Msg91EmailService;
import com.appintegrations.msg91.sms.Msg91Properties;
import com.appintegrations.msg91.sms.Msg91ReportService;
import com.appintegrations.msg91.sms.Msg91SmsService;
import com.appintegrations.msg91.sms.Msg91TemplateService;
import com.appintegrations.whatsapp.WhatsAppProperties;
import com.appintegrations.whatsapp.WhatsAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for all integration services.
 *
 * <p>This class automatically configures the integration services when the library is included as a
 * dependency and the necessary configuration properties are provided.
 *
 * <p>Available integrations:
 *
 * <ul>
 *   <li>Brevo (Email & SMS) - {@code integrations.brevo.enabled=true}
 *   <li>WhatsApp - {@code integrations.whatsapp.enabled=true}
 *   <li>Firebase (Push Notifications) - {@code integrations.firebase.enabled=true}
 *   <li>MSG91 (SMS) - {@code integrations.msg91.enabled=true}
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties({
  BrevoProperties.class,
  WhatsAppProperties.class,
  FirebaseProperties.class,
  Msg91Properties.class
})
public class IntegrationsAutoConfiguration {

  // ==================== Brevo Services ====================

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "integrations.brevo",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public BrevoEmailService brevoEmailService(BrevoProperties properties) {
    return new BrevoEmailService(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "integrations.brevo",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public BrevoSmsService brevoSmsService(BrevoProperties properties) {
    return new BrevoSmsService(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(
      prefix = "integrations.brevo.webhook",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public BrevoWebhookService brevoWebhookService(
      ObjectMapper objectMapper,
      @Autowired(required = false) List<BrevoWebhookHandler> handlers,
      BrevoProperties properties) {
    return new BrevoWebhookService(objectMapper, handlers, properties);
  }

  // ==================== WhatsApp Services ====================

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.whatsapp", name = "enabled", havingValue = "true")
  public WhatsAppService whatsAppService(WhatsAppProperties properties, ObjectMapper objectMapper) {
    return new WhatsAppService(properties, objectMapper);
  }

  // ==================== Firebase Services ====================

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.firebase", name = "enabled", havingValue = "true")
  public FirebasePushService firebasePushService(
      FirebaseProperties properties, ObjectMapper objectMapper) {
    return new FirebasePushService(properties, objectMapper);
  }

  // ==================== MSG91 Services ====================

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.msg91", name = "enabled", havingValue = "true")
  public Msg91SmsService msg91SmsService(Msg91Properties properties, ObjectMapper objectMapper) {
    return new Msg91SmsService(properties, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.msg91", name = "enabled", havingValue = "true")
  public Msg91TemplateService msg91TemplateService(
      Msg91Properties properties, ObjectMapper objectMapper) {
    return new Msg91TemplateService(properties, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.msg91", name = "enabled", havingValue = "true")
  public Msg91ReportService msg91ReportService(
      Msg91Properties properties, ObjectMapper objectMapper) {
    return new Msg91ReportService(properties, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "integrations.msg91", name = "enabled", havingValue = "true")
  public Msg91EmailService msg91EmailService(
      Msg91Properties properties, ObjectMapper objectMapper) {
    return new Msg91EmailService(properties, objectMapper);
  }

  // ==================== Shared Beans ====================

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper integrationsObjectMapper() {
    return new ObjectMapper();
  }
}
