package ca.gosyer.data.translation

import org.junit.Before
import java.io.File
import java.util.Locale
import kotlin.test.Test

class XmlResourceBundleTest {

    lateinit var rootBundle: XmlResourceBundle
    lateinit var bundles: List<Pair<String, XmlResourceBundle>>

    @Before
    fun `load root bundle`() {
        rootBundle = XmlResourceBundle.forLocale(Locale.ENGLISH)
        val url = XmlResourceBundleTest::class.java.getResource("/values/")
        requireNotNull(url)
        bundles = requireNotNull(
            File(url.toURI()).listFiles()?.mapNotNull {
                if (it.name == "values") return@mapNotNull null
                it.name.substringAfter("values-") to XmlResourceBundle.forLocale(Locale.forLanguageTag(it.name.substringAfter("values-")))
            }
        )
    }

    @Test
    fun `test each language parameters`() {
        rootBundle.lookup.entries.forEach { (key, value) ->
            if (value !is String || !value.contains('%')) return@forEach
            val testValues: Array<Any> = value.split('%').drop(1).map {
                when (val char = it[2]) {
                    's' -> "Test string"
                    'd' -> 69
                    'f' -> 6.9
                    else -> {
                        throw IllegalArgumentException("Unknown value key $char in $key")
                    }
                }
            }.toTypedArray()
            bundles.forEach { (lang, bundle) ->
                try {
                    bundle.getString(key, *testValues)
                } catch (e: Exception) {
                    throw Exception("Broken translation in $lang with $key", e)
                }
            }
        }
    }
}
