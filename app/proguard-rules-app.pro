-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

## Glide
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public class * extends com.bumptech.glide.module.AppGlideModule
#-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#  **[] $VALUES;
#  public *;
#}

## Room
#-keep @androidx.room.Dao interface *
#-keep @androidx.room.Database class *

## Retrofit
#-keep class retrofit.** { *; }
#-keep class retrofit2.** { *; }
#-keep class com.google.gson.** { *; }
#-keep class com.google.inject.** { *; }
#-keep class org.apache.http.** { *; }
#-keep class org.apache.james.mime4j.** { *; }
#-keep class javax.inject.** { *; }
#-keep class javax.xml.stream.** { *; }
#-keep class com.google.appengine.** { *; }
#-dontwarn com.squareup.okhttp.*
#-dontwarn rx.**
#-dontwarn javax.xml.stream.**
#-dontwarn com.google.appengine.**
#-dontwarn java.nio.file.**
#-dontwarn org.codehaus.**
#-dontwarn retrofit2.**
#-dontwarn org.codehaus.mojo.**
#-keepattributes RuntimeVisibleAnnotations
#-keepattributes RuntimeInvisibleAnnotations
#-keepattributes RuntimeVisibleParameterAnnotations
#-keepattributes RuntimeInvisibleParameterAnnotations
#-keepattributes EnclosingMethod
#-keepclasseswithmembers class * {
#    @retrofit2.http.* <methods>;
#}
#-keepclasseswithmembers interface * {
#    @retrofit2.* <methods>;
#}
#-keepattributes *Annotation*
#-keepattributes Signature
#-keepattributes Exceptions

