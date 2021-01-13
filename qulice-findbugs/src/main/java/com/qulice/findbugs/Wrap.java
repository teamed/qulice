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
package com.qulice.findbugs;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jcabi.aspects.Tv;
import com.mebigfatguy.fbcontrib.utils.BugType;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.PrintingBugReporter;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.config.UserPreferences;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Executed by {@link FindBugsValidator}, but in a new process.
 *
 * @since 0.3
 */
public final class Wrap {

    /**
     * Private constructor.
     */
    private Wrap() {
        // this class shouldn't be instantiated
    }

    /**
     * Run it.
     * @param args Arguments
     */
    public static void main(final String... args) {
        Wrap.run(args);
    }

    /**
     * Run it.
     * @param args Arguments
     * @todo #706:30min After fix issue mentioned in comment
     *  https://github.com/teamed/qulice/issues/706#issuecomment-225943946
     *  rollback the changes done in #706 then remove @Ignore above
     *  FindBugsValidatorTest.reportsIncorrectlyAddedThrows test
     *  and fix test findbugs-bed-bogus-exception-declaration.
     *  For the moment third item is done from comment
     *  https://github.com/teamed/qulice/issues/706#issuecomment-184611854
     */
    private static void run(final String... args) {
        final Project project =
            Wrap.project(args[0], args[1], args[2].split(","));
        try {
            Plugin.loadCustomPlugin(new File(args[Tv.THREE]), project);
        } catch (final PluginException ex) {
            throw new IllegalStateException(ex);
        }
        final FindBugs2 findbugs = new FindBugs2();
        findbugs.setProject(project);
        final Collection<String> ignored = Collections2.transform(
            Arrays.asList(BugType.values()),
                new Function<BugType, String>() {
                    @Override
                    public String apply(final BugType bug) {
                        final String name;
                        if (bug == null) {
                            name = "Missing";
                        } else {
                            name = bug.name();
                        }
                        return name;
                    }
                }
        );
        final BugReporter reporter = new PrintingBugReporter() {
            @Override
            protected void doReportBug(final BugInstance bug) {
                if (!ignored.contains(bug.getType())) {
                    super.doReportBug(bug);
                }
            }
        };
        reporter.getProjectStats().getProfiler().start(findbugs.getClass());
        reporter.setPriorityThreshold(Detector.LOW_PRIORITY);
        findbugs.setBugReporter(reporter);
        findbugs.setDetectorFactoryCollection(
            DetectorFactoryCollection.instance()
        );
        findbugs.setUserPreferences(
            UserPreferences.createDefaultUserPreferences()
        );
        findbugs.setNoClassOk(true);
        findbugs.setScanNestedArchives(true);
        try {
            if (args.length > Tv.FOUR) {
                findbugs.addFilter(args[Tv.FOUR], false);
            }
            findbugs.execute();
        } catch (final IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Create project.
     * @param basedir Base dir
     * @param outdir Output dir (with classes)
     * @param paths Paths of a classpath
     * @return The project
     */
    private static Project project(final String basedir, final String outdir,
        final String... paths) {
        final Project project = new Project();
        for (final String jar : paths) {
            if (!jar.equals(outdir)) {
                project.addAuxClasspathEntry(jar);
            }
        }
        project.addFile(outdir);
        project.addSourceDir(new File(basedir, "src/main/java").getPath());
        return project;
    }

}
