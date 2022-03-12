-dontobfuscate
-keepattributes Signature,LineNumberTable

-keep,allowoptimization class ca.gosyer.** { public protected *; }
-keep class ca.gosyer.ui.main.MainKt {
    public static void main(java.lang.String[]);
}
-keep class 'module-info'

# Kotlin
# todo optimize more
-keep class kotlin.reflect.** { *; }

# Log4J
-dontwarn org.apache.logging.log4j.**
-keep class org.apache.logging.log4j.** { *; }

# SLF4J
-dontwarn org.apache.logging.slf4j.**
-keep class org.apache.logging.slf4j.** { *; }

# OKHTTP
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.**

# DarkLaf
# todo optimize more
-keep class com.github.weisj.darklaf.** { *; }
-dontwarn com.github.weisj.darklaf.**

# Ktor
-dontwarn io.ktor.network.sockets.DatagramSendChannel

# Coroutines
-keep class kotlinx.coroutines.swing.** { *; }
-dontwarn kotlinx.coroutines.**

# Other
-dontwarn org.pbjar.jxlayer.plaf.ext.TransformUI
-dontwarn com.kitfox.svg.app.ant.SVGToImageAntTask
-dontwarn nl.adaptivity.xmlutil.StAXWriter
-keep class com.sun.jna.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class eu.kanade.tachiyomi.**$$serializer { *; }
-keepclassmembers class ca.gosyer.** {
    *** Companion;
}
-keepclasseswithmembers class ca.gosyer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Skia
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }