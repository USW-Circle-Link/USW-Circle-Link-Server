package com.USWCicrcleLink.server.notification.outbox;

import com.google.api.client.http.*;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.*;
import org.springframework.web.client.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Bounds OAuth network requests as well as FCM requests; Outbox owns retries. */
public class OutboxGoogleTokenProvider implements OutboxFcmClient.TokenProvider {
    private final Path key;
    private final HttpTransport transport;

    public OutboxGoogleTokenProvider(Path key, int connectTimeoutMs, int readTimeoutMs) {
        this.key = key;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        var http = new RestTemplate(factory);
        http.setErrorHandler(new ResponseErrorHandler() {
            public boolean hasError(ClientHttpResponse response) { return false; }
            public void handleError(ClientHttpResponse response) { }
        });
        transport = new HttpTransport() {
            @Override protected LowLevelHttpRequest buildRequest(String method, String url) {
                return new LowLevelHttpRequest() {
                    final HttpHeaders headers = new HttpHeaders();
                    public void addHeader(String name, String value) { headers.add(name, value); }
                    public LowLevelHttpResponse execute() throws IOException {
                        var bytes = new ByteArrayOutputStream();
                        if (getStreamingContent() != null) getStreamingContent().writeTo(bytes);
                        if (getContentType() != null) headers.set(HttpHeaders.CONTENT_TYPE, getContentType());
                        if (getContentEncoding() != null) headers.set(HttpHeaders.CONTENT_ENCODING, getContentEncoding());
                        try {
                            var response = http.exchange(url, HttpMethod.valueOf(method),
                                    new org.springframework.http.HttpEntity<>(bytes.toByteArray(), headers), byte[].class);
                            byte[] body = response.getBody() == null ? new byte[0] : response.getBody();
                            var entries = new ArrayList<Map.Entry<String,String>>();
                            response.getHeaders().forEach((name, values) -> values.forEach(value -> entries.add(Map.entry(name, value))));
                            return new LowLevelHttpResponse() {
                                public InputStream getContent() { return new ByteArrayInputStream(body); }
                                public String getContentEncoding() { return response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING); }
                                public long getContentLength() { return body.length; }
                                public String getContentType() { return response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE); }
                                public String getStatusLine() { return "HTTP/1.1 " + getStatusCode() + " " + getReasonPhrase(); }
                                public int getStatusCode() { return response.getStatusCode().value(); }
                                public String getReasonPhrase() { var status = HttpStatus.resolve(getStatusCode()); return status == null ? "" : status.getReasonPhrase(); }
                                public int getHeaderCount() { return entries.size(); }
                                public String getHeaderName(int index) { return entries.get(index).getKey(); }
                                public String getHeaderValue(int index) { return entries.get(index).getValue(); }
                            };
                        } catch (RestClientException error) {
                            throw new IOException("OAuth transport failed", error);
                        }
                    }
                };
            }
        };
    }

    @Override public String get() throws IOException {
        com.google.auth.oauth2.GoogleCredentials credentials;
        try (var input = Files.newInputStream(key)) {
            credentials = ServiceAccountCredentials.fromStream(input, () -> transport)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"))
                    .createWithCustomRetryStrategy(false);
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
