/*
 * Copyright (c) 2011-2019, Qulice.com
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

import com.qulice.spi.Environment;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Encapsulates the exclusion rules.
 * @since 0.18.14
 */
public class FindBugsExcludes {

    /**
     * Environment.
     */
    private final Environment env;

    /**
     * Ctor.
     * @param env Environment
     */
    public FindBugsExcludes(final Environment env) {
        this.env = env;
    }

    /**
     * Converts the exclusion to the FindBugs exclusion filter XML.
     * http://findbugs.sourceforge.net/manual/filter.html
     * @return Document XML
     */
    public final Document asXml() {
        try {
            final Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();
            final FindBugsExcludes.Xml xml = new FindBugsExcludes.Xml(doc);
            final Node root = doc.appendChild(
                doc.createElement("FindBugsFilter")
            );
            this.env.excludes("findbugs").stream()
                .map(Pattern.compile(":")::split)
                .map(xml::createMatchNode)
                .filter(Node::hasChildNodes)
                .forEach(root::appendChild);
            return doc;
        } catch (final ParserConfigurationException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * Converts excludes to command argument
     * as a path to the file.
     * @return List of arguments (zero or one)
     * @throws IOException If temp file cannot be created
     */
    public final List<String> asArguments() throws IOException {
        final Document document = this.asXml();
        final List<String> arguments = new LinkedList<>();
        if (document.getDocumentElement().hasChildNodes()) {
            final File file = File.createTempFile(
                "findbug_excludes_",
                ".xml",
                this.env.tempdir()
            );
            try {
                TransformerFactory.newInstance()
                    .newTransformer()
                    .transform(
                        new DOMSource(document),
                        new StreamResult(file)
                    );
            } catch (final TransformerException exc) {
                throw new IOException(exc);
            }
            arguments.add(file.getAbsolutePath());
        }
        return arguments;
    }

    /**
     * XML Wrapper.
     */
    private static final class Xml {
        /**
         * Constant.
         */
        private static final String NAME = "name";
        /**
         * XML Document.
         */
        private final Document doc;

        /**
         * Ctor.
         * @param document W3C Document
         */
        private Xml(final Document document) {
            this.doc = document;
        }

        /**
         * Creates the <Match/> element for filter.
         * @param names Names of the expression (class, method, rule)
         * @return Node Match node
         */
        @SuppressWarnings("PMD.UseStringIsEmptyRule")
        private Node createMatchNode(final String... names) {
            final Node match = this.doc.createElement("Match");
            if (names.length > 0) {
                match.appendChild(
                    this.createChild(
                    "Class",
                    FindBugsExcludes.Xml.NAME,
                    names[0]
                ));
            }
            if (names.length > 1) {
                match.appendChild(
                    this.createChild(
                    "Method",
                    FindBugsExcludes.Xml.NAME,
                    names[1]
                ));
            }
            if (names.length > 2) {
                match.appendChild(
                    this.createChild(
                    "Bug",
                    "pattern",
                    names[2]
                ));
            }
            return match;
        }

        /**
         * Adds a child node.
         * @param tag Tag name of the child
         * @param key Attribute name
         * @param value Attribute value
         * @return Created child node
         */
        private Node createChild(final String tag, final String key,
            final String value) {
            final DocumentFragment fragment = this.doc.createDocumentFragment();
            if (!value.isEmpty()) {
                final Element child = this.doc.createElement(tag);
                child.setAttribute(key, value);
                fragment.appendChild(child);
            }
            return fragment;
        }
    }
}
