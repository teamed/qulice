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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.List;
import java.util.Map;

/**
 * Checks the order of methods declaration.
 *
 * Right order is: public, protected and private
 * @since 0.6
 */
public final class MethodsOrderCheck extends AbstractCheck {

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
            TokenTypes.CLASS_DEF,
            TokenTypes.ENUM_DEF,
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
        if (ast.getType() == TokenTypes.CLASS_DEF
            || ast.getType() == TokenTypes.ENUM_DEF) {
            this.checkClass(ast);
        }
    }

    /**
     * Checks class definition to satisfy the rule.
     * @param node Tree node, containing class definition (CLASS_DEF).
     */
    private void checkClass(final DetailAST node) {
        final DetailAST obj = node.findFirstToken(TokenTypes.OBJBLOCK);
        if (obj != null) {
            this.checkOrder(
                MethodsOrderCheck.findAllChildren(
                    obj, TokenTypes.METHOD_DEF
                )
            );
        }
    }

    /**
     * Checks order of methods.
     *
     * @param methods Nodes representing class methods
     */
    private void checkOrder(final Iterable<DetailAST> methods) {
        MethodsOrderCheck.Modifiers prev = MethodsOrderCheck.Modifiers.PUB;
        for (final DetailAST method : methods) {
            final MethodsOrderCheck.Modifiers mtype =
                MethodsOrderCheck.getModifierType(method);
            if (mtype.getOrder() < prev.getOrder()) {
                this.log(
                    method.getLineNo(),
                    "Wrong method declaration order"
                );
            } else {
                prev = mtype;
            }
        }
    }

    /**
     * Get method modifier as enum {@code Modifiers}.
     * @param method DetailAST of method
     * @return Element of {@code Modifiers} enum
     */
    private static MethodsOrderCheck.Modifiers getModifierType(
        final DetailAST method
    ) {
        final DetailAST modifiers = method.findFirstToken(TokenTypes.MODIFIERS);
        final DetailAST modifier = Optional.fromNullable(
            modifiers.findFirstToken(
                MethodsOrderCheck.Modifiers.PUB.getType()
            )
        ).or(
            Optional.fromNullable(
                modifiers.findFirstToken(
                    MethodsOrderCheck.Modifiers.PROT.getType()
                )
            )
        ).or(
            Optional.fromNullable(
                modifiers.findFirstToken(
                    MethodsOrderCheck.Modifiers.PRIV.getType()
                )
            )
        ).orNull();
        final MethodsOrderCheck.Modifiers mod;
        if (modifier == null) {
            mod = MethodsOrderCheck.Modifiers.DEF;
        } else {
            mod = getByType(modifier.getType());
        }
        return mod;
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

    /**
     * Get Modifiers enum constant by TokenType id.
     * @param type TokenType
     * @return Modifiers constant
     */
    private static MethodsOrderCheck.Modifiers getByType(final int type) {
        return MethodsOrderCheck.Modifiers.mdos.get(type);
    }

    /**
     * Enumeration for constants of method modifiers.
     */
    private enum Modifiers {
        /**
         * PUBLIC method modifier.
         */
        PUB(TokenTypes.LITERAL_PUBLIC, 1),

        /**
         * PROTECTED method modifier.
         */
        PROT(TokenTypes.LITERAL_PROTECTED, 2),

        /**
         * DEFAULT method modifier.
         * No correspondent constant in TokenType.
         */
        DEF(-1, 3),

        /**
         * PRIVATE method modifier.
         */
        PRIV(TokenTypes.LITERAL_PRIVATE, 4);

        /**
         * Convenient map of {@code TokenType} on {@code Modifiers}.
         */
        private static Map<Integer, MethodsOrderCheck.Modifiers> mdos;

        static {
            MethodsOrderCheck.Modifiers.mdos =
                ImmutableMap.<Integer, MethodsOrderCheck.Modifiers>builder()
                    .put(PUB.getType(), PUB)
                    .put(PROT.getType(), PROT)
                    .put(-1, DEF)
                    .put(PRIV.getType(), PRIV)
                    .build();
        }

        /**
         * TokenType.
         */
        private final Integer type;

        /**
         * Order of modifier.
         */
        private final int order;

        /**
         * Constructor.
         * @param typ TokenType of DetailAST which represents modifier
         * @param ord Order of the modifier in class definition
         */
        Modifiers(final Integer typ, final Integer ord) {
            this.type = typ;
            this.order = ord;
        }

        /**
         * TokenType.
         * @return TokenType
         */
        public int getType() {
            return this.type;
        }

        /**
         * Order of modifier.
         * @return Order number
         */
        public int getOrder() {
            return this.order;
        }
    }
}
