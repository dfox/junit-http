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
package io.dfox.junit.http.api;

/**
 * A RunnerException is thrown when a {@link io.dfox.junit.http.Runner} has an 
 * error running a test (unrelated to errors in the test), or when a runner can't be created
 * because the test class is not valid.
 */
public class RunnerException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * @param message The exception message
     * @param cause The root cause of the exception
     */
    public RunnerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
