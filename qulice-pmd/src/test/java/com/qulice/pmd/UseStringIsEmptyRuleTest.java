/*
 * Copyright (c) 2011-2020, Qulice.com
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
package com.qulice.pmd;

import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.IsEmptyString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link com.qulice.pmd.rules.UseStringIsEmptyRule}.
 * @since 0.18
 */
public final class UseStringIsEmptyRuleTest {

    /**
     * UseStringIsEmpty can detect when used String.length(), when checking for
     * empty string.
     * @param file File name.
     * @throws Exception If something goes wrong.
     */
    @ParameterizedTest
    @ValueSource(
        strings = {
            "StringLengthGreaterThanZero.java",
            "StringLengthGreaterOrEqualZero.java",
            "StringLengthGreaterOrEqualOne.java",
            "StringLengthLessThanOne.java",
            "StringLengthLessOrEqualZero.java",
            "StringLengthEqualsZero.java",
            "StringLengthNotEqualsZero.java"
        }
    )
    public void detectLengthComparisons(final String file) throws Exception {
        // @checkstyle MagicNumber (7 lines)
        new PmdAssert(
            file, new IsEqual<>(false),
            new CombinableMatcher<>(containsMatcher(file, 16))
                .and(containsMatcher(file, 20))
                .and(containsMatcher(file, 24))
                .and(containsMatcher(file, 28))
                .and(containsMatcher(file, 32))
        ).validate();
    }

    /**
     * UseStringIsEmpty not detect when used String[].length, when checking for
     *  empty string.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void notDetectOnArrayOfStrings() throws Exception {
        new PmdAssert(
            "ArrayOfStringsLengthGreaterThanZero.java",
            new IsEqual<>(true),
            new IsEmptyString()
        ).validate();
    }

    /**
     * Constructs StringContains matcher for error message.
     * @param file File name.
     * @param line Line number.
     * @return StringContains matcher.
     */
    private static Matcher<String> containsMatcher(
        final String file, final int line
    ) {
        final String message =
            "Use String.isEmpty() when checking for empty string";
        return new StringContains(
            String.format(
                "PMD: %1$s[%2$d-%2$d]: %3$s (UseStringIsEmptyRule)",
                file, line, message
            )
        );
    }
}
