/**
 * Copyright (c) 2011-2016, Qulice.com
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
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 * Validates source files via <code>PMDValidator</code>.
 *
 * @author Dmitry Bashkin (dmitry.bashkin@qulice.com)
 * @version $Id$
 */
public final class SourceValidator {

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
    public SourceValidator(final Environment env) {
        this.context = new RuleContext();
        this.listener = new PmdListener(env);
        this.config = new PMDConfiguration();
    }

    /**
     * Performs validation of the input source files.
     * @param sources Input source files.
     * @param path Base path.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void validate(final Collection<DataSource> sources,
        final String path) {
        this.config.setRuleSets("com/qulice/pmd/ruleset.xml");
        final Report report = new Report();
        report.addListener(this.listener);
        this.context.setReport(report);
        for (final DataSource source : sources) {
            final String name = source.getNiceFileName(false, path);
            this.context.setSourceCodeFilename(name);
            this.context.setSourceCodeFile(new File(name));
            this.validateOne(source);
        }
    }

    /**
     * Returns violations to take place while validation.
     * @return Collection of violations.
     */
    public Collection<RuleViolation> getViolations() {
        return this.listener.getViolations();
    }

    /**
     * Performs validation of one file.
     * @param source Input source file
     */
    private void validateOne(final DataSource source) {
        final RuleSetFactory factory = new RuleSetFactory();
        // @checkstyle MagicNumber (1 line)
        factory.setMinimumPriority(RulePriority.valueOf(5));
        PMD.processFiles(
            this.config,
            factory,
            new LinkedList<DataSource>(Collections.singleton(source)),
            this.context,
            Collections.<Renderer>emptyList()
        );
    }
}
