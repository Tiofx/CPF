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
    fun xelatexTask(name: String, workingDir: File, fileName: String) = register(name, DefaultTask::class) {
        group = "Custom tasks"

        doFirst {
            exec {
                this.workingDir = workingDir
                isIgnoreExitValue = true

                commandLine("xelatex",
                        "-interaction=nonstopmode",
                        "--shell-escape",
                        "--file-line-error",
                        fileName
                )
            }
        }
    }

    val RESOURCES_FOLDER = rootProject.projectDir
            .resolve("src")
            .resolve("main")
            .resolve("resources")

    val ASSETS_FOLDER = RESOURCES_FOLDER.resolve("assets")


    val makeCPFIterationGraphImages by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_graph_imagesKt")
    val makeCPFIterationPlainGraph by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_plain_graphKt")
    val makeCPFIterationTeXReport by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_tex_reportKt")
    val makeCPFUnfoldingImage by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_imageKt")
    val makeCPFUnfoldingPlain by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_plainKt")
    val makeCPFUnfoldingTeXReport by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_tex_reportKt")
    val makeCPFTeXReport by registerTaskByMainFile("buildtask.report.cpf.Make_tex_reportKt")
    val makeTeXReport by registerTaskByMainFile("buildtask.report.Make_tex_reportKt")


    val makeCPFIterationTeXToPdfReport = xelatexTask(
            "makeCPFIterationTeXToPdfReport",
            ASSETS_FOLDER.resolve("cpf").absoluteFile,
            "iterations.tex")

    val makeCPFUnfoldingTeXToPdfReport = xelatexTask(
            "makeCPFUnfoldingTeXToPdfReport",
            ASSETS_FOLDER.resolve("cpf").absoluteFile,
            "unfolding.tex")

    val makeCPFTeXToPdfReport = xelatexTask(
            "makeCPFTeXToPdfReport",
            ASSETS_FOLDER.resolve("cpf").absoluteFile,
            "report.tex")
    }

    val teXToPdf = xelatexTask("teXToPdf", ASSETS_FOLDER.absoluteFile, "report.tex")


    val clearPlainDirectories by registering {
        doFirst {
            delete(fileTree(ASSETS_FOLDER.resolve("cpf").resolve("plain").absoluteFile))
        }
    }

    val makeTempPlainResult by registering {
        group = "application"

        dependsOn(clearPlainDirectories,
                makeCPFIterationPlainGraph,
                makeCPFUnfoldingPlain)
    }


    val makePdfReport by registering {
        group = "application"

        dependsOn(makeTeXReport)
        dependsOn(teXToPdf)
    }

    val runAndMakeFullReport by registering {
        group = "application"

        dependsOn(makeCPFIterationGraphImages)
        dependsOn(makeCPFUnfoldingImage)
        dependsOn(makePdfReport)
    }

}

fun TaskContainerScope.registerTaskByMainFile(mainPath: String) = registering(JavaExec::class) {
    group = "Custom tasks"
    classpath = sourceSets["main"].runtimeClasspath
    main = mainPath
}
