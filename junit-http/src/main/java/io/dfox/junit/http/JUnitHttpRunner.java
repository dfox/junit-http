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

import io.dfox.junit.http.api.Failure;
import io.dfox.junit.http.api.MethodNotFoundException;
import io.dfox.junit.http.api.RunnerException;
import io.dfox.junit.http.util.Collectors;
import io.dfox.junit.http.api.Summary;
import io.dfox.junit.http.api.Path;
import io.dfox.junit.http.api.Success;
import static io.dfox.junit.http.util.TestUtils.toStringList;
import com.google.common.collect.ImmutableMap;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import io.dfox.junit.http.api.Error;

/**
 * JUnitHttpRunner runs JUnit tests and converts the result to the HTTP API model.
 */
public class JUnitHttpRunner extends BlockJUnit4ClassRunner {
    
    private final ImmutableMap<Path, FrameworkMethod> testMethods;
    private final ImmutableMap<Path, FrameworkMethod> fixtureMethods;
    
    /**
     * Create a new JUnitHttpRunner.
     * 
     * @param testClass The class containing the JUnit tests.
     * 
     * @throws InitializationError If the runner cannot be initialized
     */
    public JUnitHttpRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);

        testMethods = getChildren()
            .stream()
            .collect(Collectors.toImmutableMap(m -> new Path(testClass.getName(), m.getName()), 
                UnaryOperator.identity()
            ));
        
        fixtureMethods = getTestClass().getAnnotatedMethods(Fixture.class)
            .stream()
            .collect(Collectors.toImmutableMap(m -> new Path(testClass.getName(), m.getName()), 
                UnaryOperator.identity()
            ));
    }
    
    /**
     * Invoke the methods on the test class with the specified annotations. 
     * 
     * @param annotation The annotation the methods to invoke are annotated with
     * @throws RunnerException If any of the methods throws an exception
     */
    private void invokeMethods(final Class<? extends Annotation> annotation) 
        throws RunnerException {
        
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
        for (FrameworkMethod method : methods) {
            try {
                method.invokeExplosively(null);
            }
            catch (Throwable e) {
                throw new RunnerException("Error invoking @" + annotation.getSimpleName() 
                                              + " method: " + method.getName(), e);
            }
        }
    }
    
    /**
     * Invoke the methods on the test class which are annotated with the @BeforeClass annotation.
     * 
     * @throws RunnerException If any of the methods throw an exception
     */
    public void invokeBeforeClassMethods() throws RunnerException {
        invokeMethods(BeforeClass.class);
    }
    
    /**
     * Invoke the methods on the test class which are annotated with the @AfterClass annotation.
     * 
     * @throws RunnerException If any of the methods throw an exception
     */
    public void invokeAfterClassMethods() throws RunnerException {
        invokeMethods(AfterClass.class);
    }
    
    /**
     * Run the test(s) at the specified path. If the path name is empty, run all the tests in the 
     * test class.
     * 
     * @param path The path to the test
     * @return The RunSummary containing the results of the test(s)
     */
    public Summary runFixtures(final Path path) {

        if (path.getName().isPresent()) {
            return runFixture(path);
        }
        else {
            Summary.Builder builder = Summary.builder();
            fixtureMethods.keySet()
                          .stream()
                          .forEach(existingPath -> runFixture(builder, existingPath));
            return builder.build();
        }
    }
    
    /**
     * Run the fixture at the specified path.
     * 
     * @param path The path to the fixture method
     * @param builder The builder to add the result to
     */
    private void runFixture(final Summary.Builder builder, final Path path) {
        
        final FrameworkMethod method = fixtureMethods.get(path);
        if (method == null) {
            throw new MethodNotFoundException(path);
        }
        else {
            try {
                method.invokeExplosively(createTest());
                builder.addResult(new Success(path.getGrouping(), path.getName().get()));
            }
            catch (Throwable e) {
                final Error error = new Error(
                    e.getClass().getName(),
                    Optional.ofNullable(e.getMessage())
                );
                builder.addResult(new Failure(
                    path.getGrouping(), 
                    path.getName().get(),
                    error,
                    toStringList(e.getStackTrace())
                ));
            }
        }
    }
    
    /**
     * Run the fixture at the specified path.
     * 
     * @param path The path to the fixture method
     * @return The summary of the result of running the fixtures
     */
    public Summary runFixture(final Path path) {
        Summary.Builder builder = Summary.builder();
        runFixture(builder, path);
        return builder.build();
    }
    
    /**
     * Run the specified test using the specified notifier.
     * 
     * @param notifier The notifier to capture results from the test
     * @param testPath The path to the test(s)
     * @throws MethodNotFoundException If the test(s) cannot be found
     */
    private void runTest(final RunNotifier notifier, final Path testPath) 
        throws MethodNotFoundException {
        
        final FrameworkMethod method = testMethods.get(testPath);
        if (method == null) {
            throw new MethodNotFoundException(testPath);
        }
        else {
            runChild(method, notifier);
        }
    }
    
    /**
     * Run the test(s) at the specified path. If the path name is empty, run all the tests in the 
     * test class.
     * 
     * @param path The path to the test
     * @return The RunSummary containing the results of the test(s)
     */
    public Summary runTests(final Path path) {
        final JunitHttpRunListener listener = new JunitHttpRunListener();
        
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);
        
        if (path.getName().isPresent()) {
            runTest(notifier, path);
        }
        else {
            testMethods.keySet().stream().forEach(existingPath -> runTest(notifier, existingPath));
        }
        return listener.getTestRun();
    }
}
