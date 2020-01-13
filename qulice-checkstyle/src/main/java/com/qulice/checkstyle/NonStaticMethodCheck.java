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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import java.util.regex.Pattern;

/**
 * Checks that non static method must contain at least one reference to
 * {@code this}.
 *
 * <p>If your method doesn't need {@code this} than why it is not
 * {@code static}?
 *
 * The exception here is when method has {@code @Override} annotation. There's
 * no concept of inheritance and polymorphism for static methods even if they
 * don't need {@code this} to perform the actual work.
 *
 * Another exception is when method is {@code abstract} or {@code native}.
 * Such methods don't have body so detection based on {@code this} doesn't
 * make sense for them.
 *
 * @since 0.3
 */
public final class NonStaticMethodCheck extends AbstractCheck {

    /**
     * Files to exclude from this check.
     * This is mostly to exclude JUnit tests.
     */
    private Pattern exclude = Pattern.compile("^$");

    /**
     * Exclude files matching given pattern.
     * @param excl Regexp of classes to exclude.
     */
    public void setExcludeFileNamePattern(final String excl) {
        this.exclude = Pattern.compile(excl);
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.METHOD_DEF,
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
        if (this.exclude.matcher(this.getFileContents().getFileName())
            .find()) {
            return;
        }
        if (TokenTypes.CLASS_DEF == ast.getParent().getParent().getType()) {
            this.checkClassMethod(ast);
        }
    }

    /**
     * Check that non static class method refer {@code this}. Methods that
     * are {@code native}, {@code abstract} or annotated with {@code @Override}
     * are excluded.  Additionally, if the method only throws an exception, it
     * too is excluded.
     * @param method DetailAST of method
     */
    private void checkClassMethod(final DetailAST method) {
        final DetailAST modifiers = method
            .findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null) {
            return;
        }
        final boolean onlythrow =
            method.branchContains(TokenTypes.LITERAL_THROW)
                && !method.branchContains(TokenTypes.LCURLY)
                && this.countSemiColons(method) == 1;
        if (!AnnotationUtil.containsAnnotation(method, "Override")
            && !isInAbstractOrNativeMethod(method)
            && !method.branchContains(TokenTypes.LITERAL_THIS)
            && !onlythrow) {
            final int line = method.getLineNo();
            this.log(
                line,
                // @checkstyle LineLength (1 line)
                "This method must be static, because it does not refer to \"this\""
            );
        }
    }

    /**
     * Determines whether a method is {@code abstract} or {@code native}.
     * @param method Method to check.
     * @return True if method is abstract or native.
     */
    private static boolean isInAbstractOrNativeMethod(final DetailAST method) {
        final DetailAST modifiers = method.findFirstToken(TokenTypes.MODIFIERS);
        return modifiers.branchContains(TokenTypes.ABSTRACT)
            || modifiers.branchContains(TokenTypes.LITERAL_NATIVE);
    }

    /**
     * Determines the number semicolons in a method excluding those in
     * comments.
     * @param method Method to count
     * @return The number of semicolons in the method as an int
     */
    private int countSemiColons(final DetailAST method) {
        final DetailAST openingbrace = method.findFirstToken(TokenTypes.SLIST);
        int count = 0;
        if (openingbrace != null) {
            final DetailAST closingbrace =
                openingbrace.findFirstToken(TokenTypes.RCURLY);
            final int lastline = closingbrace.getLineNo();
            final int firstline = openingbrace.getLineNo();
            final FileContents contents = this.getFileContents();
            for (int line = firstline - 1; line < lastline; line += 1) {
                if (!contents.lineIsBlank(line)
                    && !contents.lineIsComment(line)
                    && contents.getLine(line).contains(";")) {
                    count += 1;
                }
            }
        }
        return count;
    }
}
