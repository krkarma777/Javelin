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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        server.start();
    }
}
