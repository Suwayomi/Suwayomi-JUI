/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.translation

import ca.gosyer.data.translation.xml.Resources
import nl.adaptivity.xmlutil.StAXReader
import nl.adaptivity.xmlutil.serialization.XML
import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import java.util.ResourceBundle

class XmlResourceBundle internal constructor(internal val lookup: Map<String, Any>) : ResourceBundle() {

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(
        stream.reader(charset)
    )

    constructor(reader: Reader) : this(
        format.decodeFromReader<Resources>(
            StAXReader(reader)
        ).values.associate { it.name to it.value }
    )

    public override fun handleGetObject(key: String): Any? {
        return lookup[key]
    }

    override fun getKeys(): Enumeration<String> {
        return Collections.enumeration(keySet())
    }

    override fun handleKeySet(): Set<String> {
        return lookup.keys
    }

    operator fun plus(other: XmlResourceBundle): XmlResourceBundle {
        return XmlResourceBundle(lookup + other.lookup)
    }

    fun getStringA(key: String): String {
        return getString(key).replace("\\n", "\n")
    }

    fun getString(key: String, vararg replacements: String): String {
        var string = getStringA(key)
        replacements.forEachIndexed { index, s ->
            string = string.replace(
                "%" + (index + 1).toString() + '$' + "s",
                s
            )
        }
        return string
    }

    companion object {
        private val format by lazy {
            XML {
                autoPolymorphic = true
                indentString = "\t"
            }
        }

        fun forTag(tag: String): XmlResourceBundle {
            val classLoader = this::class.java.classLoader
            val rootBundle = classLoader.getResourceAsStream("values/values/strings.xml")!!
                .use { XmlResourceBundle(it) }

            val languageBundle = classLoader.getResourceAsStream("values/values-${tag.substringBefore('-')}/strings.xml")
                ?.use { XmlResourceBundle(it) }

            val languageTagBundle = if (tag.contains('-')) {
                classLoader.getResourceAsStream("values/values-$tag/strings.xml")
                    ?.use { XmlResourceBundle(it) }
            } else null

            var resultBundle = rootBundle
            if (languageBundle != null) {
                resultBundle += languageBundle
            }
            if (languageTagBundle != null) {
                resultBundle += languageTagBundle
            }
            return resultBundle
        }

        fun forLocale(locale: Locale): XmlResourceBundle {
            val classLoader = this::class.java.classLoader
            val rootBundle = classLoader.getResourceAsStream("values/values/strings.xml")!!
                .use { XmlResourceBundle(it) }

            val languageBundle = classLoader.getResourceAsStream("values/values-${locale.language}/strings.xml")
                ?.use { XmlResourceBundle(it) }

            val languageTagBundle = classLoader.getResourceAsStream("values/values-${locale.toLanguageTag()}/strings.xml")
                ?.use { XmlResourceBundle(it) }

            var resultBundle = rootBundle
            if (languageBundle != null) {
                resultBundle += languageBundle
            }
            if (languageTagBundle != null) {
                resultBundle += languageTagBundle
            }
            return resultBundle
        }
    }
}
