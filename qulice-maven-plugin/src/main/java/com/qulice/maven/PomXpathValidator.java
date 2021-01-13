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

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.qulice.spi.Environment;
import com.qulice.spi.ValidationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

/**
 * Check pom.xml with XPath validation queries.
 *
 * <p>Restrictions:
 *
 * <ol>
 * <li>Each xpath component should contains namespace prefix pom:</li>
 * <li>Each xpath query should end with /text()</li>
 * </ol>
 *
 * @since 0.6
 */
public final class PomXpathValidator implements MavenValidator {

    @Override
    public void validate(final MavenEnvironment env)
        throws ValidationException {
        PomXpathValidator.validate(PomXpathValidator.pom(env), env.asserts());
    }

    /**
     * Validate pom against xpath queries.
     * @param pom POM
     * @param xpaths Xpath queries
     * @throws ValidationException validation exception
     */
    private static void validate(final XML pom, final Iterable<String> xpaths)
        throws ValidationException {
        for (final String xpath : xpaths) {
            if (pom.xpath(xpath).isEmpty()) {
                throw new ValidationException(
                    "pom.xml don't match the xpath query [%s]", xpath
                );
            }
        }
    }

    /**
     * Parse pom.xml.
     *
     * @param env Environment
     * @return XML instance
     */
    private static XML pom(final Environment env) {
        try {
            return new XMLDocument(
                FileUtils.readFileToString(
                    new File(
                        String.format(
                            "%s%s%s",
                            env.basedir(),
                            File.separator,
                            "pom.xml"
                        )
                    ),
                    StandardCharsets.UTF_8
                )
            ).registerNs("pom", "http://maven.apache.org/POM/4.0.0");
        } catch (final IOException exc) {
            throw new IllegalArgumentException(exc);
        }
    }
}
