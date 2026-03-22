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
 * Tests for the {@link MigrateSchemaValidatorsConfig} recipe.
 */
class MigrateSchemaValidatorsConfigTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateSchemaValidatorsConfig())
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void removeDiscriminatorKeywordEnabled() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setOpenAPI3StyleDiscriminators(true);
                                        config.setFailFast(true);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setFailFast(true);
                                    }
                                }
                                """));
    }

    @Test
    void removeJavaSemantics() {
        // javaSemantics was removed — use losslessNarrowing instead
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setJavaSemantics(true);
                                        config.setLocale(java.util.Locale.US);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setLocale(java.util.Locale.US);
                                    }
                                }
                                """));
    }

    @Test
    void typeLooseIsNotRemoved() {
        // typeLoose moved to SchemaRegistryConfig — the type rename recipe handles the
        // class rename,
        // and setTypeLoose() still exists on SchemaRegistryConfig
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setTypeLoose(true);
                                        config.setJavaSemantics(true);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setTypeLoose(true);
                                    }
                                }
                                """));
    }

    @Test
    void removeAllFourRemovedSettingsAtOnce() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setJavaSemantics(true);
                                        config.setHandleNullableField(true);
                                        config.setPreloadJsonSchemaRefMaxNestingDepth(10);
                                        config.setOpenAPI3StyleDiscriminators(true);
                                        config.setFailFast(true);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setFailFast(true);
                                    }
                                }
                                """));
    }

    @Test
    void getterOfRemovedMethodGetsTodoComment() {
        // Getters for removed options can't be safely deleted (used in expressions),
        // so a TODO comment is added instead
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    boolean check(SchemaValidatorsConfig config) {
                                        return config.isJavaSemantics();
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    boolean check(SchemaValidatorsConfig config) {
                                        return /* TODO This method was removed in json-schema-validator 2.0.0 and has no direct replacement. */ config.isJavaSemantics();
                                    }
                                }
                                """));
    }

    @Test
    void removeNullableAndPreloadDepth() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setHandleNullableField(true);
                                        config.setPreloadJsonSchemaRefMaxNestingDepth(5);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                    }
                                }
                                """));
    }

    @Test
    void noChangeWhenNoRemovedMethods() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setFailFast(true);
                                        config.setCacheRefs(false);
                                    }
                                }
                                """));
    }

    @Test
    void nullableKeywordEnabledGetterGetsTodoComment() {
        // isNullableKeywordEnabled() was removed — getter in condition should get a
        // TODO
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void check(SchemaValidatorsConfig config) {
                                        if (config.isNullableKeywordEnabled()) {
                                            System.out.println("nullable");
                                        }
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void check(SchemaValidatorsConfig config) {
                                        if (/* TODO This method was removed in json-schema-validator 2.0.0 and has no direct replacement. */config.isNullableKeywordEnabled()) {
                                            System.out.println("nullable");
                                        }
                                    }
                                }
                                """));
    }

    @Test
    void removeSetsButKeepLosslessNarrowing() {
        // javaSemantics was replaced by losslessNarrowing — the latter should remain
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setJavaSemantics(true);
                                        config.setLosslessNarrowing(true);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    void configure() {
                                        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
                                        config.setLosslessNarrowing(true);
                                    }
                                }
                                """));
    }
}
