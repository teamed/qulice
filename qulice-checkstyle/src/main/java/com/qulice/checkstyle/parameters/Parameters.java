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
package com.qulice.checkstyle.parameters;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract parameters. Is used for Generic type parameters or
 *  method(constructor) arguments.
 *
 * @since 0.18.18
 */
public class Parameters {

    /**
     * Class, interface, constructor or method definition node.
     */
    private final DetailAST node;

    /**
     * Parent TokenType (TYPE_PARAMETERS or PARAMETERS).
     * @see com.puppycrawl.tools.checkstyle.api.TokenTypes
     */
    private final int parent;

    /**
     * Childs TokenType (TYPE_PARAMETER or PARAMETER_DEF).
     * @see com.puppycrawl.tools.checkstyle.api.TokenTypes
     */
    private final int childs;

    /**
     * Primary ctor.
     * @param node Class, interface, constructor or method definition node.
     * @param parent Parent TokenType (TYPE_PARAMETERS or PARAMETERS).
     * @param childs Childs TokenType (TYPE_PARAMETER or PARAMETER_DEF).
     */
    public Parameters(
        final DetailAST node, final int parent, final int childs
    ) {
        this.node = node;
        this.parent = parent;
        this.childs = childs;
    }

    /**
     * Return number of arguments.
     * @return Number of parameters.
     */
    public final int count() {
        final int result;
        final DetailAST params = this.node.findFirstToken(this.parent);
        if (params == null) {
            result = 0;
        } else {
            result = params.getChildCount(this.childs);
        }
        return result;
    }

    /**
     * Return parameters for this node.
     * @return Parameters for this node.
     */
    public final List<DetailAST> parameters() {
        final List<DetailAST> result;
        final int count = this.count();
        if (count == 0) {
            result = Collections.emptyList();
        } else {
            final DetailAST params = this.node.findFirstToken(this.parent);
            result = new ArrayList<>(count);
            DetailAST child = params.getFirstChild();
            while (child != null) {
                if (child.getType() == this.childs) {
                    result.add(child);
                }
                child = child.getNextSibling();
            }
        }
        return result;
    }
}
