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

import com.google.common.collect.ImmutableList;

/**
 * Summary represents a full test run. Depending on the {@link co.cantina.junit.http.api.Path},
 * this could represent one or more tests.
 */
public class Summary {

    private final ImmutableList<Result> results;

    /**
     * A mutable builder for immutable TestRuns.
     */
    public static class Builder {

        private final ImmutableList.Builder<Result> results = ImmutableList.builder();

        /**
         * Create the RunSummary from the state of the Builder.
         *
         * @return The RunSummary
         */
        public Summary build() {
            return new Summary(this);
        }

        /**
         * @param result The result to add
         */
        public void addResult(final Result result) {
            results.add(result);
        }
    }

    /**
     * Create a new Builder.
     *
     * @return The Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create the TestRun from the state of the Builder.
     *
     * @param builder The Builder to copy the state from
     */
    private Summary(final Builder builder) {
        results = builder.results.build();
    }

    public ImmutableList<Result> getResults() {
        return results;
    }

    /**
     * @return True if there are no results which are instances of Failure; false otherwise
     */
    public boolean isSuccessful() {
        for (Result result : results) {
            if (result instanceof Failure) {
                return false;
            }
        }
        return true;
    }
}
