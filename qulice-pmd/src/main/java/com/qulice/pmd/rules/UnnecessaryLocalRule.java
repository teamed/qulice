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

import java.util.List;
import java.util.Map;
import net.sourceforge.pmd.lang.java.ast.ASTArgumentList;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTReturnStatement;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;

/**
 * Rule to check unnecessary local variables.
 *
 * @since 0.4
 */
public final class UnnecessaryLocalRule extends AbstractJavaRule {
    @Override
    public Object visit(final ASTMethodDeclaration meth, final Object data) {
        Object ndata = data;
        if (!meth.isAbstract() && !meth.isNative()) {
            ndata = super.visit(meth, data);
        }
        return ndata;
    }

    @Override
    public Object visit(final ASTReturnStatement rtn, final Object data) {
        final ASTVariableDeclarator name =
            rtn.getFirstChildOfType(ASTVariableDeclarator.class);
        if (name != null) {
            this.usages(rtn, data, name);
        }
        return data;
    }

    @Override
    public Object visit(final ASTArgumentList rtn, final Object data) {
        final List<ASTVariableDeclarator> names =
            rtn.findChildrenOfType(ASTVariableDeclarator.class);
        for (final ASTVariableDeclarator name : names) {
            this.usages(rtn, data, name);
        }
        return data;
    }

    /**
     * Report when number of variable usages is equal to zero.
     * @param node Node to check.
     * @param data Context.
     * @param name Variable name.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void usages(final JavaNode node, final Object data,
        final ASTVariableDeclarator name) {
        final Map<NameDeclaration, List<NameOccurrence>> vars = name
            .getScope().getDeclarations();
        // @checkstyle LineLength (1 line)
        for (final Map.Entry<NameDeclaration, List<NameOccurrence>> entry
            : vars.entrySet()) {
            final List<NameOccurrence> usages = entry.getValue();
            if (usages.size() > 1) {
                continue;
            }
            for (final NameOccurrence occ: usages) {
                if (occ.getLocation().equals(name)) {
                    this.addViolation(
                        data, node, new Object[]{name.getImage()}
                    );
                }
            }
        }
    }
}
