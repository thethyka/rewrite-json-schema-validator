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
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singleton;

/**
 * Removes usages of {@code ErrorMessageType} and error code-related APIs.
 * <p>
 * In json-schema-validator 2.0.0, the concept of error codes was removed
 * entirely. {@code ErrorMessageType} has no replacement — instead, the message
 * keys used for generating localised messages can be used to distinguish
 * validation error types.
 * <p>
 * This recipe:
 * <ul>
 * <li>Removes variable declarations typed as {@code ErrorMessageType}</li>
 * <li>Removes calls to {@code CustomErrorMessageType.of(..)}</li>
 * <li>Adds TODO comments where manual intervention is needed</li>
 * </ul>
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class RemoveErrorCodeUsages extends Recipe {

    String displayName = "Remove `ErrorMessageType` and error code usages";
    String description = "In json-schema-validator 2.0.0, error codes have been removed. " +
            "`ErrorMessageType` has no replacement. Instead, use message keys to distinguish error types. " +
            "This recipe removes error code references and adds TODO comments for manual migration.";
    Set<String> tags = singleton("json-schema-validator");

    private static final String ERROR_MESSAGE_TYPE = "com.networknt.schema.ErrorMessageType";
    private static final String CUSTOM_ERROR_MESSAGE_TYPE = "com.networknt.schema.CustomErrorMessageType";
    private static final MethodMatcher CUSTOM_OF = new MethodMatcher(
            "com.networknt.schema.CustomErrorMessageType of(..)");
    private static final String TODO_TEXT = " TODO ErrorMessageType was removed in json-schema-validator 2.0.0. " +
            "Use message keys (getMessageKey()) to distinguish validation error types. ";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(ERROR_MESSAGE_TYPE, true),
                new JavaVisitor<ExecutionContext>() {

                    @Override
                    public J visitVariableDeclarations(J.VariableDeclarations multiVariable,
                            ExecutionContext ctx) {
                        J.VariableDeclarations vd = (J.VariableDeclarations) super.visitVariableDeclarations(
                                multiVariable, ctx);
                        JavaType type = vd.getType();
                        if (type != null && (TypeUtils.isOfClassType(type, ERROR_MESSAGE_TYPE) ||
                                TypeUtils.isOfClassType(type, CUSTOM_ERROR_MESSAGE_TYPE))) {
                            // For local variables, check if the variable is referenced elsewhere in the
                            // enclosing block. If so, we can't safely remove the declaration — add a TODO
                            // comment instead to guide manual migration.
                            boolean isLocalVar = getCursor().firstEnclosing(J.MethodDeclaration.class) != null;
                            if (isLocalVar) {
                                J.Block enclosingBlock = getCursor().firstEnclosing(J.Block.class);
                                if (enclosingBlock != null) {
                                    for (J.VariableDeclarations.NamedVariable variable : vd.getVariables()) {
                                        if (isReferencedInBlock(enclosingBlock, variable.getSimpleName(),
                                                getCursor())) {
                                            return addTodoComment(vd, TODO_TEXT);
                                        }
                                    }
                                }
                            }
                            maybeRemoveImport(ERROR_MESSAGE_TYPE);
                            maybeRemoveImport(CUSTOM_ERROR_MESSAGE_TYPE);
                            // Remove the variable declaration entirely
                            // noinspection DataFlowIssue
                            return null;
                        }
                        return vd;
                    }

                    @Override
                    public @Nullable J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                        if (CUSTOM_OF.matches(mi)) {
                            maybeRemoveImport(CUSTOM_ERROR_MESSAGE_TYPE);
                            maybeRemoveImport(ERROR_MESSAGE_TYPE);
                            // If this is a standalone expression statement, remove it
                            if (getCursor().getParentTreeCursor().getValue() instanceof J.Block) {
                                // noinspection DataFlowIssue
                                return null;
                            }
                        }
                        return mi;
                    }
                });
    }

    private static boolean isReferencedInBlock(J.Block block, String varName, Cursor cursor) {
        AtomicBoolean referenced = new AtomicBoolean(false);
        new JavaVisitor<AtomicBoolean>() {
            @Override
            public J visitIdentifier(J.Identifier identifier, AtomicBoolean found) {
                if (identifier.getSimpleName().equals(varName) &&
                        !(getCursor().getParentTreeCursor()
                                .getValue() instanceof J.VariableDeclarations.NamedVariable)) {
                    found.set(true);
                }
                return super.visitIdentifier(identifier, found);
            }
        }.visit(block, referenced, cursor);
        return referenced.get();
    }

    private static J.VariableDeclarations addTodoComment(J.VariableDeclarations vd, String todoText) {
        for (Comment c : vd.getPrefix().getComments()) {
            if (c instanceof TextComment &&
                    ((TextComment) c).getText().contains("was removed in json-schema-validator")) {
                return vd;
            }
        }
        TextComment comment = new TextComment(true, todoText, vd.getPrefix().getWhitespace(), Markers.EMPTY);
        return vd.withPrefix(vd.getPrefix().withComments(
                ListUtils.concat(vd.getPrefix().getComments(), comment)));
    }
}
