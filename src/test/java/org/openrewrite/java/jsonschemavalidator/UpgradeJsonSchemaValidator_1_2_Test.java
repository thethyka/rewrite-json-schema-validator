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
import static org.openrewrite.maven.Assertions.pomXml;

/**
 * Integration tests for the composite {@code UpgradeJsonSchemaValidator_1_2}
 * recipe, verifying the full migration from json-schema-validator 1.5.x to
 * 2.0.0.
 */
class UpgradeJsonSchemaValidator_1_2_Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("org.openrewrite.java.jsonschemavalidator.UpgradeJsonSchemaValidator_1_2")
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void fullMigration() {
        // End-to-end migration: type renames + method rename + Set->List container
        // change.
        // Note: this test requires all sub-recipe stubs to be implemented to pass.
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchema;
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.ValidationMessage;
                                import com.networknt.schema.SpecVersion;
                                import java.util.Set;

                                class Test {
                                    void validate(String json, SpecVersion.VersionFlag version) {
                                        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(version);
                                        JsonSchema schema = null;
                                        Set<ValidationMessage> errors = null;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.Error;
                                import com.networknt.schema.Schema;
                                import com.networknt.schema.SchemaRegistry;
                                import com.networknt.schema.SpecificationVersion;

                                import java.util.List;

                                class Test {
                                    void validate(String json, SpecificationVersion version) {
                                        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(version);
                                        Schema schema = null;
                                        List<Error> errors = null;
                                    }
                                }
                                """));
    }

    @Test
    void upgradeMavenDependency() {
        rewriteRun(
                // language=xml
                pomXml(
                        """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>com.example</groupId>
                                    <artifactId>demo</artifactId>
                                    <version>1.0</version>
                                    <dependencies>
                                        <dependency>
                                            <groupId>com.networknt</groupId>
                                            <artifactId>json-schema-validator</artifactId>
                                            <version>1.5.4</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """,
                        """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>com.example</groupId>
                                    <artifactId>demo</artifactId>
                                    <version>1.0</version>
                                    <dependencies>
                                        <dependency>
                                            <groupId>com.networknt</groupId>
                                            <artifactId>json-schema-validator</artifactId>
                                            <version>2.0.1</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """));
    }
}
