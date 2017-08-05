# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\akain\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.google.android.gms.ads.identifier.** { *; }
-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule


-keepclassmembers class com.finder.harlequinapp.valiante.harlequin.** {
  *;
}

-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }

-dontwarn rx.**
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable