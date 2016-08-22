/*
 * Copyright 2016 Cantina Consulting, Inc. All Rights Reserved.
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
package co.cantina.junit.http.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * TestUtils provides a set of utility methods which can be used in tests. They are also used
 * internally by the JUnit HTTP application.
 */
public class TestUtils {
    
    /**
     * The directory fixtures should be contained in.
     */
    public static final String DATA_DIR = "/test-data";
    
    /**
     * The ObjectMapper used by the system.
     */
    public static final ObjectMapper JSON_MAPPER = 
        new ObjectMapper().registerModule(new Jdk8Module());
    
    /**
     * TestUtils cannot be instantiated.
     */
    private TestUtils() { }
    
    /**
     * Convert the array of StackTraceElements to a list of Strings.
     * 
     * @param stackTrace The stacktrace to convert
     * @return The stacktrace as a list of Strings
     */
    public static ImmutableList<String> toStringList(final StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace)
            .map(e -> e.toString())
            .collect(Collectors.toImmutableList());
    }
    
    /**
     * Get the fixture with the specified path or null if not found. The fixture must be a valid 
     * JSON file available on the classpath in a resource directory called "fixtures". It may be 
     * contained in a further directory structure under that directory.
     * 
     * @param path The path to the fixture
     * @return The fixture as a parsed JsonNode
     * @throws IOException If the fixture cannot be loaded.
     */
    public static JsonNode getTestData(final String path) throws IOException {
        String fullPath = DATA_DIR + "/" + path;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = classLoader.getResourceAsStream(fullPath)) {
            if (stream == null) {
                try (final InputStream stream2 = TestUtils.class.getResourceAsStream(fullPath)) {
                    if (stream2 == null) {
                        return null;
                    }
                    else {
                        return JSON_MAPPER.readTree(stream2);
                    }
                }
            }
            else {
                return JSON_MAPPER.readTree(stream);
            }
        }
    }
}
