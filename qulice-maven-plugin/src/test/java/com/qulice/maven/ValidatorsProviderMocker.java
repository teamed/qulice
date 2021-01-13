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
package com.qulice.maven;

import com.qulice.spi.ResourceValidator;
import com.qulice.spi.Validator;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mockito;

/**
 * Mocker of ValidatorsProvider.
 * @since 0.4
 */
final class ValidatorsProviderMocker {

    /**
     * List of external validators.
     */
    private final Set<Validator> external = new HashSet<>();

    /**
     * List of external resource validators.
     */
    private final transient Set<ResourceValidator> rexternal = new HashSet<>();

    /**
     * List of internal validators.
     */
    private final Set<MavenValidator> internal = new HashSet<>();

    /**
     * With this external validator.
     * @param validator The validator
     * @return This object
     */
    public ValidatorsProviderMocker withExternal(final Validator validator) {
        this.external.add(validator);
        return this;
    }

    /**
     * With this external resource validator.
     * @param validator The validator
     * @return This object
     */
    public ValidatorsProviderMocker withExternalResource(
        final ResourceValidator validator) {
        this.rexternal.add(validator);
        return this;
    }

    /**
     * With this external validator.
     * @param validator The validator
     * @return This object
     */
    public ValidatorsProviderMocker withInternal(
        final MavenValidator validator) {
        this.internal.add(validator);
        return this;
    }

    /**
     * Mock it.
     * @return The provider
     */
    public ValidatorsProvider mock() {
        final ValidatorsProvider provider =
            Mockito.mock(ValidatorsProvider.class);
        Mockito.doReturn(this.internal).when(provider).internal();
        Mockito.doReturn(this.external).when(provider).external();
        Mockito.doReturn(this.rexternal).when(provider).externalResource();
        return provider;
    }

}
