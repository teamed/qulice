/**
 * Copyright (c) 2011-2018, Qulice.com
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

import com.google.common.base.Joiner;
import com.jcabi.aspects.Tv;
import com.qulice.spi.Environment;
import com.qulice.spi.Violation;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for {@link CheckstyleValidator} class.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3
 * @todo #412:30min Split this class into smaller ones and remove PMD
 *  exclude `TooManyMethods`. Good candidates for moving out of this class
 *  are all that use `validateCheckstyle` method.
 * @checkstyle MultipleStringLiterals (400 lines)
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
public final class CheckstyleValidatorTest {

    /**
     * Name of property to set to change location of the license.
     */
    private static final String LICENSE_PROP = "license";

    /**
     * Directory with classes.
     */
    private static final String DIRECTORY = "src/main/java/foo";

    /**
     * License text.
     */
    private static final String LICENSE = "Hello.";

    /**
     * License rule.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final LicenseRule rule = new LicenseRule();

    /**
     * CheckstyleValidator can catch checkstyle violations.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void catchesCheckstyleViolationsInLicense() throws Exception {
        final Environment.Mock mock = new Environment.Mock();
        final File license = this.rule.savePackageInfo(
            new File(mock.basedir(), CheckstyleValidatorTest.DIRECTORY)
        ).withLines("License-1.", "", "License-2.")
            .withEol("\n")
            .file();
        final String content =
            // @checkstyle StringLiteralsConcatenation (4 lines)
            // @checkstyle RegexpSingleline (1 line)
            "/**\n * License-3.\n *\n * License-2.\n */\n"
                + "package foo;\n"
                + "public class Foo { }\n";
        final String name = "Foo.java";
        final Environment env = mock.withParam(
            CheckstyleValidatorTest.LICENSE_PROP,
            this.toUrl(license)
        ).withFile(String.format("src/main/java/foo/%s", name), content);
        final Collection<Violation> results =
            new CheckstyleValidator(env)
                .validate(env.files(name));
        MatcherAssert.assertThat(
            results,
            Matchers.hasItem(
                new ViolationMatcher(
                    "Line does not match expected header line of", name
                )
            )
        );
    }

    /**
     * CheckstyleValidator can accept instance method references.
     * @throws Exception In case of error
     */
    @Test
    public void acceptsInstanceMethodReferences() throws Exception {
        this.runValidation(
            "InstanceMethodRef.java", true
        );
    }

    /**
     * CheckstyleValidator can report error when parameter object is not
     * documented.
     * @throws Exception In case of error
     */
    @Test
    public void reportsErrorWhenParameterObjectIsNotDocumented()
        throws Exception {
        this.validate(
            "ParametrizedClass.java", false,
            "Type Javadoc comment is missing an @param <T> tag."
        );
    }

    /**
     * CheckstyleValidator reports an error when package declaration
     * is line wrapped.
     * @throws Exception when error.
     */
    @Test
    public void reportsErrorWhenLineWrap()
        throws Exception {
        this.validate(
            "LineWrapPackage.java", false,
            "should not be line-wrapped"
        );
    }

    /**
     * CheckstyleValidator reports an error when indentation is not
     * bigger than previous line by exactly 4.
     * @throws Exception when error.
     */
    @Test
    public void reportsErrorWhenIndentationIsIncorrect() throws Exception {
        this.validate(
            "InvalidIndentation.java",
            false,
            "Indentation (14) must be same or less than"
        );
    }

    /**
     * CheckstyleValidator reports an error when comment or Javadoc has too
     * long line.
     * @throws Exception when error.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void reportsErrorWhenCommentOrJavadocIsTooLong() throws Exception {
        final Collection<Violation> results =
            this.runValidation("TooLongLines.java", false);
        MatcherAssert.assertThat(
            results,
            Matchers.hasItems(
                new ViolationMatcher(
                    "Line is longer than 80 characters (found 82)", ""
                ),
                new ViolationMatcher(
                    "Line is longer than 80 characters (found 85)", ""
                )
            )
        );
    }

    /**
     * CheckstyleValidator can report Apache Commons {@code CharEncoding} class
     * usages.
     * @throws Exception when error.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void reportsAllCharEncodingUsages() throws Exception {
        final String message =
            "Use java.nio.charset.StandardCharsets instead";
        final String file = "DoNotUseCharEncoding.java";
        final Collection<Violation> results = this.runValidation(
            file, false
        );
        final String name = "RegexpSinglelineCheck";
        MatcherAssert.assertThat(
            results,
            Matchers.hasItems(
                new ViolationMatcher(
                    message, file, "6", name
                ),
                new ViolationMatcher(
                    message, file, "7", name
                ),
                new ViolationMatcher(
                    message, file, "8", name
                ),
                new ViolationMatcher(
                    message, file, "22", name
                ),
                new ViolationMatcher(
                    message, file, "23", name
                ),
                new ViolationMatcher(
                    message, file, "24", name
                )
            )
        );
    }

    /**
     * CheckstyleValidator accepts the valid indentation
     * refused by forceStrictCondition.
     * @throws Exception when error.
     */
    @Test
    public void acceptsValidIndentation() throws Exception {
        this.runValidation(
            "ValidIndentation.java", true
        );
    }

    /**
     * CheckstyleValidator reports an error when any method contains more
     * than one return statement.
     * @throws Exception when error.
     */
    @Test
    public void reportsErrorOnMoreThanOneReturnStatement() throws Exception {
        this.validate(
            "ReturnCount.java", false,
            "Return count is 2 (max allowed is 1)"
        );
    }

    /**
     * CheckstyleValidator can accept default methods with final modifiers.
     * @throws Exception In case of error
     */
    @Test
    public void acceptsDefaultMethodsWithFinalModifiers() throws Exception {
        this.runValidation(
            "DefaultMethods.java", true
        );
    }

    /**
     * CheckstyleValidator can accept author which has only a single name.
     * This is to support case where authro wants to use a nick instead of
     * first and last name.
     * @throws Exception In case of error
     */
    @Test
    public void acceptsSingleNameAuthor() throws Exception {
        this.runValidation(
            "AuthorTag.java", true
        );
    }

    /**
     * CheckstyleValidator can accept constant used in method annotation.
     * @throws Exception In case of error
     * @todo #447:30min Right now ConstantUsageCheck takes into account
     *  usage of constants inside method annotations, add handling of constants
     *  used in field and class annotations.
     */
    @Test
    public void acceptsConstantUsedInMethodAnnotation() throws Exception {
        this.runValidation("AnnotationConstant.java", true);
    }

    /**
     * CheckstyleValidator can allow constructor parameters named just like
     * fields.
     * @throws Exception In case of error
     */
    @Test
    public void acceptsConstructorParametersNamedJustLikeFields()
        throws Exception {
        final String file = "ConstructorParams.java";
        final Collection<Violation> results = this.runValidation(file, false);
        final String name = "HiddenFieldCheck";
        MatcherAssert.assertThat(
            results,
            Matchers.allOf(
                Matchers.hasItem(
                    new ViolationMatcher(
                        "'number' hides a field.", file, "31", name
                    )
                ),
                Matchers.not(
                    Matchers.hasItem(
                        new ViolationMatcher(
                            "'number' hides a field.", file, "22", name
                        )
                    )
                )
            )
        );
    }

    /**
     * CheckstyleValidator allows local variables and catch parameters with
     * names matching {@code ^[a-z]{3,12}$} pattern.
     * Additionally, catch parameters can use name {@code ex}.
     * @throws Exception In case of error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void allowsOnlyProperlyNamedLocalVariables() throws Exception {
        final String file = "LocalVariableNames.java";
        final Collection<Violation> results = this.runValidation(
            file, false
        );
        MatcherAssert.assertThat(results, Matchers.hasSize(Tv.TEN));
        MatcherAssert.assertThat(
            results,
            Matchers.allOf(
                Matchers.not(
                    Matchers.hasItems(
                        new ViolationMatcher("aaa", file),
                        new ViolationMatcher("twelveletter", file),
                        new ViolationMatcher("ise", file),
                        new ViolationMatcher("id", file),
                        new ViolationMatcher("parametername", file)
                    )
                ),
                Matchers.hasItems(
                    new ViolationMatcher(
                        "Name 'prolongations' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'very_long_variable_id' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'camelCase' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'it' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'number1' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'ex' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'a' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'ae' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'e' must match pattern", file
                    ),
                    new ViolationMatcher(
                        "Name 'it' must match pattern", file
                    )
                )
            )
        );
    }

    /**
     * CheckstyleValidator can allow only properly ordered Javadoc at-clauses.
     * @throws Exception In case of error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void allowsOnlyProperlyOrderedAtClauses() throws Exception {
        final String file = "AtClauseOrder.java";
        final Collection<Violation> results = this.runValidation(
            file, false
        );
        MatcherAssert.assertThat(results, Matchers.hasSize(Tv.FOUR));
        final String message = "At-clauses have to appear in the order";
        final String name = "AtclauseOrderCheck";
        MatcherAssert.assertThat(
            results,
            Matchers.hasItems(
                new ViolationMatcher(
                    message, file, "23", name
                ),
                new ViolationMatcher(
                    message, file, "50", name
                ),
                new ViolationMatcher(
                    message, file, "60", name
                ),
                new ViolationMatcher(
                    message, file, "61", name
                )
            )
        );
    }

    /**
     * CheckstyleValidator will fail if  Windows EOL-s are used.
     * @throws Exception If something wrong happens inside
     */
    @Test
    @Ignore
    public void passesWindowsEndsOfLineWithoutException() throws Exception {
        this.validate("WindowsEol.java", false, "LICENSE found:");
    }

    /**
     * Fail validation with Windows-style formatting of the license and
     * Linux-style formatting of the sources.
     * @throws Exception If something wrong happens inside
     * @todo #61:30min This test and passesWindowsEndsOfLineWithoutException
     *  should be refactored to gather log4j logs and validate that they work
     *  correctly. (see changes done in #61)
     */
    @Test
    @Ignore
    public void testWindowsEndsOfLineWithLinuxSources() throws Exception {
        this.runValidation("WindowsEolLinux.java", false);
    }

    /**
     * CheckstyleValidator can allow proper indentation in complex annotations.
     * @throws Exception If something wrong happens inside
     * @todo #411:30min Sample code provided in #411 should be considered as
     *  invalid. Find a way how to do that by either custom check, or updating
     *  Checkstyle whenever IndentationCheck there will be more reliable. As for
     *  Checkstyle 6.15 there's no ready solution for that. Right now Qulice
     *  allows both correct and incorrect code from #411.
     */
    @Test
    public void allowsProperIndentationInAnnotations() throws Exception {
        this.runValidation("AnnotationIndentation.java", true);
    }

    /**
     * Fail validation with extra semicolon in the end
     * of try-with-resources head.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void testExtraSemicolonInTryWithResources() throws Exception {
        this.validate(
            "ExtraSemicolon.java", false,
            "Extra semicolon in the end of try-with-resources head."
        );
    }

    /**
     * Accepts try-with-resources without extra semicolon
     * at the end of the head.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void acceptsTryWithResourcesWithoutSemicolon() throws Exception {
        this.runValidation("ValidSemicolon.java", true);
    }

    /**
     * CheckstyleValidator cannot demand methods to be static in files with
     * names ending with {@code ITCase}.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void acceptsNonStaticMethodsInIt() throws Exception {
        this.runValidation("ValidIT.java", true);
    }

    /**
     * CheckstyleValidator cannot demand methods to be static in files with
     * names ending with {@code IT}.
     * @throws Exception If something wrong happens inside
     */
    @Test
    public void acceptsNonStaticMethodsInItCases() throws Exception {
        this.runValidation("ValidITCase.java", true);
    }

    /**
     * CheckstyleValidator does not produce errors when last thing
     * in file are imports. The only exception that should be thrown is
     * qulice ValidationException.
     * @throws Exception In case of error
     */
    @Test
    public void doesNotThrowExceptionIfImportsOnly() throws Exception {
        final Environment.Mock mock = new Environment.Mock();
        final File license = this.rule.savePackageInfo(
            new File(mock.basedir(), CheckstyleValidatorTest.DIRECTORY)
        ).withLines("License-1.", "", "License-2.")
            .withEol("\n")
            .file();
        final String crlf = "\r\n";
        final String content = Joiner.on(crlf).join(
            "package com.google;",
            crlf,
            "import java.util.*;"
        );
        final String name = "Foo.java";
        final Environment env = mock.withParam(
            CheckstyleValidatorTest.LICENSE_PROP,
            this.toUrl(license)
        ).withFile(String.format("src/main/java/foo/%s", name), content);
        final Collection<Violation> results =
            new CheckstyleValidator(env).validate(env.files(name));
        MatcherAssert.assertThat(
            results,
            Matchers.not(Matchers.<Violation>empty())
        );
    }

    /**
     * CheckstyleValidator can distinguish between valid and invalid
     * catch parameter names.
     * @throws Exception In case of error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void distinguishesValidCatchParameterNames() throws Exception {
        final String file = "CatchParameterNames.java";
        final Collection<Violation> results = this.runValidation(
            file, false
        );
        MatcherAssert.assertThat(results, Matchers.hasSize(Tv.THREE));
        final String name = "CatchParameterNameCheck";
        MatcherAssert.assertThat(
            results,
            Matchers.hasItems(
                new ViolationMatcher(
                    // @checkstyle MultipleStringLiterals (1 line)
                    "Name 'ex_invalid_1' must match pattern", file, "27", name
                ),
                new ViolationMatcher(
                    "Name '$xxx' must match pattern", file, "29", name
                ),
                new ViolationMatcher(
                    "Name '_exp' must match pattern", file, "31", name
                )
            )
        );
    }

    /**
     * Test if URLs are valid despite having a line length over 80.
     * @throws Exception In case of error
     */
    @Test
    public void doesNotRejectUrlsInLongLines() throws Exception {
        this.runValidation("UrlInLongLine.java", true);
    }

    /**
     * CheckstyleValidator can allow spaces between methods of anonymous
     * classes.
     * @throws Exception In case of error
     */
    @Test
    public void allowsSpacesBetweenMethodsOfAnonymousClasses()
        throws Exception {
        this.runValidation("BlankLinesOutsideMethodsPass.java", true);
    }

    /**
     * CheckstyleValidator can reject spaces inside methods, regardless of
     * whether they are inside of an anonymous method or not.
     * @throws Exception In case of error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void rejectsSpacesInsideMethods() throws Exception {
        final String file = "BlankLinesInsideMethodsFail.java";
        final Collection<Violation> result = this.runValidation(
            file, false
        );
        final String name = "EmptyLinesCheck";
        final String message = "Empty line inside method";
        MatcherAssert.assertThat(
            result,
            Matchers.hasItems(
                // @checkstyle MultipleStringLiterals (12 line)
                new ViolationMatcher(message, file, "17", name),
                new ViolationMatcher(message, file, "21", name),
                new ViolationMatcher(message, file, "23", name),
                new ViolationMatcher(message, file, "27", name),
                new ViolationMatcher(message, file, "30", name),
                new ViolationMatcher(message, file, "34", name),
                new ViolationMatcher(message, file, "36", name),
                new ViolationMatcher(message, file, "40", name),
                new ViolationMatcher(message, file, "43", name),
                new ViolationMatcher(message, file, "50", name),
                new ViolationMatcher(message, file, "52", name),
                new ViolationMatcher(message, file, "54", name)
            )
        );
    }

    /**
     * CheckstyleValidator can reject uppercase abbreviations in naming
     * outside of final static fields.
     *
     * @throws Exception In case of error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void rejectsUppercaseAbbreviations() throws Exception {
        final String file = "InvalidAbbreviationAsWordInNameXML.java";
        final Collection<Violation> results = this.runValidation(
            file, false
        );
        final String name = "AbbreviationAsWordInNameCheck";
        final String message = StringUtils.join(
            "Abbreviation in name '%s' ",
            "must contain no more than '1' capital letters."
        );
        MatcherAssert.assertThat(
            results,
            Matchers.hasItems(
                new ViolationMatcher(
                    String.format(
                        message, "InvalidAbbreviationAsWordInNameXML"
                    ),
                    file, "13", name
                ),
                new ViolationMatcher(
                    String.format(message, "InvalidHTML"), file,
                    "17", name
                )
            )
        );
    }

    /**
     * CheckstyleValidator can allow IT as an uppercase abbreviation.
     *
     * @throws Exception In case of error
     */
    @Test
    public void allowsITUppercaseAbbreviation() throws Exception {
        this.runValidation("ValidAbbreviationAsWordInNameIT.java", true);
    }

    /**
     * CheckstyleValidator can allow final static fields and overrides
     * to have uppercase abbreviations.
     *
     * @throws Exception In case of error
     */
    @Test
    public void allowsUppercaseAbbreviationExceptions() throws Exception {
        this.runValidation("ValidAbbreviationAsWordInName.java", true);
    }

    /**
     * CheckstyleValidator can reject non diamond operator usage.
     * @throws Exception If error
     */
    @Test
    @SuppressWarnings("unchecked")
    public void rejectsNonDiamondOperatorUsage() throws Exception {
        final String file = "InvalidDiamondsUsage.java";
        final String name = "DiamondOperatorCheck";
        final String message = "Use diamond operator";
        MatcherAssert.assertThat(
            this.runValidation(file, false),
            Matchers.hasItems(
                new ViolationMatcher(message, file, "21", name),
                new ViolationMatcher(message, file, "31", name)
                )
        );
    }

    /**
     * CheckstyleValidator can allow diamond operator usage.
     * @throws Exception If error
     * @todo #715:30min add test for next situation
     *  {@code return new ArrayList<String>();}
     */
    @Test
    public void allowsDiamondOperatorUsage() throws Exception {
        this.runValidation("ValidDiamondsUsage.java", true);
    }

    /**
     * Convert file name to URL.
     * @param file The file
     * @return The URL
     */
    private String toUrl(final File file) {
        return String.format("file:%s", file);
    }

    /**
     * Validates that checkstyle reported given violation.
     * @param file File to check.
     * @param result Expected validation result.
     * @param message Message to match
     * @throws Exception In case of error
     */
    private void validate(final String file, final boolean result,
        final String message) throws Exception {
        MatcherAssert.assertThat(
            this.runValidation(file, result),
            Matchers.hasItem(
                new ViolationMatcher(
                    message, file
                )
            )
        );
    }

    /**
     * Returns string with Checkstyle validation results.
     * @param file File to check.
     * @param result Expected validation result.
     * @return String containing validation results in textual form.
     * @throws IOException In case of error
     */
    private Collection<Violation> runValidation(final String file,
        final boolean result) throws IOException {
        final Environment.Mock mock = new Environment.Mock();
        final File license = this.rule.savePackageInfo(
            new File(mock.basedir(), CheckstyleValidatorTest.DIRECTORY)
        ).withLines(CheckstyleValidatorTest.LICENSE)
            .withEol("\n").file();
        final Environment env = mock.withParam(
            CheckstyleValidatorTest.LICENSE_PROP,
            this.toUrl(license)
        )
            .withFile(
                String.format("src/main/java/foo/%s", file),
                IOUtils.toString(
                    this.getClass().getResourceAsStream(file)
                )
            );
        final Collection<Violation> results =
            new CheckstyleValidator(env).validate(
                env.files(file)
            );
        if (result) {
            MatcherAssert.assertThat(
                results,
                Matchers.<Violation>empty()
            );
        } else {
            MatcherAssert.assertThat(
                results,
                Matchers.not(Matchers.<Violation>empty())
            );
        }
        return results;
    }

    /**
     * Validation results matcher.
     */
    private static final class ViolationMatcher extends
        TypeSafeMatcher<Violation> {

        /**
         * Message to check.
         */
        private final String message;

        /**
         * File to check.
         */
        private final String file;

        /**
         * Expected line.
         */
        private final String line;

        /**
         * Check name.
         */
        private final String check;

        /**
         * Constructor.
         * @param message Message to check
         * @param line Line to check
         * @param file File to check
         * @param check Check name
         * @checkstyle ParameterNumber (3 lines)
         */
        ViolationMatcher(final String message, final String file,
            final String line, final String check) {
            super();
            this.message = message;
            this.file = file;
            this.line = line;
            this.check = check;
        }

        /**
         * Constructor.
         * @param message Message to check
         * @param file File to check
         */
        ViolationMatcher(final String message, final String file) {
            this(message, file, "", "");
        }

        @Override
        public boolean matchesSafely(final Violation item) {
            return item.message().contains(this.message)
                && item.file().endsWith(this.file)
                && this.lineMatches(item)
                && this.checkMatches(item);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("doesn't match");
        }

        /**
         * Check name matches.
         * @param item Item to check.
         * @return True if check name matches.
         */
        private boolean checkMatches(final Violation item) {
            return this.check.isEmpty()
                || !this.check.isEmpty() && item.name().equals(this.check);
        }

        /**
         * Check that given line matches.
         * @param item Item to check.
         * @return True if line matches.
         */
        private boolean lineMatches(final Violation item) {
            return this.line.isEmpty()
                || !this.line.isEmpty() && item.lines().equals(this.line);
        }

    }

}
