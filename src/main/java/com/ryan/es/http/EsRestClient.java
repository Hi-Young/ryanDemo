package com.ryan.es.http;

import com.ryan.es.config.EsDemoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * A tiny ES HTTP client for demo purpose (keeps dependencies stable across ES versions).
 */
@Slf4j
@Component
public class EsRestClient {

    private final EsDemoProperties properties;
    private final RestTemplate restTemplate;

    public EsRestClient(EsDemoProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    public EsHttpResponse exchange(HttpMethod method, String path, String body, MediaType contentType) {
        String url = buildUrl(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (contentType != null) {
            headers.setContentType(withUtf8(contentType));
        }
        if (StringUtils.hasText(properties.getUsername())) {
            headers.setBasicAuth(
                    properties.getUsername(),
                    properties.getPassword() == null ? "" : properties.getPassword(),
                    StandardCharsets.UTF_8
            );
        }

        // RestTemplate's default StringHttpMessageConverter uses ISO-8859-1; that breaks Chinese text.
        // Use raw bytes to make request/response encoding explicit and stable across environments.
        byte[] payload = body == null ? null : body.getBytes(StandardCharsets.UTF_8);
        HttpEntity<byte[]> entity = payload == null ? new HttpEntity<>(headers) : new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(url, method, entity, byte[].class);
            String respBody = resp.getBody() == null ? null : new String(resp.getBody(), StandardCharsets.UTF_8);
            return new EsHttpResponse(resp.getStatusCodeValue(), respBody);
        } catch (HttpStatusCodeException e) {
            // ES returns useful JSON on 4xx/5xx; keep it.
            byte[] bytes = e.getResponseBodyAsByteArray();
            String errBody = bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
            return new EsHttpResponse(e.getRawStatusCode(), errBody);
        } catch (RestClientException e) {
            log.warn("ES request failed: {} {} - {}", method, url, e.getMessage());
            return new EsHttpResponse(-1, e.getMessage());
        }
    }

    public EsHttpResponse get(String path) {
        return exchange(HttpMethod.GET, path, null, null);
    }

    public EsHttpResponse head(String path) {
        return exchange(HttpMethod.HEAD, path, null, null);
    }

    public EsHttpResponse delete(String path) {
        return exchange(HttpMethod.DELETE, path, null, null);
    }

    public EsHttpResponse putJson(String path, String json) {
        return exchange(HttpMethod.PUT, path, json, MediaType.APPLICATION_JSON);
    }

    public EsHttpResponse postJson(String path, String json) {
        return exchange(HttpMethod.POST, path, json, MediaType.APPLICATION_JSON);
    }

    public EsHttpResponse postNdjson(String path, String ndjson) {
        return exchange(HttpMethod.POST, path, ndjson, MediaType.parseMediaType("application/x-ndjson"));
    }

    private static MediaType withUtf8(MediaType contentType) {
        if (contentType == null || contentType.getCharset() != null) {
            return contentType;
        }
        return new MediaType(contentType, StandardCharsets.UTF_8);
    }

    private String buildUrl(String path) {
        String base = StringUtils.hasText(properties.getBaseUrl())
                ? properties.getBaseUrl().trim()
                : "http://127.0.0.1:9200";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!StringUtils.hasText(path)) {
            return base;
        }
        if (path.startsWith("/")) {
            return base + path;
        }
        return base + "/" + path;
    }
}
