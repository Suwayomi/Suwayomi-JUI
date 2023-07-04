-dontobfuscate
-keepattributes Signature,LineNumberTable

-keep,allowoptimization class ca.gosyer.** { public protected *; }

# Fix crash with reflection access to methods
-keepclassmembers class androidx.lifecycle.SavedStateHandleSupport { *; }
-keepclassmembers class androidx.lifecycle.SavedStateHandlesVM { *; }
-keepclassmembers class androidx.lifecycle.SavedStateHandlesProvider { *; }

# Kotlin
# todo optimize more
-keep class kotlin.reflect.** { *; }

# SLF4J
-dontwarn org.apache.logging.slf4j.**
-keep class org.apache.logging.slf4j.** { *; }

# OKHTTP
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.**

# Awt and Swing
-dontwarn java.awt.**
-dontwarn javax.swing.**

## Process
-dontwarn java.lang.ProcessHandle

# Accessability
-dontwarn javax.accessibility.Accessible
-dontwarn javax.accessibility.AccessibleContext

# Ktor
-dontwarn io.ktor.network.sockets.DatagramSendChannel

# Coroutines
-dontwarn kotlinx.coroutines.**

# Other
-dontwarn org.pbjar.jxlayer.plaf.ext.TransformUI
-dontwarn com.kitfox.svg.app.ant.SVGToImageAntTask
-dontwarn nl.adaptivity.xmlutil.StAXWriter
-keep class com.sun.jna.** { *; }
-dontwarn org.slf4j.impl.StaticLoggerBinder

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

-keep,includedescriptorclasses class ca.gosyer.**$$serializer { *; }
-keepclassmembers class ca.gosyer.** {
    *** Companion;
}
-keepclasseswithmembers class ca.gosyer.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Skia
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }
