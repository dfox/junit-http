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
package co.cantina.junit.http;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class JUnitHttpServletTest {
    
    private JUnitHttpServlet servlet;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @Before
    public void setUp() {
        servlet = new JUnitHttpServlet();
        servlet.init();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    @After
    public void tearDown() {
        servlet.destroy();
        servlet = null;
    }
    
    @Test
    public void getReturnsFixture() throws ServletException, IOException {
        request.setContextPath("");
        request.setRequestURI("/" + JUnitHttpServlet.DATA_PREFIX + "/notes.json");
        
        servlet.doGet(request, response);
        
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"name\":\"my-note\",\"contents\":\"This is my note\"}", 
                     response.getOutputStreamContent());
    }
    
    @Test
    public void getNonExistentFixtureReturnsNotFound() throws ServletException, IOException {
        request.setContextPath("");
        request.setRequestURI("/" + JUnitHttpServlet.FIXTURES_PREFIX + "/doesnt-exist.json");
        
        servlet.doGet(request, response);
        
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(404, response.getStatusCode());
    }
    
    @Test
    public void postNonExistantTestReturnsNotFound() throws ServletException, IOException {
        request.setContextPath("");
        request.setRequestURI("/" + JUnitHttpServlet.TESTS_PREFIX + "/doesnt-exist");
        
        servlet.doPost(request, response);
        
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(404, response.getStatusCode());
    }
    
    @Test
    public void postNonTestReturnsServerError() throws ServletException, IOException {
        request.setContextPath("");
        request.setRequestURI("/" + JUnitHttpServlet.TESTS_PREFIX 
                              + "/co.cantina.junit.http.examples.ExampleNonTest");
        
        servlet.doPost(request, response);
        
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(500, response.getStatusCode());
    }
    
    @Test
    public void postRunsTestAndReturnsResults() throws ServletException, IOException {
        request.setContextPath("");
        request.setRequestURI("/" + JUnitHttpServlet.TESTS_PREFIX 
                              + "/co.cantina.junit.http.examples.ExampleTest/successfulTest");
        
        servlet.doPost(request, response);
        
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"results\":[{\"grouping\":\"co.cantina.junit.http.examples.ExampleTest\"," 
                     + "\"name\":\"successfulTest\",\"type\":\"success\"}],\"successful\":true}", 
                     response.getOutputStreamContent());
    }
}

