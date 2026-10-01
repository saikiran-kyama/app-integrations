# app-integrations

Centralized Spring Boot auto-configuration library for Email, SMS, and WhatsApp messaging. Integrations include brevo email & sms, firebase, msg91 email & sms, whatsapp

---

## Table of Contents

- [Setup](#setup)
- [Application Properties](#application-properties)
  - [Brevo (Email & SMS)](#brevo-email--sms-properties)
  - [WhatsApp](#whatsapp-properties)
- [Brevo Email](#brevo-email)
- [Brevo SMS](#brevo-sms)
- [WhatsApp](#whatsapp)

---

## Setup

Add the library as a dependency. The services are auto-configured — just add the properties to your `application.yaml` and inject the service beans.

---

## Application Properties

### Brevo (Email & SMS) Properties

```yaml
integrations:
  brevo:
    enabled: true                          # Enable/disable all Brevo services (default: true)
    api-key: YOUR_BREVO_API_KEY            # Brevo API key (from brevo.com → Settings → API Keys)
    api-url: https://api.brevo.com/v3      # (optional) API base URL
    connect-timeout: 5000                  # (optional) Connection timeout ms (default: 5000)
    read-timeout: 10000                    # (optional) Read timeout ms (default: 10000)

    sender:
      email: noreply@yourdomain.com        # Default sender email address
      name: Your App Name                  # Default sender display name

    sms:
      enabled: true                        # Enable/disable SMS (default: true)
      sender: YourApp                      # SMS sender name, max 11 alphanumeric chars
      api-url: https://api.brevo.com/v3/transactionalSMS/sms  # (optional)

    smtp:
      enabled: true                        # Enable/disable SMTP fallback (default: true)
      host: smtp-relay.brevo.com           # (optional, default shown)
      port: 587                            # (optional, default: 587)
      login: YOUR_BREVO_LOGIN_EMAIL        # Your Brevo account email
      smtp-key: YOUR_BREVO_SMTP_KEY        # Brevo SMTP key (Settings → SMTP & API → SMTP)
      tls: true                            # (optional, default: true)
```

### WhatsApp Properties

```yaml
integrations:
  whatsapp:
    enabled: true                          # Enable/disable WhatsApp integration (default: false)
    access-token: YOUR_ACCESS_TOKEN        # System User access token from Meta Business Manager
    phone-number-id: YOUR_PHONE_NUMBER_ID  # Phone Number ID from WhatsApp API Setup page
    business-account-id: YOUR_WABA_ID      # (optional) WhatsApp Business Account ID
    api-url: https://graph.facebook.com/v18.0  # (optional) Graph API base URL
    connect-timeout: 5000                  # (optional) Connection timeout ms (default: 5000)
    read-timeout: 10000                    # (optional) Read timeout ms (default: 10000)
    default-language: en                   # (optional) Default template language (default: en)
```

---

## Brevo Email

### Imports

```java
import com.appintegrations.brevo.BrevoEmailService;
import com.appintegrations.brevo.dto.BrevoEmailRequest;
import com.appintegrations.brevo.dto.BrevoEmailResponse;
import com.appintegrations.brevo.dto.EmailRecipient;
import java.util.List;
```

### Inject the Bean

```java
@Autowired
private BrevoEmailService brevoEmailService;
```

### Methods

| Method | Description |
|--------|-------------|
| `sendEmail(String toEmail, String subject, String htmlContent)` | Send a simple HTML email to one recipient |
| `sendEmail(String toEmail, String toName, String subject, String htmlContent)` | Send with recipient display name |
| `sendEmail(BrevoEmailRequest request)` | Full control — CC, BCC, reply-to, template, tags |
| `sendEmailAsync(String toEmail, String subject, String htmlContent)` | Async variant, returns `CompletableFuture<BrevoEmailResponse>` |
| `sendEmailViaSmtp(BrevoEmailRequest request)` | Send via SMTP relay instead of API |
| `isEnabled()` | Returns `true` if API is configured and enabled |
| `isSmtpEnabled()` | Returns `true` if SMTP is configured and enabled |

---

### Example 1 — Simple Email

```java
BrevoEmailResponse response = brevoEmailService.sendEmail(
    "user@example.com",
    "Welcome to Our App!",
    "<h1>Hello!</h1><p>Thanks for signing up.</p>"
);

if (response.isSuccess()) {
    System.out.println("Sent! MessageId: " + response.getMessageId());
} else {
    System.out.println("Error: " + response.getMessage());
}
```

**Response fields:**
```
messageId  → "<201811050339.15787.46543@smtp-relay.brevo.com>"
code       → null (null means no error)
message    → null
isSuccess()  → true
isError()    → false
```

---

### Example 2 — Email with Name

```java
BrevoEmailResponse response = brevoEmailService.sendEmail(
    "user@example.com",
    "John Doe",
    "Your Order Confirmation",
    "<p>Your order #1234 has been placed.</p>"
);
```

---

### Example 3 — Full Request (CC, BCC, Reply-To, Tags)

```java
BrevoEmailRequest request = BrevoEmailRequest.builder()
    .sender(EmailRecipient.of("noreply@yourdomain.com", "Your App"))
    .to(List.of(
        EmailRecipient.of("user@example.com", "John Doe")
    ))
    .cc(List.of(
        EmailRecipient.of("manager@yourdomain.com")
    ))
    .bcc(List.of(
        EmailRecipient.of("archive@yourdomain.com")
    ))
    .replyTo(EmailRecipient.of("support@yourdomain.com"))
    .subject("Invoice #5678")
    .htmlContent("<p>Please find your invoice attached.</p>")
    .textContent("Please find your invoice attached.")  // plain-text fallback
    .tags(List.of("invoice", "transactional"))
    .build();

BrevoEmailResponse response = brevoEmailService.sendEmail(request);
```

---

### Example 4 — Template Email

```java
import java.util.Map;

BrevoEmailRequest request = BrevoEmailRequest.builder()
    .to(List.of(EmailRecipient.of("user@example.com", "John")))
    .templateId(12L)           // Template ID from Brevo dashboard
    .params(Map.of(
        "FIRSTNAME", "John",
        "ORDER_ID",  "9876"
    ))
    .build();

BrevoEmailResponse response = brevoEmailService.sendEmail(request);
```

---

### Example 5 — Async Email

```java
brevoEmailService.sendEmailAsync("user@example.com", "Hello!", "<p>Hi there</p>")
    .thenAccept(response -> {
        if (response.isSuccess()) {
            System.out.println("Sent async! MessageId: " + response.getMessageId());
        }
    });
```

---

### Example 6 — SMTP Send

```java
BrevoEmailRequest request = BrevoEmailRequest.builder()
    .to(List.of(EmailRecipient.of("user@example.com")))
    .subject("SMTP Test")
    .htmlContent("<p>Sent via SMTP relay</p>")
    .build();

BrevoEmailResponse response = brevoEmailService.sendEmailViaSmtp(request);
```

---

### BrevoEmailResponse Fields

```java
response.getMessageId()          // String — message ID on success, null on failure
response.getCode()               // String — Brevo error code, null on success
response.getMessage()            // String — error description, null on success
response.getRateLimitLimit()     // String — total requests allowed in window
response.getRateLimitRemaining() // String — requests remaining in current window
response.getRateLimitReset()     // String — epoch seconds when limit resets
response.isSuccess()             // boolean — true when messageId is present
response.isError()               // boolean — true when code is present or message with no ID
```

---

## Brevo SMS

### Imports

```java
import com.appintegrations.brevo.BrevoSmsService;
import com.appintegrations.brevo.dto.BrevoSmsRequest;
import com.appintegrations.brevo.dto.BrevoSmsResponse;
```

### Inject the Bean

```java
@Autowired
private BrevoSmsService brevoSmsService;
```

### Methods

| Method | Description |
|--------|-------------|
| `sendSms(String phoneNumber, String content)` | Send a plain SMS |
| `sendSms(String phoneNumber, String content, String tag)` | Send SMS with a tag label |
| `sendSms(BrevoSmsRequest request)` | Full control over the request |
| `sendSmsAsync(String phoneNumber, String content)` | Async variant, returns `CompletableFuture<BrevoSmsResponse>` |
| `sendOtp(String phoneNumber, String otp)` | Convenience method — wraps OTP in standard message text |
| `isEnabled()` | Returns `true` if SMS is configured and enabled |

---

### Example 1 — Simple SMS

```java
// Phone number must include country code with + prefix, e.g. "+919876543210"
BrevoSmsResponse response = brevoSmsService.sendSms(
    "+919876543210",
    "Your appointment is confirmed for 10 AM tomorrow."
);

if (response.isSuccess()) {
    System.out.println("SMS sent! MessageId: " + response.getMessageId());
} else {
    System.out.println("Error: " + response.getMessage());
}
```

**Response fields:**
```
messageId        → 12345678 (Long)
smsCount         → 1
usedCredits      → 0.5
remainingCredits → 99.5
isSuccess()      → true
```

---

### Example 2 — SMS with Tag

```java
BrevoSmsResponse response = brevoSmsService.sendSms(
    "+919876543210",
    "Your order #1234 has been shipped.",
    "ORDER_UPDATE"
);
```

---

### Example 3 — OTP SMS

```java
BrevoSmsResponse response = brevoSmsService.sendOtp("+919876543210", "847291");
// Sends: "Your verification code is: 847291. This code expires in 10 minutes."
```

---

### Example 4 — Full Request

```java
BrevoSmsRequest request = BrevoSmsRequest.builder()
    .sender("YourApp")              // overrides default sender from properties
    .recipient("+919876543210")
    .content("Flash sale! 50% off today only.")
    .type("transactional")          // "transactional" or "marketing"
    .tag("PROMO")
    .webUrl("https://yourdomain.com/sale")  // optional delivery report URL
    .build();

BrevoSmsResponse response = brevoSmsService.sendSms(request);
```

---

### Example 5 — Async SMS

```java
brevoSmsService.sendSmsAsync("+919876543210", "Hello via async!")
    .thenAccept(response -> {
        if (response.isSuccess()) {
            System.out.println("SMS sent! Id: " + response.getMessageIdAsString());
        }
    });
```

---

### BrevoSmsResponse Fields

```java
response.getMessageId()            // Long — message ID on success
response.getMessageIdAsString()    // String — messageId as String (convenience)
response.getSmsCount()             // Integer — number of SMS parts sent
response.getUsedCredits()          // Double — credits deducted
response.getRemainingCredits()     // Double — credits left in account
response.getReference()            // String — your custom reference if set
response.getCode()                 // String — Brevo error code, null on success
response.getMessage()              // String — error description, null on success
response.getRateLimitLimit()       // String — total requests allowed in window
response.getRateLimitRemaining()   // String — requests remaining
response.getRateLimitReset()       // String — epoch seconds when limit resets
response.isSuccess()               // boolean — true when messageId is present
response.isError()                 // boolean — true on error
```

---

## WhatsApp

### Imports

```java
import com.appintegrations.whatsapp.WhatsAppService;
import com.appintegrations.whatsapp.dto.WhatsAppMessageRequest;
import com.appintegrations.whatsapp.dto.WhatsAppMessageResponse;
import java.util.List;
```

### Inject the Bean

```java
@Autowired
private WhatsAppService whatsAppService;
```

### Methods

| Method | Description |
|--------|-------------|
| `sendTextMessage(String to, String message)` | Send a plain text WhatsApp message |
| `sendTemplateMessage(String to, String templateName, String languageCode)` | Send a registered template message |
| `sendTemplateMessage(String to, String templateName, String languageCode, List<Component> components)` | Template with dynamic parameters |
| `sendMessage(WhatsAppMessageRequest request)` | Full control over the API request |
| `sendTextMessageAsync(String to, String message)` | Async text message, returns `CompletableFuture<WhatsAppMessageResponse>` |
| `sendTemplateMessageAsync(String to, String templateName, String languageCode)` | Async template message |
| `isEnabled()` | Returns `true` if token and phone number ID are configured |

> **Phone number format:** Pass numbers **without** the `+` prefix, with country code — e.g. `"919876543210"`.  
> The `+` is stripped automatically if provided.

---

### Example 1 — Simple Text Message

```java
WhatsAppMessageResponse response = whatsAppService.sendTextMessage(
    "919876543210",
    "Hello! Your order has been confirmed."
);

if (response.isSuccess()) {
    System.out.println("Sent! MessageId: " + response.getMessageId());
} else {
    System.out.println("Error: " + response.getErrorMessage());
}
```

**Response fields:**
```
messagingProduct → "whatsapp"
contacts[0].waId → "919876543210"
messages[0].id   → "wamid.HBgNOTE4..."
isSuccess()      → true
```

---

### Example 2 — Template Message (no parameters)

```java
// Uses the built-in "hello_world" template approved by Meta
WhatsAppMessageResponse response = whatsAppService.sendTemplateMessage(
    "919876543210",
    "hello_world",
    "en"
);
```

---

### Example 3 — Template Message with Body Parameters

```java
// Template body: "Your OTP is {{1}}. It expires in {{2}} minutes."
List<WhatsAppMessageRequest.Parameter> bodyParams = List.of(
    WhatsAppMessageRequest.Parameter.builder().type("text").text("847291").build(),
    WhatsAppMessageRequest.Parameter.builder().type("text").text("10").build()
);

WhatsAppMessageRequest.Component bodyComponent = WhatsAppMessageRequest.Component.builder()
    .type("body")
    .parameters(bodyParams)
    .build();

WhatsAppMessageResponse response = whatsAppService.sendTemplateMessage(
    "919876543210",
    "otp_verification",
    "en",
    List.of(bodyComponent)
);
```

---

### Example 4 — Template with Header + Body + Button

```java
// Header image
WhatsAppMessageRequest.Component header = WhatsAppMessageRequest.Component.builder()
    .type("header")
    .parameters(List.of(
        WhatsAppMessageRequest.Parameter.builder()
            .type("image")
            .image(WhatsAppMessageRequest.MediaObject.builder()
                .link("https://yourdomain.com/banner.png")
                .build())
            .build()
    ))
    .build();

// Body text params
WhatsAppMessageRequest.Component body = WhatsAppMessageRequest.Component.builder()
    .type("body")
    .parameters(List.of(
        WhatsAppMessageRequest.Parameter.builder().type("text").text("John").build(),
        WhatsAppMessageRequest.Parameter.builder().type("text").text("#5678").build()
    ))
    .build();

// Quick reply button at index 0
WhatsAppMessageRequest.Component button = WhatsAppMessageRequest.Component.builder()
    .type("button")
    .subType("quick_reply")
    .index(0)
    .parameters(List.of(
        WhatsAppMessageRequest.Parameter.builder().type("text").text("CONFIRM_5678").build()
    ))
    .build();

WhatsAppMessageResponse response = whatsAppService.sendTemplateMessage(
    "919876543210",
    "order_confirmation",
    "en",
    List.of(header, body, button)
);
```

---

### Example 5 — Full Custom Request

```java
WhatsAppMessageRequest request = WhatsAppMessageRequest.builder()
    .to("919876543210")
    .type("text")
    .text(WhatsAppMessageRequest.TextContent.builder()
        .body("Hello from a custom request!")
        .previewUrl(false)
        .build())
    .build();

WhatsAppMessageResponse response = whatsAppService.sendMessage(request);
```

---

### Example 6 — Async Text Message

```java
whatsAppService.sendTextMessageAsync("919876543210", "Async hello!")
    .thenAccept(response -> {
        if (response.isSuccess()) {
            System.out.println("Sent! Id: " + response.getMessageId());
        } else {
            System.out.println("Failed: " + response.getErrorMessage());
        }
    });
```

---

### WhatsAppMessageResponse Fields

```java
response.getMessagingProduct()              // String — always "whatsapp"
response.getContacts()                      // List<Contact> — recipient info
response.getContacts().get(0).getWaId()     // String — WhatsApp ID of recipient
response.getContacts().get(0).getInput()    // String — the number you sent to
response.getMessages()                      // List<Message> — sent message info
response.getMessages().get(0).getId()       // String — WhatsApp message ID (wamid.xxx)
response.getMessageId()                     // String — shortcut to messages[0].id
response.getError()                         // Error — null on success
response.getError().getMessage()            // String — error description
response.getError().getCode()               // Integer — Meta error code
response.getErrorMessage()                  // String — shortcut to error.message
response.isSuccess()                        // boolean — true when messages list is non-empty and no error
response.isError()                          // boolean — true when error is present
```
