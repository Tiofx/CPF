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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0-alpha")
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
    val CPF_FOLDER = ASSETS_FOLDER.resolve("cpf")


    val makeCPFIterationGraphImages by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_graph_imagesKt")
    val makeCPFIterationPlainGraph by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_plain_graphKt")
    val makeCPFIterationTeXReport by registerTaskByMainFile("buildtask.report.cpf.iteration.Make_tex_reportKt")
    val makeCPFUnfoldingImage by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_imageKt")
    val makeCPFUnfoldingPlain by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_plainKt")
    val makeCPFUnfoldingTeXReport by registerTaskByMainFile("buildtask.report.cpf.unfolding.Make_tex_reportKt")
    val makeCPFExecutionGraphImage by registerTaskByMainFile("buildtask.report.cpf.executiongraph.Make_imageKt")
    val makeCPFExecutionGraphPlain by registerTaskByMainFile("buildtask.report.cpf.executiongraph.Make_plainKt")
    val makeCPFExecutionGraphTeXReport by registerTaskByMainFile("buildtask.report.cpf.executiongraph.Make_tex_reportKt")
    val makeCPFTeXReport by registerTaskByMainFile("buildtask.report.cpf.Make_tex_reportKt")
    val makeProgramCodeTeXReport by registerTaskByMainFile("buildtask.report.Make_program_code_tex_reportKt")
    val makeTeXReport by registerTaskByMainFile("buildtask.report.Make_tex_reportKt")
    val makeFullTeXReport by registerTaskByMainFile("buildtask.report.Make_full_tex_reportKt")


    val makeCPFIterationTeXToPdfReport = xelatexTask(
            "makeCPFIterationTeXToPdfReport",
            CPF_FOLDER.absoluteFile,
            "iterations.tex")

    val makeCPFUnfoldingTeXToPdfReport = xelatexTask(
            "makeCPFUnfoldingTeXToPdfReport",
            CPF_FOLDER.absoluteFile,
            "unfolding.tex")

    val makeCPFExecutionGraphTeXToPdfReport = xelatexTask(
            "makeCPFExecutionGraphTeXToPdfReport",
            CPF_FOLDER.absoluteFile,
            "execution_graph.tex")

    val makeCPFTeXToPdfReport = xelatexTask(
            "makeCPFTeXToPdfReport",
            CPF_FOLDER.absoluteFile,
            "report.tex")

    val teXToPdfReport = xelatexTask("teXToPdfReport", ASSETS_FOLDER.absoluteFile, "report.tex")
    val programCodeTeXToPdfReport = xelatexTask("programCodeteXToPdfReport", ASSETS_FOLDER.absoluteFile, "program_code_report.tex")
    val fullTeXToPdfReport = xelatexTask("fullTeXToPdfReport", RESOURCES_FOLDER.absoluteFile, "report.tex")

    val remakeCPFReport by registering {
        group = "application"

        dependsOn(
                makeCPFIterationGraphImages,
                makeCPFIterationTeXReport,
                makeCPFIterationTeXToPdfReport,

                makeCPFUnfoldingImage,
                makeCPFUnfoldingTeXReport,
                makeCPFUnfoldingTeXToPdfReport,

                makeCPFExecutionGraphImage,
                makeCPFExecutionGraphTeXReport,
                makeCPFExecutionGraphTeXToPdfReport,

                makeCPFTeXReport,
                makeCPFTeXToPdfReport
        )
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
                makeCPFUnfoldingPlain,
                makeCPFExecutionGraphPlain)
    }

    val makeFullReport by registering {
        group = "Custom tasks"

        dependsOn(makeFullTeXReport, fullTeXToPdfReport)
    }

    val remakeProgramCodeReport by registering {
        group = "application"

        dependsOn(makeProgramCodeTeXReport, programCodeTeXToPdfReport)
    }


    val remakeReport by registering {
        group = "application"

        dependsOn(makeTeXReport, teXToPdfReport)
    }

    val remakeFullReport by registering {
        group = "application"

        dependsOn(remakeCPFReport, remakeReport, makeFullReport)
    }


    val orders = listOf(
            listOf(
                    makeCPFIterationGraphImages,
                    makeCPFIterationTeXReport,
                    makeCPFIterationTeXToPdfReport
            ),
            listOf(
                    makeCPFUnfoldingImage,
                    makeCPFUnfoldingTeXReport,
                    makeCPFUnfoldingTeXToPdfReport
            ),
            listOf(
                    makeCPFExecutionGraphImage,
                    makeCPFExecutionGraphTeXReport,
                    makeCPFExecutionGraphTeXToPdfReport
            ),
            listOf(
                    makeCPFIterationTeXToPdfReport,
                    makeCPFTeXReport
            ),
            listOf(
                    makeCPFUnfoldingTeXToPdfReport,
                    makeCPFTeXReport
            ),
            listOf(
                    makeCPFTeXReport,
                    makeCPFTeXToPdfReport
            ),
            listOf(
                    makeProgramCodeTeXReport,
                    programCodeTeXToPdfReport
            ),
            listOf(
                    makeTeXReport,
                    teXToPdfReport
            ),
            listOf(
                    makeFullTeXReport,
                    fullTeXToPdfReport
            ),
            listOf(
                    remakeCPFReport,
                    fullTeXToPdfReport
            ),
            listOf(
                    remakeReport,
                    fullTeXToPdfReport
            )
    )
    orders.forEach {
        it.zipWithNext().forEach { it.second.get().mustRunAfter(it.first) }
    }
}

fun TaskContainerScope.registerTaskByMainFile(mainPath: String) = registering(JavaExec::class) {
    group = "Custom tasks"
    classpath = sourceSets["main"].runtimeClasspath
    main = mainPath
}
