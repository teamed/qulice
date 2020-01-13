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
package com.qulice.maven;

import com.qulice.checkstyle.CheckstyleValidator;
import com.qulice.findbugs.FindBugsValidator;
import com.qulice.pmd.PmdValidator;
import com.qulice.spi.Environment;
import com.qulice.spi.ResourceValidator;
import com.qulice.spi.Validator;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider of validators.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class DefaultValidatorsProvider implements ValidatorsProvider {
    /**
     * Environment to use for validation.
     */
    private final Environment env;

    /**
     * Constructor.
     * @param env Environment to use for validation.
     */
    DefaultValidatorsProvider(final Environment env) {
        this.env = env;
    }

    @Override
    public Set<MavenValidator> internal() {
        final Set<MavenValidator> validators = new LinkedHashSet<>();
        validators.add(new PomXpathValidator());
        validators.add(new EnforcerValidator());
        validators.add(new DuplicateFinderValidator());
        validators.add(new SvnPropertiesValidator());
        validators.add(new DependenciesValidator());
        validators.add(new SnapshotsValidator());
        return validators;
    }

    // @todo #61:30min Make FindBugsValidator inherit from ResourceValidator and
    //  use it similarly to CheckstyleValidator. Remember to move it to
    //  externalResource method and make sure all the tests pass.
    @Override
    public Set<Validator> external() {
        final Set<Validator> validators = new LinkedHashSet<>();
        validators.add(new FindBugsValidator());
        return validators;
    }

    @Override
    public Collection<ResourceValidator> externalResource() {
        return Arrays.asList(
            new CheckstyleValidator(this.env),
            new PmdValidator(this.env)
        );
    }
}
