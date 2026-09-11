# Build status — 1.0.3

Target: IntelliJ IDEA Ultimate 2025.1–2025.3, Java 21.

This source tree includes Gradle configuration for IntelliJ Platform verification
and CI. With JBR 21 and the IntelliJ IDEA Ultimate 2025.1 SDK, `compileJava`,
`compileTestJava`, `verifyPluginProjectConfiguration`, `buildPlugin`, and
`verifyPluginStructure` complete successfully. All six lexer, parser, and
declaration-index tests pass in headless mode. A full multi-IDE Plugin Verifier run
has not been completed for this snapshot.

Recommended verification:

```bash
gradle clean verifyPluginProjectConfiguration test buildPlugin verifyPluginStructure verifyPlugin
```

## 1.0.3

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

Static project, XML, registered-class and archive checks pass for this source
snapshot. The invalid earlier plugin ID reported by Plugin Verifier was replaced
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
