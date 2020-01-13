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
package com.qulice.findbugs;

import com.google.common.base.Joiner;
import com.qulice.spi.Environment;
import com.qulice.spi.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link FindBugsValidator}.
 * @since 0.3
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class FindBugsValidatorTest {

    /**
     * FindbugsValidator can pass correct files with no exceptions.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void passesCorrectFilesWithNoExceptions() throws Exception {
        final Environment env = new Environment.Mock()
            .withFile("src/main/java/Main.java", "class Main { int x = 0; }")
            .withDefaultClasspath();
        new FindBugsValidator().validate(env);
    }

    /**
     * FindbugsValidator can report incorrectly added throws.
     * @throws Exception If something wrong happens inside
     */
    @Disabled
    @Test
    public void reportsIncorrectlyAddedThrows() throws Exception {
        Assertions.assertThrows(
            ValidationException.class,
            () -> {
                final byte[] bytecode = new BytecodeMocker()
                .withSource(
                    Joiner.on("\n").join(
                        "package test;",
                        "final class Main {",
                        "public void foo() throws InterruptedException {",
                        "System.out.println(\"test\");",
                        "}",
                        "}"
                    )
                ).mock();
                final Environment env = new Environment.Mock()
                    .withFile("target/classes/Main.class", bytecode)
                    .withDefaultClasspath();
                new FindBugsValidator().validate(env);
            }
        );
    }

    /**
     * FindbugsValidator can ignore correct throws.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void ignoresCorrectlyAddedThrows() throws Exception {
        final byte[] bytecode = new BytecodeMocker()
            .withSource(
                Joiner.on("\n").join(
                    "package test;",
                    "final class Main {",
                    "public void foo() throws InterruptedException {",
                    "Thread.sleep(1);",
                    "}",
                    "}"
                )
            )
            .mock();
        final Environment env = new Environment.Mock()
            .withFile("target/classes/Main.class", bytecode)
            .withDefaultClasspath();
        new FindBugsValidator().validate(env);
    }

    /**
     * FindbugsValidator throw exception for invalid file.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void throwsExceptionOnViolation() throws Exception {
        Assertions.assertThrows(
            ValidationException.class,
            () -> {
                final byte[] bytecode = new BytecodeMocker()
                    .withSource(
                        "class Foo { public Foo clone() { return this; } }"
                    ).mock();
                final Environment env = new Environment.Mock()
                    .withFile("target/classes/Foo.class", bytecode)
                    .withDefaultClasspath();
                new FindBugsValidator().validate(env);
            }
        );
    }

    /**
     * FindbugsValidator can exclude classes from check.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void excludesIncorrectClassFormCheck() throws Exception {
        final byte[] bytecode = new BytecodeMocker()
            .withSource("class Foo { public Foo clone() { return this; } }")
            .mock();
        final Environment env = new Environment.Mock()
            .withFile("target/classes/Foo.class", bytecode)
            .withExcludes("Foo")
            .withDefaultClasspath();
        new FindBugsValidator().validate(env);
    }

    /**
     * FindbugsValidator can exclude several classes from check.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void excludesSeveralIncorrectClassFromCheck() throws Exception {
        final byte[] bytecode = new BytecodeMocker()
            .withSource("class Foo { public Foo clone() { return this; } }")
            .mock();
        final byte[] another = new BytecodeMocker()
            .withSource("class Bar { public Bar clone() { return this; } }")
            .mock();
        final Environment env = new Environment.Mock()
            .withFile("target/classes/Foo.class", bytecode)
            .withFile("target/classes/Bar.class", another)
            .withExcludes("Foo,Bar")
            .withDefaultClasspath();
        new FindBugsValidator().validate(env);
    }

}
