# rewrite-json-schema-validator

Migrate networknt json-schema-validator projects. Automatically.

[![Apache 2.0](https://img.shields.io/github/license/thethyka/rewrite-json-schema-validator.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Contributing Guide](https://img.shields.io/badge/Contributing-Guide-informational)](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md)
[![GitHub](https://img.shields.io/badge/GitHub-thethyka%2Frewrite--json--schema--validator-blue?logo=github)](https://github.com/thethyka/rewrite-json-schema-validator)

## What is this?

This project implements a [Rewrite module](https://github.com/openrewrite/rewrite) that performs common migration tasks for the [networknt/json-schema-validator](https://github.com/networknt/json-schema-validator) library.

Currently, it provides recipes to migrate from **1.5.x to 2.0.0**, handling the significant API overhaul introduced in that version.

### Recipes

- **`org.openrewrite.java.jsonschemavalidator.UpgradeJsonSchemaValidator_1_2`** — Migrates from json-schema-validator 1.5.x to 2.0.0. This includes:
  - Migrating `SchemaValidatorsConfig` from setter-based construction to the builder pattern
  - Updating `ValidationMessage` API (`getCode()` → `getType()`, `getMessage()` → `getMessageWithArgs()`)
  - Removing the deleted `ErrorCode` enum and its usages
  - Updating `OutputFormat` enum values (`HIERARCHICAL` → `DEFAULT`, `OPENAPI_3` → `DEFAULT`)
  - Renaming changed method signatures on `JsonSchema` and related types
  - Updating dependency version in Maven and Gradle build files

## Contributing

Contributions are welcome! See the [OpenRewrite contributing guide](https://github.com/openrewrite/.github/blob/main/CONTRIBUTING.md) for general guidance on working with OpenRewrite projects.

## Resources

- [json-schema-validator 2.0.0 Migration Guide](https://github.com/networknt/json-schema-validator/blob/master/doc/migration-2.0.0.md)
- [json-schema-validator releases](https://github.com/networknt/json-schema-validator/releases)
- [OpenRewrite documentation](https://docs.openrewrite.org/)

![Logo](https://github.com/openrewrite/rewrite/raw/main/doc/logo-oss.png)
