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

    fun getStringA(key: String): String {
        return getString(key)
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\?", "?")
            .replace("\\@", "@")
    }

    fun getString(key: String, vararg replacements: Any): String {
        val stringBuilder = StringBuilder()
        var string = getStringA(key)
        while (true) {
            val index = string.indexOf('%')
            if (index < 0) {
                stringBuilder.append(string)
                break
            } else if (string[(index - 1).coerceAtLeast(0)] == '\\') {
                stringBuilder.append(string.substring(0, (index - 1).coerceAtLeast(0)))
                stringBuilder.append('%')
                string = string.substring((index + 1).coerceAtMost(string.length))
            } else {
                stringBuilder.append(string.substring(0, index))
                val stringConfig = string.substring(index, index + 4)
                val item = replacements[stringConfig[1].digitToInt() - 1]
                when (stringConfig[3]) {
                    's', 'S' -> {
                        stringBuilder.append(
                            if (item is Formattable) {
                                val formatter = Formatter()
                                item.formatTo(formatter, 0, -1, -1)
                                formatter.toString().also { formatter.close() }
                            } else item.toString()
                        )
                    }
                    'b', 'B' -> when (item) {
                        null -> stringBuilder.append("false")
                        is Boolean -> stringBuilder.append(item.toString())
                        else -> stringBuilder.append("true")
                    }
                    'h', 'H' -> stringBuilder.append(item?.hashCode()?.let { Integer.toHexString(it) }.toString())
                    'd' -> when (item) {
                        is Int -> stringBuilder.append(item)
                        is Long -> stringBuilder.append(item)
                        else -> throw IllegalArgumentException("Expected Int or Long, got ${item?.let { it::class.java.simpleName }}")
                    }
                    'c', 'C' -> stringBuilder.append(
                        (item as? Char) ?: throw IllegalArgumentException("Expected Char, got ${item?.let { it::class.java.simpleName }}")
                    )
                    'o' -> when (item) {
                        is Int -> stringBuilder.append(Integer.toOctalString(item))
                        is Long -> stringBuilder.append(JLong.toOctalString(item))
                        else -> throw IllegalArgumentException("Expected Int or Long, got ${item?.let { it::class.java.simpleName }}")
                    }
                    'x', 'X' -> when (item) {
                        is Int -> stringBuilder.append(Integer.toHexString(item))
                        is Long -> stringBuilder.append(JLong.toHexString(item))
                        else -> throw IllegalArgumentException("Expected Int or Long, got ${item?.let { it::class.java.simpleName }}")
                    }
                    'f' -> when (item) {
                        is Float -> stringBuilder.append(item)
                        is Double -> stringBuilder.append(item)
                        else -> throw IllegalArgumentException("Expected Float or Double, got ${item?.let { it::class.java.simpleName }}")
                    }
                }
                stringBuilder.append(item)
                string = string.substring((index + 4).coerceAtMost(string.length))
            }
        }
        return stringBuilder.toString()
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
