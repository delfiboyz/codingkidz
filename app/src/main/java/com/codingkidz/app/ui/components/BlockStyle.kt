package com.codingkidz.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material.icons.rounded.ExposurePlus1
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.TurnLeft
import androidx.compose.material.icons.rounded.TurnRight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.codingkidz.app.engine.BlockType

/** Kategori blok dengan warna khasnya, seperti di editor visual profesional. */
enum class BlockCategory(val color: Color, val label: String) {
    MOTION(Color(0xFF3B82F6), "Gerak"),
    ACTION(Color(0xFF16A34A), "Aksi"),
    LOOP(Color(0xFFF97316), "Perulangan"),
    LOGIC(Color(0xFF8B5CF6), "Logika"),
    FUNCTION(Color(0xFFDB2777), "Fungsi"),
    ART(Color(0xFF0D9488), "Seni"),
    VARIABLE(Color(0xFFB45309), "Variabel"),
    LOOKS(Color(0xFFC026D3), "Tampilan"),
    EVENT(Color(0xFF4F46E5), "Event"),
}

data class BlockStyle(val label: String, val icon: ImageVector, val category: BlockCategory) {
    val color: Color get() = category.color
}

fun styleOf(type: BlockType): BlockStyle = when (type) {
    BlockType.MOVE -> BlockStyle("Maju", Icons.Rounded.ArrowUpward, BlockCategory.MOTION)
    BlockType.TURN_LEFT -> BlockStyle("Belok Kiri", Icons.Rounded.TurnLeft, BlockCategory.MOTION)
    BlockType.TURN_RIGHT -> BlockStyle("Belok Kanan", Icons.Rounded.TurnRight, BlockCategory.MOTION)
    BlockType.PICK -> BlockStyle("Ambil", Icons.Rounded.Diamond, BlockCategory.ACTION)
    BlockType.PAINT -> BlockStyle("Cat", Icons.Rounded.Brush, BlockCategory.ART)
    BlockType.REPEAT -> BlockStyle("Ulangi", Icons.Rounded.Repeat, BlockCategory.LOOP)
    BlockType.REPEAT_UNTIL -> BlockStyle("Ulangi sampai finis", Icons.Rounded.SportsScore, BlockCategory.LOOP)
    BlockType.IF -> BlockStyle("Jika", Icons.AutoMirrored.Rounded.CallSplit, BlockCategory.LOGIC)
    BlockType.IF_ELSE -> BlockStyle("Jika–Lainnya", Icons.AutoMirrored.Rounded.CallSplit, BlockCategory.LOGIC)
    BlockType.CALL_A -> BlockStyle("Fungsi A", Icons.Rounded.Functions, BlockCategory.FUNCTION)
    BlockType.CALL_B -> BlockStyle("Fungsi B", Icons.Rounded.Functions, BlockCategory.FUNCTION)
    BlockType.SET_VAR -> BlockStyle("angka =", Icons.Rounded.Inventory2, BlockCategory.VARIABLE)
    BlockType.CHANGE_VAR -> BlockStyle("Ubah angka", Icons.Rounded.ExposurePlus1, BlockCategory.VARIABLE)
    BlockType.MOVE_VAR -> BlockStyle("Maju sebanyak angka", Icons.Rounded.DoubleArrow, BlockCategory.MOTION)
    BlockType.SAY -> BlockStyle("Katakan", Icons.Rounded.ChatBubble, BlockCategory.LOOKS)
    BlockType.WAIT -> BlockStyle("Tunggu", Icons.Rounded.HourglassBottom, BlockCategory.LOOKS)
}

/** Warna-warna robot yang bisa dipilih anak. */
val AvatarColors = listOf(
    Color(0xFF3B82F6) to "Biru Langit",
    Color(0xFF8B5CF6) to "Ungu Galaksi",
    Color(0xFFEC4899) to "Merah Jambu",
    Color(0xFFF97316) to "Oranye Senja",
    Color(0xFF22C55E) to "Hijau Daun",
    Color(0xFF14B8A6) to "Tosca Laut",
    Color(0xFFEAB308) to "Kuning Mentari",
    Color(0xFFEF4444) to "Merah Api",
)

fun avatarColor(index: Int): Color = AvatarColors[index.mod(AvatarColors.size)].first

/** Kalimat siap pakai untuk blok "Katakan" (anak juga bisa mengetik sendiri). */
val SayPresets = listOf("Halo!", "Hore! 🎉", "Aku Bito!", "Ayo main!", "Wah, permata!", "Aku menang! 🏆", "♪ La la la ♪", "Sampai jumpa!")
