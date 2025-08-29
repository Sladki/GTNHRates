import org.gradle.api.tasks.JavaExec

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

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

