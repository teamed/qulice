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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.reporting.exec.DefaultMavenPluginManagerHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Executor of plugins.
 *
 * @since 0.3
 */
public final class MojoExecutor {

    /**
     * Plugin manager.
     */
    private final MavenPluginManager manager;

    /**
     * Maven session.
     */
    private final MavenSession session;

    /**
     * Helper for plugin manager.
     */
    private final DefaultMavenPluginManagerHelper helper;

    /**
     * Public ctor.
     * @param mngr The manager
     * @param sesn Maven session
     */
    @SuppressWarnings("PMD.NonStaticInitializer")
    public MojoExecutor(final MavenPluginManager mngr,
        final MavenSession sesn) {
        this.manager = mngr;
        this.session = sesn;
        this.helper = new DefaultMavenPluginManagerHelper() {
            {
                this.mavenPluginManager = MojoExecutor.this.manager;
            }
        };
    }

    /**
     * Find and configure a mojor.
     * @param coords Maven coordinates,
     *  e.g. "com.qulice:maven-qulice-plugin:1.0"
     * @param goal Maven plugin goal to execute
     * @param config The configuration to set
     * @throws ValidationException If something is wrong inside
     */
    public void execute(final String coords, final String goal,
        final Properties config) throws ValidationException {
        final Plugin plugin = new Plugin();
        final String[] sectors = StringUtils.split(coords, ':');
        plugin.setGroupId(sectors[0]);
        plugin.setArtifactId(sectors[1]);
        plugin.setVersion(sectors[2]);
        final MojoDescriptor descriptor = this.descriptor(plugin, goal);
        try {
            this.helper.setupPluginRealm(
                descriptor.getPluginDescriptor(),
                this.session,
                Thread.currentThread().getContextClassLoader(),
                new LinkedList<String>(),
                new LinkedList<String>()
            );
        } catch (final PluginResolutionException ex) {
            throw new IllegalStateException("Plugin resolution problem", ex);
        } catch (final PluginContainerException ex) {
            throw new IllegalStateException("Can't setup realm", ex);
        }
        final Xpp3Dom xpp = Xpp3Dom.mergeXpp3Dom(
            this.toXppDom(config, "configuration"),
            this.toXppDom(descriptor.getMojoConfiguration())
        );
        final MojoExecution execution = new MojoExecution(descriptor, xpp);
        final Mojo mojo = this.mojo(execution);
        try {
            Logger.info(this, "Calling %s:%s...", coords, goal);
            mojo.execute();
        } catch (final MojoExecutionException ex) {
            throw new IllegalArgumentException(ex);
        } catch (final MojoFailureException ex) {
            throw new ValidationException(ex);
        } finally {
            this.manager.releaseMojo(mojo, execution);
        }
    }

    /**
     * Create descriptor.
     * @param plugin The plugin
     * @param goal Maven plugin goal to execute
     * @return The descriptor
     */
    private MojoDescriptor descriptor(final Plugin plugin, final String goal) {
        try {
            return this.helper.getPluginDescriptor(
                plugin,
                this.session.getCurrentProject().getRemotePluginRepositories(),
                this.session
            ).getMojo(goal);
        } catch (final PluginResolutionException ex) {
            throw new IllegalStateException("Can't resolve plugin", ex);
        } catch (final PluginDescriptorParsingException ex) {
            throw new IllegalStateException("Can't parse descriptor", ex);
        } catch (final InvalidPluginDescriptorException ex) {
            throw new IllegalStateException("Invalid plugin descriptor", ex);
        }
    }

    /**
     * Create mojo.
     * @param execution The execution
     * @return The mojo
     */
    private Mojo mojo(final MojoExecution execution) {
        final Mojo mojo;
        try {
            mojo = this.manager.getConfiguredMojo(
                Mojo.class, this.session, execution
            );
        } catch (final PluginConfigurationException ex) {
            throw new IllegalStateException("Can't configure MOJO", ex);
        } catch (final PluginContainerException ex) {
            throw new IllegalStateException("Plugin container failure", ex);
        }
        return mojo;
    }

    /**
     * Recuresively convert Properties to Xpp3Dom.
     * @param config The config to convert
     * @param name High-level name of it
     * @return The Xpp3Dom document
     * @see #execute(String,String,Properties)
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Xpp3Dom toXppDom(final Properties config, final String name) {
        final Xpp3Dom xpp = new Xpp3Dom(name);
        for (final Map.Entry<?, ?> entry : config.entrySet()) {
            if (entry.getValue() instanceof String) {
                final Xpp3Dom child = new Xpp3Dom(entry.getKey().toString());
                child.setValue(config.getProperty(entry.getKey().toString()));
                xpp.addChild(child);
            } else if (entry.getValue() instanceof String[]) {
                final Xpp3Dom child = new Xpp3Dom(entry.getKey().toString());
                for (final String val : String[].class.cast(entry.getValue())) {
                    final Xpp3Dom row = new Xpp3Dom(entry.getKey().toString());
                    row.setValue(val);
                    child.addChild(row);
                }
                xpp.addChild(child);
            } else if (entry.getValue() instanceof Collection) {
                final Xpp3Dom child = new Xpp3Dom(entry.getKey().toString());
                for (final Object val
                    : Collection.class.cast(entry.getValue())) {
                    final Xpp3Dom row = new Xpp3Dom(entry.getKey().toString());
                    if (val != null) {
                        row.setValue(val.toString());
                        child.addChild(row);
                    }
                }
                xpp.addChild(child);
            } else if (entry.getValue() instanceof Properties) {
                xpp.addChild(
                    this.toXppDom(
                        Properties.class.cast(entry.getValue()),
                        entry.getKey().toString()
                    )
                );
            } else {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid properties value at '%s'",
                        entry.getKey().toString()
                    )
                );
            }
        }
        return xpp;
    }

    /**
     * Recursively convert PLEXUS config to Xpp3Dom.
     * @param config The config to convert
     * @return The Xpp3Dom document
     * @see #execute(String,String,Properties)
     */
    private Xpp3Dom toXppDom(final PlexusConfiguration config) {
        final Xpp3Dom result = new Xpp3Dom(config.getName());
        result.setValue(config.getValue(null));
        for (final String name : config.getAttributeNames()) {
            try {
                result.setAttribute(name, config.getAttribute(name));
            } catch (final PlexusConfigurationException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        for (final PlexusConfiguration child : config.getChildren()) {
            result.addChild(this.toXppDom(child));
        }
        return result;
    }

}
