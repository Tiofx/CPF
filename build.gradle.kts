import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    application
}

group = "CPF"
version = "1.0-SNAPSHOT"

application.mainClassName = "MainKt"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("guru.nidi:graphviz-java:0.8.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val RESOURCES_FOLDER = rootProject.projectDir
            .resolve("src")
            .resolve("main")
            .resolve("resources")

    val run by existing

    val texToPdf by registering {
        val fileName = "report.tex"

        doFirst {
            exec {
                workingDir = RESOURCES_FOLDER.absoluteFile
                isIgnoreExitValue = true

                commandLine("xelatex", "-interaction=nonstopmode", "--shell-escape", "--file-line-error", fileName)
            }
        }
    }

    val makeReport by registering {
        dependsOn(texToPdf)
    }

    val runAndMakeReport by registering {
        dependsOn(run)
        dependsOn(makeReport)
    }

}