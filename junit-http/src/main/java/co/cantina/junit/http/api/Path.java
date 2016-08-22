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
package co.cantina.junit.http.api;

import co.cantina.junit.http.util.Collectors;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
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
    private final Optional<String> name;
    
    /**
     * Create a TestPath. If name is all whitespace, empty, or null, an empty Optional will be used.
     * 
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     * @param name The name of the test. For JUnit tests, this is the test method name.
     */
    public Path(final String grouping, final String name) {
        this(grouping, Optional.ofNullable(StringUtils.stripToNull(name)));
    }

    /**
     * Create a TestPath with an empty name.
     * 
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     */
    public Path(final String grouping) {
        this(grouping, Optional.empty());
    }

    /**
     * @param grouping The group the test belongs to. For JUnit tests, this is the test class name.
     * @param name The name of the test. For JUnit tests, this is the test method name.
     */
    public Path(final String grouping, final Optional<String> name) {
        Validate.notEmpty(grouping, "grouping cannot be empty");
        Validate.notNull(name, "name cannot be null");
        
        this.grouping = grouping;
        this.name = name;
    }
    
    /**
     * Parse a String into a RunPath. The path is expected to be in the format 
     * &lt;grouping&gt;[/&lt;name&gt;].
     * 
     * @param path The string path
     * @return The test path or an empty Optional if the path is not valid or could not be parsed
     */
    public static Optional<Path> parse(final String path) {
        final ImmutableList<String> parts = Arrays.stream(path.split("/"))
            .map(s -> s.trim())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toImmutableList());
        
        switch (parts.size()) {
            case 1:
                return Optional.of(new Path(parts.get(0), Optional.empty()));
            case 2:
                return Optional.of(new Path(parts.get(0), Optional.of(parts.get(1))));
            default:
                return Optional.empty();
        }
    }

    public String getGrouping() {
        return grouping;
    }

    public Optional<String> getName() {
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
