# kotlinx.serialization: aturan bawaan library sudah mencakup kelas @Serializable; ini pengaman tambahan
# untuk data yang disimpan ke DataStore (progres, pengaturan, karya Studio berisi blok program).
-keep,includedescriptorclasses class com.codingkidz.app.data.**$$serializer { *; }
-keep,includedescriptorclasses class com.codingkidz.app.engine.**$$serializer { *; }
-keepclassmembers class com.codingkidz.app.data.**, com.codingkidz.app.engine.** {
    *** Companion;
}
-keepclasseswithmembers class com.codingkidz.app.data.**, com.codingkidz.app.engine.** {
    kotlinx.serialization.KSerializer serializer(...);
}
