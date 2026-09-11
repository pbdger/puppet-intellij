# Build status — 1.0.3

Target: IntelliJ IDEA Ultimate 2025.1–2025.3, Java 21.

This source tree includes Gradle configuration for IntelliJ Platform verification
and CI. With JBR 21 and the IntelliJ IDEA Ultimate 2025.1 SDK, `compileJava`,
`compileTestJava`, `verifyPluginProjectConfiguration`, `buildPlugin`, and
`verifyPluginStructure` complete successfully. All ten lexer, parser,
declaration-index, and runtime-resolution tests pass in headless mode. The built
plugin ZIP contains the checksum-verified Puppet Editor Services 2.0.4 release.
IntelliJ Plugin Verifier 1.410 reports the plugin as compatible with
IU-251.23774.435, IU-252.23892.409, and IU-253.28294.334, with no deprecated,
internal, or override-only API usages reported.

Recommended verification:

```bash
gradle clean verifyPluginProjectConfiguration test buildPlugin verifyPluginStructure verifyPlugin
```

## 1.0.3

The plugin now has one runtime abstraction shared by the LSP, Puppet parser
validation, puppet-lint, version reporting, and PDK actions. AUTO detection prefers
PDK and falls back to Puppet Agent. The language server no longer depends on a
separate user installation: Puppet Editor Services 2.0.4 is fetched from the
official release, verified against its published SHA-256 digest, and embedded in
the plugin artifact.

The IntelliJ 2025 Structure View compile errors reported by CI have been corrected:

- `PsiStructureViewFactory` is imported from `com.intellij.lang`;
- `PsiElement` navigation is guarded through `com.intellij.pom.Navigatable`;
- the Structure View uses the matching `lang.psiStructureViewFactory` extension
  point;
- JUnit 4 is declared explicitly for the existing IntelliJ fixture tests;
- the unavailable `com.intellij.modules.lsp` dependency is removed for IntelliJ
  2025 so the Puppet plugin can load during fixture tests. IDEA Ultimate supplies
  the 2025 LSP API used by the plugin;
- the file type explicitly declares `language="Puppet"` for IntelliJ 2025.

Project, XML, registered-class, archive, test, and multi-IDE compatibility checks
pass for this source snapshot. The invalid earlier plugin ID reported by Plugin Verifier was replaced
with `de.fovea.puppet`, and the Java package namespace is now consistently
`de.fovea.puppet`. Gradle still reports the intentional `untilBuild = "253.*"`
compatibility-cap recommendation.

The GitHub Actions workflow uses Node.js 24-compatible action releases:
`actions/checkout@v6`, `actions/setup-java@v5`,
`gradle/actions/setup-gradle@v5`, and `actions/upload-artifact@v6`.


1.0.2 adds a PsiReferenceContributor and shared declaration resolver. Full Gradle/IDE SDK compilation still requires an online JetBrains dependency environment.


1.0.2 source-level checks passed (plugin.xml and registered classes). Full IntelliJ SDK compilation and Plugin Verifier still require the external JetBrains/Gradle dependency environment.


1.0.2 adds BasePlatformTestCase fixture coverage and conservative rename gating. Tests are present but not claimed as executed in this offline runtime.


## 1.0.2

Static hardening completed. The misplaced IntelliJ test-framework dependency and
several likely IntelliJ API compile issues were corrected. CI now runs the fixture
tests explicitly. A real IntelliJ 2025 SDK compile and Plugin Verifier run remains
pending because this runtime cannot download Gradle or JetBrains dependencies.
