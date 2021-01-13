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
package com.qulice.spotbugs;

import com.qulice.spi.Environment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SpotBugsValidator}.
 * @since 0.19
 * @todo #884:30min Continue migration from FindBugs to SpotBugs. Implement
 *  SpotBugsValidator for this and then uncomment SpotBugsValidatorTest class
 *  so the minimal tests on this class can be run. Then, after all is set,
 *  remove FindBugs references from pom and from qulice executions.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@Disabled
public final class SpotBugsValidatorTest {

    /**
     * SpotBugsValidatorTest can pass correct files with no exceptions.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void passesCorrectFilesWithNoExceptions() throws Exception {
        final Environment env = new Environment.Mock()
            .withFile("src/main/java/Main.java", "class Main { int x = 0; }")
            .withDefaultClasspath();
        new SpotBugsValidator().validate(env);
    }

    /**
     * SpotBugs can exclude classes from check.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void excludesIncorrectClassFormCheck() throws Exception {
        final Environment env = new Environment.Mock()
            .withFile(
                "target/classes/Foo.class",
                "class Foo { public Foo clone() { return this; } }"
            ).withExcludes("Foo").withDefaultClasspath();
        new SpotBugsValidator().validate(env);
    }

    /**
     * FindbugsValidator can exclude several classes from check.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void excludesSeveralIncorrectClassFromCheck() throws Exception {
        final Environment env = new Environment.Mock()
            .withFile(
                "target/classes/Foo.java",
                "class Foo { public Foo clone() { return this; } }"
            ).withFile(
                "target/classes/Bar.java",
                "class Bar { public Bar clone() { return this; } }"
            ).withExcludes("Foo,Bar")
            .withDefaultClasspath();
        new SpotBugsValidator().validate(env);
    }
}
