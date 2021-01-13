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
package com.qulice.checkstyle;

import com.jcabi.aspects.Tv;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.cactoos.text.Joined;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Builder of {@code LICENSE.txt} content.
 * @since 0.4
 */
public final class LicenseRule implements TestRule {

    /**
     * The text.
     */
    private String[] lines;

    /**
     * EOL.
     */
    private String eol;

    /**
     * Package name.
     */
    private String pkg = "foo";

    /**
     * Directory for package-info.java.
     */
    private File directory;

    @Override
    public Statement apply(final Statement statement,
        final Description description) {
        return statement;
    }

    /**
     * Use this EOL.
     * @param txt What to use as end-of-line character
     * @return This object
     */
    public LicenseRule withEol(final String txt) {
        this.eol = txt;
        return this;
    }

    /**
     * Use this text (lines).
     * @param lns The lines to use
     * @return This object
     */
    public LicenseRule withLines(final String... lns) {
        this.lines = new String[lns.length];
        System.arraycopy(lns, 0, this.lines, 0, lns.length);
        return this;
    }

    /**
     * Use this package name.
     * @param name The name of package
     * @return This object
     */
    public LicenseRule withPackage(final String name) {
        this.pkg = name;
        return this;
    }

    /**
     * Save package-info.java into this folder.
     * @param dir The folder to save to
     * @return This object
     */
    public LicenseRule savePackageInfo(final File dir) {
        this.directory = dir;
        return this;
    }

    /**
     * Make a file.
     * @return The location of LICENSE.txt
     * @throws IOException If something wrong happens inside
     */
    public File file() throws IOException {
        final File license = File.createTempFile("LICENSE", ".txt");
        FileUtils.forceDeleteOnExit(license);
        FileUtils.writeStringToFile(
            license,
            new Joined(this.eol, this.lines).asString(),
            StandardCharsets.UTF_8
        );
        if (this.directory != null) {
            this.makePackageInfo(this.directory);
        }
        return license;
    }

    /**
     * Save package-info.java to the directory.
     * @param dir The directory
     * @throws IOException If something wrong happens inside
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private void makePackageInfo(final File dir) throws IOException {
        final File info = new File(dir, "package-info.java");
        final StringBuilder body = new StringBuilder(Tv.HUNDRED);
        body.append("/*").append(this.eol);
        for (final String line : this.lines) {
            body.append(" *");
            if (!line.isEmpty()) {
                body.append(' ').append(line);
            }
            body.append(this.eol);
        }
        body.append(" */").append(this.eol)
            .append("/**").append(this.eol)
            .append(" * Hm...").append(this.eol)
            .append(" */").append(this.eol)
            .append("package ").append(this.pkg)
            .append(';').append(this.eol);
        FileUtils.writeStringToFile(
            info,
            body.toString(),
            StandardCharsets.UTF_8
        );
    }

}
