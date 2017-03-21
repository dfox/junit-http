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

import io.dfox.junit.http.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.server.RatpackServer;

/**
 * The Server runs tests via a REST API and returns their results as JSON.
 * 
 * Tests are requested via paths with the format /&lt;test class&gt;/[&lt;test method&gt;]
 */
public class Server {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final int DEFAULT_PORT = 8081;
    
    /**
     * Create the server.
     */
    private Server() { }
    
    /**
     * Parse the port from the specified command line arguments.
     * 
     * @param args The arguments to parse
     * @return The port to run on
     */
    private static int parsePort(final String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                LOGGER.error("Invalid port: {}: Using default: {}", args[0], port);
            }
        }
        return port;
    }
    
    /**
     * Main entry point. 
     * @param args The application arguments
     * @throws Exception If an error occurs
     */
    public static void main(final String ... args) throws Exception {
        final int port = parsePort(args);
        Api api = new Api();
        
        RatpackServer.start(s -> s
            .serverConfig(config -> config
                .port(port)
            )
            .registryOf(registry -> registry
                .add(TestUtils.JSON_MAPPER)
                .add(api.handleServerErrors())
            )
            .handlers(chain -> chain
                .all(context -> {
                    context.getResponse().contentType("application/json");
                    context.next();
                })
                .prefix("data", c -> c.all(api.getTestData()))
                .prefix("tests", c -> c.all(api.runTest()))
                .prefix("fixtures", c -> c.all(api.runFixture()))
            )
        );
    }
}
