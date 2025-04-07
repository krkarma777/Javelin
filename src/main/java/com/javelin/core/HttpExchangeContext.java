package com.javelin.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javelin.core.upload.DefaultMultipartForm;
import com.javelin.core.upload.MultipartForm;
import com.javelin.core.upload.UploadedFile;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.javelin.constants.HttpConstants.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * Implementation of {@link Context} based on Java's built-in {@link HttpExchange}.
 * <p>
 * This class wraps the low-level {@code HttpExchange} object and provides
 * utility methods to simplify request/response handling for Javelin handlers.
 */
public class HttpExchangeContext implements Context {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpExchange exchange;
    private final Map<String, String> queryParams;
    private Map<String, String> formParams;

    // Path variables extracted from router
    private final Map<String, String> pathVars = new HashMap<>();

    // Middleware chain
    private List<Middleware> middlewareChain;
    private Runnable finalHandler;
    private int currentIndex = -1;

    // Response status code
    private int statusCode = 200;

    /**
     * Constructs a new context based on the provided {@code HttpExchange}.
     *
     * @param exchange the underlying HTTP exchange
     */
    public HttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
        this.queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
    }

    // ========== Path & Query ==========

    /**
     * Returns the path portion of the request URI.
     *
     * @return the path (e.g. {@code "/users"})
     */
    @Override
    public String path() {
        return exchange.getRequestURI().getPath();
    }

    /**
     * Returns the value of a query parameter from the request URL.
     * <p>
     * For example, given "/search?q=hello", calling {@code queryParam("q")} returns {@code "hello"}.
     *
     * @param key the name of the query parameter
     * @return the value, or {@code null} if not found
     */
    @Override
    public String queryParam(String key) {
        return queryParams.get(key);
    }

    /**
     * Returns the captured path variable by name.
     * E.g., for "/users/{id}" if path is "/users/123", then {@code pathVar("id")} => "123".
     *
     * @param name the variable name (e.g. "id")
     * @return the path variable's value, or null if not present
     */
    @Override
    public String pathVar(String name) {
        return pathVars.get(name);
    }

    /**
     * Sets all path variables captured during routing.
     *
     * @param vars a map of variableName -> value
     */
    @Override
    public void setPathVars(Map<String, String> vars) {
        this.pathVars.putAll(vars);
    }

    // ========== Response Handling ==========

    /**
     * Sets the HTTP status code for the response.
     * This must be called before sending the response.
     *
     * @param code the HTTP status code (e.g., 200, 404, 500)
     */
    @Override
    public void status(int code) {
        this.statusCode = code;
    }

    /**
     * Sets a response header before sending the response.
     *
     * @param name  the header name (e.g. "X-Custom-Header")
     * @param value the header value (e.g. "Enabled")
     */
    @Override
    public void setHeader(String name, String value) {
        exchange.getResponseHeaders().set(name, value);
    }

    /**
     * Sends a plain text response with the currently set HTTP status.
     * This also closes the exchange.
     *
     * @param body the response body as plain text
     */
    @Override
    public void send(String body) {
        try {
            String method = exchange.getRequestMethod();
            byte[] bytes = body.getBytes();

            // HEAD request => no body
            if (METHOD_HEAD.equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set(HEADER_CONTENT_LENGTH, "0");
                exchange.sendResponseHeaders(statusCode, 0);
            } else {
                exchange.sendResponseHeaders(statusCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Sends a JSON response with the currently set HTTP status.
     * Sets {@code Content-Type: application/json}.
     * This also closes the exchange.
     *
     * @param data the object to serialize into JSON
     */
    @Override
    public void json(Object data) {
        try {
            byte[] json = mapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set(HEADER_CONTENT_TYPE, APPLICATION_JSON);
            exchange.sendResponseHeaders(statusCode, json.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Returns the value of a request header.
     *
     * @param name header name (case-insensitive)
     * @return the header value or {@code null} if not present
     */
    @Override
    public String header(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    // ========== Request Body ==========

    /**
     * Parses the HTTP request body as JSON into the given class type.
     *
     * @param clazz the class to deserialize into
     * @param <T>   the type of object to return
     * @return the deserialized object
     * @throws RuntimeException if the body cannot be parsed
     */
    @Override
    public <T> T body(Class<T> clazz) {
        try (InputStream is = exchange.getRequestBody()) {
            return mapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse request body", e);
        }
    }

    // ========== Middleware Chain ==========

    @Override
    public void setMiddlewareChain(List<Middleware> chain) {
        this.middlewareChain = chain;
    }

    @Override
    public void setFinalHandler(Runnable finalHandler) {
        this.finalHandler = finalHandler;
    }

    /**
     * Proceeds to the next middleware in the chain, or the final route handler.
     *
     * @throws Exception if any middleware or the final handler throws
     */
    @Override
    public void next() throws Exception {
        currentIndex++;
        if (middlewareChain != null && currentIndex < middlewareChain.size()) {
            middlewareChain.get(currentIndex).handle(this);
        } else if (finalHandler != null) {
            finalHandler.run();
        }
    }

    /**
     * Sends a binary response (e.g. image or file) with the currently set HTTP status.
     * This also closes the exchange.
     *
     * @param data the binary response data
     */
    @Override
    public void sendBytes(byte[] data) {
        try {
            String method = exchange.getRequestMethod();
            if (METHOD_HEAD.equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set(HEADER_CONTENT_LENGTH, "0");
                exchange.sendResponseHeaders(statusCode, 0);
            } else {
                exchange.sendResponseHeaders(statusCode, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    /**
     * Retrieves the value of a form parameter from the request body.
     * <p>
     * Only works when the Content-Type is {@code application/x-www-form-urlencoded}.
     * This method will lazily parse the body only on first access.
     *
     * @param key the name of the form field
     * @return the value of the form field, or {@code null} if not found
     */
    @Override
    public String formParam(String key) {
        if (formParams == null) {
            formParams = parseFormParams();
        }
        return formParams.get(key);
    }

    // ========== Internal Helpers ==========

    /**
     * Parses a query string (or URL-encoded form body) into a map of key-value pairs.
     * <p>
     * Keys and values are automatically URL-decoded using UTF-8.
     *
     * @param rawQuery the raw query string (e.g. {@code "id=1&name=test"})
     * @return a map of parsed parameters
     */
    private Map<String, String> parseQueryParams(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return result;

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String name = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            result.put(name, value);
        }
        return result;
    }

    /**
     * Safely decodes a URL-encoded string using UTF-8.
     *
     * @param value the encoded string
     * @return the decoded string
     */
    private String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Parses form-encoded parameters from the request body.
     * <p>
     * This applies only to {@code application/x-www-form-urlencoded} requests.
     *
     * @return a map of form parameter names and values, or an empty map if not applicable
     */
    private Map<String, String> parseFormParams() {
        if (!"application/x-www-form-urlencoded".equalsIgnoreCase(header("Content-Type"))) {
            return Map.of();
        }
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), UTF_8);
            return parseQueryParams(body);
        } catch (IOException e) {
            return Map.of();
        }
    }

    /**
     * Retrieves the value of a cookie from the request headers by name.
     * <p>
     * If multiple cookies are present, this method will search and return the first match.
     *
     * @param name the name of the cookie to look for
     * @return the value of the cookie, or {@code null} if not found
     */
    @Override
    public String cookie(String name) {
        String cookieHeader = header("Cookie"); // Fetch the raw Cookie header from request
        if (cookieHeader == null) return null;

        String[] cookies = cookieHeader.split(";"); // Split multiple cookies
        for (String cookie : cookies) {
            String[] kv = cookie.trim().split("=", 2); // Split name=value
            if (kv.length == 2 && kv[0].trim().equals(name)) {
                return kv[1].trim(); // Return the matching cookie value
            }
        }
        return null; // Not found
    }

    /**
     * Adds a {@code Set-Cookie} header to the response with the given name, value, and lifetime.
     * <p>
     * The cookie will be set with path "/", and a max-age indicating its expiration time.
     *
     * @param name the name of the cookie to set
     * @param value the value of the cookie
     * @param maxAgeSeconds the cookie's lifetime in seconds (e.g. 3600 = 1 hour)
     */
    @Override
    public void setCookie(String name, String value, int maxAgeSeconds) {
        // Construct the Set-Cookie header value
        String cookie = name + "=" + value + "; Path=/; Max-Age=" + maxAgeSeconds;
        setHeader("Set-Cookie", cookie); // Add it to response headers
    }

    /**
     * Returns the client's IP address, considering reverse proxies.
     * <p>
     * It checks the {@code X-Forwarded-For} header first, then falls back
     * to the socket's remote address.
     *
     * @return the client's IP address as a string
     */
    @Override
    public String remoteIp() {
        // Check X-Forwarded-For header first (in case behind a reverse proxy)
        String xff = header("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // If multiple IPs are present, take the first one (client IP)
            return xff.split(",")[0].trim();
        }

        // Fall back to the actual remote address
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    /**
     * Parses the incoming request as a {@code multipart/form-data} form.
     * <p>
     * This method supports file uploads and text fields.
     *
     * @return the parsed multipart form object
     * @throws IllegalStateException if the request is not multipart/form-data
     */
    @Override
    public MultipartForm multipart() {
        String contentType = header("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new IllegalStateException("Request is not multipart/form-data");
        }

        // Extract boundary from header
        String boundary = null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring("boundary=".length());
                break;
            }
        }

        if (boundary == null) {
            throw new IllegalStateException("No boundary found in Content-Type");
        }

        boundary = "--" + boundary; // actual boundary marker
        String finalBoundary = boundary + "--";

        try (InputStream is = exchange.getRequestBody()) {
            byte[] raw = is.readAllBytes();
            String body = new String(raw, StandardCharsets.ISO_8859_1); // binary-safe encoding

            DefaultMultipartForm form = new DefaultMultipartForm();
            String[] parts = body.split(boundary + "\r\n");

            for (String part : parts) {
                if (part.isBlank() || part.equals("--") || part.equals(finalBoundary)) continue;

                parsePart(part, form);
            }

            return form;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse multipart body", e);
        }
    }

    /**
     * Returns the HTTP method of the current request.
     *
     * @return the HTTP method string (e.g. "GET", "POST", "OPTIONS")
     */
    @Override
    public String method() {
        return exchange.getRequestMethod();
    }

    /**
     * Parses a single part of a multipart/form-data body and stores it in the form.
     * This method handles both text fields and file uploads.
     *
     * @param part the raw multipart body part
     * @param form the form object to populate
     */
    private void parsePart(String part, DefaultMultipartForm form) {
        String[] sections = part.split("\r\n\r\n", 2); // [headers][body]
        if (sections.length < 2) return;

        String headersBlock = sections[0];
        String bodyBlock = sections[1];

        Map<String, String> headers = new HashMap<>();
        for (String headerLine : headersBlock.split("\r\n")) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex != -1) {
                String name = headerLine.substring(0, colonIndex).trim();
                String value = headerLine.substring(colonIndex + 1).trim();
                headers.put(name.toLowerCase(), value);
            }
        }

        String disposition = headers.get("content-disposition");
        if (disposition == null || !disposition.contains("name=")) return;

        String name = extractAttribute(disposition, "name");
        String filename = extractAttribute(disposition, "filename");
        String contentType = headers.getOrDefault("content-type", "text/plain");

        // Remove trailing \r\n--
        String cleanedBody = bodyBlock.replaceAll("\r\n--$", "").stripTrailing();

        if (filename != null && !filename.isEmpty()) {
            byte[] data = cleanedBody.getBytes(StandardCharsets.ISO_8859_1);
            form.addFile(name, new UploadedFile(filename, contentType, data));
        } else {
            form.addField(name, cleanedBody);
        }
    }

    /**
     * Extracts the value of an attribute from a multipart header string.
     * <p>
     * For example, given {@code name="file"} or {@code filename="image.jpg"}.
     *
     * @param header   the full header string
     * @param attrName the attribute name to extract
     * @return the attribute value or {@code null} if not found
     */
    private String extractAttribute(String header, String attrName) {
        for (String part : header.split(";")) {
            part = part.trim();
            if (part.startsWith(attrName + "=")) {
                String value = part.substring(attrName.length() + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }
}