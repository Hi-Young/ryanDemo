package com.ryan.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ES demo configuration (coupon search oriented).
 *
 * <p>Keep it simple/portable: the demo uses Elasticsearch REST API via HTTP.</p>
 */
@Component
@ConfigurationProperties(prefix = "demo.elasticsearch")
public class EsDemoProperties {

    /**
     * Example: http://127.0.0.1:9200
     */
    private String baseUrl = "http://127.0.0.1:9200";

    /**
     * Optional; set when ES enables basic auth.
     */
    private String username;

    /**
     * Optional; set when ES enables basic auth.
     */
    private String password;

    private int connectTimeoutMs = 2000;

    private int readTimeoutMs = 5000;

    /**
     * Coupon index name.
     */
    private String couponIndex = "coupon_demo_v1";

    /**
     * Mapping preset: standard|ik (ik requires IK plugin on ES).
     */
    private String couponIndexMapping = "standard";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getCouponIndex() {
        return couponIndex;
    }

    public void setCouponIndex(String couponIndex) {
        this.couponIndex = couponIndex;
    }

    public String getCouponIndexMapping() {
        return couponIndexMapping;
    }

    public void setCouponIndexMapping(String couponIndexMapping) {
        this.couponIndexMapping = couponIndexMapping;
    }
}

