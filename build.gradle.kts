import org.gradle.api.tasks.JavaExec

tasks.named<JavaExec>("runServer21") {
    val task = this
    doFirst {
        val angelicaArtifactId = "Angelica"
        val filteredClasspathFiles = task.classpath.filter { file ->
            !file.name.contains(angelicaArtifactId)
        }
        task.setClasspath(filteredClasspathFiles)
    }
}

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

