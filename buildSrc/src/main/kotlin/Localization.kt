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
                    ?.sorted()
                    .orEmpty()

                val langFile = file("src/commonMain/resources/MR/files/languages.json")
                if (langFile.exists()) {
                    val currentLangs = langFile.reader().use {
                        Gson().fromJson(it, JsonObject::class.java)
                            .getAsJsonArray("langs")
                            .mapNotNull { it.asString }
                            .toSet()
                    }

                    if (currentLangs == langs.toSet()) return@doFirst
                }

                val json = JsonObject().apply {
                    val array = JsonArray().apply {
                        langs.forEach(::add)
                    }
                    add("langs", array)
                }

                langFile.writer().use {
                    Gson().toJson(json, it)
                }
            }
        }
    }
}