import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.gson.Gson
import org.gradle.internal.impldep.com.google.gson.JsonArray
import org.gradle.internal.impldep.com.google.gson.JsonObject
import org.gradle.kotlin.dsl.TaskContainerScope

fun TaskContainerScope.registerLocalizationTask(project: Project) {
    with(project) {
        register("generateLocales") {
            doFirst {
                val langs = listOf("en") + file("src/commonMain/resources/MR/values").listFiles()?.map { it.name }
                    ?.minus("base")
                    ?.map { it.replace("-r", "-") }
                    .orEmpty()
                val json = JsonObject().apply {
                    val array = JsonArray().apply {
                        langs.forEach {
                            add(it)
                        }
                    }
                    add("langs", array)
                }
                file("src/commonMain/resources/MR/files/languages.json").writer().use {
                    Gson().toJson(json, it)
                }
            }
        }
    }
}