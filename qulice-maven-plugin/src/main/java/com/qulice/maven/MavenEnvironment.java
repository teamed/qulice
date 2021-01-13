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

import com.qulice.spi.Environment;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.context.Context;

/**
 * Environment, passed from MOJO to validators.
 *
 * @since 0.3
 */
@SuppressWarnings("PMD.TooManyMethods")
interface MavenEnvironment extends Environment {

    /**
     * Get project.
     * @return The project
     */
    MavenProject project();

    /**
     * Get properties.
     * @return The properties
     */
    Properties properties();

    /**
     * Get context.
     * @return The context
     */
    Context context();

    /**
     * Get plugin configuration properties.
     * @return The props
     */
    Properties config();

    /**
     * Get MOJO executor.
     * @return The executor
     */
    MojoExecutor executor();

    /**
     * Get xpath queries for pom.xml validation.
     * @return The asserts
     */
    Collection<String> asserts();

    /**
     * Wrapper of maven environment.
     */
    final class Wrap implements MavenEnvironment {
        /**
         * Parent environment.
         */
        private final Environment env;
        /**
         * Parent maven environment.
         */
        private final MavenEnvironment menv;
        /**
         * Public ctor.
         * @param penv Parent env
         * @param pmenv Parent maven env
         */
        Wrap(final Environment penv,
            final MavenEnvironment pmenv) {
            this.env = penv;
            this.menv = pmenv;
        }
        @Override
        public File basedir() {
            return this.env.basedir();
        }
        @Override
        public File tempdir() {
            return this.env.tempdir();
        }
        @Override
        public File outdir() {
            return this.env.outdir();
        }
        @Override
        public String param(final String name, final String value) {
            return this.env.param(name, value);
        }
        @Override
        public ClassLoader classloader() {
            return this.env.classloader();
        }
        @Override
        public Collection<String> classpath() {
            return this.env.classpath();
        }
        @Override
        public MavenProject project() {
            return this.menv.project();
        }
        @Override
        public Properties properties() {
            return this.menv.properties();
        }
        @Override
        public Context context() {
            return this.menv.context();
        }
        @Override
        public Properties config() {
            return this.menv.config();
        }
        @Override
        public MojoExecutor executor() {
            return this.menv.executor();
        }
        @Override
        public Collection<String> asserts() {
            return this.menv.asserts();
        }
        @Override
        public Collection<File> files(final String pattern) {
            return this.env.files(pattern);
        }
        @Override
        public boolean exclude(final String check, final String name) {
            return this.env.exclude(check, name);
        }
        @Override
        public Collection<String> excludes(final String checker) {
            return this.env.excludes(checker);
        }
    }
}
