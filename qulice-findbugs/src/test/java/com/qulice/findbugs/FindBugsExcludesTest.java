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
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.xml.HasXPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

/**
 * Test for exclusion filters.
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle LineLengthCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.18.15
 */
@SuppressWarnings("PMD.TooManyMethods")
class FindBugsExcludesTest {

    /**
     * Count matches.
     */
    public static final String COUNT_MATCH = "count(/FindBugsFilter/Match)";

    /**
     * Count classes.
     */
    public static final String COUNT_CLASS = "count(//Match/Class)";

    /**
     * Count methods.
     */
    public static final String COUNT_METHOD = "count(//Match/Method)";

    /**
     * Count filter nodes.
     */
    public static final String COUNT_FILTER = "count(/FindBugsFilter)";

    @Test
    public void ifCantCreateTempFile() {
        final Environment env = Mockito.mock(Environment.class);
        Mockito.when(env.tempdir()).thenReturn(new File("/dev/null"));
        Mockito.when(env.excludes("findbugs")).thenReturn(
            new ListOf<>("org.package.Class")
        );
        Assertions.assertThrows(
            IOException.class,
            new FindBugsExcludes(env)::asArguments,
            "Didn't throw an exception for invalid file"
        );
    }

    @Test
    public void noExcludesNoArguments() throws Exception {
        final List<String> arguments = new FindBugsExcludes(
            new Environment.Mock().withExcludes("")
        ).asArguments();
        MatcherAssert.assertThat(
            "The arguments were not empty for empty excludes",
            arguments,
            new IsEmptyCollection<>()
        );
    }

    @Test
    public void notEmptyExcludesShouldBeNotEmptyArguments() throws Exception {
        final List<String> arguments = new FindBugsExcludes(
            new Environment.Mock().withExcludes("Foo,Bar")
        ).asArguments();
        MatcherAssert.assertThat(
            "The argument was empty for not empty excludes",
            arguments,
            new IsNot<>(
                new IsEmptyCollection<>()
            )
        );
        MatcherAssert.assertThat(
            "Expected a single argument",
            arguments.size(),
            new IsEqual<>(1)
        );
        final Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(arguments.get(0));
        MatcherAssert.assertThat(
            doc,
            new HasXPath(
                FindBugsExcludesTest.COUNT_FILTER,
                new IsEqual<>("1")
            )
        );
    }

    @Test
    public void argumentIsReadableXml() throws Exception {
        final List<String> arguments = new FindBugsExcludes(
            new Environment.Mock().withExcludes("ClassToExclude,AnotherClass")
        ).asArguments();
        MatcherAssert.assertThat(
            arguments.isEmpty(),
            new IsEqual<>(false)
        );
        final Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(arguments.get(0));
        MatcherAssert.assertThat(
            doc,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_FILTER,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("2")
                )
            )
        );
    }

    @Test
    public void noExclude() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("0")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_METHOD,
                    new IsEqual<>("0")
                )
            )
        );
    }

    @Test
    public void emptyClassNotEmptyMethod() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes(":toString")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Method[@name='toString'])",
                    new IsEqual<>("1")
                )
            )
        );
    }

    @Test
    public void emptyClassNotEmptyMethodEmptyRule() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes(":equals:")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Method[@name='equals'])",
                    new IsEqual<>("1")
                )
            )
        );
    }

    @Test
    public void emptyClassEmptyMethodEmptyRule() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes(":,::,:::")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_FILTER,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("0")
                )
            )
        );
    }

    @Test
    public void singleClass() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("Foo")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Class[@name='Foo'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Foo']]/Method)",
                    new IsEqual<>("0")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Foo']]/Bug)",
                    new IsEqual<>("0")
                )
            )
        );
    }

    @Test
    public void multipleClasses() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("Alice:set:FI_EMPTY,Bob:get")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    "count(//Match)",
                    new IsEqual<>("2")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_CLASS,
                    new IsEqual<>("2")
                ),
                new HasXPath(
                    "count(//Method)",
                    new IsEqual<>("2")
                ),
                new HasXPath(
                    "count(//Bug)",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Class[@name='Alice'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Alice']]/Method[@name='set'])",
                    new IsEqual<>("1")
                )
            )
        );
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    "count(//Match[Class[@name='Alice'] and Method[@name='set']]/Bug[@pattern='FI_EMPTY'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Class[@name='Bob'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Bob']]/Method[@name='get'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Bob']]/Bug)",
                    new IsEqual<>("0")
                )
            )
        );
    }

    @Test
    public void classMethod() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("MyClass:myMethod")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            new HasXPath(
                "count(/FindBugsFilter/Match[Class[@name='MyClass']]/Method[@name='myMethod'])",
                new IsEqual<>("1")
            )
        );
    }

    @Test
    public void classMethodRule() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes(
                "Myclass:myMethod:DLS_DEAD_LOCAL_STORE"
            )
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Class[@name='Myclass'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match[Class[@name='Myclass'] and Method[@name='myMethod']]/Bug[@pattern='DLS_DEAD_LOCAL_STORE'])",
                    new IsEqual<>("1")
                )
            )
        );
    }

    @Test
    public void wholeClassOneRule() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("MyClass::FI_USELESS")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_CLASS,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Class[@name='MyClass'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_METHOD,
                    new IsEqual<>("0")
                ),
                new HasXPath(
                    "count(//Match/Bug[@pattern='FI_USELESS'])",
                    new IsEqual<>("1")
                )
            )
        );
    }

    @Test
    public void anyClassOneRule() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("::DM_EXIT")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_CLASS,
                    new IsEqual<>("0")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_METHOD,
                    new IsEqual<>("0")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/*)",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Bug[@pattern='DM_EXIT'])",
                    new IsEqual<>("1")
                )
            )
        );
    }

    @Test
    public void tooManyColonsAreIgnored() throws Exception {
        final Document xml = new FindBugsExcludes(
            new Environment.Mock().withExcludes("MyClass:myMethod:FI_EMPTY:::")
        ).asXml();
        MatcherAssert.assertThat(
            xml,
            Matchers.allOf(
                new HasXPath(
                    FindBugsExcludesTest.COUNT_FILTER,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    FindBugsExcludesTest.COUNT_MATCH,
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(/FindBugsFilter/Match/Class[@name='MyClass'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Method[@name='myMethod'])",
                    new IsEqual<>("1")
                ),
                new HasXPath(
                    "count(//Match/Bug[@pattern='FI_EMPTY'])",
                    new IsEqual<>("1")
                )
            )
        );
    }
}

