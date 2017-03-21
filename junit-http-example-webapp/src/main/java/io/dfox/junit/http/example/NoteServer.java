/*
 * Copyright 2016 David Fox. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dfox.junit.http.example;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.path.PathBinding;
import ratpack.server.RatpackServer;

public class NoteServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteServer.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String EXAMPLE_CLIENT_HOST = "http://localhost:8082";

    private static final File TEMP_DIR = new File("/tmp", NoteRepository.class.getName());
    private static final NoteRepository REPOSITORY = new NoteRepository(TEMP_DIR);
    private static final int PORT = 8080;
        
    /**
     * Get the path past the current binding.
     * 
     * @param context The context
     * @return The path
     */
    private static String getPastBinding(final Context context) {
        PathBinding binding = context.getPathBinding();
        return binding.getPastBinding();
    }
    
    public static void main(final String ... args) throws Exception {
        REPOSITORY.init();
        
        RatpackServer.start(s -> s
            .serverConfig(config -> config.port(PORT))
            .handlers(chain -> chain
                .all(context -> {
                    context.getResponse().getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN, EXAMPLE_CLIENT_HOST);
                    context.next();
                })
                .path("notes/:name", context -> {
                    String name = context.getPathTokens().get("name");
                    context.byMethod(method -> method
                        .post(() -> {
                            LOGGER.debug("About to write note: {}", name);
                            context.getRequest().getBody().then(body -> {
                                LOGGER.debug("Writing note: {}", body.getText());
                                REPOSITORY.saveNote(name, body.getText());
                                context.render("");
                            });
                        })
                        .get(() -> {
                            LOGGER.debug("Getting note: {}", name);
                            Optional<String> note = REPOSITORY.getNote(name);
                            if (note.isPresent()) {
                                context.render(note);
                            }
                            else {
                                context.notFound();
                            }
                        })
                    );
                })
            )
        );
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    REPOSITORY.cleanup();
                }
                catch (IOException e) {
                    LOGGER.error("Error shutting down repository", e);
                }
            }
        });
    }
}
