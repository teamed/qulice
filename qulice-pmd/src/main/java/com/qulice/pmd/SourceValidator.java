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
package com.qulice.pmd;

import com.jcabi.log.Logger;
import com.qulice.spi.Environment;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.util.ResourceLoader;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 * Validates source files via <code>PmdValidator</code>.
 *
 * @since 0.3
 */
final class SourceValidator {

    /**
     * Rule context.
     */
    private final RuleContext context;

    /**
     * Report listener.
     */
    private final PmdListener listener;

    /**
     * Rules.
     */
    private final PMDConfiguration config;

    /**
     * Creates new instance of <code>SourceValidator</code>.
     * @param env Environment
     */
    SourceValidator(final Environment env) {
        this.context = new RuleContext();
        this.listener = new PmdListener(env);
        this.config = new PMDConfiguration();
    }

    /**
     * Performs validation of the input source files.
     * @param sources Input source files.
     * @param path Base path.
     * @return Collection of violations.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<RuleViolation> validate(
        final Collection<DataSource> sources, final String path) {
        this.config.setRuleSets("com/qulice/pmd/ruleset.xml");
        this.config.setThreads(0);
        this.config.setMinimumPriority(RulePriority.LOW);
        this.config.setIgnoreIncrementalAnalysis(true);
        this.config.setShowSuppressedViolations(true);
        final Report report = new Report();
        report.addListener(this.listener);
        this.context.setReport(report);
        for (final DataSource source : sources) {
            final String name = source.getNiceFileName(false, path);
            Logger.debug(this, "Processing file: %s", name);
            this.context.setSourceCodeFilename(name);
            this.context.setSourceCodeFile(new File(name));
            this.validateOne(source);
        }
        report.errors().forEachRemaining(
            error -> Logger.error(
                this,
                "Processing error in %s: (%s) %s, %s[exception]",
                error.getFile(),
                error.getMsg(),
                error.getDetail(),
                error.getError()
            )
        );
        report.configErrors().forEachRemaining(
            error -> Logger.error(
                this,
                "Config error %s: %s",
                error.rule().getName(),
                error.issue()
            )
        );
        return this.listener.getViolations();
    }

    /**
     * Performs validation of one file.
     * @param source Input source file
     */
    private void validateOne(final DataSource source) {
        final RuleSetFactory factory = new RuleSetFactory(
            new ResourceLoader(),
            RulePriority.LOW,
            false,
            true
        );
        PMD.processFiles(
            this.config,
            factory,
            new LinkedList<>(Collections.singleton(source)),
            this.context,
            Collections.emptyList()
        );
    }
}
