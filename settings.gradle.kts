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
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            name = "localOffline"
            url = uri("E:/local-maven-repo")
            content {
                includeGroup("androidx.compose.material3.adaptive")
                includeGroup("androidx.lifecycle")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "DiscountCards"
include(":app")
 