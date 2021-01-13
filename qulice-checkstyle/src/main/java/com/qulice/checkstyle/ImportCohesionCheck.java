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

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
import java.io.File;

/**
 * Check if import lines are all together without any empty lines or comments.
 *
 * <p>All {@code import} instructions shall stay together, without any empty
 * lines between them. If you need to separate them because the list is too
 * big - it's time to refactor the class and make is smaller.
 *
 * @since 0.3
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class ImportCohesionCheck extends AbstractFileSetCheck {

    /**
     * The "import" keyword.
     */
    private static final String IMPORT = "import ";

    // @checkstyle ExecutableStatementCount (42 lines)
    @Override
    public void processFiltered(final File file, final FileText lines) {
        int first = -1;
        int last = -1;
        for (int pos = 0; pos < lines.size(); pos += 1) {
            final String line = lines.get(pos);
            if (line.startsWith(ImportCohesionCheck.IMPORT)) {
                if (first == -1) {
                    first = pos;
                }
                last = pos;
            }
        }
        if (first == -1) {
            return;
        }
        if (this.check(first, last, lines)) {
            this.fireErrors(file.getPath());
        }
    }

    /**
     * Perform check for empty lines and comments inside imports.
     * @param first Line number where import occurred first
     * @param last Line number where import occurred first
     * @param lines All file line by line
     * @return True if check is failed
     */
    private boolean check(final int first, final int last,
        final FileText lines
    ) {
        boolean failure = false;
        if (first == 0 || !lines.get(first - 1).isEmpty()) {
            this.log(first, "Line before imports should be empty");
            failure = true;
        }
        if (lines.size() > last + 1 && !lines.get(last + 1).isEmpty()) {
            this.log(last + 2, "Line after imports should be empty");
            failure = true;
        }
        for (int pos = first; pos < last; pos += 1) {
            final String line = lines.get(pos);
            if (!line.startsWith(ImportCohesionCheck.IMPORT)) {
                this.log(
                    pos + 1,
                    "Empty line or comment between imports is not allowed"
                );
                failure = true;
            }
        }
        return failure;
    }

}
