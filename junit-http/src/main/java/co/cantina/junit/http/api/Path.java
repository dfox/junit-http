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
import static org.apache.commons.lang.StringUtils.stripToNull;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * RunPath encapsulates the path to a test. For JUnit tests, the grouping is the full canonical
 * name of the test class, and the name is the test method. If name is empty, the path represents
 * all tests in the test class.
 */
public class Path {

    private final String grouping;
    private final String name;


    /**
     * Create a TestPath with an empty name.
     *
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     */
    public Path(final String grouping) {
        this(grouping, null);
    }

    /**
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     * @param name The name of the test. For JUnit tests, this is the test method name.
     */
    public Path(final String grouping, final String name) {
        Validate.notEmpty(grouping, "grouping cannot be empty");

        this.grouping = grouping;
        this.name = stripToNull(name);
    }

    /**
     * Split the specified path by the '/' character, then trim each split string, returning a
     * new list containing all the strings which are non-empty.
     *
     * @param path The path to process
     * @return The list of trimmed, filters strings
     */
    private static ImmutableList<String> splitTrimAndFilter(final String path) {
        String[] parts = path.split("/");
        ImmutableList.Builder<String> trimmedAndFiltered = ImmutableList.builder();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                trimmedAndFiltered.add(trimmed);
            }
        }
        return trimmedAndFiltered.build();
    }

    /**
     * Parse a String into a RunPath. The path is expected to be in the format
     * &lt;grouping&gt;[/&lt;name&gt;].
     *
     * @param path The string path
     * @return The test path or null if the path is not valid or could not be parsed
     */
    public static Path parse(final String path) {
        final ImmutableList<String> parts = splitTrimAndFilter(path);

        switch (parts.size()) {
            case 1:
                return new Path(parts.get(0), null);
            case 2:
                return new Path(parts.get(0), parts.get(1));
            default:
                return null;
        }
    }

    public String getGrouping() {
        return grouping;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        else if (other == this) {
            return true;
        }
        else if (other.getClass() != getClass()) {
          return false;
        }
        else {
            Path otherPath = (Path) other;
            return new EqualsBuilder()
                .append(grouping, otherPath.grouping)
                .append(name, otherPath.name)
                .isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(grouping)
            .append(name)
            .toHashCode();
    }
}
