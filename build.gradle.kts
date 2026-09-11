import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "de.consoluta.puppet"
version = "1.0.2"

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
            <h3>1.0.2</h3>
            <ul>
              <li>Adds puppet parser validate and puppet-lint actions for the current manifest.</li>
              <li>Adds Puppet, PDK, puppet-lint and language-server version reporting.</li>
              <li>Adds optional modulepath and environmentpath settings.</li>
              <li>Adds file-based LSP debug logging and richer IDE log diagnostics.</li>
              <li>Adds Puppet actions to editor and Project view context menus.</li>
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
