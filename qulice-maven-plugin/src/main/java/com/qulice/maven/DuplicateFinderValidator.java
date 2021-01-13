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

import com.qulice.spi.ValidationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.commons.collections.CollectionUtils;

/**
 * Validate with maven-duplicate-finder-plugin.
 * @since 0.5
 * @todo #250 Maven-duplicate-finder-plugin should support exclusions.
 *  Let's add exclusions of following formats (examples):
 *  - duplicate:about.html
 *  - duplicate:org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M5
 *  - duplicate:org.codehaus.groovy.ast.expr.RegexExpression
 *  - duplicate:org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M5
 *  |xml-apis:xml-apis:1.0.0|about.html
 *  - duplicate:org.eclipse.sisu:org.eclipse.sisu.plexus:0.0.0.M5
 *  |xml-apis:xml-apis:1.0.0|org.w3c.dom.UserDataHandler
 *  See https://github.com/tpc2/qulice/issues/152#issuecomment-39028953
 *  and https://github.com/teamed/qulice/issues/250 for details
 */
public final class DuplicateFinderValidator implements MavenValidator {

    // @checkstyle MethodBodyCommentsCheck (50 lines)
    // @todo #250 Fix a problem with maven configuration of duplicate finder
    //  plugin in commented out code below, and enable
    //  duplicate-finder-ignore-deps IT in pom.xml.
    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void validate(final MavenEnvironment env)
        throws ValidationException {
        if (!env.exclude("duplicatefinder", "")) {
            final Properties props = new Properties();
            props.put("failBuildInCaseOfConflict", "true");
            props.put("checkTestClasspath", "false");
            props.put(
                "ignoredResources",
                CollectionUtils.union(
                    env.excludes("duplicatefinder"),
                    Arrays.asList("META-INF/.*", "module-info.class")
                )
            );
            final Collection<Properties> deps = new LinkedList<>();
            //  for (String sdep : env.excludes("duplicatefinder")) {
            //      if (StringUtils.countMatches(sdep, ":") == 2) {
            //          String[] parts = sdep.split(":");
            //          Properties main = new Properties();
            //          Properties prop = new Properties();
            //          prop.put("groupId", parts[0]);
            //          prop.put("artifactId", parts[1]);
            //          prop.put("version", parts[2]);
            //          main.put("dependency", prop);
            //          deps.add(prop);
            //      }
            //  }
            props.put("ignoredDependencies", deps);
            env.executor().execute(
                "com.ning.maven.plugins:maven-duplicate-finder-plugin:1.0.7",
                "check",
                props
            );
        }
    }

}
