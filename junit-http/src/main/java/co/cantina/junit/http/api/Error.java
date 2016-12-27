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

import org.apache.commons.lang.Validate;

/**
 * Error represents an error which occurred during a test. For JUnit tests, this is the
 * exception which was thrown.
 */
public class Error {

    private final String name;
    private final String message;

    /**
     * @param name The name of the error. For JUnit tests, this is the exception name.
     * @param message The optional exception message. For JUnit tests, this is the exception
     * message.
     */
    public Error(final String name, final String message) {
        Validate.notEmpty(name, "name cannot be empty");
        Validate.notNull(message, "message cannot be null");

        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
