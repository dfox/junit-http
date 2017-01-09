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

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;

/**
 * Error represents an failure of a test.
 */
public class Failure extends BaseResult {

    private final Error error;
    private final ImmutableList<String> trace;
    
    /**
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     * @param name The name of the test. For JUnit tests, this is the test method name.
     * @param error The error which occurred. For JUnit tests, this is the exception which was 
     * thrown.
     * @param stackTrace The stack at the point the error occurred
     */
    public Failure(final String grouping, final String name, final Error error,
                   final ImmutableList<String> stackTrace) {
        super(grouping, name);
        
        Validate.notNull(error, "error cannot be null");
        Validate.notNull(stackTrace, "stackTrace cannot be null");

        this.error = error;
        this.trace = stackTrace;
    }

    public Error getError() {
        return error;
    }

    public ImmutableList<String> getTrace() {
        return trace;
    }
}
