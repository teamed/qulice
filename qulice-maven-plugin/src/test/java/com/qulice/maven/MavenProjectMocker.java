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

import java.io.File;
import org.apache.maven.model.Build;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.mockito.Mockito;

/**
 * Mocker of {@link MavenProject}.
 * @since 0.4
 */
public final class MavenProjectMocker {

    /**
     * Mock of project.
     */
    private final MavenProject project =
        Mockito.mock(MavenProject.class);

    /**
     * In this basedir.
     * @param dir The directory
     * @return This object
     * @throws Exception If something wrong happens inside
     */
    public MavenProjectMocker inBasedir(final File dir) throws Exception {
        Mockito.doReturn(dir).when(this.project).getBasedir();
        final Build build = Mockito.mock(Build.class);
        Mockito.doReturn(build).when(this.project).getBuild();
        Mockito.doReturn(new File(dir, "target").getPath())
            .when(build).getOutputDirectory();
        return this;
    }

    /**
     * Mock it.
     * @return The mock
     * @throws Exception If something wrong happens inside
     */
    public MavenProject mock() throws Exception {
        Mockito.doReturn("jar").when(this.project).getPackaging();
        final Scm scm = new Scm();
        scm.setConnection("scm:svn:...");
        Mockito.doReturn(scm).when(this.project).getScm();
        return this.project;
    }

}
