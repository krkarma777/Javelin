package com.javelin.middleware;

import com.javelin.core.Context;
import com.javelin.core.Middleware;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.javelin.constants.HttpConstants.*;

/**
 * A middleware that serves static files from a given root directory.
 * <p>
 * Example usage:
 * <pre>
 *   server.use(new StaticFileHandler("/static", "public"));
 * </pre>
 */
public class StaticFileHandler implements Middleware {

    private final String urlPrefix;
    private final Path rootDirectory;

    public StaticFileHandler(String urlPrefix, String rootDir) {
        this.urlPrefix = urlPrefix;
        this.rootDirectory = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String requestPath = ctx.path();

        if (!requestPath.startsWith(urlPrefix)) {
            ctx.next();
            return;
        }

        String relative = requestPath.substring(urlPrefix.length());
        if (relative.startsWith("/")) relative = relative.substring(1);
        if (relative.isEmpty()) {
            ctx.next();
            return;
        }

        Path filePath = rootDirectory.resolve(relative).normalize();

        if (!filePath.startsWith(rootDirectory)) {
            serve404(ctx);
            return;
        }

        if (Files.notExists(filePath) || Files.isDirectory(filePath)) {
            serve404(ctx);
            return;
        }

        String contentType = guessMimeType(filePath);
        ctx.setHeader(HEADER_CONTENT_TYPE, contentType);

        try {
            byte[] data = Files.readAllBytes(filePath);
            ctx.sendBytes(data);
        } catch (IOException e) {
            serve404(ctx);
        }
    }

    private void serve404(Context ctx) {
        ctx.status(404);
        ctx.send(MESSAGE_NOT_FOUND);
    }

    private String guessMimeType(Path path) {
        String file = path.getFileName().toString().toLowerCase();
        return MIME_MAP.entrySet()
                .stream()
                .filter(entry -> file.endsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(APPLICATION_OCTET_STREAM);
    }

    private static final Map<String, String> MIME_MAP = Map.of(
            ".html", TEXT_HTML,
            ".css", TEXT_CSS,
            ".js", TEXT_JS,
            ".png", IMAGE_PNG,
            ".jpg", IMAGE_JPEG,
            ".jpeg", IMAGE_JPEG,
            ".gif", IMAGE_GIF,
            ".svg", IMAGE_SVG
    );
}