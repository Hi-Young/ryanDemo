package com.ryan.es.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsHttpResponse {

    /**
     * HTTP status code; -1 means transport/runtime error (no HTTP response).
     */
    private int statusCode;

    /**
     * Raw response body (JSON string on success/error), or exception message when statusCode = -1.
     */
    private String body;

    public boolean is2xx() {
        return statusCode >= 200 && statusCode < 300;
    }
}

