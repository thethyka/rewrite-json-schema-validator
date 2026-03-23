# Migration Plan: networknt json-schema-validator 1.5.x → 2.0.0

Source: [json-schema-validator 2.0.0 Migration Guide](https://github.com/networknt/json-schema-validator/blob/master/doc/migration-2.0.0.md)

---

## Compatibility

| Version             | Java    | Jackson   | Notes                             |
| ------------------- | ------- | --------- | --------------------------------- |
| `1.5.x`             | Java 8  | Jackson 2 | Baseline                          |
| `2.0.0`             | Java 8  | Jackson 2 | Incremental upgrade path to 3.0.0 |
| `3.0.0` _(planned)_ | Java 17 | Jackson 3 | —                                 |

---

## Summary of Breaking Changes (from Migration Guide)

- Configuration on a per-schema basis is no longer possible.
- Errors are returned as `List` instead of `Set`.
- Error messages no longer have `instanceLocation` prepended to `getMessage()`. Use `toString()` for the old behavior.
- Error codes (`ErrorMessageType`) have been removed entirely. Use message keys to distinguish error types.
- External resources are no longer fetched automatically. Requires opt-in via configuration.
- Major renaming and relocation of public API classes.
- Removal of deprecated methods from 1.x.

---

## Phase 1 — Implemented

### Implemented Changes

| #   | Change                                                                                              | Recipe Type                                   | Recipe                                         |
| --- | --------------------------------------------------------------------------------------------------- | --------------------------------------------- | ---------------------------------------------- |
| 1   | Upgrade Maven/Gradle dependency to `2.0.x`                                                          | Declarative YAML (`UpgradeDependencyVersion`) | `UpgradeJsonSchemaValidator_1_2_Dependencies`  |
| 2   | All class renames and package relocations (see table below)                                         | Declarative YAML (`ChangeType`)               | `UpgradeJsonSchemaValidator_1_2_TypeChanges`   |
| 3   | `Set<ValidationMessage>` → `List<ValidationMessage>` in variable declarations and method signatures | Imperative Java                               | `MigrateValidationResultTypes`                 |
| 4   | Remove deleted `SchemaValidatorsConfig` setter/getter methods that have no replacement in 2.0.0     | Imperative Java                               | `MigrateSchemaValidatorsConfig`                |
| 5   | Remove `ErrorMessageType` / `CustomErrorMessageType` usages (error code concept removed)            | Imperative Java                               | `RemoveErrorCodeUsages`                        |
| 6   | `JsonSchemaFactory.getInstance(..)` → `withDefaultDialect(..)`                                      | Declarative YAML (`ChangeMethodName`)         | `UpgradeJsonSchemaValidator_1_2_MethodChanges` |

### Class Renames (from Migration Guide — all implemented via `ChangeType`)

| Old (1.5.x)                                           | New (2.0.0)                                          |
| ----------------------------------------------------- | ---------------------------------------------------- |
| `com.networknt.schema.JsonSchema`                     | `com.networknt.schema.Schema`                        |
| `com.networknt.schema.JsonSchemaFactory`              | `com.networknt.schema.SchemaRegistry`                |
| `com.networknt.schema.ValidationMessage`              | `com.networknt.schema.Error`                         |
| `com.networknt.schema.SchemaValidatorsConfig`         | `com.networknt.schema.SchemaRegistryConfig`          |
| `com.networknt.schema.ValidationContext`              | `com.networknt.schema.SchemaContext`                 |
| `com.networknt.schema.ValidationResult`               | `com.networknt.schema.Result`                        |
| `com.networknt.schema.JsonSchemaValidator`            | `com.networknt.schema.Validator`                     |
| `com.networknt.schema.JsonSchemaException`            | `com.networknt.schema.SchemaException`               |
| `com.networknt.schema.JsonSchemaIdValidator`          | `com.networknt.schema.SchemaIdValidator`             |
| `com.networknt.schema.JsonSchemaRef`                  | `com.networknt.schema.SchemaRef`                     |
| `com.networknt.schema.JsonNodePath`                   | `com.networknt.schema.NodePath`                      |
| `com.networknt.schema.VersionCode`                    | `com.networknt.schema.SpecificationVersionRange`     |
| `com.networknt.schema.SpecVersion.VersionFlag`        | `com.networknt.schema.SpecificationVersion`          |
| `com.networknt.schema.JsonMetaSchema`                 | `com.networknt.schema.dialect.Dialect`               |
| `com.networknt.schema.JsonMetaSchemaFactory`          | `com.networknt.schema.dialect.DialectRegistry`       |
| `com.networknt.schema.SchemaId`                       | `com.networknt.schema.dialect.DialectId`             |
| `com.networknt.schema.ValidatorTypeCode`              | `com.networknt.schema.keyword.KeywordType`           |
| `com.networknt.schema.JsonValidator`                  | `com.networknt.schema.keyword.KeywordValidator`      |
| `com.networknt.schema.VocabularyFactory`              | `com.networknt.schema.vocabulary.VocabularyRegistry` |
| `com.networknt.schema.walk.JsonSchemaWalker`          | `com.networknt.schema.walk.Walker`                   |
| `com.networknt.schema.serialization.JsonNodeReader`   | `com.networknt.schema.serialization.NodeReader`      |
| `com.networknt.schema.annotation.JsonNodeAnnotation`  | `com.networknt.schema.annotation.Annotation`         |
| `com.networknt.schema.annotation.JsonNodeAnnotations` | `com.networknt.schema.annotation.Annotations`        |

### `SchemaValidatorsConfig` Deleted Methods (handled by `MigrateSchemaValidatorsConfig`)

These methods were removed in 2.0.0 with no replacement. The recipe removes their setter call sites and adds TODO comments to getter call sites.

| Removed Method                                                                               | Reason                                                         |
| -------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `setDiscriminatorKeywordEnabled(boolean)` / `isDiscriminatorKeywordEnabled()`                | Removed — dialect must contain a discriminator keyword instead |
| `setOpenAPI3StyleDiscriminators(boolean)` / `isOpenAPI3StyleDiscriminators()`                | Same as above                                                  |
| `setJavaSemantics(boolean)` / `isJavaSemantics()`                                            | Removed — did the same thing as `losslessNarrowing`            |
| `setHandleNullableField(boolean)` / `isHandleNullableField()` / `isNullableKeywordEnabled()` | Removed — dialect must contain a nullable keyword instead      |
| `setPreloadJsonSchemaRefMaxNestingDepth(int)`                                                | Removed — no longer needed                                     |

> **Note:** `SchemaValidatorsConfig` setters that _moved_ to other config classes (e.g., `failFast`, `locale`, `formatAssertionsEnabled` → `SchemaRegistryConfig`; walk listeners → `WalkConfig`; `readOnly`/`writeOnly` → `ExecutionConfig`) are **not** automatically migrated. The class rename is handled by `ChangeType`, but callers that construct `SchemaValidatorsConfig` and set these properties will need to manually split them into the appropriate builder calls on the new classes.

### Error Code Removal (handled by `RemoveErrorCodeUsages`)

The migration guide states: _"The concept of error codes has been removed. Instead, the message keys used for generating localised messages can be used to distinguish error types."_

The recipe removes:

- Variable declarations typed as `ErrorMessageType` or `CustomErrorMessageType`
- Calls to `CustomErrorMessageType.of(..)`

Callers should use `error.getType()` (the message key string) where they previously used `ErrorMessageType` constants.

---

## Recipe Type Rationale

- **Declarative YAML** (`ChangeType`, `ChangeMethodName`, `UpgradeDependencyVersion`): used for straightforward name/version substitutions with no conditional logic.
- **Imperative Java** (`JavaVisitor`, `JavaIsoVisitor`): used where the transformation requires:
  - Inspecting the type of a method call's argument or return type context (`MigrateValidationResultTypes`)
  - Conditionally removing statements versus flagging them with TODO comments (`MigrateSchemaValidatorsConfig`, `RemoveErrorCodeUsages`)
  - Tracking import changes that depend on AST-level decisions made during traversal

---

## Future Work (Not Yet Implemented)

These changes are documented in the migration guide but not yet covered by a recipe:

| Change                                                                                                   | Notes                                                                                                                                    |
| -------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `ValidationMessage.getMessage()` behavior — no longer prepends `instanceLocation`                        | Callers relying on the old format should use `error.toString()` instead. Needs analysis of call sites.                                   |
| `ValidationMessage.getCode()` removed — use `getType()` for the message key                              | `getCode()` returned the error code concept which is gone. `getType()` returns the keyword/message key.                                  |
| `SchemaValidatorsConfig` setters that moved to `SchemaRegistryConfig` / `WalkConfig` / `ExecutionConfig` | The class rename is handled, but construction site refactoring (setter-style → builder pattern on the right new class) is not automated. |
| `SpecVersion.VersionFlag` enum value renames (e.g., `V7` → `DRAFT_07`)                                   | Type rename is handled; individual constant renames are not.                                                                             |
| `SchemaRegistry` builder pattern usage (e.g., `withDialect(..)`, `withDefaultDialect(..)`)               | Beyond the simple `getInstance` → `withDefaultDialect` rename, the full builder API migration is not handled.                            |
| External resource fetching opt-in                                                                        | Behavior change: fetching is now disabled by default. No recipe; requires manual configuration.                                          |
| Walk API configuration split (`JsonSchemaWalker` → `Walker`, walk listener registration)                 | Class rename is handled; walk configuration construction is not.                                                                         |
