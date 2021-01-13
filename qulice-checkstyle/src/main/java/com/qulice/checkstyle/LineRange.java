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

/**
 * Represent a line range. For example, a Java method can be described by an
 * instance of this class. The alpha line could be the method definition and
 * the omega line could be the end closing bracket.
 *
 * @since 0.16
 */
public final class LineRange {

    /**
     * The first (alpha) line number in the range.
     */
    private final int alpha;

    /**
     * The last (omega) line number in the range.
     */
    private final int omega;

    /**
     * Default constructor.
     * @param first The alpha line number.
     * @param last The omega line number.
     */
    public LineRange(final int first, final int last) {
        this.alpha = first;
        this.omega = last;
    }

    /**
     * Is the given line number within range.
     * @param line The given line number to check.
     * @return True if the given line number is within this range.
     */
    public boolean within(final int line) {
        return line >= this.first() && line <= this.last();
    }

    /**
     * Is the given range entirely within the LineRange. Example, given a
     * LineRange of [10, 50], the given range of [12,48] should be within
     * side that. And the method should return true.
     * @param range The given LineRange to check.
     * @return True if the given is entirely within this LineRange.
     */
    public boolean within(final LineRange range) {
        return range.first() >= this.first()
            && range.last()  <= this.last();
    }

    /**
     * Get the alpha line number.
     * @return The alpha line number.
     */
    public int first() {
        return this.alpha;
    }

    /**
     * Get the omega line number.
     * @return The omega line number.
     */
    public int last() {
        return this.omega;
    }
}
