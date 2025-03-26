package com.javelin.constants;

/**
 * Common HTTP-related constants used across Javelin.
 * <p>
 * This class contains standard header names, MIME types,
 * method names, charset encodings, and status descriptions.
 */
public final class HttpConstants {

    // Prevent instantiation
    private HttpConstants() {}

    // Common HTTP headers
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_USER_AGENT = "User-Agent";

    // Common content types
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";

    // Charset
    public static final String CHARSET_UTF8 = "UTF-8";

    // HTTP methods (as Strings)
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";

    // Default messages
    public static final String MESSAGE_NOT_FOUND = "404 Not Found";
    public static final String MESSAGE_INTERNAL_ERROR = "Internal Server Error";
    public static final String MESSAGE_UNAUTHORIZED = "Unauthorized";
    public static final String MESSAGE_FORBIDDEN = "Forbidden";
    public static final String MESSAGE_BAD_REQUEST = "Bad Request";
}
