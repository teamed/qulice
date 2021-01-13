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

import com.jcabi.log.Logger;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract mojo.
 *
 * @since 0.3
 */
public abstract class AbstractQuliceMojo extends AbstractMojo
    implements Contextualizable {

    /**
     * Environment to pass to validators.
     */
    private final DefaultMavenEnvironment environment =
        new DefaultMavenEnvironment();

    /**
     * Maven project, to be injected by Maven itself.
     */
    @Component
    private MavenProject project;

    /**
     * Maven session, to be injected by Maven itself.
     */
    @Component
    private MavenSession sess;

    /**
     * Maven plugin manager, to be injected by Maven itself.
     */
    @Component
    private MavenPluginManager manager;

    /**
     * Shall we skip execution?
     */
    @Parameter(property = "qulice.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Location of License file. If it is an absolute file name you should
     * prepend it with "file:" prefix. Otherwise it is treated like a resource
     * name and will be found in classpath (if available).
     */
    @Parameter(property = "qulice.license", defaultValue = "LICENSE.txt")
    private String license = "LICENSE.txt";

    /**
     * List of regular expressions to exclude.
     */
    @Parameter(property = "qulice.excludes")
    private final Collection<String> excludes = new LinkedList<>();

    /**
     * List of xpath queries to validate pom.xml.
     * @checkstyle IndentationCheck (5 lines)
     */
    @Parameter(
        property = "qulice.asserts",
        required = false
    )
    private final Collection<String> asserts = new LinkedList<>();

    /**
     * Set Maven Project (used mostly for unit testing).
     * @param proj The project to set
     */
    public final void setProject(final MavenProject proj) {
        this.project = proj;
    }

    /**
     * Set skip option (mostly for unit testing).
     * @param skp The "skip" option
     */
    public final void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * Set asserts option.
     * @param asser Asserts to use.
     */
    public final void setAsserts(final Collection<String> asser) {
        this.asserts.clear();
        this.asserts.addAll(asser);
    }

    /**
     * Set license address.
     * @param lcs The "license" option
     */
    public final void setLicense(final String lcs) {
        this.license = lcs;
    }

    /**
     * Set excludes.
     * @param exprs Expressions
     */
    public final void setExcludes(final Collection<String> exprs) {
        this.excludes.clear();
        this.excludes.addAll(exprs);
    }

    @Override
    public final void contextualize(final Context ctx) {
        this.environment.setContext(ctx);
    }

    @Override
    public final void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            this.getLog().info("Execution skipped");
            return;
        }
        this.environment.setProperty("license", this.license);
        this.environment.setProject(this.project);
        this.environment.setMojoExecutor(
            new MojoExecutor(this.manager, this.sess)
        );
        this.environment.setExcludes(this.excludes);
        this.environment.setAsser(this.asserts);
        final long start = System.nanoTime();
        this.doExecute();
        Logger.info(
            this,
            "Qulice quality check completed in %[nano]s",
            System.nanoTime() - start
        );
    }

    /**
     * Current maven session.
     * @return Current maven session
     */
    public final MavenSession session() {
        return this.sess;
    }

    /**
     * Do the real execution.
     * @throws MojoFailureException If some failure inside
     * @checkstyle NonStaticMethod (2 lines)
     */
    protected abstract void doExecute() throws MojoFailureException;

    /**
     * Get the environment.
     * @return The environment
     */
    protected final MavenEnvironment env() {
        return this.environment;
    }

}
