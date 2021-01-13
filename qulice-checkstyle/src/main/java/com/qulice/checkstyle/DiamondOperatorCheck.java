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
 * Checks if possible to use Diamond operator in generic instances creation.
 *
 * @since 0.17
 */
public final class DiamondOperatorCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
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
    public void visitToken(final DetailAST node) {
        final DetailAST generic = DiamondOperatorCheck
            .findFirstChildNodeOfType(
                node.findFirstToken(TokenTypes.TYPE), TokenTypes.TYPE_ARGUMENTS
            );
        final DetailAST assign = node.findFirstToken(TokenTypes.ASSIGN);
        final DetailAST instance;
        if (assign == null || generic == null) {
            instance = null;
        } else {
            instance = assign.getFirstChild().getFirstChild();
        }
        if (instance != null && instance.getType() == TokenTypes.LITERAL_NEW
            && DiamondOperatorCheck.isNotObjectBlock(instance)
            && DiamondOperatorCheck.isNotArray(instance)) {
            final DetailAST type =
                DiamondOperatorCheck.findFirstChildNodeOfType(
                    instance, TokenTypes.TYPE_ARGUMENTS
                );
            if (generic.equalsTree(type)) {
                log(type, "Use diamond operator");
            }
        }
    }

    /**
     * Checks if node is not array.
     *
     * @param node Node
     * @return True if not array
     */
    private static boolean isNotArray(final DetailAST node) {
        return node.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null;
    }

    /**
     * Checks if node is object block.
     *
     * @param node Node
     * @return True if not object block
     */
    private static boolean isNotObjectBlock(final DetailAST node) {
        return node.getLastChild().getType() != TokenTypes.OBJBLOCK;
    }

    /**
     * Returns the first child node of a specified type.
     *
     * @param node AST subtree to process.
     * @param type Type of token
     * @return Child node of specified type
     */
    private static DetailAST findFirstChildNodeOfType(
        final DetailAST node, final int type
    ) {
        DetailAST result = node.findFirstToken(type);
        if (result == null) {
            final DetailAST child = node.getFirstChild();
            if (child != null) {
                result = DiamondOperatorCheck
                    .findFirstChildNodeOfType(child, type);
            }
        }
        return result;
    }
}
