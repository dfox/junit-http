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
package co.cantina.junit.http.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

@WebServlet("/")
public class NoteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UTF_8 = "UTF-8";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String EXAMPLE_CLIENT_HOST = "http://localhost:8082";

    private NoteRepository repository;

    @Override
    public void init() {
        final File tempDir = new File("/tmp", NoteRepository.class.getName());
        repository = new NoteRepository(tempDir);
        repository.init();
    }

    @Override
    public void destroy() {
        try {
            repository.cleanup();
        }
        catch (IOException e) {
            log("Error running repository cleanup", e);
        }
    }

    private String getNoteName(final HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length() + 1);
    }

    private void addBasicHeaders(final HttpServletResponse response) {
        response.setCharacterEncoding(UTF_8);
        response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, EXAMPLE_CLIENT_HOST);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        addBasicHeaders(response);

        String name = getNoteName(request);

        try (InputStream stream = request.getInputStream()) {
            repository.saveNote(name, stream);
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        addBasicHeaders(response);

        String name = getNoteName(request);

        try (InputStream noteStream = repository.getNote(name)) {
            if (noteStream == null) {
                response.setStatus(404);
            }
            else {
                try (OutputStream outputStream = response.getOutputStream()) {
                    IOUtils.copy(noteStream, outputStream);
                }
            }
        }
    }
}
