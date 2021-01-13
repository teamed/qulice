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
package com.qulice.checkstyle.parameters;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTag;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Method or constructor arguments.
 *
 * @since 0.18.18
 */
public class Arguments {

    /**
     * Parameters.
     */
    private final Parameters parameters;

    /**
     * Secondary ctor.
     * @param node Constructor or method defenition node.
     */
    public Arguments(final DetailAST node) {
        this(
            new Parameters(
                node, TokenTypes.PARAMETERS, TokenTypes.PARAMETER_DEF
            )
        );
    }

    /**
     * Primary ctor.
     * @param parameters Parameters.
     */
    public Arguments(final Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Return number of arguments.
     * @return Number of arguments.
     */
    public final int count() {
        return this.parameters.count();
    }

    /**
     * Checks for consistency the order of arguments and their Javadoc
     *  parameters.
     * @param tags Javadoc parameter tags.
     * @param consumer Consumer accepts JavadocTag which is located out of
     *  order.
     */
    public final void checkOrder(
        final List<JavadocTag> tags, final Consumer<JavadocTag> consumer
    ) {
        final List<DetailAST> params = this.parameters.parameters();
        if (tags.size() < params.size()) {
            throw new IllegalStateException(
                // @checkstyle LineLength (1 lines)
                "Number of Javadoc parameters does not match the number of arguments"
            );
        }
        final Iterator<JavadocTag> iterator = tags.listIterator();
        for (final DetailAST param : params) {
            final String type =
                param.findFirstToken(TokenTypes.IDENT).getText();
            final JavadocTag tag = iterator.next();
            final String arg = tag.getFirstArg();
            if (!arg.equals(type)) {
                consumer.accept(tag);
            }
        }
    }
}
