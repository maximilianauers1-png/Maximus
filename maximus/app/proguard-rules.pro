# SQLCipher: native code calls back into these classes via JNI.
-keep class net.zetetic.database.** { *; }

# C3: strip all android.util.Log calls from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# P6 Maximus: MediaPipe LLM Inference calls Java classes from native code (JNI) and parses
# protobuf-lite metadata reflectively; keep both. AutoValue/annotation references are optional.
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.mediapipe.**
-dontwarn com.google.protobuf.**
-dontwarn com.google.auto.value.**
-dontwarn javax.lang.model.**
