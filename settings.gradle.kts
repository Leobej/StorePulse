rootProject.name = "StorePulse"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    val springBootVersion = providers.gradleProperty("springBootVersion").orElse("3.5.6").get()
    val springDependencyManagementVersion =
        providers.gradleProperty("springDependencyManagementVersion").orElse("1.1.7").get()

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
}

include(
    ":modules:analytics-core",
    ":modules:eventbus-core",
    ":modules:common",
    ":modules:ingest-core",
    ":modules:processing-core",
    ":modules:scheduler-core",
    ":apps:api"
)