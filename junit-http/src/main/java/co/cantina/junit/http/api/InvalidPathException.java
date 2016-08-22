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
package co.cantina.junit.http.api;

/**
 * An InvalidPathException is thrown when a String path cannot be parsed by 
 * {@link Path#parse(java.lang.String)}.
 */
public class InvalidPathException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String path;
    
    /**
     * @param path The invalid path
     */
    public InvalidPathException(final String path) {
        super("Invalid path: " + path);
        
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
