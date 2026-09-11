# Build status — 1.0.2

Target: IntelliJ IDEA Ultimate 2025.1–2025.3, Java 21.

This source tree includes Gradle configuration for IntelliJ Platform verification and CI. The current execution environment does not have a usable Gradle distribution/JetBrains SDK cache, so a real `buildPlugin` + Plugin Verifier run could not be completed locally here. The source has been statically checked for internal consistency.

Recommended verification:

```bash
gradle clean verifyPluginProjectConfiguration buildPlugin verifyPluginStructure verifyPlugin
```


1.0.2 adds a PsiReferenceContributor and shared declaration resolver. Full Gradle/IDE SDK compilation still requires an online JetBrains dependency environment.


1.0.2 source-level checks passed (plugin.xml and registered classes). Full IntelliJ SDK compilation and Plugin Verifier still require the external JetBrains/Gradle dependency environment.


1.0.2 adds BasePlatformTestCase fixture coverage and conservative rename gating. Tests are present but not claimed as executed in this offline runtime.


## 1.0.2

Static hardening completed. The misplaced IntelliJ test-framework dependency and
several likely IntelliJ API compile issues were corrected. CI now runs the fixture
tests explicitly. A real IntelliJ 2025 SDK compile and Plugin Verifier run remains
pending because this runtime cannot download Gradle or JetBrains dependencies.
