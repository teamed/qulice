/**
 * Copyright (c) 2011-2013, Qulice.com
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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Environment.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Environment {

    /**
     * Get project's basedir.
     * @return The directory
     */
    File basedir();

    /**
     * Get directory to keep temporary files in.
     * @return The directory
     */
    File tempdir();

    /**
     * Get directory where <tt>.class</tt> files are stored.
     * @return The directory
     */
    File outdir();

    /**
     * Get parameter by name, and return default if it's not set.
     * @param name The name of parameter
     * @param value Default value to return as default
     * @return The value
     */
    String param(String name, String value);

    /**
     * Get classloader for this project.
     * @return The classloader
     */
    ClassLoader classloader();

    /**
     * Get list of paths in classpath.
     * @return The collection of paths
     * @todo #153 Using File as classpath element is dangerous, as it won't
     *  work under Windows without conversion - classpaths always use slashes,
     *  while File objects under Windows will always use backslashes. This
     *  method should return a collection of elements that are not affected
     *  by OS.
     */
    Collection<String> classpath();

    /**
     * Returns the files matching the specified pattern.
     *
     * <p>The pattern matching scheme used is wildcard matching. The characters
     * '?' and '*' represents single or multiple wildcard characters,
     * respectively. Pattern matching is case sensitive.
     *
     * @param pattern File name pattern.
     * @return Collection of files, matching the specified pattern.
     */
    Collection<File> files(String pattern);

    /**
     * Shall this item be excluded from report?
     * @param check Name of the check that is asking
     * @param name File or any other item, which is subject of validation
     * @return TRUE if it should be ignored
     */
    boolean exclude(String check, String name);

    /**
     * String with exclude patterns, separated by coma.
     * @param checker Name of the checker that is asking (pmd, codenarc ...)
     * @return Exclude patterns
     */
    String excludes(String checker);

    /**
     * Mock of {@link Environment}.
     */
    final class Mock implements Environment {
        /**
         * The basedir.
         */
        private final transient File basedir;
        /**
         * Files for classpath.
         */
        private final transient Set<String> classpath = new HashSet<String>(0);
        /**
         * Map of params.
         */
        private final transient ConcurrentMap<String, String> params =
            new ConcurrentHashMap<String, String>();
        /**
         * Exclude patterns.
         */
        private transient String excl;

        /**
         * Public ctor.
         * @throws IOException If some IO problem
         */
        public Mock() throws IOException {
            final File temp = File.createTempFile(
                "mock", ".qulice",
                new File(System.getProperty("java.io.tmpdir"))
            );
            if (!temp.delete()) {
                throw new IllegalStateException("files collision");
            }
            if (!temp.mkdirs()) {
                throw new IllegalStateException("mkdir failed");
            }
            FileUtils.forceDeleteOnExit(temp);
            this.basedir = new File(temp, "basedir");
            if (this.basedir.mkdirs()) {
                assert this.basedir != null;
            }
            this.classpath.add(
                this.outdir().getAbsolutePath().replace(File.separatorChar, '/')
            );
        }
        /**
         * With this param and its value.
         * @param name Param name
         * @param value Param value
         * @return This object
         */
        public Environment.Mock withParam(final String name,
            final String value) {
            this.params.put(name, value);
            return this;
        }
        /**
         * With this file on board.
         * @param name File name related to basedir
         * @param content File content to write
         * @return This object
         * @throws IOException If some IO problem
         */
        public Environment.Mock withFile(final String name,
            final String content)
            throws IOException {
            final File file = new File(this.basedir, name);
            FileUtils.writeStringToFile(file, content);
            return this;
        }

        /**
         * With this file on board.
         * @param name File name related to basedir
         * @param bytes File content to write
         * @return This object
         * @throws IOException If some IO problem
         */
        public Environment.Mock withFile(final String name,
            final byte[] bytes)
            throws IOException {
            final File file = new File(this.basedir, name);
            FileUtils.writeByteArrayToFile(file, bytes);
            return this;
        }

        /**
         * With exclude patterns.
         * @param excludes Exclude patterns
         * @return This object
         */
        public Environment.Mock withExcludes(final String excludes) {
            this.excl = excludes;
            return this;
        }
        /**
         * With default classpath.
         * @return This object
         */
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public Environment.Mock withDefaultClasspath() {
            Collections.addAll(
                this.classpath,
                System.getProperty("java.class.path")
                    .split(System.getProperty("path.separator"))
            );
            return this;
        }
        @Override
        public File basedir() {
            return this.basedir;
        }
        @Override
        public File tempdir() {
            final File file = new File(this.basedir, "target/tempdir");
            if (file.mkdirs()) {
                assert file != null;
            }
            return file;
        }
        @Override
        public File outdir() {
            final File file = new File(this.basedir, "target/classes");
            if (file.mkdirs()) {
                assert file != null;
            }
            return file;
        }
        @Override
        public String param(final String name, final String value) {
            String val = this.params.get(name);
            if (val == null) {
                val = value;
            }
            return val;
        }
        @Override
        public ClassLoader classloader() {
            return Thread.currentThread().getContextClassLoader();
        }
        @Override
        public Collection<String> classpath() {
            return Collections.unmodifiableCollection(this.classpath);
        }
        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public Collection<File> files(final String pattern) {
            final Collection<File> files = new LinkedList<File>();
            final IOFileFilter filter = new WildcardFileFilter(pattern);
            final String[] dirs = new String[] {
                "src",
            };
            for (final String dir : dirs) {
                final File sources = new File(this.basedir(), dir);
                if (sources.exists()) {
                    files.addAll(
                        FileUtils.listFiles(
                            sources,
                            filter,
                            DirectoryFileFilter.INSTANCE
                        )
                    );
                }
            }
            return files;
        }
        @Override
        public boolean exclude(final String check, final String name) {
            return false;
        }

        @Override
        public String excludes(final String checker) {
            return this.excl;
        }
    }
}
