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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks node/closing brackets to be the last symbols on the line.
 *
 * <p>This is how a correct bracket structure should look like:
 *
 * <pre>
 * String text = String.format(
 *   "some text: %s",
 *   new Foo().with(
 *     "abc",
 *     "foo"
 *   )
 * );
 * </pre>
 *
 * <p>The motivation for such formatting is simple - we want to see the entire
 * block as fast as possible. When you look at a block of code you should be
 * able to see where it starts and where it ends. In exactly the same way
 * we organize curled brackets.
 *
 * <p>In other words, when you open a bracket and can't close it at the same
 * line - you should leave it as the last symbol at this line.
 *
 * @since 0.3
 */
public final class BracketsStructureCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.LITERAL_NEW,
            TokenTypes.METHOD_CALL,
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
        if (ast.getType() == TokenTypes.METHOD_CALL
            || ast.getType() == TokenTypes.LITERAL_NEW) {
            this.checkParams(ast);
        } else {
            final DetailAST brackets = ast.findFirstToken(TokenTypes.LPAREN);
            if (brackets != null) {
                this.checkParams(brackets);
            }
        }
    }

    /**
     * Checks params statement to satisfy the rule.
     * @param node Tree node, containing method call statement.
     */
    private void checkParams(final DetailAST node) {
        final DetailAST closing = node.findFirstToken(TokenTypes.RPAREN);
        if (closing != null) {
            this.checkLines(node, node.getLineNo(), closing.getLineNo());
        }
    }

    /**
     * Checks params statement to satisfy the rule.
     * @param node Tree node, containing method call statement.
     * @param start First line
     * @param end Final line
     */
    private void checkLines(final DetailAST node, final int start,
        final int end) {
        if (start != end) {
            final DetailAST elist = node.findFirstToken(TokenTypes.ELIST);
            final int pline = elist.getLineNo();
            if (pline == start) {
                this.log(pline, "Parameters should start on a new line");
            }
            this.checkExpressionList(elist, end);
        }
    }

    /**
     * Checks expression list if closing bracket is on new line.
     * @param elist Tree node, containing expression list
     * @param end Final line
     */
    private void checkExpressionList(final DetailAST elist, final int end) {
        if (elist.getChildCount() > 0) {
            final DetailAST last = elist.getLastChild();
            final int lline = last.getLineNo();
            if (lline == end) {
                this.log(lline, "Closing bracket should be on a new line");
            }
        }
    }

}
