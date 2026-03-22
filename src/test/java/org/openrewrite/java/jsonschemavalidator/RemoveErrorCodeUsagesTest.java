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
 * Tests for the {@link RemoveErrorCodeUsages} recipe.
 */
class RemoveErrorCodeUsagesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemoveErrorCodeUsages())
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void removeErrorMessageTypeVariableDeclaration() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.CustomErrorMessageType;
                                import com.networknt.schema.ErrorMessageType;

                                class Test {
                                    void process() {
                                        ErrorMessageType type = CustomErrorMessageType.of("custom");
                                        System.out.println("done");
                                    }
                                }
                                """,
                        """
                                class Test {
                                    void process() {
                                        System.out.println("done");
                                    }
                                }
                                """));
    }

    @Test
    void removeErrorMessageTypeFieldDeclaration() {
        // ErrorMessageType as a class-level field
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.CustomErrorMessageType;
                                import com.networknt.schema.ErrorMessageType;

                                class Test {
                                    private ErrorMessageType customType = CustomErrorMessageType.of("mycode");

                                    void process() {
                                        System.out.println("done");
                                    }
                                }
                                """,
                        """
                                class Test {

                                    void process() {
                                        System.out.println("done");
                                    }
                                }
                                """));
    }

    @Test
    void removeMultipleErrorMessageTypeDeclarations() {
        // Multiple declarations in the same method
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.CustomErrorMessageType;
                                import com.networknt.schema.ErrorMessageType;

                                class Test {
                                    void process() {
                                        ErrorMessageType typeA = CustomErrorMessageType.of("code-a");
                                        ErrorMessageType typeB = CustomErrorMessageType.of("code-b");
                                        System.out.println("done");
                                    }
                                }
                                """,
                        """
                                class Test {
                                    void process() {
                                        System.out.println("done");
                                    }
                                }
                                """));
    }

    @Test
    void noChangeWhenNoErrorMessageType() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;

                                class Test {
                                    void check(ValidationMessage msg) {
                                        String message = msg.getMessage();
                                    }
                                }
                                """));
    }
}
