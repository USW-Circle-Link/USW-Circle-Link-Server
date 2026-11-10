package com.USWCicrcleLink.server.notification.outbox;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class OutboxFcmClient implements OutboxSender {
    @FunctionalInterface
    public interface TokenProvider { String get() throws IOException; }
    private final RestTemplate http;
    private final String url;
    private final TokenProvider tokens;

    public OutboxFcmClient(String url, TokenProvider tokens, int connectTimeoutMs, int readTimeoutMs) {
        this.url = url;
        this.tokens = tokens;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        http = new RestTemplate(factory);
    }

    @Override public Outcome send(OutboxWorkerStore.Delivery task) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(tokens.get());
            var payload = Map.of("message", Map.of("token", task.token(),
                    "notification", Map.of("title", task.title(), "body", task.body())));
            var response = http.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
            return response.getStatusCode().value() == 200 ? Outcome.sent() : Outcome.retry("UNEXPECTED_HTTP_STATUS", null);
        } catch (RestClientResponseException error) {
            int status = error.getStatusCode().value();
            if (status == 429 || status >= 500 || status == 401 || status == 403) {
                String after = error.getResponseHeaders() == null ? null : error.getResponseHeaders().getFirst("Retry-After");
                return Outcome.retry("HTTP_" + status, retryAfter(after));
            }
            return Outcome.failed("HTTP_" + status);
        } catch (IOException error) {
            return Outcome.retry("AUTH_IO", null);
        } catch (RestClientException error) {
            return Outcome.retry("NETWORK_OR_TIMEOUT", null);
        }
    }

    private Duration retryAfter(String value) {
        if (value == null) return null;
        try {
            long seconds;
            try { seconds = Long.parseLong(value.trim()); }
            catch (NumberFormatException ignored) {
                seconds = Duration.between(Instant.now(), ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()).getSeconds() + 1;
            }
            return seconds >= 0 && seconds <= 86400 ? Duration.ofSeconds(seconds) : null;
        } catch (RuntimeException invalid) { return null; }
    }
}
