allprojects {
    group = "org.projects"
    version = "0.1.0"
    repositories { mavenCentral() }
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
        }
        tasks.withType<Test> { useJUnitPlatform() }
    }
}