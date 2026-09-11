import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "de.fovea.puppet"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // LSP is a commercial IntelliJ Platform API. Build against IDEA Ultimate 2025.1.
        intellijIdeaUltimate("2025.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Puppet"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "251"
            untilBuild = "253.*"
        }

        description = """
            Puppet language support for IntelliJ IDEA 2025 using Puppet Editor Services,
            with Puppet/PDK tool discovery and common PDK actions.
        """.trimIndent()

        changeNotes = """
            <h3>1.0.3</h3>
            <ul>
              <li>Uses the IntelliJ 2025 PsiStructureViewFactory API from com.intellij.lang.</li>
              <li>Navigates structure symbols safely through the Navigatable interface.</li>
              <li>Registers the Structure View with the matching psiStructureViewFactory extension point.</li>
              <li>Adds the JUnit 4 test dependency required by the existing fixture tests.</li>
              <li>Removes the unavailable com.intellij.modules.lsp dependency for IntelliJ 2025.</li>
              <li>Declares the Puppet language explicitly on the custom file type.</li>
              <li>Uses a Plugin Verifier-compliant plugin ID without reserved template words.</li>
              <li>Moves the plugin and Java namespace from de.consoluta.puppet to de.fovea.puppet.</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2025.1")
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2025.2")
            create(IntelliJPlatformType.IntellijIdeaUltimate, "2025.3")
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }
}
