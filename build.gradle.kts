import org.gradle.api.tasks.JavaExec
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

tasks.named<JavaExec>("runServer21") {
    val task = this
    doFirst {
        val dependenciesToExclude = listOf ("Angelica")
        val filteredClasspathFiles = task.classpath.filter { file ->
            !dependenciesToExclude.any { d -> file.name.contains(d) }
        }
        task.setClasspath(filteredClasspathFiles)
    }
}

// IFU uses Navigator 1.1.4 crashing the game, also need Angelica, so tiresome
val compileClasspath = configurations.named("compileClasspath")
val constraintHandler = dependencies.constraints

listOf("runtimeClasspath", "java17Dependencies").forEach { configurationName ->
    configurations.named(configurationName) {
        dependencyConstraints.addAllLater(
            compileClasspath.map { compile ->
                compile.incoming.resolutionResult.allComponents
                    .mapNotNull { it.id as? ModuleComponentIdentifier }
                    .map { id ->
                        constraintHandler.create(
                            "${id.group}:${id.module}:${id.version}"
                        ).apply {
                            because("Runtime should not be older than compileClasspath")
                        }
                    }
            }
        )
    }
}

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

