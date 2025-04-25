# Сохраняем используемые классы из plugin.xml
-keep class kz.pandev.plugins.autoworklog.widgets.PanDevStatusBarFactory { *; }
-keep class kz.pandev.plugins.autoworklog.PanDevAutoWorklogStartupActivity { *; }
-keep class kz.pandev.plugins.autoworklog.actions.Menu { *; }
-keep class kz.pandev.plugins.autoworklog.PanDevJiraAutoWorklog { *; }

# Сохраняем зависимости IntelliJ Platform
-keep class com.intellij.** { *; }
-keep interface com.intellij.** { *; }

# Сохраняем атрибуты, необходимые для работы IntelliJ
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Устраняем предупреждения
-dontwarn com.intellij.**
-dontwarn kz.pandev.plugins.autoworklog.**

# Убираем оптимизацию и сжатие кода
-dontoptimize
-dontshrink

# Сохраняем классы, реализующие PersistentStateComponent
-keep class * implements com.intellij.openapi.components.PersistentStateComponent { *; }

# Сохраняем статические экземпляры
-keepclassmembers class * { public static ** INSTANCE; }

# Сохраняем API-клиент и используемые классы
-keep class kz.pandev.plugins.autoworklog.clients.ApiClient { *; }

# Сохраняем Jackson для сериализации/десериализации
-keep class com.fasterxml.jackson.databind.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Сохраняем стандартные классы Java, используемые HttpClient
-keep class java.net.http.** { *; }
-keep class java.net.URI { *; }

# Устраняем предупреждения для стандартных классов
-dontwarn java.net.http.**
-dontwarn java.net.URI
-dontwarn com.fasterxml.jackson.databind.**

# DTO для сериализации (сохраняем все поля и методы)
-keep class kz.pandev.plugins.autoworklog.models.** { *; }

# Jackson аннотации
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
}
-keep @com.fasterxml.jackson.annotation.JsonInclude class *
-keep @com.fasterxml.jackson.annotation.JsonFormat class *
-keep @com.fasterxml.jackson.annotation.JsonProperty class *
-keep @com.fasterxml.jackson.databind.annotation.JsonDeserialize class *
