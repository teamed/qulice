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
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * Check for empty lines inside methods and constructors.
 *
 * <p>We believe that comments and empty lines are evil. If you need to use
 * an empty line in order to add a vertical separator of concepts - refactor
 * your code and make it more cohesive and readable. The bottom line is
 * that every method should look solid and do just <b>one thing</b>.
 *
 * This class is thread safe. It relies on building a list of line ranges by
 * visiting each method definition and each anonymous inner type. It stores
 * these references in a non-static thread local.
 *
 * @since 0.3
 */
public final class EmptyLinesCheck extends AbstractCheck {

    /**
     * Pattern for empty line check.
     */
    private static final Pattern PATTERN = Pattern.compile("^\\s*$");

    /**
     * Line ranges of all anonymous inner types.
     */
    private final LineRanges anons = new LineRanges();

    /**
     * Line ranges of all method and constructor bodies.
     */
    private final LineRanges methods = new LineRanges();

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.OBJBLOCK,
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
        this.getLine(ast.getLastChild().getLine());
        if (ast.getType() == TokenTypes.OBJBLOCK
            && ast.getParent() != null
            && ast.getParent().getType() == TokenTypes.LITERAL_NEW) {
            final DetailAST left = ast.getFirstChild();
            final DetailAST right = ast.getLastChild();
            if (left != null && right != null) {
                this.anons.add(
                    new LineRange(left.getLineNo(), right.getLineNo())
                );
            }
        } else if (ast.getType() == TokenTypes.METHOD_DEF
            || ast.getType() == TokenTypes.CTOR_DEF) {
            final DetailAST opening = ast.findFirstToken(TokenTypes.SLIST);
            if (opening != null) {
                final DetailAST right =
                    opening.findFirstToken(TokenTypes.RCURLY);
                this.methods.add(
                    new LineRange(opening.getLineNo(), right.getLineNo())
                );
            }
        }
    }

    @Override
    public void finishTree(final DetailAST root) {
        final String[] lines = this.getLines();
        for (int line = 0; line < lines.length; ++line) {
            if (this.methods.inRange(line + 1)
                && EmptyLinesCheck.PATTERN.matcher(lines[line]).find()
                && this.insideMethod(line + 1)) {
                this.log(line + 1, "Empty line inside method");
            }
        }
        this.methods.clear();
        this.anons.clear();
        super.finishTree(root);
    }

    /**
     * If this is within a valid anonymous class, make sure that is still
     * directly inside of a method of that anonymous inner class.
     * Note: This implementation only checks one level deep, as nesting
     * anonymous inner classes should never been done.
     * @param line The line to check if it is within a method or not.
     * @return True if the line is directly inside of a method.
     */
    private boolean insideMethod(final int line) {
        final int method = EmptyLinesCheck.linesBetweenBraces(
            line, this.methods::iterator, Integer.MIN_VALUE
        );
        final int clazz = EmptyLinesCheck.linesBetweenBraces(
            line, this.anons::iterator, Integer.MAX_VALUE
        );
        return method < clazz;
    }

    /**
     * Find number of lines between braces that contain a given line.
     * @param line Line to check
     * @param iterator Iterable of line ranges
     * @param def Default value if line is not within ranges
     * @return Number of lines between braces
     */
    private static int linesBetweenBraces(final int line,
        final Iterable<LineRange> iterator, final int def) {
        return StreamSupport.stream(iterator.spliterator(), false)
            .filter(r -> r.within(line))
            .min(Comparator.comparingInt(r -> r.last() - r.first()))
            .map(r -> r.last() - r.first())
            .orElse(def);
    }
}
