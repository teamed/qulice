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
package com.qulice.maven;

import com.qulice.spi.Environment;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.mockito.Mockito;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Mocker of {@link MavenProject}.
 * @since 0.4
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class MavenEnvironmentMocker {

    /**
     * Env mocker.
     */
    private final Environment.Mock ienv;

    /**
     * Project.
     */
    private MavenProjectMocker prj;

    /**
     * Plexus container, mock.
     */
    private final PlexusContainer container;

    /**
     * Xpath queries to test pom.xml.
     */
    private Collection<String> ass;

    /**
     * Public ctor.
     * @throws IOException If some IO problem inside
     */
    public MavenEnvironmentMocker() throws IOException {
        this.prj = new MavenProjectMocker();
        this.container = Mockito.mock(PlexusContainer.class);
        this.ass = new LinkedList<>();
        this.ienv = new Environment.Mock();
    }

    /**
     * Inject this object into plexus container.
     * @param role The role
     * @param hint The hint
     * @param object The object to return
     * @return This object
     * @throws Exception If something wrong happens inside
     */
    public MavenEnvironmentMocker inPlexus(final String role, final String hint,
        final Object object) throws Exception {
        Mockito.doReturn(object).when(this.container).lookup(role, hint);
        return this;
    }

    /**
     * With this project mocker.
     * @param mocker The project mocker
     * @return This object
     */
    public MavenEnvironmentMocker with(final MavenProjectMocker mocker) {
        this.prj = mocker;
        return this;
    }

    /**
     * With this file on board.
     * @param name File name related to basedir
     * @param content File content to write
     * @return This object
     * @throws IOException If some IO problem
     */
    public MavenEnvironmentMocker withFile(final String name,
        final String content) throws IOException {
        this.ienv.withFile(name, content);
        return this;
    }

    /**
     * With this file on board.
     * @param name File name related to basedir
     * @param bytes File content to write
     * @return This object
     * @throws IOException If some IO problem
     */
    public MavenEnvironmentMocker withFile(final String name,
        final byte[] bytes) throws IOException {
        this.ienv.withFile(name, bytes);
        return this;
    }

    /**
     * With list of xpath queries to validate pom.xml.
     * @param asserts Collection of xpath queries
     * @return This object
     */
    public MavenEnvironmentMocker withAsserts(
        final Collection<String> asserts) {
        this.ass = Collections.unmodifiableCollection(asserts);
        return this;
    }

    /**
     * Mock it.
     * @return The environment just mocked
     * @throws Exception If something wrong happens inside
     */
    public MavenEnvironment mock() throws Exception {
        StaticLoggerBinder.getSingleton().setMavenLog(Mockito.mock(Log.class));
        this.prj.inBasedir(this.ienv.basedir());
        final MavenProject project = this.prj.mock();
        final Environment parent = this.ienv;
        final MavenEnvironment env = Mockito.mock(MavenEnvironment.class);
        Mockito.doReturn(project).when(env).project();
        final Context context = Mockito.mock(Context.class);
        Mockito.doReturn(context).when(env).context();
        Mockito.doReturn(this.container).when(context).get(Mockito.anyString());
        Mockito.doReturn(this.ass).when(env).asserts();
        return new MavenEnvironment.Wrap(parent, env);
    }

}
