/*
 * Copyright 2026 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.jsonschemavalidator;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

/**
 * Tests for the {@link MigrateValidationResultTypes} recipe.
 */
class MigrateValidationResultTypesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateValidationResultTypes())
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void migrateSetValidationMessageToListError() {
        // TODO: Enable once implementation is complete and parser classpath is
        // available
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Test {
                                    Set<ValidationMessage> validate() {
                                        return null;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Test {
                                    List<ValidationMessage> validate() {
                                        return null;
                                    }
                                }
                                """));
    }

    @Test
    void migrateVariableDeclaration() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Test {
                                    void process() {
                                        Set<ValidationMessage> errors = null;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Test {
                                    void process() {
                                        List<ValidationMessage> errors = null;
                                    }
                                }
                                """));
    }

    @Test
    void migrateMethodParameter() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Test {
                                    void handle(Set<ValidationMessage> errors) {
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Test {
                                    void handle(List<ValidationMessage> errors) {
                                    }
                                }
                                """));
    }

    @Test
    void migrateFieldDeclaration() {
        // Set<ValidationMessage> as a class-level field
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Validator {
                                    private Set<ValidationMessage> lastErrors;

                                    Set<ValidationMessage> getErrors() {
                                        return lastErrors;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Validator {
                                    private List<ValidationMessage> lastErrors;

                                    List<ValidationMessage> getErrors() {
                                        return lastErrors;
                                    }
                                }
                                """));
    }

    @Test
    void noChangeForSetOfOtherType() {
        // Set<String> should not be converted — only Set<ValidationMessage>
        rewriteRun(
                // language=java
                java(
                        """
                                import java.util.Set;

                                class Test {
                                    Set<String> names;
                                }
                                """));
    }

    @Test
    void noChangeWhenAlreadyList() {
        // List<ValidationMessage> is already the right container — no change needed
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Test {
                                    List<ValidationMessage> errors;
                                }
                                """));
    }

    @Test
    void migrateMultipleOccurrencesInSameClass() {
        // Multiple Set<ValidationMessage> usages in the same class — all should be
        // migrated
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Service {
                                    Set<ValidationMessage> validate() {
                                        return null;
                                    }

                                    void process(Set<ValidationMessage> errors) {
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.List;

                                class Service {
                                    List<ValidationMessage> validate() {
                                        return null;
                                    }

                                    void process(List<ValidationMessage> errors) {
                                    }
                                }
                                """));
    }
}
