
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.11.2"
}

group = "com.tsybulka"
version = "2.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("org.reflections:reflections:0.9.11")
    implementation("org.knowm.xchart:xchart:3.8.7")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    // JUnit 5 (JUnit Jupiter API)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    // JUnit Jupiter Engine for running tests
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    // Mockito for mocking objects
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    // Mockito extension for JUnit 5 integration
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    test{
        useJUnitPlatform()
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("242.*")
    }
}
