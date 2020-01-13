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
package com.qulice.checkstyle;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import java.util.List;

/**
 * Checks that final class doesn't contain protected methods unless they are
 * overriding protected methods from superclass.
 *
 * @since 0.6
 */
public final class ProtectedMethodInFinalClassCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.CLASS_DEF,
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
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            final DetailAST modifiers = ast.findFirstToken(
                TokenTypes.MODIFIERS
            );
            if (modifiers.findFirstToken(TokenTypes.FINAL) != null) {
                this.checkMethods(ast);
            }
        }
    }

    /**
     * Checks methods in current class have no protected modifier.
     * @param ast DetailAST of CLASS_DEF
     */
    private void checkMethods(final DetailAST ast) {
        final DetailAST objblock = ast.findFirstToken(TokenTypes.OBJBLOCK);
        for (final DetailAST method
            : ProtectedMethodInFinalClassCheck.findAllChildren(
                objblock, TokenTypes.METHOD_DEF
            )
        ) {
            if (method
                .findFirstToken(TokenTypes.MODIFIERS)
                .findFirstToken(TokenTypes.LITERAL_PROTECTED) != null) {
                if (AnnotationUtil.containsAnnotation(method, "Override")) {
                    this.log(
                        method.getLineNo(),
                        "Protected method is overriding default scoped method"
                    );
                } else {
                    this.log(
                        method.getLineNo(),
                        "Final class should not contain protected methods"
                    );
                }
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
