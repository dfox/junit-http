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
package io.dfox.junit.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.dfox.junit.http.api.InvalidPathException;
import io.dfox.junit.http.api.MethodNotFoundException;
import io.dfox.junit.http.api.RunnerException;
import io.dfox.junit.http.api.Summary;
import io.dfox.junit.http.util.TestUtils;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.error.ServerErrorHandler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import static ratpack.jackson.Jackson.json;
import ratpack.path.PathBinding;
import ratpack.server.RatpackServer;

/**
 * The JUnitHttpServer runs tests via a REST API and returns their results as JSON.
 * 
 * Tests are requested via paths with the format /&lt;test class&gt;/[&lt;test method&gt;]
 */
public class JUnitHttpServer {
    
    /**
     * Create the server.
     */
    private JUnitHttpServer() { }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JUnitHttpServer.class);

    private static final int PORT = 8081;
    private static final int INTERNAL_ERROR_STATUS = 500;

    private static final JUnitHttpApplication APP = new JUnitHttpApplication();
    
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
    
    private static Handler getTestData() {
        return context -> {
            String path = getPastBinding(context);
            LOGGER.debug("Getting path: {}", path);
            Optional<JsonNode> data = APP.getData(path);
            if (data.isPresent()) {
                context.render(data.get().toString());
            }
            else {
                context.notFound();
            }
        };
    }
    
    /**
     * @return A handler to run a test
     */
    private static Handler runTest() {
        return context -> {
            try {
                String path = getPastBinding(context);
                Summary summary = APP.runTest(path);
                context.render(json(summary));
            }
            catch (MethodNotFoundException | InvalidPathException e) {
                context.notFound();
            }
        };
    }
    
    /**
     * @return A handler to run a fixture
     */
    private static Handler runFixture() {
        return context -> {
            try {
                String path = getPastBinding(context);
                Summary summary = APP.runFixture(path);
                context.render(json(summary));
            }
            catch (MethodNotFoundException | InvalidPathException e) {
                context.notFound();
            }
        };
    }
    
    /**
     * @return A handler for server errors
     */
    public static ServerErrorHandler handleServerErrors() {
        return (context, e) -> {
            if (e instanceof MethodNotFoundException || e instanceof InvalidPathException) {
                context.notFound();
            }
            else if (e instanceof RunnerException) {
                context.getResponse().status(INTERNAL_ERROR_STATUS);
                LOGGER.error("A server error occurred servicing resource: " + context.getRequest().getUri(), e);
            }
        };
    }
    
    /**
     * Main entry point. 
     * @param args The application arguments
     * @throws Exception If an error occurs
     */
    public static void main(final String ... args) throws Exception {
        RatpackServer.start(s -> s
            .serverConfig(config -> config
                .port(PORT)
            )
            .registryOf(registry -> registry
                .add(TestUtils.JSON_MAPPER)
                .add(handleServerErrors())
            )
            .handlers(chain -> chain
                .all(context -> {
                    context.getResponse().contentType("application/json");
                    context.next();
                })
                .prefix("data", dataChain -> dataChain.all(getTestData()))
                .prefix("tests", dataChain -> dataChain.all(runTest()))
                .prefix("fixtures", dataChain -> dataChain.all(runFixture()))
            )
        );
    }
}
