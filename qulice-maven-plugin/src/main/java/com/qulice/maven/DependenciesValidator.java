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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.jcabi.log.Logger;
import com.qulice.spi.ValidationException;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalysis;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzer;
import org.apache.maven.shared.dependency.analyzer.ProjectDependencyAnalyzerException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.ContextException;

/**
 * Validator of dependencies.
 *
 * @since 0.3
 */
final class DependenciesValidator implements MavenValidator {

    /**
     * Separator between lines.
     */
    private static final String SEP = "\n\t";

    @Override
    public void validate(final MavenEnvironment env)
        throws ValidationException {
        final Collection<String> excludes = env.excludes("dependencies");
        if (!env.outdir().exists()
            || "pom".equals(env.project().getPackaging())
            || excludes.contains(".*")
            ) {
            Logger.info(this, "No dependency analysis in this project");
            return;
        }
        final Collection<String> unused = Collections2.filter(
            DependenciesValidator.unused(env),
            Predicates.not(new DependenciesValidator.ExcludePredicate(excludes))
        );
        if (!unused.isEmpty()) {
            Logger.warn(
                this,
                "Unused declared dependencies found:%s%s",
                DependenciesValidator.SEP,
                StringUtils.join(unused, DependenciesValidator.SEP)
            );
        }
        final Collection<String> used = Collections2.filter(
            DependenciesValidator.used(env),
            Predicates.not(new DependenciesValidator.ExcludePredicate(excludes))
        );
        if (!used.isEmpty()) {
            Logger.warn(
                this,
                "Used undeclared dependencies found:%s%s",
                DependenciesValidator.SEP,
                StringUtils.join(used, DependenciesValidator.SEP)
            );
        }
        final int failures = used.size() + unused.size();
        if (failures > 0) {
            throw new ValidationException(
                "%d dependency problem(s) found",
                failures
            );
        }
        Logger.info(this, "No dependency problems found");
    }

    /**
     * Analyze the project.
     * @param env The environment
     * @return The result of analysis
     */
    private static ProjectDependencyAnalysis analyze(
        final MavenEnvironment env) {
        try {
            return ((ProjectDependencyAnalyzer)
                ((PlexusContainer)
                    env.context().get(PlexusConstants.PLEXUS_KEY)
                ).lookup(ProjectDependencyAnalyzer.ROLE, "default")
            ).analyze(env.project());
        } catch (final ContextException | ComponentLookupException
            | ProjectDependencyAnalyzerException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Find unused artifacts.
     * @param env Environment
     * @return Collection of unused artifacts
     */
    private static Collection<String> used(final MavenEnvironment env) {
        final ProjectDependencyAnalysis analysis =
            DependenciesValidator.analyze(env);
        final Collection<String> used = new LinkedList<>();
        for (final Object artifact : analysis.getUsedUndeclaredArtifacts()) {
            used.add(artifact.toString());
        }
        return used;
    }

    /**
     * Find unused artifacts.
     * @param env Environment
     * @return Collection of unused artifacts
     */
    private static Collection<String> unused(final MavenEnvironment env) {
        final ProjectDependencyAnalysis analysis =
            DependenciesValidator.analyze(env);
        final Collection<String> unused = new LinkedList<>();
        for (final Object obj : analysis.getUnusedDeclaredArtifacts()) {
            final Artifact artifact = (Artifact) obj;
            if (!artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
                continue;
            }
            unused.add(artifact.toString());
        }
        return unused;
    }

    /**
     * Predicate for excluded dependencies.
     */
    private static class ExcludePredicate implements Predicate<String> {

        /**
         * List of excludes.
         */
        private final Collection<String> excludes;

        /**
         * Constructor.
         * @param excludes List of excludes.
         */
        ExcludePredicate(final Collection<String> excludes) {
            this.excludes = excludes;
        }

        @Override
        public boolean apply(final String name) {
            boolean ignore = false;
            for (final String exclude : this.excludes) {
                if (name.startsWith(exclude)) {
                    ignore = true;
                    break;
                }
            }
            return ignore;
        }
    }
}
