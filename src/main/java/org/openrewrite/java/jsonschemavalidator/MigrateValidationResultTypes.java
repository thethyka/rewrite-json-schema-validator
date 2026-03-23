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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.NameTree;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Migrates validation result types from 1.5.x to 2.0.0 conventions.
 * <p>
 * In json-schema-validator 2.0.0:
 * <ul>
 * <li>{@code Set<ValidationMessage>} is replaced with {@code List<Error>}</li>
 * <li>Error messages no longer include {@code instanceLocation} as part of
 * {@code getMessage()}; use {@code toString()} to get the message with
 * instanceLocation prepended</li>
 * <li>{@code ValidationResult} is renamed to {@code Result}</li>
 * </ul>
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateValidationResultTypes extends Recipe {

    String displayName = "Migrate `Set<ValidationMessage>` to `List<ValidationMessage>`";
    String description = "In json-schema-validator 2.0.0, validation errors are returned as `List` " +
            "instead of `Set`. This recipe updates variable declarations, method return types, " +
            "and method parameters to use `List` instead of `Set` when parameterized with `ValidationMessage`.";
    Set<String> tags = singleton("json-schema-validator");

    private static final String VALIDATION_MESSAGE = "com.networknt.schema.ValidationMessage";
    private static final String SET_TYPE = "java.util.Set";
    private static final String LIST_TYPE = "java.util.List";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(VALIDATION_MESSAGE, true),
                new JavaIsoVisitor<ExecutionContext>() {
                    private boolean setToListChanged;

                    @Override
                    public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                        setToListChanged = false;
                        J.CompilationUnit result = super.visitCompilationUnit(cu, ctx);
                        if (setToListChanged) {
                            boolean alreadyHasListImport = result.getImports().stream()
                                    .anyMatch(imp -> TypeUtils.isOfClassType(imp.getQualid().getType(), LIST_TYPE));
                            if (alreadyHasListImport) {
                                // List is already imported — just remove the Set import
                                result = result.getPadding().withImports(
                                        ListUtils.map(result.getPadding().getImports(), padded -> {
                                            J.Import imp = padded.getElement();
                                            if (TypeUtils.isOfClassType(imp.getQualid().getType(), SET_TYPE)) {
                                                return null;
                                            }
                                            return padded;
                                        }));
                            } else {
                                // Rename Set import to List in-place to preserve import layout
                                result = result.getPadding().withImports(
                                        ListUtils.map(result.getPadding().getImports(), padded -> {
                                            J.Import imp = padded.getElement();
                                            if (TypeUtils.isOfClassType(imp.getQualid().getType(), SET_TYPE)) {
                                                JavaType.ShallowClass listType = JavaType.ShallowClass.build(LIST_TYPE);
                                                J.FieldAccess newQualid = imp.getQualid()
                                                        .withName(imp.getQualid().getName()
                                                                .withSimpleName("List")
                                                                .withType(listType))
                                                        .withType(listType);
                                                return padded.withElement(imp.withQualid(newQualid));
                                            }
                                            return padded;
                                        }));
                            }
                        }
                        return result;
                    }

                    @Override
                    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable,
                            ExecutionContext ctx) {
                        J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);
                        if (vd.getTypeExpression() instanceof J.ParameterizedType &&
                                isSetOfValidationMessage((J.ParameterizedType) vd.getTypeExpression())) {
                            setToListChanged = true;
                            vd = vd.withTypeExpression(
                                    replaceSetWithList((J.ParameterizedType) vd.getTypeExpression()));
                            vd = vd.withVariables(ListUtils.map(vd.getVariables(), v -> {
                                JavaType newVarType = replaceSetJavaType(v.getType());
                                v = v.withType(newVarType);
                                J.Identifier name = v.getName();
                                name = name.withType(newVarType);
                                if (name.getFieldType() != null) {
                                    name = name.withFieldType(name.getFieldType().withType(newVarType));
                                }
                                return v.withName(name);
                            }));
                        }
                        return vd;
                    }

                    @Override
                    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method,
                            ExecutionContext ctx) {
                        J.MethodDeclaration md = super.visitMethodDeclaration(method, ctx);
                        if (md.getReturnTypeExpression() instanceof J.ParameterizedType &&
                                isSetOfValidationMessage((J.ParameterizedType) md.getReturnTypeExpression())) {
                            setToListChanged = true;
                            md = md.withReturnTypeExpression(
                                    replaceSetWithList((J.ParameterizedType) md.getReturnTypeExpression()));
                            if (md.getMethodType() != null) {
                                md = md.withMethodType(md.getMethodType().withReturnType(
                                        replaceSetJavaType(md.getMethodType().getReturnType())));
                            }
                        }
                        return md;
                    }

                    private boolean isSetOfValidationMessage(J.ParameterizedType pt) {
                        return TypeUtils.isOfClassType(pt.getClazz().getType(), SET_TYPE) &&
                                pt.getTypeParameters() != null && pt.getTypeParameters().size() == 1 &&
                                TypeUtils.isOfClassType(pt.getTypeParameters().get(0).getType(), VALIDATION_MESSAGE);
                    }

                    private J.ParameterizedType replaceSetWithList(J.ParameterizedType pt) {
                        JavaType.ShallowClass listType = JavaType.ShallowClass.build(LIST_TYPE);
                        NameTree newClazz = pt.getClazz();
                        if (newClazz instanceof J.Identifier) {
                            newClazz = ((J.Identifier) newClazz)
                                    .withSimpleName("List").withType(listType);
                        }
                        JavaType newType = pt.getType();
                        if (newType instanceof JavaType.Parameterized) {
                            newType = ((JavaType.Parameterized) newType).withType(listType);
                        }
                        return pt.withClazz(newClazz).withType(newType);
                    }

                    private @Nullable JavaType replaceSetJavaType(@Nullable JavaType type) {
                        if (type instanceof JavaType.Parameterized) {
                            JavaType.Parameterized pt = (JavaType.Parameterized) type;
                            if (TypeUtils.isOfClassType(pt.getType(), SET_TYPE)) {
                                return pt.withType(JavaType.ShallowClass.build(LIST_TYPE));
                            }
                        }
                        return type;
                    }
                });
    }
}
