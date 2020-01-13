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
package com.qulice.findbugs;

import com.google.common.io.Files;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Mocks bytecode.
 * @since 0.3
 */
public final class BytecodeMocker {

    /**
     * The source code.
     */
    private String source;

    /**
     * Use this source code.
     * @param src Source code in Java
     * @return This object
     */
    public BytecodeMocker withSource(final String src) {
        this.source = src;
        return this;
    }

    /**
     * Create bytecode and return it.
     * @return The bytecode
     * @throws IOException If some problem
     */
    public byte[] mock() throws IOException {
        final File outdir = Files.createTempDir();
        final File input = File.createTempFile("input", ".java");
        FileUtils.writeStringToFile(
            input,
            this.source,
            StandardCharsets.UTF_8
        );
        final ProcessBuilder builder = new ProcessBuilder(
            "javac",
            "-d",
            outdir.getPath(),
            input.getPath()
        );
        final Process process = builder.start();
        try {
            process.waitFor();
        } catch (final InterruptedException ex) {
            throw new IllegalStateException(ex);
        } finally {
            input.delete();
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(
                String.format(
                    "Failed to compile '%s':%n%s", this.source,
                    IOUtils.toString(
                        process.getErrorStream(),
                        StandardCharsets.UTF_8
                    )
                )
            );
        }
        final byte[] bytes = this.findIn(outdir);
        Logger.debug(
            this, "#mock(): produced %d bytes in bytecode for '%s'",
            bytes.length, this.source
        );
        FileUtils.deleteDirectory(outdir);
        return bytes;
    }

    /**
     * Find bytecode file in the directory and return its content.
     * @param dir The directory
     * @return The bytecode
     * @throws IOException If some problem
     */
    public byte[] findIn(final File dir) throws IOException {
        final Collection<File> produced = FileUtils.listFiles(
            dir, new String[] {"class"}, true
        );
        if (produced.isEmpty()) {
            throw new IllegalStateException("No files generated");
        }
        final File found = produced.iterator().next();
        final byte[] bytes = FileUtils.readFileToByteArray(found);
        Logger.debug(
            this,
            "#findIn('%s'): found file '%s' with %d bytes",
            dir,
            found,
            bytes.length
        );
        return bytes;
    }

}
