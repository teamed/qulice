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
import com.qulice.checkstyle.CheckstyleValidator;
import com.qulice.findbugs.FindBugsValidator;
import com.qulice.pmd.PmdValidator;
import com.qulice.spi.Environment;
import com.qulice.spi.ResourceValidator;
import com.qulice.spi.ValidationException;
import com.qulice.spi.Validator;
import com.qulice.spi.Violation;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * Ant Task for Qulice.
 *
 * @since 0.13
 * @checkstyle ClassDataAbstractionCouplingCheck (170 lines)
 */
public final class QuliceTask extends Task {

    /**
     * Sources dirs.
     */
    private Path sources;

    /**
     * Classes dir (only one dir is supported).
     */
    private File classes;

    /**
     * Classpath dirs and files.
     */
    private Path classpath;

    /**
     * Set source dirs.
     * @param srcdr Source dirs
     */
    public void setSrcdir(final Path srcdr) {
        this.sources = srcdr;
    }

    /**
     * Set classes dir.
     * @param clssedr Classes dir
     */
    public void setClassesdir(final File clssedr) {
        this.classes = clssedr;
    }

    /**
     * Set classpath.
     * @param clsspth Classpath
     */
    public void setClasspath(final Path clsspth) {
        this.classpath = clsspth;
    }

    @Override
    public void execute() {
        super.execute();
        final Environment env = this.environment();
        try {
            final long start = System.nanoTime();
            QuliceTask.validate(env);
            Logger.info(
                this,
                "Qulice quality check completed in %[nano]s",
                System.nanoTime() - start
            );
        } catch (final ValidationException ex) {
            Logger.info(
                this,
                "Read our quality policy: http://www.qulice.com/quality.html"
            );
            throw new BuildException("Failure", ex);
        }
    }

    /**
     * Create Environment.
     * @return Environment.
     */
    private Environment environment() {
        if (this.sources == null) {
            throw new BuildException("srcdir not defined for QuliceTask");
        }
        if (this.classes == null) {
            throw new BuildException("classesdir not defined for QuliceTask");
        }
        if (this.classpath == null) {
            throw new BuildException("classpath not defined for QuliceTask");
        }
        return new AntEnvironment(
            this.getProject(),
            this.sources,
            this.classes,
            this.classpath
        );
    }

    /**
     * Validate and throws exception if there are any problems.
     * @param env Environment
     * @throws ValidationException If there are any problems.
     */
    private static void validate(final Environment env)
        throws ValidationException {
        final Collection<Violation> results = new LinkedList<>();
        final Collection<File> files = env.files("*.*");
        if (!files.isEmpty()) {
            final Collection<ResourceValidator> validators =
                QuliceTask.validators(env);
            for (final ResourceValidator validator : validators) {
                results.addAll(validator.validate(files));
            }
            for (final Violation result : results) {
                Logger.info(
                    QuliceTask.class,
                    "%s: %s[%s]: %s (%s)",
                    result.validator(),
                    result.file(),
                    result.lines(),
                    result.message(),
                    result.name()
                );
            }
        }
        for (final Validator validator : QuliceTask.validators()) {
            validator.validate(env);
        }
    }

    /**
     * Create collection of validators.
     * @return Collection of validators.
     */
    private static Collection<Validator> validators() {
        return Arrays.<Validator>asList(
            new FindBugsValidator()
        );
    }

    /**
     * Create collection of validators.
     * @param env Environment to use.
     * @return Collection of validators.
     */
    private static Collection<ResourceValidator> validators(
        final Environment env) {
        return Arrays.asList(
            new CheckstyleValidator(env),
            new PmdValidator(env)
        );
    }
}
