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
package com.qulice.maven;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DefaultMavenEnvironment} class.
 * @since 0.8
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class DefaultMavenEnvironmentTest {

    /**
     * DefaultMavenEnvironment can produce list of excludes.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void excludeAllFiles() throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        env.setExcludes(Collections.singletonList("codenarc:**/*.groovy"));
        MatcherAssert.assertThat(
            env.excludes("codenarc"),
            Matchers.contains("**/*.groovy")
        );
    }

    /**
     * DefaultMavenEnvironment can produce list of excludes from empty source.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void emptyExclude() throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        env.setExcludes(Collections.<String>emptyList());
        MatcherAssert.assertThat(
            env.excludes("codenarc").iterator().hasNext(),
            Matchers.is(false)
        );
    }

    /**
     * DefaultMavenEnvironment can produce list of excludes without excludes.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void noExclude() throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        MatcherAssert.assertThat(
            env.excludes("codenarc").iterator().hasNext(),
            Matchers.is(false)
        );
    }

    /**
     * DefaultMavenEnvironment can produce list of excludes.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void excludeSomeFiles() throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        env.setExcludes(
            ImmutableList.<String>builder()
                .add("codenarc:**/src/ex1/Main.groovy")
                .add("codenarc:**/src/ex2/Main2.groovy")
                .build()
        );
        MatcherAssert.assertThat(
            env.excludes("codenarc"),
            Matchers.containsInAnyOrder(
                "**/src/ex1/Main.groovy",
                "**/src/ex2/Main2.groovy"
            )
        );
    }

    /**
     * DefaultMavenEnvironment can work with whitespaces in classpath.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void passPathsWithWhitespaces()  throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getRuntimeClasspathElements())
            .thenReturn(
                Collections.singletonList("/Users/Carlos Miranda/git")
            );
        env.setProject(project);
        MatcherAssert.assertThat(
            env.classloader(),
            Matchers.notNullValue()
        );
    }

    /**
     * DefaultMavenEnvironment can produce empty collection when no matches
     * with checker.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public final void producesEmptyExcludesWhenNoMatches() throws Exception {
        final DefaultMavenEnvironment env = new DefaultMavenEnvironment();
        env.setExcludes(
            ImmutableList.of(
                "checkstyle:**/src/ex1/Main.groovy",
                "pmd:**/src/ex2/Main2.groovy"
            )
        );
        MatcherAssert.assertThat(
            env.excludes("dependencies"),
            Matchers.empty()
        );
    }
}
