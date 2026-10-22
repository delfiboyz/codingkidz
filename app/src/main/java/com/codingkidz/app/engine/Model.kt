package com.codingkidz.app.engine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Arah hadap robot. Urutan searah jarum jam supaya belok kanan = +1. */
enum class Dir(val dx: Int, val dy: Int, val label: String) {
    NORTH(0, -1, "atas"),
    EAST(1, 0, "kanan"),
    SOUTH(0, 1, "bawah"),
    WEST(-1, 0, "kiri");

    fun left(): Dir = entries[(ordinal + 3) % 4]
    fun right(): Dir = entries[(ordinal + 1) % 4]

    /** Rotasi gambar robot dalam derajat, 0 = menghadap atas. */
    val degrees: Float get() = ordinal * 90f
}

data class Pos(val x: Int, val y: Int) {
    operator fun plus(d: Dir) = Pos(x + d.dx, y + d.dy)
}

enum class Tile { FLOOR, WALL, BLUE, ORANGE }

@Serializable
enum class PaintColor(val code: Char, val argb: Long, val label: String, val codeName: String) {
    RED('r', 0xFFEF4444, "Merah", "merah"),
    ORANGE('o', 0xFFF97316, "Oranye", "oranye"),
    YELLOW('y', 0xFFFACC15, "Kuning", "kuning"),
    GREEN('g', 0xFF22C55E, "Hijau", "hijau"),
    BLUE('b', 0xFF3B82F6, "Biru", "biru"),
    PURPLE('p', 0xFFA855F7, "Ungu", "ungu"),
    WHITE('w', 0xFFF8FAFC, "Putih", "putih");

    companion object {
        fun fromCode(c: Char): PaintColor? = entries.firstOrNull { it.code == c }
    }
}

@Serializable
enum class Condition(val label: String, val emoji: String, val py: String, val js: String) {
    PATH_AHEAD("jalan depan terbuka", "⬆️", "depan_terbuka", "depanTerbuka"),
    PATH_LEFT("jalan kiri terbuka", "⬅️", "kiri_terbuka", "kiriTerbuka"),
    PATH_RIGHT("jalan kanan terbuka", "➡️", "kanan_terbuka", "kananTerbuka"),
    ON_GEM("ada permata", "💎", "ada_permata", "adaPermata"),
    ON_BLUE("di petak biru", "🟦", "di_petak_biru", "diPetakBiru"),
    ON_ORANGE("di petak oranye", "🟧", "di_petak_oranye", "diPetakOranye"),
}

/**
 * Event: blok di bawah "Ketika …" dijalankan otomatis saat sesuatu terjadi.
 * Event pemicu ([isTrigger]) diperiksa setelah Bito melangkah; event tombol/sentuh dipakai di Studio.
 */
@Serializable
enum class EventKind(val label: String, val emoji: String, val code: String, val jsCode: String) {
    ON_GEM("Ketika menginjak permata", "💎", "permata", "permata"),
    ON_BLUE("Ketika menginjak petak biru", "🟦", "petak_biru", "petakBiru"),
    ON_ORANGE("Ketika menginjak petak oranye", "🟧", "petak_oranye", "petakOranye"),
    WALL_AHEAD("Ketika jalan di depan buntu", "🧱", "buntu", "buntu"),
    TAP_ROBOT("Ketika Bito disentuh", "👆", "bito_disentuh", "bitoDisentuh"),
    BUTTON_UP("Ketika tombol ⬆ ditekan", "⬆️", "tombol_atas", "tombolAtas"),
    BUTTON_LEFT("Ketika tombol ⬅ ditekan", "⬅️", "tombol_kiri", "tombolKiri"),
    BUTTON_RIGHT("Ketika tombol ➡ ditekan", "➡️", "tombol_kanan", "tombolKanan");

    val isTrigger: Boolean get() = this == ON_GEM || this == ON_BLUE || this == ON_ORANGE || this == WALL_AHEAD

    /** Id semu untuk menyorot kepala event saat dijalankan. */
    val hatId: Long get() = -(ordinal + 1L)
}

/** Jenis blok yang bisa muncul di palet. */
enum class BlockType {
    MOVE, TURN_LEFT, TURN_RIGHT, PICK, PAINT, REPEAT, REPEAT_UNTIL, IF, IF_ELSE, CALL_A, CALL_B,
    SET_VAR, CHANGE_VAR, MOVE_VAR, SAY, WAIT,
}

@Serializable
sealed interface Block {
    val id: Long
}

@Serializable
@SerialName("move")
data class MoveBlock(override val id: Long) : Block

@Serializable
@SerialName("left")
data class TurnLeftBlock(override val id: Long) : Block

@Serializable
@SerialName("right")
data class TurnRightBlock(override val id: Long) : Block

@Serializable
@SerialName("pick")
data class PickBlock(override val id: Long) : Block

@Serializable
@SerialName("paint")
data class PaintBlock(override val id: Long, val color: PaintColor) : Block

@Serializable
@SerialName("repeat")
data class RepeatBlock(override val id: Long, val times: Int, val body: List<Block> = emptyList()) : Block

@Serializable
@SerialName("until")
data class RepeatUntilBlock(override val id: Long, val body: List<Block> = emptyList()) : Block

@Serializable
@SerialName("if")
data class IfBlock(
    override val id: Long,
    val condition: Condition,
    val then: List<Block> = emptyList(),
    /** `null` berarti blok "jika" tanpa "lainnya". */
    val otherwise: List<Block>? = null,
) : Block

@Serializable
@SerialName("call")
data class CallBlock(override val id: Long, val fn: Int) : Block

/** angka = [value] */
@Serializable
@SerialName("set")
data class SetVarBlock(override val id: Long, val value: Int) : Block

/** angka = angka + [delta] */
@Serializable
@SerialName("change")
data class ChangeVarBlock(override val id: Long, val delta: Int) : Block

/** Maju sebanyak nilai angka. */
@Serializable
@SerialName("movevar")
data class MoveVarBlock(override val id: Long) : Block

@Serializable
@SerialName("say")
data class SayBlock(override val id: Long, val text: String) : Block

@Serializable
@SerialName("wait")
data class WaitBlock(override val id: Long) : Block

@Serializable
data class EventHandler(val kind: EventKind, val body: List<Block> = emptyList())

@Serializable
data class Program(
    val main: List<Block> = emptyList(),
    /** functions[0] = Fungsi A, functions[1] = Fungsi B. */
    val functions: List<List<Block>> = emptyList(),
    val events: List<EventHandler> = emptyList(),
) {
    /** Jumlah blok; setiap event yang berisi dihitung +1 untuk blok "Ketika"-nya. */
    fun blockCount(): Int = main.countBlocks() + functions.sumOf { it.countBlocks() } +
        events.filter { it.body.isNotEmpty() }.sumOf { 1 + it.body.countBlocks() }

    fun isEmpty(): Boolean = main.isEmpty() && events.all { it.body.isEmpty() }

    fun everyBlock(): List<Block> = main.allBlocks() + functions.flatMap { it.allBlocks() } + events.flatMap { it.body.allBlocks() }

    fun maxId(): Long = everyBlock().maxOfOrNull { it.id } ?: 0L

    fun function(index: Int): List<Block> = functions.getOrElse(index) { emptyList() }

    fun event(kind: EventKind): List<Block> = events.firstOrNull { it.kind == kind }?.body.orEmpty()

    val usesVariable: Boolean get() = everyBlock().any { it is SetVarBlock || it is ChangeVarBlock || it is MoveVarBlock }
}

fun Block.children(): List<List<Block>> = when (this) {
    is RepeatBlock -> listOf(body)
    is RepeatUntilBlock -> listOf(body)
    is IfBlock -> listOfNotNull(then, otherwise)
    else -> emptyList()
}

fun List<Block>.countBlocks(): Int = sumOf { 1 + it.children().sumOf { c -> c.countBlocks() } }

fun List<Block>.allBlocks(): List<Block> = flatMap { listOf(it) + it.children().flatMap { c -> c.allBlocks() } }

val Block.type: BlockType
    get() = when (this) {
        is MoveBlock -> BlockType.MOVE
        is TurnLeftBlock -> BlockType.TURN_LEFT
        is TurnRightBlock -> BlockType.TURN_RIGHT
        is PickBlock -> BlockType.PICK
        is PaintBlock -> BlockType.PAINT
        is RepeatBlock -> BlockType.REPEAT
        is RepeatUntilBlock -> BlockType.REPEAT_UNTIL
        is IfBlock -> if (otherwise == null) BlockType.IF else BlockType.IF_ELSE
        is CallBlock -> if (fn == 0) BlockType.CALL_A else BlockType.CALL_B
        is SetVarBlock -> BlockType.SET_VAR
        is ChangeVarBlock -> BlockType.CHANGE_VAR
        is MoveVarBlock -> BlockType.MOVE_VAR
        is SayBlock -> BlockType.SAY
        is WaitBlock -> BlockType.WAIT
    }

val Block.isContainer: Boolean get() = this is RepeatBlock || this is RepeatUntilBlock || this is IfBlock
