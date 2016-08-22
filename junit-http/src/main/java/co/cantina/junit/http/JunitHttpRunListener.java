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
package co.cantina.junit.http;

import co.cantina.junit.http.api.Summary;
import co.cantina.junit.http.api.Failure;
import co.cantina.junit.http.api.Error;
import co.cantina.junit.http.api.Ignored;
import co.cantina.junit.http.api.Success;
import static co.cantina.junit.http.util.TestUtils.toStringList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

/**
 * JunitHttpRunListener listens for test events from JUnit and builds a {@link Summary}.
 */
public class JunitHttpRunListener extends RunListener {
    
    private final Summary.Builder testRunBuilder = Summary.builder();
    private final Map<Description, Boolean> tests = new HashMap<>();
    
    public Summary getTestRun() {
        return testRunBuilder.build();
    }
    
    @Override
    public void testStarted(final Description description) throws Exception {
        tests.put(description, Boolean.TRUE);
    }

    @Override
    public void testFinished(final Description description) throws Exception {
        if (tests.get(description)) {
            testRunBuilder.addResult(new Success(description.getClassName(), description.getMethodName()));
        }
    }

    @Override
    public void testFailure(final org.junit.runner.notification.Failure failure) throws Exception {
        tests.put(failure.getDescription(), Boolean.FALSE);
        
        final Error error = new Error(
            failure.getException().getClass().getName(),
            Optional.ofNullable(failure.getException().getMessage())
        );
        
        testRunBuilder.addResult(new Failure(
            failure.getDescription().getClassName(),
            failure.getDescription().getMethodName(),
            error,
            toStringList(failure.getException().getStackTrace())
        ));
    }
    
    @Override
    public void testIgnored(final Description description) throws Exception {
        tests.put(description, Boolean.FALSE);
        testRunBuilder.addResult(new Ignored(description.getClassName(), description.getMethodName()));
    }
}
