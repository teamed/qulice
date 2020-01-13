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
package com.qulice.checkstyle;

import com.google.common.collect.Lists;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.qulice.spi.Environment;
import com.qulice.spi.ResourceValidator;
import com.qulice.spi.Violation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.text.Replaced;
import org.cactoos.text.TextOf;
import org.cactoos.text.Trimmed;
import org.xml.sax.InputSource;

/**
 * Validator with Checkstyle.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCoupling (260 lines)
 */
public final class CheckstyleValidator implements ResourceValidator {

    /**
     * Checkstyle checker.
     */
    private final Checker checker;

    /**
     * Listener of checkstyle messages.
      */
    private final CheckstyleListener listener;

    /**
     * Constructor.
     * @param env Environment to use.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public CheckstyleValidator(final Environment env) {
        this.checker = new Checker();
        this.checker.setClassLoader(env.classloader());
        this.checker.setModuleClassLoader(
            Thread.currentThread().getContextClassLoader()
        );
        try {
            this.checker.configure(this.configuration(env));
        } catch (final CheckstyleException ex) {
            throw new IllegalStateException("Failed to configure checker", ex);
        }
        this.listener = new CheckstyleListener(env);
        this.checker.addListener(this.listener);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Violation> validate(final Collection<File> files) {
        try {
            this.checker.process(Lists.newArrayList(files));
        } catch (final CheckstyleException ex) {
            throw new IllegalStateException("Failed to process files", ex);
        }
        final List<AuditEvent> events = this.listener.events();
        final Collection<Violation> results = new LinkedList<>();
        for (final AuditEvent event : events) {
            final String check = event.getSourceName();
            results.add(
                new Violation.Default(
                    this.name(),
                    check.substring(check.lastIndexOf('.') + 1),
                    event.getFileName(),
                    String.valueOf(event.getLine()),
                    event.getMessage()
                )
            );
        }
        return results;
    }

    @Override public String name() {
        return "Checkstyle";
    }

    /**
     * Load checkstyle configuration.
     * @param env The environment
     * @return The configuration just loaded
     * @see #validate(Collection)
     */
    private Configuration configuration(final Environment env) {
        final File cache =
            new File(env.tempdir(), "checkstyle/checkstyle.cache");
        final File parent = cache.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException(
                String.format(
                    "Unable to create directories needed for %s",
                    cache.getPath()
                )
            );
        }
        final Properties props = new Properties();
        props.setProperty("cache.file", cache.getPath());
        props.setProperty("header", this.header(env));
        final InputSource src = new InputSource(
            this.getClass().getResourceAsStream("checks.xml")
        );
        final Configuration config;
        try {
            config = ConfigurationLoader.loadConfiguration(
                src,
                new PropertiesExpander(props),
                ConfigurationLoader.IgnoredModulesOptions.OMIT
            );
        } catch (final CheckstyleException ex) {
            throw new IllegalStateException("Failed to load config", ex);
        }
        return config;
    }

    /**
     * Create header content, from file.
     * @param env The environment
     * @return The content of header
     * @see #configuration(Environment)
     */
    private String header(final Environment env) {
        final String name = env.param("license", "LICENSE.txt");
        final URL url = CheckstyleValidator.toUrl(env, name);
        final String content;
        try {
            content = new Replaced(
                new Trimmed(
                    new TextOf(
                        url.openStream()
                    )
                ),
                "[\\r\\n]+$",
                ""
            ).asString();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to read license", ex);
        }
        final StringBuilder builder = new StringBuilder(Tv.HUNDRED);
        final String eol = System.getProperty("line.separator");
        builder.append("/*").append(eol);
        for (final String line
            : StringUtils.splitPreserveAllTokens(content, eol)) {
            builder.append(" *");
            if (!line.trim().isEmpty()) {
                builder.append(' ').append(line.trim());
            }
            builder.append(eol);
        }
        builder.append(" */");
        final String license = builder.toString();
        Logger.debug(this, "LICENSE found: %s", url);
        Logger.debug(
            this,
            "LICENSE full text after parsing:\n%s",
            license
        );
        return license;
    }

    /**
     * Convert file name to URL.
     * @param env The environment
     * @param name The name of file
     * @return The URL
     * @see #header(Environment)
     */
    private static URL toUrl(final Environment env, final String name) {
        final URL url;
        if (name.startsWith("file:")) {
            try {
                url = new URL(name);
            } catch (final MalformedURLException ex) {
                throw new IllegalStateException("Invalid URL", ex);
            }
        } else {
            url = env.classloader().getResource(name);
            if (url == null) {
                throw new IllegalStateException(
                    String.format(
                        "'%s' resource is not found in classpath",
                        name
                    )
                );
            }
        }
        return url;
    }
}
