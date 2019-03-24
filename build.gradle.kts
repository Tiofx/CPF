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

fun TaskContainerScope.registerTaskByMainFile(mainPath: String) = registering(JavaExec::class) {
    group = "Custom tasks"
    classpath = sourceSets["main"].runtimeClasspath
    main = mainPath
}

tasks {


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

    val makeCPFIterationTeXToPdfReport by registering {
        group = "Custom tasks"
        val fileName = "iterations.tex"

        doFirst {
            exec {
                workingDir = ASSETS_FOLDER
                        .resolve("cpf")
                        .absoluteFile

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

    val makeCPFUnfoldingTeXToPdfReport by registering {
        group = "Custom tasks"
        val fileName = "unfolding.tex"

        doFirst {
            exec {
                workingDir = ASSETS_FOLDER
                        .resolve("cpf")
                        .absoluteFile

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

    val makeCPFTeXToPdfReport by registering {
        group = "Custom tasks"
        val fileName = "report.tex"

        doFirst {
            exec {
                workingDir = ASSETS_FOLDER
                        .resolve("cpf")
                        .absoluteFile

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

    val teXToPdf by registering {
        group = "Custom tasks"
        val fileName = "report.tex"

        doFirst {
            exec {
                workingDir = ASSETS_FOLDER.absoluteFile
                isIgnoreExitValue = true

                commandLine("xelatex",
//                        "-output-directory",
//                        ASSETS_FOLDER.resolve("reports"),
                        "-interaction=nonstopmode",
                        "--shell-escape",
                        "--file-line-error",
                        fileName
                )
            }
        }
    }


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

