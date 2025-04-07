/*
 * Copyright 2025 Javelin Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javelin;

import com.javelin.core.CorsMiddleware;
import com.javelin.core.StaticFileHandler;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        VirtualThreadServer server = new VirtualThreadServer(8080);

        server.use(new CorsMiddleware());
        server.use(new StaticFileHandler("/static", "public"));

        server.get("/", ctx -> {
            try (var in = Main.class.getResourceAsStream("/public/index.html")) {
                if (in == null) {
                    ctx.status(404).send("index.html not found");
                    return;
                }
                String html = new String(in.readAllBytes());
                ctx.setHeader("Content-Type", "text/html");
                ctx.send(html);
            }
        });

        server.get("/images/*", ctx -> {
            // pathVar("wildcard")로 *에 해당하는 실제 경로 추출
            String fileName = ctx.pathVar("wildcard");  // 예: "logo.png" or "folder/nested.png"
            String filePath = "/public/images/" + fileName;

            try (var in = Main.class.getResourceAsStream(filePath)) {
                if (in == null) {
                    ctx.status(404).send("Image not found: " + fileName);
                    return;
                }

                String contentType = getMimeType(fileName);
                ctx.setHeader("Content-Type", contentType);
                ctx.sendBytes(in.readAllBytes());

            } catch (IOException e) {
                ctx.status(500).send("Error reading image");
            }
        });


        server.start();
    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".gif")) return "image/gif";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
