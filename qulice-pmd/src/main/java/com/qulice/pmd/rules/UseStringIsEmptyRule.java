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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTResultType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractInefficientZeroCheck;
import net.sourceforge.pmd.lang.java.symboltable.JavaNameOccurrence;
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import net.sourceforge.pmd.util.StringUtil;

/**
 * Rule to prohibit use of String.length() when checking for empty string.
 * String.isEmpty() should be used instead.
 * @since 0.18
 */
public final class UseStringIsEmptyRule extends AbstractInefficientZeroCheck {

    @Override
    public boolean appliesToClassName(final String name) {
        return StringUtil.isSame(name, "String", true, true, true);
    }

    @Override
    public Map<String, List<String>> getComparisonTargets() {
        final Map<String, List<String>> rules = new HashMap<>();
        rules.put("<", Arrays.asList("1"));
        rules.put(">", Arrays.asList("0"));
        rules.put("==", Arrays.asList("0"));
        rules.put("!=", Arrays.asList("0"));
        rules.put(">=", Arrays.asList("0", "1"));
        rules.put("<=", Arrays.asList("0"));
        return rules;
    }

    @Override
    public boolean isTargetMethod(final JavaNameOccurrence occ) {
        final NameOccurrence name = occ.getNameForWhichThisIsAQualifier();
        return name != null && "length".equals(name.getImage());
    }

    @Override
    public Object visit(
        final ASTVariableDeclaratorId variable, final Object data
    ) {
        final Node node = variable.getTypeNameNode();
        if (node instanceof ASTReferenceType) {
            final Class<?> clazz = variable.getType();
            final String type = variable.getNameDeclaration().getTypeImage();
            if (clazz != null && !clazz.isArray()
                && this.appliesToClassName(type)
            ) {
                final List<NameOccurrence> declarations = variable.getUsages();
                this.checkDeclarations(declarations, data);
            }
        }
        variable.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(
        final ASTMethodDeclaration declaration, final Object data
    ) {
        final ASTResultType result = declaration.getResultType();
        if (!result.isVoid()) {
            final ASTType node = (ASTType) result.jjtGetChild(0);
            final Class<?> clazz = node.getType();
            final String type = node.getTypeImage();
            if (clazz != null && !clazz.isArray()
                && this.appliesToClassName(type)
            ) {
                final Scope scope = declaration.getScope().getParent();
                final MethodNameDeclaration method = new MethodNameDeclaration(
                    declaration.getMethodDeclarator()
                );
                final List<NameOccurrence> declarations = scope
                    .getDeclarations(MethodNameDeclaration.class)
                    .get(method);
                this.checkDeclarations(declarations, data);
            }
        }
        declaration.childrenAccept(this, data);
        return data;
    }

    /**
     * Checks all uses of a variable or method with a String type.
     * @param occurrences Variable or method occurrences.
     * @param data Rule context.
     */
    private void checkDeclarations(
        final Iterable<NameOccurrence> occurrences, final Object data
    ) {
        for (final NameOccurrence occurrence : occurrences) {
            final JavaNameOccurrence jocc = (JavaNameOccurrence) occurrence;
            if (this.isTargetMethod(jocc)) {
                final JavaNode location = jocc.getLocation();
                final Node expr = location.getFirstParentOfType(
                    ASTExpression.class
                );
                this.checkNodeAndReport(
                    data, occurrence.getLocation(), expr.jjtGetChild(0)
                );
            }
        }
    }
}
