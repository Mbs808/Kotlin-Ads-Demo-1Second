#
-keep class androidx.appcompat.widget.** { *; }

-keep class com.google.android.** { *; }

#-flattenpackagehierarchy
-ignorewarnings

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** d(...);
    public static *** w(...);
    public static *** v(...);
    public static *** i(...);
    public static *** e(...);
}
-keepattributes LineNumberTable

-keep public class com.google.android.gms.ads.**{
   public *;
}
#==================================
# Glide
-keep class com.bumptech.glide.** { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
  @retrofit2.http.* <methods>;
}

# Lottie Animation
-keep class com.airbnb.lottie.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Google Ad
-keep class com.google.android.gms.ads.** { *; }

# Google Play
-keep class com.google.android.play.** { *; }

# ===============================
-dontwarn okio.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-dontwarn com.bumptech.glide.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**

-keep class android.net.http.* { *; }
-keep interface org.apache.* { *; }
-keep enum org.apache.* { *; }
-keep class org.apache.* { *; }
-keep class org.apache.commons.* { *; }
-keep class org.apache.http.* { *; }
-keep class com.bumptech.glide.**{*;}

-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

-keep class com.google.gson.stream.* { *; }

-keep class com.google.gson.examples.android.model.** { <fields>; }

-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Gson
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
        @retrofit.http.* <methods>;
}
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# keep enum so gson can deserialize it
-keepclassmembers enum * { *; }

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class net.mreunionlabs.wob.model.request.** { *; }
-keep class net.mreunionlabs.wob.model.response.** { *; }
-keep class net.mreunionlabs.wob.model.gson.** { *; }

-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-keep class com.coremedia.iso.** {*;}
-keep class com.googlecode.mp4parser.** {*;}
-keep class com.mp4parser.** {*;}

-dontwarn com.coremedia.**
-dontwarn com.googlecode.mp4parser.**

-dontwarn android.support.**
-dontwarn com.origin.moreads.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.chromium.net.*
-dontwarn org.apache.http.conn.ssl.DefaultHostnameVerifier
-dontwarn org.apache.http.HttpHost
-dontwarn org.conscrypt.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.util.**
-dontwarn javax.tools.**
-dontwarn java.lang.**
-dontwarn javax.xml.**

# Rendescript
-keepclasseswithmembernames class * {
   native <methods>;
}

-keep class androidx.renderscript.** { *; }

# lifecycle
-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}
-keep !interface * implements androidx.lifecycle.LifecycleObserver {
}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }
# lifecycle

# for 33
-keepclassmembers class kotlin.SafePublicationLazyImpl {
    java.lang.Object _value;
}


# My Customize
-keep class com.origin.moreads.models.** {*;}

-obfuscationdictionary "C:\Users\mehul\AppData\Local\Android\Sdk\tools\class_encode_dictionary.txt"
-classobfuscationdictionary "C:\Users\mehul\AppData\Local\Android\Sdk\tools\class_encode_dictionary.txt"
-packageobfuscationdictionary "C:\Users\mehul\AppData\Local\Android\Sdk\tools\class_encode_dictionary.txt"

-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses "com.origin.adsdemo"