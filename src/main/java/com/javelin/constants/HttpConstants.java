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
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

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

    // MIME
    public static final String TEXT_CSS = "text/css";
    public static final String TEXT_JS = "text/javascript";
    public static final String IMAGE_PNG = "public/images/png";
    public static final String IMAGE_JPEG = "public/images/jpeg";
    public static final String IMAGE_GIF = "public/images/gif";
    public static final String IMAGE_SVG = "public/images/svg+xml";
    public static final String TEXT_TXT = "text/plain";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    // CORS headers
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String HEADER_ORIGIN = "Origin";
}
