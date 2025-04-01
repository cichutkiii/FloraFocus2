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
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:<ksp_version>")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

    }
}

rootProject.name = "FloraFocus"
include(":app")
 