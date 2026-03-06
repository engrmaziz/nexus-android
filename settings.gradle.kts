pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexus"
include(":app")
include(":common")
include(":domain")
include(":data")
include(":core-download-engine")
include(":core-stream-detector")
include(":core-manifest-parser")
include(":core-playlist-engine")
