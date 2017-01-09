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
package co.cantina.junit.http.examples;

import co.cantina.junit.http.Fixture;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

/**
 * Example tests which can be used to demonstrate the servlet.
 */
public class ExampleTest {
    
    public static final String SOME_VALUE = "value";
    private static String someClassValue;
    private static String someFixture;
    
    @BeforeClass
    public static void setUpClass() {
        someClassValue = SOME_VALUE;
    }
    
    @AfterClass
    public static void tearDownClass() {
        someClassValue = null;
    }
    
    @Fixture
    public void someFixture() {
        someFixture = SOME_VALUE;
    }
    
    @Test
    public void fixtureWasRun() {
        assertEquals(SOME_VALUE, someFixture);
    }
    
    @Test
    public void classValueWasSetUp() {
        assertEquals(SOME_VALUE, someClassValue);
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
