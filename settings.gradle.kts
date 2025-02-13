pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io")}
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        kotlin("jvm") version "2.1.10"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Xed-Editor"
include(":app")
include(":core:main")

include(":core:editor")
include(":core:editor-lsp")
include(":core:language-textmate")
include(":core:runner")
include(":core:filetree")
include(":core:settings")
include(":core:components")
include(":core:commons")
//include(":core:external-editor")
include(":core:resources")
include(":core:karbon-exec")
include(":core:mutator-engine")
include(":core:file")
include(":core:extension")

