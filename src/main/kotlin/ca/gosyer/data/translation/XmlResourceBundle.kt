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
import java.util.Formatter
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap

class XmlResourceBundle internal constructor(internal val lookup: ConcurrentHashMap<String, Any>) : ResourceBundle() {

    constructor(stream: InputStream, charset: Charset = Charsets.UTF_8) : this(
        stream.reader(charset)
    )

    constructor(reader: Reader) : this(
        ConcurrentHashMap(
            format.decodeFromReader<Resources>(
                StAXReader(reader)
            ).values.associate { it.name to it.value }
        )
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
        return XmlResourceBundle(ConcurrentHashMap(lookup + other.lookup))
    }

    private fun String.replaceAndroid() = replace("\\n", "%n")

    fun getStringA(key: String): String {
        return Formatter().format(getString(key).replaceAndroid())
            .let { formatter ->
                formatter.toString().also { formatter.close() }
            }
    }

    fun getString(key: String, vararg replacements: Any?): String {
        return Formatter().format(getString(key).replaceAndroid(), *replacements)
            .let { formatter ->
                formatter.toString().also { formatter.close() }
            }
    }

    companion object {
        private val format by lazy {
            XML {
                autoPolymorphic = true
                indentString = "\t"
            }
        }

        fun forLocale(locale: Locale): XmlResourceBundle {
            val classLoader = this::class.java.classLoader
            val rootBundle = classLoader.getResourceAsStream("values/values/strings.xml")!!
                .use { XmlResourceBundle(it) }

            val languageBundle = classLoader.getResourceAsStream("values/values-${locale.toLanguageTag()}/strings.xml")
                ?.use { XmlResourceBundle(it) }

            return if (languageBundle != null) {
                rootBundle + languageBundle
            } else {
                rootBundle
            }
        }
    }
}
