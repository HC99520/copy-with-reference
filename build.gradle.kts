plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.copywithreference"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    // Use local IntelliJ IDEA installation — no need to download 700MB
    localPath.set("/Applications/IntelliJ IDEA.app/Contents")
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("251")
        untilBuild.set("253.*")

        changeNotes.set("""
            <h3>1.0.0</h3>
            <ul>
              <li>Right-click context menu: Copy with File Reference</li>
              <li>Copies selected code with file path and line numbers</li>
              <li>Format optimized for Claude Code / ChatGPT (@path:lines + code fence)</li>
            </ul>
        """.trimIndent())
    }

    // Skip these tasks — not needed for local use
    buildSearchableOptions { enabled = false }
    signPlugin { enabled = false }
    publishPlugin { enabled = false }
    runIde { enabled = false }
}
