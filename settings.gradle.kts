@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven(url = "https://maven-central.storage-download.googleapis.com/maven2/") {
            name = "mavenCentralMirror"
        }
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "vendroidNative16Kb"
                    url = uri(rootDir.resolve(".vendroid-cache/native-16kb/maven"))
                    metadataSources {
                        mavenPom()
                        artifact()
                        ignoreGradleMetadataRedirection()
                    }
                }
            }
            filter {
                includeModule("me.jahnen.libaums", "core")
                includeModule("me.jahnen.libaums", "libusbcommunication")
            }
        }
        google()
        maven(url = "https://maven-central.storage-download.googleapis.com/maven2/") {
            name = "mavenCentralMirror"
        }
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Vendroid"
include(":app")
