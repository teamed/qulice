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
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTag;
import com.qulice.checkstyle.parameters.Arguments;
import com.qulice.checkstyle.parameters.TypeParameters;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks method parameters order to comply with what is defined in method
 * javadoc.
 *
 * @since 0.18.10
 */
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.LongVariable"})
public final class JavadocParameterOrderCheck extends AbstractCheck {

    /**
     * Compiled regexp to match Javadoc tags that take an argument.
     */
    private static final Pattern MATCH_JAVADOC_ARG = Pattern.compile(
        "^\\s*(?>\\*|\\/\\*\\*)?\\s*@(param)\\s+(\\S+)\\s+\\S*"
    );

    /**
     * Compiled regexp to match first part of multilineJavadoc tags.
     */
    private static final Pattern MATCH_JAVADOC_ARG_MULTILINE_START =
        Pattern.compile(
            "^\\s*(?>\\*|\\/\\*\\*)?\\s*@(param)\\s+(\\S+)\\s*$"
        );

    /**
     * Compiled regexp to look for a continuation of the comment.
     */
    private static final Pattern MATCH_JAVADOC_MULTILINE_CONT =
        Pattern.compile("(\\*/|@|[^\\s\\*])");

    /**
     * Multiline finished at end of comment.
     */
    private static final String END_JAVADOC = "*/";

    /**
     * Multiline finished at next Javadoc.
     */
    private static final String NEXT_TAG = "@";

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.INTERFACE_DEF,
            TokenTypes.CLASS_DEF,
            TokenTypes.CTOR_DEF,
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
        final FileContents contents = this.getFileContents();
        final TextBlock doc = contents.getJavadocBefore(ast.getLineNo());
        if (doc != null) {
            this.checkParameters(ast, doc);
        }
    }

    /**
     * Returns the param tags in a javadoc comment.
     *
     * @param comment The Javadoc comment
     * @return The param tags found
     */
    private static List<JavadocTag> getMethodTags(final TextBlock comment) {
        final String[] lines = comment.getText();
        final List<JavadocTag> tags = new LinkedList<>();
        int current = comment.getStartLineNo() - 1;
        final int start = comment.getStartColNo();
        for (int line = 0; line < lines.length; line = line + 1) {
            current = current + 1;
            final Matcher docmatcher =
                MATCH_JAVADOC_ARG.matcher(lines[line]);
            final Matcher multiline =
                MATCH_JAVADOC_ARG_MULTILINE_START.matcher(lines[line]);
            if (docmatcher.find()) {
                final int col = calculateTagColumn(
                    docmatcher, line, start
                );
                tags.add(
                    new JavadocTag(
                        current,
                        col,
                        docmatcher.group(1),
                        docmatcher.group(2)
                    )
                );
            } else if (multiline.find()) {
                final int col =
                    calculateTagColumn(
                        multiline,
                        line,
                        start
                    );
                tags.addAll(
                    getMultilineArgTags(
                        multiline,
                        col,
                        lines,
                        line,
                        current
                    )
                );
            }
        }
        return tags;
    }

    /**
     * Calculates column number using Javadoc tag matcher.
     * @param matcher Found javadoc tag matcher
     * @param line Line number of Javadoc tag in comment
     * @param start Column number of Javadoc comment beginning
     * @return Column number
     */
    private static int calculateTagColumn(
        final Matcher matcher, final int line, final int start
    ) {
        int col = matcher.start(1) - 1;
        if (line == 0) {
            col += start;
        }
        return col;
    }

    /**
     * Gets multiline Javadoc tags with arguments.
     * @param matcher Javadoc tag Matcher
     * @param column Column number of Javadoc tag
     * @param lines Comment text lines
     * @param index Line number that contains the javadoc tag
     * @param line Javadoc tag line number in file
     * @return Javadoc tags with arguments
     * @checkstyle ParameterNumberCheck (30 lines)
     */
    private static List<JavadocTag> getMultilineArgTags(
        final Matcher matcher, final int column, final String[] lines,
        final int index, final int line) {
        final List<JavadocTag> tags = new ArrayList<>(0);
        final String paramone = matcher.group(1);
        final String paramtwo = matcher.group(2);
        int remindex = index + 1;
        while (remindex < lines.length) {
            final Matcher multiline =
                MATCH_JAVADOC_MULTILINE_CONT.matcher(lines[remindex]);
            if (multiline.find()) {
                remindex = lines.length;
                final String lfin = multiline.group(1);
                if (!lfin.equals(JavadocParameterOrderCheck.NEXT_TAG)
                    && !lfin.equals(JavadocParameterOrderCheck.END_JAVADOC)) {
                    tags.add(new JavadocTag(line, column, paramone, paramtwo));
                }
            }
            remindex = remindex + 1;
        }
        return tags;
    }

    /**
     * Checks method parameters order to comply with what is defined in method
     * javadoc.
     * @param ast The method node.
     * @param doc Javadoc text block.
     */
    private void checkParameters(final DetailAST ast, final TextBlock doc) {
        final List<JavadocTag> tags = getMethodTags(doc);
        final Arguments args = new Arguments(ast);
        final TypeParameters types = new TypeParameters(ast);
        final int count = args.count() + types.count();
        if (tags.size() == count) {
            final Consumer<JavadocTag> logger = (tag) -> this.log(
                tag.getLineNo(),
                // @checkstyle LineLength (1 line)
                "Javadoc parameter order different than method signature"
            );
            args.checkOrder(tags, logger);
            types.checkOrder(tags, logger);
        } else {
            this.log(
                ast.getLineNo(),
                // @checkstyle LineLength (1 line)
                "Number of javadoc parameters different than method signature"
            );
        }
    }
}
