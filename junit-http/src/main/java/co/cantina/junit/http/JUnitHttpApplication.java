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

import co.cantina.junit.http.api.MethodNotFoundException;
import co.cantina.junit.http.api.InvalidPathException;
import co.cantina.junit.http.api.RunnerException;
import co.cantina.junit.http.util.TestUtils;
import co.cantina.junit.http.api.Path;
import co.cantina.junit.http.api.Summary;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.junit.runners.model.InitializationError;

/**
 * JUnitHttpApplication is the main application class for JUnit HTTP.
 */
public class JUnitHttpApplication {

    private final Map<String, JUnitHttpRunner> runners = new HashMap<>();

    /**
     * Run the {@link co.cantina.junit.http.JUnitHttpRunner#invokeAfterClassMethods() } for every
     * cached runner.
     *
     * @throws RunnerException If any of the test's @AfterClass methods throws an exception
     */
    public void destroy() throws RunnerException {
        runners.values().stream().forEach(JUnitHttpRunner::invokeAfterClassMethods);
    }

    /**
     * Return a runner from the cache, or instantiate one, add it to the cache, and return it if
     * one hasn't been requested for the group specified in the
     * {@link co.cantina.junit.http.api.Path}.
     *
     * @param testPath The path of the test to return the runner for
     * @return The runner for the specified path
     * @throws MethodNotFoundException If the test could not be found (no test class exists for that
     * grouping)
     * @throws RunnerException If the runner could not be created
     */
    public synchronized JUnitHttpRunner getRunner(final Path testPath)
        throws MethodNotFoundException, RunnerException {

        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final JUnitHttpRunner runner = runners.get(testPath.getGrouping());
            if (runner == null) {
                final Class<?> testClass = Class.forName(testPath.getGrouping(), true, classLoader);
                final JUnitHttpRunner newRunner = new JUnitHttpRunner(testClass);
                newRunner.invokeBeforeClassMethods();
                runners.put(testPath.getGrouping(), newRunner);
                return newRunner;
            }
            else {
                return runner;
            }
        }
        catch (InitializationError e) {
            throw new RunnerException("Could not initialize runner for: "
                                         + testPath.getGrouping(), e);
        }
        catch (ClassNotFoundException e) {
            throw new MethodNotFoundException(testPath);
        }
    }

    /**
     * Get the fixture at the specified path. The path should be to a resource available on the
     * classpath within a directory called "fixtures". The path should not include the "fixtures"
     * directory in the path components.
     *
     * @param path The path to the fixture
     * @return The fixture as a JsonNode
     * @throws IOException If the fixture could not be loaded
     */
    public JsonNode getData(final String path) throws IOException {
        return TestUtils.getTestData(path);
    }

    /**
     * Run the specified function using the context created by the specified path.
     *
     * @param path The path to the function
     * @param func The function to execute using the runner and parsed path
     * @return The runner Summary
     * @throws InvalidPathException If the path is invalid
     */
    public Summary run(final String path, final BiFunction<JUnitHttpRunner, Path, Summary> func)
        throws InvalidPathException {

        final Optional<Path> maybePath = Path.parse(path);

        if (maybePath.isPresent()) {
            Path testPath = maybePath.get();
            final JUnitHttpRunner runner = getRunner(testPath);
            return func.apply(runner, testPath);
        }
        else {
            throw new InvalidPathException(path);
        }
    }

    /**
     * Run the fixture at the specified path.
     *
     * @param path The path to the fixture. The path must follow the format specified by the
     * {@link Path#parse(java.lang.String)} method.
     *
     * @return The Summary representing the results of the test(s)
     * @throws InvalidPathException If the path is not valid
     */
    public Summary runFixture(final String path) throws InvalidPathException {
        return run(path, (runner, testPath) -> runner.runFixture(testPath));
    }

    /**
     * Run the test at the specified path.
     *
     * @param path The path to the test. The path must follow the format specified by the
     * {@link Path#parse(java.lang.String)} method.
     *
     * @return The Summary representing the results of the test(s)
     * @throws InvalidPathException If the path is not valid
     */
    public Summary runTest(final String path) throws InvalidPathException {
        return run(path, (runner, testPath) -> runner.runTests(testPath));
    }
}
