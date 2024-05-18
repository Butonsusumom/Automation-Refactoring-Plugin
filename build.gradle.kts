
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
    id("jacoco")
}

group = "com.tsybulka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("org.knowm.xchart:xchart:3.8.7")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

jacoco {
    toolVersion = "0.8.7"
    reportsDirectory.set(layout.buildDirectory.dir("reports"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java"))
}

tasks {

    test {
        useJUnitPlatform()
        testLogging {
            events("started", "passed", "skipped", "failed")
        }
        finalizedBy(jacocoTestReport)
    }


    jacocoTestReport {
        enabled = true
        dependsOn(test)
        reports {
            xml.required.set(false)
            csv.required.set(false)
            html.required.set(true)
            xml.required.set(true)
            html.outputLocation.set(layout.projectDirectory.dir(".qodana/code-coverage/resultHTML"))
            xml.outputLocation.set(layout.projectDirectory.file(".qodana/code-coverage/result.xml").asFile)
        }
        additionalSourceDirs.setFrom(files("src/main/java"))
        sourceDirectories.setFrom(files("src/main/java"))
        classDirectories.setFrom(fileTree("build/classes"))
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
