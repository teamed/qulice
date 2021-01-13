/*
 * Copyright (c) 2011-2021, Qulice.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the Qulice.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qulice.checkstyle;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a set of LineRange objects. For example, an instance of this class
 * could represent all the line ranges for methods in a given Java source code
 * file.
 *
 * @since 0.16
 */
public final class LineRanges {

    /**
     * ArrayList of line ranges.
     */
    private final LineRanges.LocalCollection lines =
        new LineRanges.LocalCollection();

    /**
     * Adds a line range to the collection.
     * @param line The line range to add to the collection
     */
    public void add(final LineRange line) {
        this.lines.collection().add(line);
    }

    /**
     * Returns an iterator for this collection.
     * @return Iterator pointing to the internal collections elements.
     */
    public Iterator<LineRange> iterator() {
        return this.lines.collection().iterator();
    }

    /**
     * Detects if the given line number is within any of the line ranges.
     * @param line The given line number to check
     * @return True if the given line number is within any line range.
     */
    public boolean inRange(final int line) {
        return !this.lines.collection().isEmpty()
            && FluentIterable.from(this.lines.collection())
            .anyMatch(new LineRanges.LineWithAny(line));
    }

    /**
     * Gets the subset of LineRanges that are within all given ranges. Does
     * not return null; instead, returns empty range if there are no matches.
     * @param ranges The ranges to filter on.
     * @return Returns all LineRange elements that are within range.
     */
    public LineRanges within(final LineRanges ranges) {
        final LineRanges result = new LineRanges();
        final Iterator<LineRange> iterator = ranges.iterator();
        while (iterator.hasNext()) {
            final LineRange next = iterator.next();
            for (final LineRange line : this.lines.collection()) {
                if (next.within(line)) {
                    result.add(line);
                }
            }
        }
        return result;
    }

    /**
     * Clears the collection.
     */
    public void clear() {
        this.lines.collection().clear();
    }

    /**
     * Predicate to determine if a given line is within range of any of
     * the line ranges.
     */
    private static final class LineWithAny implements Predicate<LineRange> {

        /**
         * The given line.
         */
        private final int given;

        /**
         * Default constructor.
         * @param line The given line to check against all the line ranges.
         */
        private LineWithAny(final int line) {
            this.given = line;
        }

        @Override
        public boolean apply(final LineRange range) {
            return range != null && range.within(this.given);
        }
    }

    /**
     * Thread-safe collection of line ranges.
     */
    private static final class LocalCollection
        extends ThreadLocal<Collection<LineRange>> {

        /**
         * Internal Collection.
         */
        private final transient Collection<LineRange> ranges =
            new ArrayList<>(20);

        /**
         * Get the collection specific to the current thread only.
         * @return The collection for this thread.
         */
        public Collection<LineRange> collection() {
            return this.ranges;
        }
    }
}
