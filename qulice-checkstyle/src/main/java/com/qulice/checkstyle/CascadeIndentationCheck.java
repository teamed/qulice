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

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 * Make sure each line indentation is either:
 * <ul>
 * <li>the same as previous one or less
 * <li>bigger than previous by exactly 4
 * </ul>
 * All other cases must cause a failure.
 *
 * @since 0.3
 */
public final class CascadeIndentationCheck extends AbstractFileSetCheck {
    /**
     * Exact indentation increase difference.
     */
    private static final int LINE_INDENT_DIFF = 4;

    @Override
    public void processFiltered(final File file, final FileText lines) {
        int previous = 0;
        for (int pos = 0; pos < lines.size(); pos += 1) {
            final String line = lines.get(pos);
            final int current = CascadeIndentationCheck.indentation(line);
            if (CascadeIndentationCheck.inCommentBlock(line)
                || line.isEmpty()) {
                continue;
            }
            if (current > previous
                && current != previous
                + CascadeIndentationCheck.LINE_INDENT_DIFF) {
                this.log(
                    pos + 1,
                    String.format(
                        StringUtils.join(
                            "Indentation (%d) must be same or ",
                            "less than previous line (%d), or ",
                            "bigger by exactly 4"
                        ),
                        current,
                        previous
                    )
                );
            }
            previous = current;
        }
    }

    /**
     * Checks if the line belongs to a comment block.
     * @param line Input.
     * @return True if the line belongs to a comment block.
     */
    private static boolean inCommentBlock(final String line) {
        final String trimmed = line.trim();
        return !trimmed.isEmpty()
            && (trimmed.charAt(0) == '*'
                || trimmed.startsWith("/*")
                || trimmed.startsWith("*/")
                );
    }

    /**
     * Calculates indentation of a line.
     * @param line Input line
     * @return Indentation of the given line.
     */
    private static int indentation(final String line) {
        int result = 0;
        for (int pos = 0; pos < line.length(); pos += 1) {
            if (!Character.isWhitespace(line.charAt(pos))) {
                break;
            }
            result += 1;
        }
        return result;
    }

}
