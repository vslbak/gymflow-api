package com.gymflow.api.service;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripePaymentService {

    @Value("${frontend.base.url:http://localhost:5173}")
    private String frontendBase;

    @Value("${gymflow.payment.currency:EUR}")
    private String currency;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    public Session createCheckoutSession(UUID bookingId, String className, BigDecimal amount) throws StripeException {

        Map<String, String> meta = new HashMap<>();
        meta.put("bookingId", bookingId.toString());

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(frontendBase + "/booking/success?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(frontendBase + "/booking/cancel?session_id={CHECKOUT_SESSION_ID}")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(currency)
                                                        .setUnitAmount(getAmountInCents(amount))
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(className)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.AUTOMATIC)
                                        .putAllMetadata(meta)
                                        .build()
                        )
                        .build();

        return Session.create(params);
    }
    public Optional<PaymentResponse> handleWebhookEvent(String payload, String sigHeader) throws  StripeException {

        Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        log.info("Stripe event type: {}", event.getType());

        return switch (event.getType()) {

            case "checkout.session.completed" -> {
                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                Session session = (Session) deserializer.getObject().orElse(event.getData().getObject());

                String piId = session.getPaymentIntent();
                PaymentIntent pi = PaymentIntent.retrieve(piId);

                String bookingIdRaw = pi.getMetadata().get("bookingId");
                UUID bookingId = bookingIdRaw != null ? UUID.fromString(bookingIdRaw) : null;

                log.info("Payment succeeded for PI {} (booking {})", piId, bookingId);

                yield Optional.of(new PaymentResponse(
                        PaymentResponse.PaymentResponseStatus.SUCCESS,
                        piId,
                        bookingId
                ));
            }

            case "payment_intent.payment_failed" -> {
                log.warn("Payment failed event received: {}", event.getId());
                // you can later look up Booking by PI if you want
                yield Optional.of(new PaymentResponse(
                        PaymentResponse.PaymentResponseStatus.FAILURE,
                        null,
                        null
                ));
            }

            default -> {
                log.debug("Ignoring Stripe event type {}", event.getType());
                yield Optional.empty(); // or an IGNORE status
            }
        };
    }

    private long getAmountInCents(BigDecimal price) {
         return price.multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    public record PaymentResponse(PaymentResponseStatus status, String paymentIntentId, UUID bookingId) {
        public enum PaymentResponseStatus{SUCCESS, FAILURE}
    }
}


