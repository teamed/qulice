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
package com.qulice.ant;

import com.jcabi.log.Logger;
import com.qulice.spi.Environment;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Environment, passed from ant task to validators.
 * @since 0.13
 */
public final class AntEnvironment implements Environment {

    /**
     * Ant project.
     */
    private final Project project;
    /**
     * Sources dirs.
     */
    private final Path sources;
    /**
     * Classes dir (only one dir is supported).
     */
    private final File classes;
    /**
     * Classpath dirs and files.
     */
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final Path classpath;

    /**
     * Public ctor.
     * @param prjct Ant project
     * @param srcs Sources dirs
     * @param clss Classes dir
     * @param clsspth Classpath
     * @checkstyle ParameterNumber (5 lines)
     */
    public AntEnvironment(
        final Project prjct,
        final Path srcs,
        final File clss,
        final Path clsspth) {
        this.project = prjct;
        this.sources = srcs;
        this.classes = clss;
        this.classpath = clsspth;
    }

    @Override
    public File basedir() {
        return this.project.getBaseDir();
    }

    @Override
    public File tempdir() {
        return new File(this.basedir(), "temp");
    }

    @Override
    public File outdir() {
        return this.classes;
    }

    @Override
    public String param(final String name, final String value) {
        String property = this.project.getProperty(name);
        if (property == null) {
            property = value;
        }
        return property;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public ClassLoader classloader() {
        final List<URL> urls = new LinkedList<>();
        try {
            for (final String path : this.classpath()) {
                urls.add(
                    new File(path).toURI().toURL()
                );
            }
            urls.add(this.classes.toURI().toURL());
        } catch (final MalformedURLException ex) {
            throw new IllegalStateException("Failed to build URL", ex);
        }
        final URLClassLoader loader = AccessController.doPrivileged(
            new AntEnvironment.PrivilegedClassLoader(urls)
        );
        for (final URL url : loader.getURLs()) {
            Logger.debug(this, "Classpath: %s", url);
        }
        return loader;
    }

    @Override
    public Collection<String> classpath() {
        return Arrays.asList(this.classpath.list());
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<File> files(final String pattern) {
        final Collection<File> files = new LinkedList<>();
        final IOFileFilter filter = new WildcardFileFilter(pattern);
        for (final String dir : this.sources.list()) {
            final File source = new File(dir);
            if (source.exists() && source.isDirectory()) {
                files.addAll(
                    FileUtils.listFiles(
                        source,
                        filter,
                        DirectoryFileFilter.INSTANCE
                    )
                );
            }
        }
        return files;
    }

    @Override
    // @todo #337 Implement exclude and excludes for ant QuliceTask
    public boolean exclude(final String check, final String name) {
        return false;
    }

    @Override
    public Collection<String> excludes(final String checker) {
        return Collections.emptyList();
    }

    /**
     * Creates URL ClassLoadere in privileged block.
     */
    private static final class PrivilegedClassLoader implements
        PrivilegedAction<URLClassLoader> {
        /**
         * URLs for class loading.
         */
        private final List<URL> urls;

        /**
         * Constructor.
         * @param urls URLs for class loading.
         */
        private PrivilegedClassLoader(final List<URL> urls) {
            this.urls = urls;
        }

        @Override
        public URLClassLoader run() {
            return new URLClassLoader(
                this.urls.toArray(new URL[this.urls.size()]),
                Thread.currentThread().getContextClassLoader()
            );
        }
    }
}
