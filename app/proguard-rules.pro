# kotlinx.serialization: aturan bawaan library sudah cukup untuk kelas @Serializable.
# Simpan nama kelas data yang disimpan ke DataStore agar file JSON lama tetap terbaca.
-keep,includedescriptorclasses class com.codingkidz.app.data.**$$serializer { *; }
-keepclassmembers class com.codingkidz.app.data.** {
    *** Companion;
}
