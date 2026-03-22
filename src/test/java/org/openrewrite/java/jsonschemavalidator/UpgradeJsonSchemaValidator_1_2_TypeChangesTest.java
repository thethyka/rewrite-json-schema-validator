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
 * Tests for the composite {@code UpgradeJsonSchemaValidator_1_2_TypeChanges}
 * recipe, verifying that all type renames from 1.5.x to 2.0.0 are applied
 * correctly.
 */
class UpgradeJsonSchemaValidator_1_2_TypeChangesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources(
                "org.openrewrite.java.jsonschemavalidator.UpgradeJsonSchemaValidator_1_2_TypeChanges")
                .parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "json-schema-validator-1.5.4"))
                // Output types are from 2.0.0 which is not on the parser classpath
                .afterTypeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    void renameJsonSchemaToSchema() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchema;

                                class Test {
                                    JsonSchema schema;
                                }
                                """,
                        """
                                import com.networknt.schema.Schema;

                                class Test {
                                    Schema schema;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaFactoryToSchemaRegistry() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaFactory;

                                class Test {
                                    JsonSchemaFactory factory;
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaRegistry;

                                class Test {
                                    SchemaRegistry factory;
                                }
                                """));
    }

    @Test
    void renameValidationMessageToError() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationMessage;
                                import java.util.Set;

                                class Test {
                                    Set<ValidationMessage> errors;
                                }
                                """,
                        """
                                import com.networknt.schema.Error;

                                import java.util.Set;

                                class Test {
                                    Set<Error> errors;
                                }
                                """));
    }

    @Test
    void renameJsonMetaSchemaToDialect() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonMetaSchema;

                                class Test {
                                    JsonMetaSchema metaSchema;
                                }
                                """,
                        """
                                import com.networknt.schema.dialect.Dialect;

                                class Test {
                                    Dialect metaSchema;
                                }
                                """));
    }

    @Test
    void renameValidatorTypeCodeToKeywordType() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidatorTypeCode;

                                class Test {
                                    ValidatorTypeCode code;
                                }
                                """,
                        """
                                import com.networknt.schema.keyword.KeywordType;

                                class Test {
                                    KeywordType code;
                                }
                                """));
    }

    @Test
    void renameSpecVersionVersionFlagToSpecificationVersion() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SpecVersion;

                                class Test {
                                    SpecVersion.VersionFlag flag;
                                }
                                """,
                        """
                                import com.networknt.schema.SpecificationVersion;

                                class Test {
                                    SpecificationVersion flag;
                                }
                                """));
    }

    @Test
    void renameValidationResultToResult() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationResult;

                                class Test {
                                    ValidationResult result;
                                }
                                """,
                        """
                                import com.networknt.schema.Result;

                                class Test {
                                    Result result;
                                }
                                """));
    }

    @Test
    void renameSchemaValidatorsConfigToSchemaRegistryConfig() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaValidatorsConfig;

                                class Test {
                                    SchemaValidatorsConfig config;
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaRegistryConfig;

                                class Test {
                                    SchemaRegistryConfig config;
                                }
                                """));
    }

    @Test
    void renameJsonNodePathToNodePath() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonNodePath;

                                class Test {
                                    JsonNodePath path;
                                }
                                """,
                        """
                                import com.networknt.schema.NodePath;

                                class Test {
                                    NodePath path;
                                }
                                """));
    }

    @Test
    void renameAnnotationTypes() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.annotation.JsonNodeAnnotation;
                                import com.networknt.schema.annotation.JsonNodeAnnotations;

                                class Test {
                                    JsonNodeAnnotation annotation;
                                    JsonNodeAnnotations annotations;
                                }
                                """,
                        """
                                import com.networknt.schema.annotation.Annotation;
                                import com.networknt.schema.annotation.Annotations;

                                class Test {
                                    Annotation annotation;
                                    Annotations annotations;
                                }
                                """));
    }

    @Test
    void renameJsonMetaSchemaFactoryToDialectRegistry() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonMetaSchemaFactory;

                                class Test {
                                    JsonMetaSchemaFactory registry;
                                }
                                """,
                        """
                                import com.networknt.schema.dialect.DialectRegistry;

                                class Test {
                                    DialectRegistry registry;
                                }
                                """));
    }

    @Test
    void renameJsonValidatorToKeywordValidator() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonValidator;

                                class Test {
                                    JsonValidator validator;
                                }
                                """,
                        """
                                import com.networknt.schema.keyword.KeywordValidator;

                                class Test {
                                    KeywordValidator validator;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaWalkerToWalker() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.walk.JsonSchemaWalker;

                                class Test {
                                    JsonSchemaWalker walker;
                                }
                                """,
                        """
                                import com.networknt.schema.walk.Walker;

                                class Test {
                                    Walker walker;
                                }
                                """));
    }

    @Test
    void renameSchemaIdToDialectId() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.SchemaId;

                                class Test {
                                    SchemaId id;
                                }
                                """,
                        """
                                import com.networknt.schema.dialect.DialectId;

                                class Test {
                                    DialectId id;
                                }
                                """));
    }

    @Test
    void renameVocabularyFactoryToVocabularyRegistry() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.VocabularyFactory;

                                class Test {
                                    VocabularyFactory factory;
                                }
                                """,
                        """
                                import com.networknt.schema.vocabulary.VocabularyRegistry;

                                class Test {
                                    VocabularyRegistry factory;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaIdValidatorToSchemaIdValidator() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaIdValidator;

                                class Test {
                                    JsonSchemaIdValidator idValidator;
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaIdValidator;

                                class Test {
                                    SchemaIdValidator idValidator;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaRefToSchemaRef() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaRef;

                                class Test {
                                    JsonSchemaRef ref;
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaRef;

                                class Test {
                                    SchemaRef ref;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaExceptionToSchemaException() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaException;

                                class Test {
                                    void method() throws JsonSchemaException {}
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaException;

                                class Test {
                                    void method() throws SchemaException {}
                                }
                                """));
    }

    @Test
    void renameJsonNodeReaderToNodeReader() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.serialization.JsonNodeReader;

                                class Test {
                                    JsonNodeReader reader;
                                }
                                """,
                        """
                                import com.networknt.schema.serialization.NodeReader;

                                class Test {
                                    NodeReader reader;
                                }
                                """));
    }

    @Test
    void renameValidationContextToSchemaContext() {
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.ValidationContext;

                                class Test {
                                    ValidationContext context;
                                }
                                """,
                        """
                                import com.networknt.schema.SchemaContext;

                                class Test {
                                    SchemaContext context;
                                }
                                """));
    }

    @Test
    void renameJsonSchemaUsedAsParameterAndLocalVariable() {
        // JsonSchema used as a parameter type and a local variable in the same method
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchema;

                                class Service {
                                    void process(JsonSchema schema) {
                                        JsonSchema cached = schema;
                                        System.out.println(cached);
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.Schema;

                                class Service {
                                    void process(Schema schema) {
                                        Schema cached = schema;
                                        System.out.println(cached);
                                    }
                                }
                                """));
    }

    @Test
    void renameMultipleTypesInSameFile() {
        // Multiple type renames in one file — all should be applied in a single pass
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchema;
                                import com.networknt.schema.JsonSchemaFactory;
                                import com.networknt.schema.ValidationResult;

                                class Test {
                                    JsonSchemaFactory factory;
                                    JsonSchema schema;
                                    ValidationResult result;
                                }
                                """,
                        """
                                import com.networknt.schema.Result;
                                import com.networknt.schema.Schema;
                                import com.networknt.schema.SchemaRegistry;

                                class Test {
                                    SchemaRegistry factory;
                                    Schema schema;
                                    Result result;
                                }
                                """));
    }

    @Test
    void renameUsedAsMethodReturnTypeAndParameter() {
        // Type rename when JsonSchema appears as both return type and parameter
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchema;

                                class Test {
                                    JsonSchema load(String path, JsonSchema parent) {
                                        return null;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.Schema;

                                class Test {
                                    Schema load(String path, Schema parent) {
                                        return null;
                                    }
                                }
                                """));
    }

    @Test
    void renameUsedInThrowsClause() {
        // JsonSchemaException in throws clause
        rewriteRun(
                // language=java
                java(
                        """
                                import com.networknt.schema.JsonSchemaException;
                                import com.networknt.schema.JsonSchema;

                                class Test {
                                    JsonSchema load() throws JsonSchemaException {
                                        return null;
                                    }
                                }
                                """,
                        """
                                import com.networknt.schema.Schema;
                                import com.networknt.schema.SchemaException;

                                class Test {
                                    Schema load() throws SchemaException {
                                        return null;
                                    }
                                }
                                """));
    }
}
