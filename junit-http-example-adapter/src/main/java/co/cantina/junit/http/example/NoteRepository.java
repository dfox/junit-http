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
package co.cantina.junit.http.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class NoteRepository {
    
    private final File dir;
    
    public NoteRepository(final File dir) {
        this.dir = dir;
    }
    
    public void init() {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(dir);
    }
    
    public synchronized Optional<InputStream> getNote(final String name) throws IOException {
        File note = new File(dir, name);
        if (note.exists()) {
            return Optional.of(IOUtils.toBufferedInputStream(FileUtils.openInputStream(note)));
        }
        else {
            return Optional.empty();
        }
    }
    
    public synchronized void saveNote(final String name, final InputStream contents) 
        throws IOException {
        File note = new File(dir, name);
        FileUtils.copyInputStreamToFile(contents, note);
    }
}
