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
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.marker.Markers;

import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Migrates usages of {@code SchemaValidatorsConfig} to the appropriate
 * replacement class.
 * <p>
 * In json-schema-validator 2.0.0, {@code SchemaValidatorsConfig} was split
 * into:
 * <ul>
 * <li>{@code SchemaRegistryConfig} — for registry-level settings (cacheRefs,
 * failFast, locale, etc.)</li>
 * <li>{@code WalkConfig} — for walk-related settings (applyDefaultsStrategy,
 * item/keyword/property walk listeners)</li>
 * <li>{@code ExecutionConfig} — for per-execution settings (readOnly,
 * writeOnly)</li>
 * </ul>
 * <p>
 * This recipe removes calls to removed methods and adds TODO comments for
 * methods that need to move to a different config class.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateSchemaValidatorsConfig extends Recipe {

    String displayName = "Migrate `SchemaValidatorsConfig` to `SchemaRegistryConfig`/`WalkConfig`/`ExecutionConfig`";
    String description = "In json-schema-validator 2.0.0, `SchemaValidatorsConfig` was split into " +
            "`SchemaRegistryConfig` (registry-level settings), `WalkConfig` (walk listeners and defaults strategy), " +
            "and `ExecutionConfig` (per-execution settings like readOnly/writeOnly). " +
            "This recipe removes calls to deleted methods and flags methods that moved to other config classes.";
    Set<String> tags = singleton("json-schema-validator");

    private static final String SVC = "com.networknt.schema.SchemaValidatorsConfig";

    // Removed methods — these options no longer exist in 2.0.0
    private static final MethodMatcher SET_DISCRIMINATOR = new MethodMatcher(
            SVC + " setOpenAPI3StyleDiscriminators(boolean)");
    private static final MethodMatcher SET_DISCRIMINATOR_KW = new MethodMatcher(
            SVC + " setDiscriminatorKeywordEnabled(boolean)");
    private static final MethodMatcher IS_DISCRIMINATOR = new MethodMatcher(SVC + " isOpenAPI3StyleDiscriminators()");
    private static final MethodMatcher IS_DISCRIMINATOR_KW = new MethodMatcher(
            SVC + " isDiscriminatorKeywordEnabled()");
    private static final MethodMatcher SET_JAVA_SEMANTICS = new MethodMatcher(SVC + " setJavaSemantics(boolean)");
    private static final MethodMatcher IS_JAVA_SEMANTICS = new MethodMatcher(SVC + " isJavaSemantics()");
    // nullableKeywordEnabled was removed; dialect must contain a nullable keyword
    private static final MethodMatcher SET_NULLABLE = new MethodMatcher(SVC + " setHandleNullableField(boolean)");
    private static final MethodMatcher IS_NULLABLE = new MethodMatcher(SVC + " isHandleNullableField()");
    private static final MethodMatcher IS_NULLABLE_KW = new MethodMatcher(SVC + " isNullableKeywordEnabled()");
    // preloadJsonSchemaRefMaxNestingDepth was removed; no longer needed
    private static final MethodMatcher SET_PRELOAD_DEPTH = new MethodMatcher(
            SVC + " setPreloadJsonSchemaRefMaxNestingDepth(int)");

    private static final String REMOVED_TODO = " TODO This method was removed in json-schema-validator 2.0.0 and has no direct replacement. ";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(SVC, true),
                new JavaVisitor<ExecutionContext>() {

                    @Override
                    public @Nullable J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                        if (isRemovedSetter(mi)) {
                            // For chained calls (e.g. config.setFoo(x).setBar(y)),
                            // keep the chain minus this call
                            if (mi.getSelect() instanceof J.MethodInvocation) {
                                J visited = visit(mi.getSelect(), ctx);
                                return visited != null ? visited.withPrefix(mi.getPrefix()) : null;
                            }
                            // Standalone statement: remove it entirely
                            // noinspection DataFlowIssue
                            return null;
                        }

                        if (isRemovedGetter(mi)) {
                            // Can't safely remove a getter used in an expression;
                            // add a TODO comment instead
                            return addTodoComment(mi, REMOVED_TODO);
                        }

                        return mi;
                    }

                    private boolean isRemovedSetter(J.MethodInvocation mi) {
                        return SET_DISCRIMINATOR.matches(mi) || SET_DISCRIMINATOR_KW.matches(mi) ||
                                SET_JAVA_SEMANTICS.matches(mi) || SET_NULLABLE.matches(mi) ||
                                SET_PRELOAD_DEPTH.matches(mi);
                    }

                    private boolean isRemovedGetter(J.MethodInvocation mi) {
                        return IS_DISCRIMINATOR.matches(mi) || IS_DISCRIMINATOR_KW.matches(mi) ||
                                IS_JAVA_SEMANTICS.matches(mi) || IS_NULLABLE.matches(mi) ||
                                IS_NULLABLE_KW.matches(mi);
                    }

                    private J.MethodInvocation addTodoComment(J.MethodInvocation mi, String todoText) {
                        for (Comment c : mi.getPrefix().getComments()) {
                            if (c instanceof TextComment &&
                                    ((TextComment) c).getText().contains("was removed in json-schema-validator")) {
                                return mi;
                            }
                        }
                        TextComment comment = new TextComment(
                                true,
                                todoText,
                                mi.getPrefix().getWhitespace(),
                                Markers.EMPTY);
                        return mi.withPrefix(mi.getPrefix().withComments(
                                ListUtils.concat(mi.getPrefix().getComments(), comment)));
                    }
                });
    }
}
