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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

tasks {
    val RESOURCES_FOLDER = rootProject.projectDir
            .resolve("src")
            .resolve("main")
            .resolve("resources")
    
    
    val teXToPdf by registering {
        group = "Custom tasks"
        val fileName = "report.tex"

        doFirst {
            exec {
                workingDir = RESOURCES_FOLDER.absoluteFile
                isIgnoreExitValue = true

                commandLine("xelatex", "-interaction=nonstopmode", "--shell-escape", "--file-line-error", fileName)
            }
        }
    }

    val makeCPFIterationChangeGraphs by registering(JavaExec::class) {
        group = "Custom tasks"
        classpath = sourceSets["main"].runtimeClasspath
        main = "report.graphviz.iteration.CpfIterationGraphImagesKt"
    }

    val makeCPFIterationGraphTeXReport by registering(JavaExec::class) {
        group = "Custom tasks"
        classpath = sourceSets["main"].runtimeClasspath
        main = "report.graphviz.iteration.MakeTeXReportKt"
    }

    val makeCPFUnflodingGraph by registering(JavaExec::class) {
        group = "Custom tasks"
        classpath = sourceSets["main"].runtimeClasspath
        main = "report.graphviz.BaseKt"
    }

    val makeTeXReport by registering(JavaExec::class) {
        group = "Custom tasks"
        classpath = sourceSets["main"].runtimeClasspath
        main = "report.latex.MainKt"
    }

    val makePdfReport by registering {
        group = "application"

        dependsOn(makeTeXReport)
        dependsOn(teXToPdf)
    }

    val runAndMakeFullReport by registering {
        group = "application"

        dependsOn(makeCPFIterationChangeGraphs)
        dependsOn(makeCPFUnflodingGraph)
        dependsOn(makePdfReport)
    }

}