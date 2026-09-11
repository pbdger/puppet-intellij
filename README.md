# Puppet IntelliJ Plugin 1.0.2

Target: IntelliJ IDEA **2025.1–2025.3** (`251`–`253.*`), Java 21.

## Features

- Puppet `.pp` and `.epp` file type and syntax highlighting
- `Puppetfile` association and Puppet LSP activation
- Puppet Editor Services / `puppet-languageserver --stdio`
- LSP completion, hover, diagnostics and navigation according to IntelliJ 2025 LSP capabilities
- Automatic lookup of `puppet-languageserver`, PDK, Puppet and Ruby via `PATH` and common Puppet Labs paths
- Settings under **Settings | Tools | Puppet** with a **Detect tools** button
- PDK Validate
- PDK Unit Test
- PDK New Class
- PDK New Defined Type
- Puppet Language Server status
- Puppet Language Server restart
- PDK actions automatically use the nearest Puppet module root containing `metadata.json`
- Explicit Plugin Verifier targets for IntelliJ IDEA 2025.1, 2025.2 and 2025.3

`metadata.json` and `hiera.yaml` deliberately keep IntelliJ's native JSON/YAML editors; the Puppet plugin does not hijack those file types.

## Language server

The plugin starts an executable `puppet-languageserver` directly. If a configured language-server path ends in `.rb`, the configured or auto-detected Ruby interpreter is used.

Example:

```text
puppet-languageserver --stdio --timeout=0 --local-workspace=/path/to/module
```

For a file inside a Puppet module, the nearest parent containing `metadata.json` is used as `--local-workspace`. Otherwise the IntelliJ project root is used.

## Requirements

- IntelliJ IDEA Ultimate / IntelliJ IDEA 2025.1–2025.3 with the JetBrains LSP API
- JDK 21 for building the plugin
- Gradle 9.0+ for IntelliJ Platform Gradle Plugin 2.18.1
- Puppet Editor Services providing `puppet-languageserver`
- PDK for PDK actions

## Build

With JDK 21 and Gradle 9 installed:

```bash
gradle verifyPluginProjectConfiguration
gradle buildPlugin
gradle verifyPluginStructure
gradle verifyPlugin
```

The installable plugin ZIP is produced below:

```text
build/distributions/
```

The repository also includes `.github/workflows/build.yml`, which builds and verifies the plugin and uploads the installable ZIP as a CI artifact.

## Install

In IntelliJ IDEA:

```text
Settings → Plugins → ⚙ → Install Plugin from Disk…
```

Select the ZIP produced in `build/distributions/`.

## Current verification status

The project has been statically checked against the current JetBrains IntelliJ Platform Plugin SDK documentation for the IntelliJ 2025 LSP API and Gradle plugin configuration. The execution environment used to prepare this source snapshot cannot resolve external Gradle/JetBrains artifacts, so a local `buildPlugin`/Plugin Verifier run could not be completed here.

## 0.5.0 additions

- `Puppet | Parser Validate Current Manifest` (`puppet parser validate`)
- `Puppet | Lint Current Manifest` (`puppet-lint`)
- tool-version dialog for Puppet, PDK, puppet-lint and Puppet Editor Services
- optional `modulepath` and `environmentpath` settings
- optional Puppet Editor Services debug log file (debug output is never sent to STDOUT)
- Puppet submenu in Tools, editor context menu and Project view context menu
- IntelliJ log entries for language-server executable, workspace and optional debug-log location

The plugin passes configured module/environment paths to Puppet CLI actions directly. For Puppet Editor Services these are encoded via its documented `--puppet-settings` option.


## 1.0.2 editor diagnostics

For `.pp` files the plugin now registers an IntelliJ `ExternalAnnotator`. It runs `puppet parser validate` and `puppet-lint` in the background and converts file/line/column output into native editor error/warning annotations. These annotations participate in IntelliJ's normal highlighting/problem infrastructure. The existing manual actions remain available for full console output.

The annotator uses a 15 second process timeout and skips a tool cleanly when it is not installed/configured.


## 1.0.2 IDE navigation

This release adds a Puppet-oriented Structure View for top-level `class`, `define`,
`function`, `type`, and `node` declarations. It also adds **Puppet > Go to Puppet
Class...** and **Show Puppet Module**. The class navigator scans project `.pp`
files and resolves class/defined-type declarations by qualified Puppet name.

This is deliberately a lightweight IntelliJ-native layer alongside Puppet Editor
Services. Full PSI-based references, Find Usages, rename refactoring, and parameter
symbols are planned follow-ups; 1.0.2 does not pretend regex-backed symbols are a
complete Puppet parser.


## 1.0.2 Puppet references

The IntelliJ-native layer now contributes references for qualified Puppet symbols in
common declaration/use forms such as:

```puppet
include profile::apache
contain profile::mysql
require profile::base
realize profile::something
class { 'profile::apache': ... }
```

The reference resolver searches project Puppet manifests for matching `class`,
`define`, `function`, or `type` declarations. This enables IntelliJ's normal
declaration navigation for those references where the lightweight lexer exposes the
qualified name as a PSI leaf.

This remains intentionally conservative: it does not attach references to every
identifier, and it does not yet implement a full Puppet grammar/stub index. The next
architectural step is a generated/parser-backed PSI plus stub indexes; that is what
will make Find Usages and Rename reliable at large control-repo scale.


## 1.0.2 PSI and indexed declarations

0.9 introduces an IntelliJ `ParserDefinition` and `PsiFileBase` for Puppet manifests.
The first parser is intentionally tolerant: it retains lexer tokens even while a
manifest is incomplete. This gives IntelliJ a proper Puppet PSI/AST foundation
without competing with Puppet Editor Services for diagnostics.

Declaration lookup no longer scans every `.pp` file on each navigation request.
A content-dependent `FileBasedIndex` maps qualified Puppet declaration names to
candidate files; exact declaration offsets are then resolved inside only those files.

This is the first indexing architecture, not the final grammar. A future release can
replace the tolerant parser with Grammar-Kit/generated PSI and move declarations to
stub indexes without changing the navigation-facing API.


## 1.0.2 IntelliJ-native symbol workflow

1.0 adds the first end-to-end IntelliJ symbol workflow on top of the tolerant Puppet
PSI and declaration index:

* indexed qualified-name completion after `include`, `contain`, `require`, and `realize`;
* a Puppet `FindUsagesProvider` backed by the language lexer/word scanner;
* reference rename support for the Puppet references introduced in 0.8;
* a conservative rename processor for PSI leaf declarations/references.

The Puppet language server remains the authoritative semantic service for diagnostics,
hover and its own completion/navigation features. The IntelliJ-native layer is focused
on project indexing and refactoring integration.

### Important 1.0 limitation

The parser is still tolerant rather than a complete Puppet grammar. Rename should be
treated as preview-first in large control repositories. The next hardening milestone is
typed declaration PSI (`PsiNamedElement`) plus stub indexes and automated IntelliJ
fixture tests covering nested namespaces, resource-like declarations and incomplete
manifests.


## 1.0.2 hardening

This patch release adds IntelliJ fixture tests for:
- qualified Puppet-name lexing;
- variables, strings and comments;
- tolerant parsing of incomplete manifests;
- resource-like class syntax;
- FileBasedIndex declaration lookup and missing-symbol behavior.

Rename activation is also more conservative: the rename processor now accepts only
known indexed declarations or PSI elements carrying a Puppet symbol reference.

These tests are source-complete but still need to be executed in an environment able
to resolve the IntelliJ 2025 SDK/Gradle dependencies.


## 1.0.2 build hardening

This patch fixes source-level IntelliJ API/build issues found while preparing the
first real SDK build:

- moves `testFramework(...)` into the IntelliJ Platform dependency block;
- imports `ASTWrapperPsiElement` from the IntelliJ extapi PSI package;
- uses `TokenSet.create(TokenType.WHITE_SPACE)` in the parser definition;
- passes `TokenSet` instances to `DefaultWordsScanner`;
- runs `gradle test` explicitly in GitHub Actions before packaging/verifying.

The CI quality gate is now:

```bash
gradle verifyPluginProjectConfiguration
gradle test
gradle buildPlugin
gradle verifyPluginStructure
gradle verifyPlugin
```

The local preparation runtime has Java 21, but Gradle/JetBrains artifact downloads
are blocked, so no successful IntelliJ SDK compile is claimed yet.
