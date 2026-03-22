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
 * Tests for the composite {@code UpgradeJsonSchemaValidator_1_2_MethodChanges}
 * recipe, verifying that method renames from 1.5.x to 2.0.0 are applied
 * correctly.
 * <p>
 * Note: these tests run the MethodChanges recipe in isolation, so type names
 * remain as they are in 1.5.x (the TypeChanges recipe is a separate step).
 */
class UpgradeJsonSchemaValidator_1_2_MethodChangesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources(
                "org.openrewrite.java.jsonschemavalidator.UpgradeJsonSchemaValidator_1_2_MethodChanges")
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void renameGetInstanceToWithDefaultDialect() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    void create() {
                                        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    void create() {
                                        JsonSchemaFactory factory = JsonSchemaFactory.withDefaultDialect(SpecVersion.VersionFlag.V202012);
                                    }
                                }
                                """));
    }

    @Test
    void renameGetInstanceWithVariableArgument() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    JsonSchemaFactory create(SpecVersion.VersionFlag version) {
                                        return JsonSchemaFactory.getInstance(version);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    JsonSchemaFactory create(SpecVersion.VersionFlag version) {
                                        return JsonSchemaFactory.withDefaultDialect(version);
                                    }
                                }
                                """));
    }

    @Test
    void renameGetInstanceMultipleCallsInSameMethod() {
        // Rename applies to every getInstance call in the same method body
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    void create() {
                                        JsonSchemaFactory draft7 = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                                        JsonSchemaFactory draft202012 = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    void create() {
                                        JsonSchemaFactory draft7 = JsonSchemaFactory.withDefaultDialect(SpecVersion.VersionFlag.V7);
                                        JsonSchemaFactory draft202012 = JsonSchemaFactory.withDefaultDialect(SpecVersion.VersionFlag.V202012);
                                    }
                                }
                                """));
    }

    @Test
    void noChangeWhenNotJsonSchemaFactory() {
        // A class with a different getInstance method should not be renamed
        rewriteRun(
                // language=java
                java(
                        """
                                class Registry {
                                    static Registry getInstance(String version) {
                                        return null;
                                    }
                                }
                                """));
    }
}
