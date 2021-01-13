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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Check if the class/interface javadoc contains properly formatted author
 * and version tags.
 *
 * <p>Correct format is the following (of a class javadoc):
 *
 * <pre>
 * &#47;**
 *  * This is my new class.
 *  *
 *  * &#64;author John Doe (john&#64;example.com)
 *  * &#64;version &#36;Id&#36;
 *  *&#47;
 * public final class Foo {
 *     // ...
 * </pre>
 *
 * <p>"&#36;Id&#36;" will be replaced by a full text automatically
 * by Subversion as explained in their documentation (see link below).
 *
 * @see <a href="http://svnbook.red-bean.com/en/1.4/svn.advanced.props.special.keywords.html">Keywords substitution in Subversion</a>
 * @since 0.3
 */
public final class JavadocTagsCheck extends AbstractCheck {

    /**
     * Map of tag and its pattern.
     */
    private final Map<String, Pattern> tags = new HashMap<>();

    /**
     * List of prohibited javadoc tags.
     */
    private final Collection<String> prohibited =
        Arrays.asList("author", "version");

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
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
    public void init() {
        this.tags.put(
            "since",
            Pattern.compile(
                "^\\d+(\\.\\d+){1,2}(\\.[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?$"
            )
        );
    }

    @Override
    public void visitToken(final DetailAST ast) {
        final String[] lines = this.getLines();
        final int start = ast.getLineNo();
        final int cstart = JavadocTagsCheck.findCommentStart(lines, start);
        final int cend = JavadocTagsCheck.findCommentEnd(lines, start);
        if (cend > cstart && cstart >= 0) {
            for (final String tag : this.prohibited) {
                this.findProhibited(lines, start, cstart, cend, tag);
            }
            for (final String tag : this.tags.keySet()) {
                this.matchTagFormat(lines, cstart, cend, tag);
            }
        } else {
            this.log(0, "Problem finding class/interface comment");
        }
    }

    /**
     * Get the text of the given tag.
     * @param line Line with the tag.
     * @return The text of the tag.
     */
    private static String getTagText(final String line) {
        return line.substring(
            line.indexOf(' ', line.indexOf('@')) + 1
        );
    }

    /**
     * Find a text in lines, by going up.
     * @param lines List of lines to check.
     * @param start Start searching from this line number.
     * @param text Text to find.
     * @return Line number with found text, or -1 if it wasn't found.
     */
    private static int findTrimmedTextUp(final String[] lines,
        final int start, final String text) {
        int found = -1;
        for (int pos = start - 1; pos >= 0; pos -= 1) {
            if (lines[pos].trim().equals(text)) {
                found = pos;
                break;
            }
        }
        return found;
    }

    /**
     * Find javadoc starting comment.
     * @param lines List of lines to check.
     * @param start Start searching from this line number.
     * @return Line number with found starting comment or -1 otherwise.
     */
    private static int findCommentStart(final String[] lines, final int start) {
        return JavadocTagsCheck.findTrimmedTextUp(lines, start, "/**");
    }

    /**
     * Find javadoc ending comment.
     * @param lines List of lines to check.
     * @param start Start searching from this line number.
     * @return Line number with found ending comment, or -1 if it wasn't found.
     */
    private static int findCommentEnd(final String[] lines, final int start) {
        return JavadocTagsCheck.findTrimmedTextUp(lines, start, "*/");
    }

    /**
     * Check if the tag text matches the format from pattern.
     * @param lines List of all lines.
     * @param start Line number where AST starts.
     * @param cstart Line number where comment starts.
     * @param cend Line number where comment ends.
     * @param tag Name of the tag.
     * @checkstyle ParameterNumber (3 lines)
     */
    private void findProhibited(final String[] lines, final int start,
        final int cstart, final int cend, final String tag) {
        final List<Integer> found =
            this.findTagLineNum(lines, cstart, cend, tag);
        if (!found.isEmpty()) {
            this.log(
                start + 1,
                StringUtils.join(
                    "Prohibited ''@{0}'' tag in",
                    " class/interface comment"
                ),
                tag
            );
        }
    }

    /**
     * Check if the tag text matches the format from pattern.
     * @param lines List of all lines.
     * @param start Line number where comment starts.
     * @param end Line number where comment ends.
     * @param tag Name of the tag.
     * @checkstyle ParameterNumber (3 lines)
     */
    private void matchTagFormat(final String[] lines, final int start,
        final int end, final String tag) {
        final List<Integer> found = this.findTagLineNum(lines, start, end, tag);
        if (found.isEmpty()) {
            this.log(
                start + 1,
                "Missing ''@{0}'' tag in class/interface comment",
                tag
            );
            return;
        }
        for (final Integer item : found) {
            final String text = JavadocTagsCheck.getTagText(lines[item]);
            if (!this.tags.get(tag).matcher(text).matches()) {
                this.log(
                    item + 1,
                    "Tag text ''{0}'' does not match the pattern ''{1}''",
                    text,
                    this.tags.get(tag).toString()
                );
            }
        }
    }

    /**
     * Find given tag in comment lines.
     * @param lines Lines to search for the tag.
     * @param start Starting line number.
     * @param end Ending line number.
     * @param tag Name of the tag to look for.
     * @return Line number with found tag or -1 otherwise.
     * @checkstyle ParameterNumber (3 lines)
     */
    private List<Integer> findTagLineNum(final String[] lines, final int start,
        final int end, final String tag) {
        final String prefix = String.format(" * @%s ", tag);
        final List<Integer> found = new ArrayList<>(1);
        for (int pos = start; pos <= end; pos += 1) {
            final String line = lines[pos];
            if (line.contains(String.format("@%s ", tag))) {
                if (!line.trim().startsWith(prefix.trim())) {
                    this.log(
                        start + pos + 1,
                        "Line with ''@{0}'' does not start with a ''{1}''",
                        tag,
                        prefix
                    );
                    break;
                }
                found.add(pos);
            }
        }
        return found;
    }
}

