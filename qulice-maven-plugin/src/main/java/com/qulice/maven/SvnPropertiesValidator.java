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
import com.qulice.spi.ValidationException;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.cactoos.text.TextOf;
import org.cactoos.text.Trimmed;
import org.cactoos.text.UncheckedText;

/**
 * Check for required svn properties in all text files.
 *
 * <p>Every text file should have two SVN properties:
 *
 * <pre>
 * svn:keywords=Id
 * svn:eol-style=native
 * </pre>
 *
 * <p>Read SVN documentation about how you can set them.
 *
 * @see <a href="http://svnbook.red-bean.com/en/1.5/svn.ref.properties.html">Properties in Subversion</a>
 * @since 0.3
 */
public final class SvnPropertiesValidator implements MavenValidator {

    @Override
    public void validate(final MavenEnvironment env)
        throws ValidationException {
        if (SvnPropertiesValidator.isSvn(env.project())) {
            final File dir = new File(env.project().getBasedir(), "src");
            if (dir.exists()) {
                this.validate(dir);
            } else {
                Logger.info(
                    this,
                    "%s directory is absent, no need to check SVN properties",
                    dir
                );
            }
        } else {
            Logger.info(this, "This is not an SVN project");
        }
    }

    /**
     * Validate directory.
     * @param dir The directory
     * @throws ValidationException If fails
     */
    private void validate(final File dir) throws ValidationException {
        final Collection<File> files = FileUtils.listFiles(
            dir,
            new String[] {
                "java", "txt", "xsl", "xml", "html", "js", "css", "vm",
                "php", "py", "groovy", "ini", "properties", "bsh", "xsd", "sql",
            },
            true
        );
        int errors = 0;
        for (final File file : files) {
            if (!this.valid(file)) {
                ++errors;
            }
        }
        if (errors == 0) {
            Logger.info(
                this,
                "%d text files have all required SVN properties",
                files.size()
            );
        } else {
            Logger.info(
                this,
                "%d of %d files don't have required SVN properties",
                errors,
                files.size()
            );
            throw new ValidationException(
                "%d files with invalid SVN properties",
                errors
            );
        }
    }

    /**
     * Check whether this project uses SVN.
     * @param project The Maven project
     * @return TRUE if yes
     */
    private static boolean isSvn(final MavenProject project) {
        return project.getScm() != null
            && project.getScm().getConnection() != null
            && project.getScm().getConnection().startsWith("scm:svn");
    }

    /**
     * Check one file.
     * @param file The file to check
     * @return TRUE if valid
     */
    private boolean valid(final File file) {
        boolean valid = true;
        final String style = SvnPropertiesValidator.propget(
            file, "svn:eol-style"
        );
        if (!"native".equals(style)) {
            Logger.error(
                this,
                "File %s doesn't have 'svn:eol-style' set to 'native': '%s'",
                file,
                style
            );
            valid = false;
        }
        final String keywords = SvnPropertiesValidator.propget(
            file, "svn:keywords"
        );
        if (!keywords.contains("Id")) {
            Logger.error(
                this,
                "File %s doesn't have 'svn:keywords' with 'Id': '%s'",
                file,
                keywords
            );
            valid = false;
        }
        return valid;
    }

    /**
     * Get SVN property from the file.
     * @param file The file to check
     * @param name Property name
     * @return Property value
     */
    private static String propget(final File file, final String name) {
        final ProcessBuilder builder = new ProcessBuilder(
            "svn",
            "propget",
            name,
            file.getAbsolutePath()
        );
        builder.redirectErrorStream(true);
        try {
            final Process process = builder.start();
            process.waitFor();
            return new UncheckedText(
                new Trimmed(
                    new TextOf(
                        process.getInputStream()
                    )
                )
            ).asString();
        } catch (final java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

}
