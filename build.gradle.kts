import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "de.fovea.puppet"
version = "1.0.3"

val editorServicesVersion = "2.0.4"
val editorServicesSha256 = "897ffe47974ca6414f8b66480b6bb86f8b133fc5d8230431a78d8895c28d1981"
val editorServicesResources = layout.buildDirectory.dir("generated/editor-services-resources")
val editorServicesArchive = editorServicesResources.map {
    it.file("puppet-editor-services/puppet_editor_services_v$editorServicesVersion.zip")
}

val downloadPuppetEditorServices by tasks.registering(DownloadVerifiedFile::class) {
    description = "Downloads the pinned Puppet Editor Services release used by the plugin"
    sourceUrl.set(
        "https://github.com/puppetlabs/puppet-editor-services/releases/download/" +
            "v$editorServicesVersion/puppet_editor_services_v$editorServicesVersion.zip"
    )
    expectedSha256.set(editorServicesSha256)
    destination.set(editorServicesArchive)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

dependencies {
    intellijPlatform {
        // LSP is a commercial IntelliJ Platform API. Build against IDEA Ultimate 2025.1.
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        if (localIdePath == null) {
            intellijIdeaUltimate("2025.1") { useInstaller = false }
            jetbrainsRuntime()
        } else {
            local(localIdePath)
        }
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets.main {
    resources.srcDir(editorServicesResources)
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
              <li>Adds a central PuppetRuntime shared by LSP, Puppet, puppet-lint, and PDK actions.</li>
              <li>Auto-detects PDK first and falls back to Puppet Agent, with explicit runtime selection available.</li>
              <li>Bundles the checksum-verified Puppet Editor Services 2.0.4 release.</li>
              <li>Uses the IntelliJ 2025 PsiStructureViewFactory API from com.intellij.lang.</li>
              <li>Navigates structure symbols safely through the Navigatable interface.</li>
              <li>Registers the Structure View with the matching psiStructureViewFactory extension point.</li>
              <li>Adds the JUnit 4 test dependency required by the existing fixture tests.</li>
              <li>Removes the unavailable com.intellij.modules.lsp dependency for IntelliJ 2025.</li>
              <li>Declares the Puppet language explicitly on the custom file type.</li>
              <li>Uses a Plugin Verifier-compliant plugin ID without reserved template words.</li>
              <li>Moves the plugin and Java namespace from de.consoluta.puppet to de.fovea.puppet.</li>
              <li>Updates GitHub Actions to Node.js 24-compatible releases.</li>
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
    processResources {
        dependsOn(downloadPuppetEditorServices)
    }
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }
    test {
        systemProperty("java.awt.headless", "true")
    }
    named<VerifyPluginTask>("verifyPlugin") {
        systemProperty(
            "plugin.verifier.home.dir",
            layout.buildDirectory.dir("plugin-verifier-home").get().asFile.absolutePath
        )
    }
}
