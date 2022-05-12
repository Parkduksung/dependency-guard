package com.dropbox.gradle.plugins.dependencyguard.internal

import com.dropbox.gradle.plugins.dependencyguard.models.ArtifactDependency
import com.dropbox.gradle.plugins.dependencyguard.models.Dependency
import com.dropbox.gradle.plugins.dependencyguard.models.ModuleDependency

internal data class DependencyGuardReportData(
    val projectPath: String,
    val configurationName: String,
    val dependencies: List<Dependency>,
    val allowedFilter: (dependencyName: String) -> Boolean,
    /**
     * Transform or remove (with null) dependencies in the baseline
     */
    val baselineMap: (dependencyName: String) -> String?,
) {

    private val artifactDeps: List<ArtifactDependency> = dependencies
        .filterIsInstance<ArtifactDependency>()
        .distinct()
        .sortedBy { it.name }

    private val moduleDeps: List<ModuleDependency> = dependencies
        .filterIsInstance<ModuleDependency>()
        .distinct()
        .sortedBy { it.name }

    private fun allDepsReport(artifacts: Boolean, modules: Boolean): String = StringBuilder().apply {
        if (modules) {
            moduleDeps.toReportString().apply {
                if (this.isNotBlank()) {
                    append(this)
                }
            }
        }
        if (artifacts) {
            artifactDeps.toReportString().apply {
                if (this.isNotBlank()) {
                    append(this)
                }
            }
        }
    }.toString()

    val disallowed: List<Dependency> = dependencies
        .filter { !allowedFilter(it.name) }
        .distinct()
        .sortedBy { it.name }

    private fun List<Dependency>.toReportString(): String {
        val deps = this
        return StringBuilder().apply {
            deps.forEach {
                val mappedName = baselineMap(it.name)
                if (mappedName != null) {
                    appendLine(mappedName)
                }
            }
        }.toString()
    }

    fun reportForConfig(artifacts: Boolean = true, modules: Boolean = true): String {
        return allDepsReport(artifacts, modules)
    }
}
