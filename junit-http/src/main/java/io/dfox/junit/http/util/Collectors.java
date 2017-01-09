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
package io.dfox.junit.http.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Collectors provides static methods for creating Collectors for Guava collections.
 */
public class Collectors {
    
    /**
     * Collectors cannot be instantiated.
     */
    private Collectors() { }
    
    /**
     * Create a new Collector for a Guava ImmutableList.
     * 
     * @param <T> The type of list
     * @return The Collector for a Guava ImmutableList
     */
    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
        return Collector.of(
            ImmutableList.Builder::new,
            (builder, e) -> builder.add(e),
            (b1, b2) -> b1.addAll(b2.build()),
            (builder) -> builder.build()
        );
    }
    
    /**
     * Create a new Collector for a Guava ImmutableMap.
     * 
     * @param <T> The type of value being collected
     * @param <K> The type of key
     * @param <U> The type of value
     * @param keyMapper The mapper for the keys of the map
     * @param valueMapper The mapper for the values of the map
     * @return The Collector for a Guava ImmutableMap
     */
    public static <T, K, U>
    Collector<T, ?, ImmutableMap<K, U>> toImmutableMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper) {
        
        return Collector.of(
            ImmutableMap.Builder<K, U> ::new,
            (builder, e) -> builder.put(keyMapper.apply(e), valueMapper.apply(e)),
            (b1, b2) -> b1.putAll(b2.build()),
            (builder) -> builder.build()
        );
    }
}
