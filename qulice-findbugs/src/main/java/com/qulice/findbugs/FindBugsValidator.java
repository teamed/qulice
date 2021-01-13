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

import com.google.common.collect.Iterables;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import com.mebigfatguy.fbcontrib.FBContrib;
import com.qulice.spi.Environment;
import com.qulice.spi.ValidationException;
import com.qulice.spi.Validator;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.formatStringChecker.FormatterNumberFormatException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.meta.When;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.jaxen.JaxenException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

/**
 * Validates source code and compiled binaries with FindBugs.
 *
 * @since 0.3
 */
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.AvoidDuplicateLiterals"})
public final class FindBugsValidator implements Validator {

    @Override
    public void validate(final Environment env) throws ValidationException {
        if (env.outdir().exists()) {
            if (!env.exclude("findbugs", "")) {
                this.check(this.findbugs(env));
            }
        } else {
            Logger.info(
                this,
                "No classes at %s, no FindBugs validation",
                env.outdir()
            );
        }
    }

    @Override
    public String name() {
        return "FindBugs";
    }

    /**
     * Start findbugs and return its output.
     * @param env Environment
     * @return Output of findbugs
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private String findbugs(final Environment env) {
        final List<String> args = new LinkedList<>();
        args.add("java");
        args.addAll(this.options());
        args.add(Wrap.class.getName());
        args.add(env.basedir().getPath());
        args.add(env.outdir().getPath());
        args.add(
            StringUtils.join(env.classpath(), ",").replace("\\", "/")
        );
        final Iterable<String> excludes = env.excludes("findbugs");
        args.add(this.jar(FBContrib.class).toString());
        if (excludes.iterator().hasNext()) {
            args.add(FindBugsValidator.excludes(env, excludes));
        }
        return new VerboseProcess(
            new ProcessBuilder(args), Level.INFO, Level.INFO
        ).stdout();
    }

    /**
     * Java options.
     * @return Options
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private Collection<String> options() {
        final Collection<String> opts = new LinkedList<>();
        opts.add("-classpath");
        opts.add(
            StringUtils.join(
                Arrays.asList(
                    this.jar(Wrap.class),
                    this.jar(FindBugs2.class),
                    this.jar(ClassFormatException.class),
                    this.jar(DocumentException.class),
                    this.jar(JaxenException.class),
                    this.jar(ClassNode.class),
                    this.jar(ClassVisitor.class),
                    this.jar(When.class),
                    this.jar(FormatterNumberFormatException.class),
                    this.jar(StringEscapeUtils.class),
                    this.jar(FBContrib.class),
                    this.jar(Iterables.class)
                ),
                System.getProperty("path.separator")
            ).replace("\\", "/")
        );
        return opts;
    }

    /**
     * Creates file with findbug excludes.
     * @param env Environment
     * @param excludes Iterable with exclude patterns
     * @return Path to file with findbug excludes
     */
    private static String excludes(final Environment env,
        final Iterable<String> excludes) {
        final String path = StringUtils.join(
            env.tempdir().getPath(),
            System.getProperty("path.separator"),
            "findbug_excludes_",
            String.valueOf(System.nanoTime()),
            ".xml"
        );
        try {
            FileUtils.writeStringToFile(
                new File(path),
                FindBugsValidator.generateExcludes(excludes),
                StandardCharsets.UTF_8
            );
        } catch (final IOException exc) {
            throw new IllegalStateException(
                "can't generate exclude rules for findbugs",
                exc
            );
        }
        return path;
    }

    /**
     * Creates xml with exclude patterns in findbugs native format.
     * @param excludes Exclude patterns
     * @return XML with findbugs excludes
     */
    @SuppressFBWarnings(
        value = "XFB_XML_FACTORY_BYPASS",
        justification = "No other way to create dom4j XMLWriter"
    )
    private static String generateExcludes(final Iterable<String> excludes) {
        final Document document = DocumentHelper.createDocument();
        final Element root = document
            .addElement("FindBugsFilter")
            .addElement("Match")
            .addElement("Or");
        for (final String exclude : excludes) {
            if (exclude != null) {
                root.addElement("Class").addAttribute("name", exclude);
            }
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new XMLWriter(out).write(document);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get address of our JAR or directory.
     * @param resource Name of resource
     * @return The file
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private File jar(final Class<?> resource) {
        final String name = resource.getName().replace(".", "/");
        final URL res = this.getClass().getResource(
            String.format("/%s.class", name)
        );
        if (res == null) {
            throw new IllegalStateException(
                String.format(
                    "can't find JAR for %s",
                    name
                )
            );
        }
        final String path = res.getFile().replaceAll("\\!.*$", "");
        final File file;
        if ("jar".equals(FilenameUtils.getExtension(path))) {
            file = new File(URI.create(path).getPath());
        } else {
            file = new File(path).getParentFile()
                .getParentFile()
                .getParentFile()
                .getParentFile();
        }
        Logger.debug(this, "#jar(%s): found at %s", resource.getName(), file);
        return file;
    }

    /**
     * Check report for errors.
     * @param report The report
     * @throws ValidationException If it contains errors
     */
    private void check(final String report) throws ValidationException {
        int total = 0;
        for (final String line
            : report.split(System.getProperty("line.separator"))) {
            if (line.matches("[a-zA-Z ]+: .*")) {
                Logger.warn(this, "FindBugs: %s", line);
                ++total;
            }
        }
        if (total > 0) {
            throw new ValidationException(
                "%d FindBugs violations (see log above)",
                total
            );
        }
    }

}
