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
