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
package co.cantina.junit.http;

import co.cantina.junit.http.api.MethodNotFoundException;
import co.cantina.junit.http.api.InvalidPathException;
import co.cantina.junit.http.api.RunnerException;
import co.cantina.junit.http.api.Summary;
import static co.cantina.junit.http.util.TestUtils.JSON_MAPPER;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * The JUnitHttpServlet runs tests via a REST API and returns their results as JSON.
 * 
 * Tests are requested via paths with the format /&lt;test class&gt;/[&lt;test method&gt;]
 */
@WebServlet("/")
public class JUnitHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    public static final String FIXTURES_PREFIX = "fixtures";
    public static final String TESTS_PREFIX = "tests";
    public static final String DATA_PREFIX = "data";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final int NOT_FOUND_STATUS = 404;
    private static final int INTERNAL_ERROR_STATUS = 500;
    private static final int SUCCESS_STATUS = 200;
    private static final String UTF_8 = "UTF-8";
    
    private JUnitHttpApplication application;
    
    @Override
    public void init() {
        application = new JUnitHttpApplication();
    }
    
    @Override
    public void destroy() {
        application.destroy();
        application = null;
    }
    
    /**
     * Get the path from the request. The path is the relative path after the specified prefix.
     * For example, if the full request URI is "/contextName/fixtures/foobars/foo.json", the 
     * array returned will contain "fixtures", "foobars", and "foo.json".
     * 
     * @param request The request
     * @return The parsed path
     */
    private String[] parsePath(final HttpServletRequest request) {
        final int prefixLength = request.getContextPath().length() + 1;
        String path = request.getRequestURI().substring(prefixLength);
        return path.split("/");
    }
    
    /**
     * Join the path components after the first element, which is the dispatch prefix.
     * 
     * @param pathComponents The path components
     * @return The joined path
     */
    private String joinPathAfterPrefix(final String[] pathComponents) {
        final String[] dataPathComponents = 
                Arrays.copyOfRange(pathComponents, 1, pathComponents.length);
        return StringUtils.join(dataPathComponents, "/");
    }
    
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        
        response.setCharacterEncoding(UTF_8);
        
        final String[] pathComponents = parsePath(request);
        
        if (pathComponents.length < 2 || !pathComponents[0].equals(DATA_PREFIX)) {
            response.setStatus(NOT_FOUND_STATUS);
        }
        else {
            final String path = joinPathAfterPrefix(pathComponents);
            final JsonNode data = application.getData(path);

            try (PrintWriter writer = response.getWriter()) {
                if (data == null) {
                    response.setStatus(NOT_FOUND_STATUS);
                    writer.append("Data not found: " + path);
                }
                else {
                    response.setStatus(SUCCESS_STATUS);
                    response.setHeader(CONTENT_TYPE, APPLICATION_JSON);

                    JSON_MAPPER.writeValue(writer, data);
                }
            }
        }
    }
    
    /**
     * Run the test with the specified path.
     * 
     * @param path The path to the test to run
     * @param response The response to serialize the Summary to
     * @throws ServletException If an unrecoverable error occurs
     * @throws IOException If the test cannot be run
     */
    private void runTest(final String path, final HttpServletResponse response)
        throws ServletException, IOException {
        
        try (PrintWriter writer = response.getWriter()) {
            try {
                Summary summary = application.runTest(path);

                response.setStatus(SUCCESS_STATUS);
                response.setHeader(CONTENT_TYPE, APPLICATION_JSON);

                JSON_MAPPER.writeValue(writer, summary);
            }
            catch (MethodNotFoundException | InvalidPathException e) {
                response.setStatus(NOT_FOUND_STATUS);
                writer.append("Test(s) not found: " + path);
            }
            catch (RunnerException e) {
                response.setStatus(INTERNAL_ERROR_STATUS);
                writer.append("Internal error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Run the fixture at the specified path.
     * 
     * @param path The path to the fixture to run
     * @param response The response to serialize the Summary to
     * @throws ServletException If an unrecoverable error occurs
     * @throws IOException If the fixture cannot be run
     */
    private void runFixture(final String path, final HttpServletResponse response)
        throws ServletException, IOException {
        
        try (PrintWriter writer = response.getWriter()) {
            try {
                Summary summary = application.runFixture(path);

                response.setStatus(SUCCESS_STATUS);
                response.setHeader(CONTENT_TYPE, APPLICATION_JSON);

                JSON_MAPPER.writeValue(writer, summary);
            }
            catch (MethodNotFoundException | InvalidPathException e) {
                response.setStatus(NOT_FOUND_STATUS);
                writer.append("Fixture(s) not found: " + path);
            }
            catch (RunnerException e) {
                response.setStatus(INTERNAL_ERROR_STATUS);
                writer.append("Internal error: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        response.setCharacterEncoding(UTF_8);
        
        final String[] pathComponents = parsePath(request);
        
        if (pathComponents.length < 2) {
            response.setStatus(NOT_FOUND_STATUS);
        }
        else {
            final String path = joinPathAfterPrefix(pathComponents);
            switch (pathComponents[0]) {
                case TESTS_PREFIX:
                    runTest(path, response);
                    break;
                case FIXTURES_PREFIX:
                    runFixture(path, response);
                    break;
                default:
                    response.setStatus(NOT_FOUND_STATUS);
                    break;
            }
        }
    }
}
