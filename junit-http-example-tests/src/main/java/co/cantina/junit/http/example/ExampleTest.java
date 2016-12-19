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

import co.cantina.junit.http.Fixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.BeforeClass;
import static co.cantina.junit.http.util.TestUtils.getTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example tests which can be used to demonstrate the servlet.
 */
public class ExampleTest {

    private static NoteRepository repository;



    @BeforeClass
    public static void setUp() throws IOException {
        final File tempDir = new File("/tmp", NoteRepository.class.getName());
        repository = new NoteRepository(tempDir);
        repository.init();

        JsonNode notesFixture = getTestData("notes.json");
        JsonNode noteFixture = notesFixture.path("load");
        String name = noteFixture.path("name").asText();
        String contents = noteFixture.path("contents").asText();

        repository.saveNote(name, IOUtils.toInputStream(contents));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        repository.cleanup();
    }

    @Fixture
    public void createNote() throws IOException {
        JsonNode notesFixture = getTestData("notes.json");
        JsonNode noteFixture = notesFixture.path("fixture");
        String name = noteFixture.path("name").asText();
        String contents = noteFixture.path("contents").asText();

        repository.saveNote(name, IOUtils.toInputStream(contents));
    }

    @Fixture
    public void throwException() throws Exception {
        throw new Exception("BOOM!");
    }

    private void assertNoteExists(final String notePath) throws IOException {
        JsonNode notesFixture = getTestData("notes.json");
        JsonNode noteFixture = notesFixture.path(notePath);
        String name = noteFixture.path("name").asText();
        String expectedContents = noteFixture.path("contents").asText();

        Optional<InputStream> note = repository.getNote(name);
        assertTrue(note.isPresent());
        try(InputStream stream = note.get()){
            String contents = IOUtils.toString(stream);
            assertEquals(expectedContents, contents);
        }
    }

    @Test
    public void fixtureRun() throws IOException {
        assertNoteExists("fixture");
    }

    @Test
    public void noteSaved() throws IOException {
        assertNoteExists("save");
    }

    @Test
    public void successfulTest() {
        assertTrue("Assertion must be true", true);
    }

    @Test
    @Ignore
    public void ignoredTest() {
        assertTrue(true);
    }

    @Test
    public void failedAssertionNoMessage() {
        assertFalse(true);
    }

    @Test
    public void failedAssertion() {
        assertFalse("Assertion must be false", true);
    }

    @Test
    public void exceptionTest() {
        throw new RuntimeException("BOOM!");
    }
}
