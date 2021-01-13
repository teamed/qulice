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
package com.qulice.spi;

import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Environment}.
 * @since 0.3
 */
public final class EnvironmentTest {

    /**
     * Environment interface can be mocked/instantiated with Mocker.
     * @throws Exception If something wrong happens inside.
     */
    @Test
    public void canBeInstantiatedWithMocker() throws Exception {
        final Environment env = new Environment.Mock();
        MatcherAssert.assertThat(env.basedir().exists(), Matchers.is(true));
        MatcherAssert.assertThat(env.tempdir().exists(), Matchers.is(true));
        MatcherAssert.assertThat(env.outdir().exists(), Matchers.is(true));
    }

    /**
     * EnvironmentMocker can create file.
     * @throws Exception If something wrong happens inside.
     */
    @Test
    public void writesFileContentToTheDesignatedLocation() throws Exception {
        final String name = "src/main/java/Main.java";
        final String content = "class Main {}";
        final Environment env = new Environment.Mock()
            .withFile(name, content);
        final File file = new File(env.basedir(), name);
        MatcherAssert.assertThat(file.exists(), Matchers.is(true));
    }

    /**
     * EnvironmentMocker can write bytearray too.
     * @throws Exception If something wrong happens inside.
     */
    @Test
    public void writesByteArrayToTheDesignatedLocation() throws Exception {
        final String name = "src/main/java/Foo.java";
        final byte[] bytes = "class Foo {}".getBytes();
        final Environment env = new Environment.Mock()
            .withFile(name, bytes);
        final File file = new File(env.basedir(), name);
        MatcherAssert.assertThat(file.exists(), Matchers.is(true));
    }

    /**
     * EnvironmentMocker can set classpath for the mock.
     * @throws Exception If something wrong happens inside.
     */
    @Test
    public void setsClasspathOnTheMock() throws Exception {
        final Environment env = new Environment.Mock();
        MatcherAssert.assertThat(
            env.classpath().size(),
            Matchers.greaterThan(0)
        );
    }

    /**
     * EnvironmentMocker can mock params.
     * @throws Exception If something wrong happens inside.
     */
    @Test
    public void configuresParametersInMock() throws Exception {
        final String name = "alpha";
        final String value = "some complex value";
        final Environment env = new Environment.Mock()
            .withParam(name, value);
        MatcherAssert.assertThat(env.param(name, ""), Matchers.equalTo(value));
    }

}
