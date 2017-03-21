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
package io.dfox.junit.http;

import com.fasterxml.jackson.databind.JsonNode;
import io.dfox.junit.http.api.Failure;
import io.dfox.junit.http.api.Ignored;
import io.dfox.junit.http.api.InvalidPathException;
import io.dfox.junit.http.api.MethodNotFoundException;
import io.dfox.junit.http.api.Path;
import io.dfox.junit.http.api.Result;
import io.dfox.junit.http.api.RunnerException;
import io.dfox.junit.http.api.Success;
import io.dfox.junit.http.api.Summary;
import io.dfox.junit.http.examples.ExampleNonTest;
import io.dfox.junit.http.examples.ExampleTest;
import io.dfox.junit.http.examples.ExampleTestWithBadBeforeClass;
import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class JUnitHttpApplicationTest {
    
    private Service application = new Service();
    
    @Before
    public void setUp() {
        application = new Service();
    }
    
    @After
    public void tearDown() {
        application.destroy();
        application = null;
    }
    
    @Test
    public void runsSingleTest() {
        String path = ExampleTest.class.getName() + "/successfulTest";
        Summary testRun = application.runTest(path);
        
        assertTrue(testRun.isSuccessful());
        assertEquals(1, testRun.getResults().size());
        Result success = testRun.getResults().get(0);
        
        assertTrue(success instanceof Success);
        assertSuccessfulTestResult((Success) success);
    }
    
    public void assertSuccessfulTestResult(final Success success) {
        assertEquals(ExampleTest.class.getName(), success.getGrouping());
        assertEquals("successfulTest", success.getName());
        assertEquals("success", success.getType());
    }
    
    public void assertFixtureWasRun(final Success success) {
        assertEquals(ExampleTest.class.getName(), success.getGrouping());
        assertEquals("fixtureWasRun", success.getName());
        assertEquals("success", success.getType());
    }
    
    public void assertClassValueWasSetUp(final Success success) {
        assertEquals(ExampleTest.class.getName(), success.getGrouping());
        assertEquals("classValueWasSetUp", success.getName());
        assertEquals("success", success.getType());
    }
    
    public void assertIgnoredTestResult(final Ignored ignored) {
        assertEquals(ExampleTest.class.getName(), ignored.getGrouping());
        assertEquals("ignoredTest", ignored.getName());
        assertEquals("ignored", ignored.getType());
    }
    
    public void assertFailedAssertionNoMessage(final Failure failure) {
        assertEquals(ExampleTest.class.getName(), failure.getGrouping());
        assertEquals("failedAssertionNoMessage", failure.getName());
        assertEquals("failure", failure.getType());
        assertEquals("java.lang.AssertionError", failure.getError().getName());
        assertFalse(failure.getError().getMessage().isPresent());
    }
    
    public void assertFailedAssertion(final Failure failure) {
        assertEquals(ExampleTest.class.getName(), failure.getGrouping());
        assertEquals("failedAssertion", failure.getName());
        assertEquals("failure", failure.getType());
        assertEquals("java.lang.AssertionError", failure.getError().getName());
        assertTrue(failure.getError().getMessage().isPresent());
        assertEquals("Assertion must be false", failure.getError().getMessage().get());
    }
    
    public void assertException(final Failure failure) {
        assertEquals(ExampleTest.class.getName(), failure.getGrouping());
        assertEquals("exceptionTest", failure.getName());
        assertEquals("failure", failure.getType());
        assertEquals("java.lang.RuntimeException", failure.getError().getName());
        assertTrue(failure.getError().getMessage().isPresent());
        assertNotNull(failure.getTrace());
        assertTrue(failure.getTrace().size() > 0);
        assertEquals("BOOM!", failure.getError().getMessage().get());
    }
    
    @Test
    public void runsFixtureAndAllTestsInTestClass() {
        
        Summary fixtureRun = application.runFixture(ExampleTest.class.getName() + "/someFixture");
        
        assertTrue(fixtureRun.isSuccessful());
        assertEquals(1, fixtureRun.getResults().size());
        assertEquals(ExampleTest.class.getName(), fixtureRun.getResults().get(0).getGrouping());
        assertEquals("someFixture", fixtureRun.getResults().get(0).getName());
        assertEquals("success", fixtureRun.getResults().get(0).getType());
        
        Summary testRun = application.runTest(ExampleTest.class.getName());
        
        assertFalse(testRun.isSuccessful());
        assertEquals(7, testRun.getResults().size());
        
        testRun.getResults().stream().forEach(result -> {
            switch (result.getName()) {
                case "successfulTest":
                    assertSuccessfulTestResult((Success) result);
                    break;
                case "ignoredTest":
                    assertIgnoredTestResult((Ignored) result);
                    break;
                case "failedAssertionNoMessage":
                    assertFailedAssertionNoMessage((Failure) result);
                    break;
                case "failedAssertion":
                    assertFailedAssertion((Failure) result);
                    break;
                case "exceptionTest":
                    assertException((Failure) result);
                    break;
                case "classValueWasSetUp" : 
                    assertClassValueWasSetUp((Success) result);
                    break;
                case "fixtureWasRun" : 
                    assertFixtureWasRun((Success) result);
                    break;
                default:
                    fail("Unexpected test: " + result.getName());
            }
        });
    }

    @Test
    public void runnerIsCached() {
        Path path = new Path(ExampleTest.class.getName());
        Runner runnerA = application.getRunner(path);
        Runner runnerB = application.getRunner(path);
        
        assertSame(runnerA, runnerB);
    }
    
    @Test
    public void invalidTestPathExceptionWhenInvalidPath() {
        String path = "/some/invalid/path";
        try {
            application.runTest(path);
            fail("Exception should be thrown for invalid path");
        }
        catch (InvalidPathException e) {
            assertEquals(path, e.getPath());
        }
    }
    
    @Test(expected = MethodNotFoundException.class)
    public void testNotFoundExceptionWhenMissingClass() {
        application.runTest("some.non.existant.class.Name");
    }
    
    @Test
    public void testNotFoundExceptionWhenMissingMethod() {
        String path = ExampleTest.class.getName() + "/nonExistentMethod";
        try {
            application.runTest(path);
            fail("Test should not be found");
        }
        catch (MethodNotFoundException e) {
            assertEquals(Path.parse(path).get(), e.getTestPath());
        }
    }
    
    @Test(expected = RunnerException.class)
    public void testRunnerExceptionWhenNonTestClass() {
        application.runTest(ExampleNonTest.class.getName());
    }
    
    @Test(expected = RunnerException.class)
    public void testRunnerExceptionWhenBadBeforeClass() {
        application.runTest(ExampleTestWithBadBeforeClass.class.getName());
    }
    
    @Test
    public void fixtureLoadsAndParses() throws IOException {
        Optional<JsonNode> fixture = application.getData("notes.json");
        
        assertTrue(fixture.isPresent());
        assertEquals("my-note", fixture.get().path("name").asText());
        assertEquals("This is my note", fixture.get().path("contents").asText());
    }
}
