/*
 * Copyright (c) 2019 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini;

import io.reactivex.rxjava3.core.Flowable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Map.entry;

public interface Utils {

    /**
     * Returns the streams of concatenated streams
     *
     * @param streams the streams
     * @param <T>
     */
    static <T> Stream<T> concat(Stream<T>... streams) {
        return Stream.of(streams)
                .flatMap(Function.identity());
    }

    /**
     * Returns the collector of Map
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> entriesToMap() {
        return Collectors.toMap(Entry<K, V>::getKey, Entry<K, V>::getValue);
    }

    static <T> Optional<T> find(Stream<T> stream, Predicate<T> test) {
        return stream.dropWhile(Predicate.not(test)).limit(1).findAny();
    }

    static <K, V> Optional<V> getValue(Map<K, V> map, K value) {
        return Optional.ofNullable(map.get(value));
    }

    static <K, V> Function<K, Optional<V>> getValue(Map<K, V> map) {
        return key -> getValue(map, key);
    }

    /**
     * @param iterator iterator
     * @param <T>      the type of item
     */
    static <T> List<T> iter2List(Iterator<T> iterator) {
        return stream(iterator).collect(Collectors.toList());
    }

    static <T> Iterable<T> iterable(Iterator<T> iter) {
        return () -> iter;
    }

    static <K, V> Stream<Tuple2<K, V>> join(List<K> list1, List<V> list2) {
        return list1.stream().flatMap(k ->
                list2.stream().map(v -> Tuple2.of(k, v))
        );
    }

    static <K, K1, V> Function<Entry<K, V>, Entry<K1, V>> mapKey(Function<K, K1> mapper) {
        return entry -> entry(mapper.apply(entry.getKey()), entry.getValue());
    }

    static <K, V, V1> Function<Entry<K, V>, Entry<K, V1>> mapValue(Function<V, V1> mapper) {
        return entry -> entry(entry.getKey(), mapper.apply(entry.getValue()));
    }

    /**
     * @param opt
     * @param <T>
     */
    static <T> Flowable<T> optionalToFlow(Optional<T> opt) {
        return opt.map(Flowable::just).orElse(Flowable.empty());
    }

    /**
     * @param iterator
     * @param <T>
     * @return
     */
    static <T> Stream<T> stream(Iterator<T> iterator) {
        return Stream.iterate(iterator,
                        Iterator::hasNext,
                        y -> y)
                .map(Iterator::next);
    }

    /**
     * @param iterator
     * @param <T>
     * @return
     */
    static <T> List<T> toList(Iterator<T> iterator) {
        return stream(iterator).collect(Collectors.toList());
    }

    /**
     * Returns the list of an iterable
     *
     * @param iterable the iterable
     * @param <T>      the item type
     */
    static <T> List<T> toList(Iterable<T> iterable) {
        return stream(iterable.iterator()).collect(Collectors.toList());
    }

    /**
     * Returns the stream of an index, value of a list
     *
     * @param list the list
     * @param <T>  the item type
     */
    static <T> Stream<Tuple2<Integer, T>> zipWithIndex(List<T> list) {
        return IntStream.range(0, list.size())
                .mapToObj(i -> Tuple2.of(i, list.get(i)));
    }
}
