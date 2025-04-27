plugins {
    id("java")
    id("application")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "kz.pandev"
version = "2025.1.1"

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.yaml:snakeyaml:2.4")
    implementation("jakarta.json:jakarta.json-api:2.1.3")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
}

repositories {
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

kotlin {
    jvmToolchain(17)
}

intellij {
    version.set("2024.1")
    type.set("IC")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("261.*")
    }

    withType<Test> {
        useJUnitPlatform()
        reports {
            junitXml.required.set(true)
            html.required.set(false)
            junitXml.outputLocation.set(file(layout.buildDirectory.file("test-results")))
        }
    }

    // Задача для обфускации
    register<proguard.gradle.ProGuardTask>("proguard") {
        dependsOn("instrumentedJar")
        verbose()

        // Пути для входного и выходного JAR
        injars("build/libs/instrumented-${project.name}-${project.version}.jar")
        outjars("build/obfuscated/output/instrumented-${project.name}-${project.version}.jar")

        // Добавляем стандартные библиотеки и зависимости
        val javaHome = System.getProperty("java.home")
        File("$javaHome/jmods/").listFiles()?.forEach { libraryjars(it.absolutePath) }
        libraryjars(configurations.compileClasspath.get())

        // Используем файл правил ProGuard
        configuration("proguard-rules.pro")
    }

    // Задача для сборки плагина с обфусцированным JAR
    named("buildPlugin") {
        dependsOn("proguard") // Обфускация перед сборкой плагина
    }

    prepareSandbox {
        dependsOn("proguard")
        val obfuscatedJar = layout.buildDirectory.file("obfuscated/output/instrumented-${project.name}-${project.version}.jar").get().asFile
        pluginJar.set(obfuscatedJar)
    }
}

buildscript {
    repositories {
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
            setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
            setUrl("https://plugins.gradle.org/m2/")
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}