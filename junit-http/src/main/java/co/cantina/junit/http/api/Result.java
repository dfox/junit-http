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
package co.cantina.junit.http.api;

/**
 * Result is the interface all results in a {@link co.cantina.junit.http.api.Summary} must 
 * implement.
 */
public interface Result {
    
    /**
     * @return The type of the test. This should be the class name of the model, without the "Test" 
     * prefix in all lowercase. So if the result type is TestExplosion, this method should return
     * "explosion".
     */
    String getType();
        
    /**
     * @return The group the test belongs to. For JUnit tests, this is the test class name.
     */
    String getGrouping();

    /**
     * @return The name of the test. For JUnit tests, this is the test method name. 
     */
    String getName();
}
