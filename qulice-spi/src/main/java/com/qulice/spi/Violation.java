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
package com.qulice.spi;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Validation result.
 * @since 0.17
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Violation extends Comparable<Violation> {

    /**
     * Name of the validator that generated this violation information.
     * @return Name of the validator
     */
    String validator();

    /**
     * Name of the failed check.
     * @return Name of the failed check
     */
    String name();

    /**
     * Validated file.
     * @return Validated file.
     */
    String file();

    /**
     * Lines with the problem.
     * @return Lines with the problem
     */
    String lines();

    /**
     * Validation message.
     * @return Validation message.
     */
    String message();

    /**
     * Default validation result.
     */
    @EqualsAndHashCode
    @ToString
    class Default implements Violation {

        /**
         * Name of the validator that generated this violation information.
         */
        private final String vldtr;

        /**
         * Name of the failed check.
         */
        private final String nam;

        /**
         * Lines with the problem.
         */
        private final String lns;

        /**
         * Validated file.
         */
        private final String fle;

        /**
         * Validation message.
         */
        private final String msg;

        /**
         * Constructor.
         * @param vldtr Name of the validator
         * @param nam Name of the failed check
         * @param fle Validated file
         * @param lns Lines with the problem
         * @param msg Validation message
         * @checkstyle ParameterNumber (3 lines)
         */
        public Default(final String vldtr, final String nam, final String fle,
            final String lns, final String msg) {
            this.vldtr = vldtr;
            this.nam = nam;
            this.fle = fle;
            this.lns = lns;
            this.msg = msg;
        }

        @Override
        public String validator() {
            return this.vldtr;
        }

        @Override
        public String name() {
            return this.nam;
        }

        @Override
        public String file() {
            return this.fle;
        }

        @Override
        public String lines() {
            return this.lns;
        }

        @Override
        public String message() {
            return this.msg;
        }

        @Override
        public int compareTo(final Violation other) {
            return this.vldtr.compareToIgnoreCase(other.validator());
        }
    }

}
