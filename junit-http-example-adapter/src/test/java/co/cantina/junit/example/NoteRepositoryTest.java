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
package co.cantina.junit.example;

import co.cantina.junit.http.example.NoteRepository;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class NoteRepositoryTest {

    private NoteRepository repository;
    
    @Before
    public void setUp() {
        final File tempDir = new File("/tmp", NoteRepository.class.getName());
        repository = new NoteRepository(tempDir);
        repository.init();
    }
    
    @After
    public void tearDown() throws IOException {
        repository.cleanup();
    }
    
    @Test
    public void testSaveFind() throws IOException {
        
        final String name = "test-note";
        final String contents = "This is my note";
        
        assertFalse(repository.getNote(name).isPresent());
        
        try(InputStream contentsStream = IOUtils.toInputStream(contents)) {
            repository.saveNote(name, contentsStream);
        }
        
        Optional<InputStream> note = repository.getNote(name);
        assertTrue(note.isPresent());
        try(InputStream stream = note.get()){
            String savedContents = IOUtils.toString(stream);
            Assert.assertEquals(contents, savedContents);
        }
    }
}