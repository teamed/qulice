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
package com.qulice.pmd.rules;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTStatementExpression;
import net.sourceforge.pmd.lang.java.rule.AbstractJUnitRule;

/**
 * Rule to check plain assertions in JUnit tests.
 * @since 0.17
 */
public final class ProhibitPlainJunitAssertionsRule extends AbstractJUnitRule {

    /**
     * Mask of prohibited imports.
     */
    private static final String[] PROHIBITED = {
        "org.junit.Assert.assert",
        "junit.framework.Assert.assert",
    };

    @Override
    public Object visit(final ASTMethodDeclaration method, final Object data) {
        if (this.isJUnitMethod(method, data)
            && this.containsPlainJunitAssert(method.getBlock())) {
            this.addViolation(data, method);
        }
        return data;
    }

    @Override
    public Object visit(final ASTImportDeclaration imp, final Object data) {
        for (final String element : ProhibitPlainJunitAssertionsRule
            .PROHIBITED) {
            if (imp.getImportedName().contains(element)) {
                this.addViolation(data, imp);
                break;
            }
        }
        return super.visit(imp, data);
    }

    /**
     * Recursively verifies if node contains plain JUnit assert statements.
     * @param node Root statement node to search
     * @return True if statement contains plain JUnit assertions, false
     *  otherwise
     */
    private boolean containsPlainJunitAssert(final Node node) {
        boolean found = false;
        if (node instanceof ASTStatementExpression
            && ProhibitPlainJunitAssertionsRule.isPlainJunitAssert(node)) {
            found = true;
        }
        if (!found) {
            for (int iter = 0; iter < node.jjtGetNumChildren(); iter += 1) {
                final Node child = node.jjtGetChild(iter);
                if (this.containsPlainJunitAssert(child)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Tells if the statement is an assert statement or not.
     * @param statement Root node to search assert statements
     * @return True is statement is assert, false otherwise
     */
    private static boolean isPlainJunitAssert(final Node statement) {
        final ASTPrimaryExpression expression =
            ProhibitPlainJunitAssertionsRule.getChildNodeWithType(
                statement, ASTPrimaryExpression.class
            );
        final ASTPrimaryPrefix prefix =
            ProhibitPlainJunitAssertionsRule.getChildNodeWithType(
                expression, ASTPrimaryPrefix.class
            );
        final ASTName name = ProhibitPlainJunitAssertionsRule
            .getChildNodeWithType(prefix, ASTName.class);
        boolean assrt = false;
        if (name != null) {
            final String img = name.getImage();
            assrt = img != null && (img.startsWith("assert")
                || img.startsWith("Assert.assert"));
        }
        return assrt;
    }

    /**
     * Gets child node with specified type.
     * @param node Parent node
     * @param clazz Specified class
     * @param <T> Node type
     * @return Child node if exists, null otherwise
     */
    private static <T extends Node> T getChildNodeWithType(final Node node,
        final Class<T> clazz) {
        T expression = null;
        if (node != null && node.jjtGetNumChildren() > 0
            && clazz.isInstance(node.jjtGetChild(0))) {
            expression = clazz.cast(node.jjtGetChild(0));
        }
        return expression;
    }

}
