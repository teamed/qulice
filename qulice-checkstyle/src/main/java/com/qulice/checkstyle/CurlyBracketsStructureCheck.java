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
package com.qulice.checkstyle;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.List;

/**
 * Checks node/closing curly brackets to be the last symbols on the line.
 *
 * <p>This is how a correct curly bracket structure should look like:
 *
 * <pre>
 * String[] array = new String[] {
 *      "first",
 *      "second"
 * };
 * </pre>
 *
 * or
 *
 * <pre>
 * String[] array = new String[] {"first", "second"};
 * </pre>
 *
 * <p>The motivation for such formatting is simple - we want to see the entire
 * block as fast as possible. When you look at a block of code you should be
 * able to see where it starts and where it ends.
 *
 * @since 0.6
 */
public final class CurlyBracketsStructureCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.ARRAY_INIT,
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return this.getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return this.getDefaultTokens();
    }

    @Override
    public void visitToken(final DetailAST ast) {
        if (ast.getType() == TokenTypes.ARRAY_INIT) {
            this.checkParams(ast);
        }
    }

    /**
     * Checks params statement to satisfy the rule.
     * @param node Tree node, containing containing array init statement.
     */
    private void checkParams(final DetailAST node) {
        final DetailAST closing = node.findFirstToken(TokenTypes.RCURLY);
        if (closing != null) {
            this.checkLines(node, node.getLineNo(), closing.getLineNo());
        }
    }

    /**
     * Checks params statement to satisfy the rule.
     * @param node Tree node, containing array init statement.
     * @param start First line
     * @param end Final line
     */
    private void checkLines(final DetailAST node, final int start,
        final int end) {
        if (start != end) {
            this.checkExpressions(
                CurlyBracketsStructureCheck.findAllChildren(
                    node,
                    TokenTypes.EXPR
                ),
                start,
                end
            );
        }
    }

    /**
     * Checks that all EXPR nodes satisfy the rule.
     * @param exprs Iterable of EXPR nodes
     * @param start First line of ARRAY_INIT node
     * @param end Final line ARRAY_INIT node (corresponds to RCURLY)
     */
    private void checkExpressions(final Iterable<DetailAST> exprs,
        final int start,
        final int end
    ) {
        for (final DetailAST expr : exprs) {
            final int pline = expr.getLineNo();
            if (pline == start) {
                this.log(pline, "Parameters should start on a new line");
            }
            final DetailAST last = expr.getLastChild();
            final int lline = last.getLineNo();
            if (lline == end) {
                this.log(lline, "Closing bracket should be on a new line");
            }
        }
    }

    /**
     * Search for all children of given type.
     * @param base Parent node to start from
     * @param type Node type
     * @return Iterable
     */
    private static Iterable<DetailAST> findAllChildren(final DetailAST base,
        final int type) {
        final List<DetailAST> children = Lists.newArrayList();
        DetailAST child = base.getFirstChild();
        while (child != null) {
            if (child.getType() == type) {
                children.add(child);
            }
            child = child.getNextSibling();
        }
        return children;
    }
}
