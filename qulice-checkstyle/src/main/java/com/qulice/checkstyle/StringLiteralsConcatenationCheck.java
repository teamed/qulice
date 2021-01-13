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
import java.util.LinkedList;
import java.util.List;

/**
 * Checks for not using concatenation of string literals in any form.
 *
 * <p>The following constructs are prohibited:
 *
 * <pre>
 * String a = "done in " + time + " seconds";
 * System.out.println("File not found: " + file);
 * x += "done";
 * </pre>
 *
 * <p>You should avoid string concatenation at all cost. Why? There are two
 * reasons: readability of the code and translateability. First of all it's
 * difficult to understand how the text will look after concatenation,
 * especially if the text is long and there are more than a few {@code +}
 * operators. Second, you won't be able to translate your text to other
 * languages later, if you don't have solid string literals.
 *
 * <p>There are two alternatives to concatenation: {@link StringBuilder}
 * and {@link String#format(String,Object[])}.
 *
 * @since 0.3
 */
public final class StringLiteralsConcatenationCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[] {TokenTypes.OBJBLOCK};
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
        final List<DetailAST> pluses = this.findChildAstsOfType(
            ast,
            TokenTypes.PLUS,
            TokenTypes.PLUS_ASSIGN
        );
        for (final DetailAST plus : pluses) {
            if (!this.findChildAstsOfType(
                plus,
                TokenTypes.STRING_LITERAL
            ).isEmpty()) {
                this.log(plus, "Concatenation of string literals prohibited");
            }
        }
    }

    /**
     * Recursively traverse the <code>tree</code> and return all ASTs subtrees
     * matching any type from <code>types</code>.
     * @param tree AST to traverse.
     * @param types Token types to match against.
     * @return All ASTs subtrees with token types matching any from
     *  <tt>types</tt>.
     * @see TokenTypes
     */
    private List<DetailAST> findChildAstsOfType(final DetailAST tree,
        final int... types) {
        final List<DetailAST> children = new LinkedList<>();
        DetailAST child = tree.getFirstChild();
        while (child != null) {
            if (StringLiteralsConcatenationCheck.isOfType(child, types)) {
                children.add(child);
            } else {
                children.addAll(this.findChildAstsOfType(child, types));
            }
            child = child.getNextSibling();
        }
        return children;
    }

    /**
     * Checks if this <code>ast</code> is of any type from <code>types</code>.
     * @param ast AST to check.
     * @param types Token types to match against.
     * @return True if of type, false otherwise.
     * @see TokenTypes
     */
    private static boolean isOfType(final DetailAST ast, final int... types) {
        boolean yes = false;
        for (final int type : types) {
            if (ast.getType() == type) {
                yes = true;
                break;
            }
        }
        return yes;
    }
}
