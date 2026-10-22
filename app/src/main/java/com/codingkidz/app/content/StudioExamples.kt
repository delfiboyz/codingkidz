package com.codingkidz.app.content

import com.codingkidz.app.engine.Board
import com.codingkidz.app.engine.EventKind
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.program

data class StudioExample(
    val title: String,
    val emoji: String,
    val description: String,
    val program: Program,
    val map: List<String>? = null,
)

/** Alat di mode desain peta Studio. */
enum class DesignTool(val emoji: String, val label: String, val char: Char) {
    WALL("🧱", "Tembok", '#'),
    GEM("💎", "Permata", '*'),
    GOAL("🏁", "Bendera", 'G'),
    BLUE("🟦", "Petak biru", 'b'),
    ORANGE("🟧", "Petak oranye", 'o'),
    ROBOT("🤖", "Posisi Bito", '>'),
    ERASE("🧽", "Hapus", '.'),
}

/** Kanvas, peta, dan contoh program untuk Studio Kreasi. */
object Studio {
    const val SIZE = 8

    private val startChars = setOf('>', '<', '^', 'v')

    fun emptyMap(): List<String> = List(SIZE) { y -> if (y == 0) ">" + ".".repeat(SIZE - 1) else ".".repeat(SIZE) }

    fun canvas(): Board = Board.canvas(SIZE, SIZE)

    fun board(map: List<String>?): Board = map?.let { runCatching { Board.parse(it) }.getOrNull() } ?: canvas()

    /** Menerapkan alat desain di petak [pos]. Bito selalu ada tepat satu; bendera paling banyak satu. */
    fun applyTool(map: List<String>, pos: Pos, tool: DesignTool): List<String> {
        val grid = map.map { it.toCharArray() }
        val current = grid[pos.y][pos.x]
        when (tool) {
            DesignTool.ROBOT -> {
                if (current in startChars) {
                    // Ketuk Bito lagi untuk memutar arahnya.
                    grid[pos.y][pos.x] = when (current) { '>' -> 'v'; 'v' -> '<'; '<' -> '^'; else -> '>' }
                } else {
                    grid.forEach { row -> row.indices.forEach { if (row[it] in startChars) row[it] = '.' } }
                    grid[pos.y][pos.x] = '>'
                }
            }
            else -> {
                if (current in startChars) return map
                if (tool == DesignTool.GOAL) grid.forEach { row -> row.indices.forEach { if (row[it] == 'G') row[it] = '.' } }
                grid[pos.y][pos.x] = if (current == tool.char) '.' else tool.char
            }
        }
        return grid.map { String(it) }
    }

    private val gameMap = listOf(
        ">..#....",
        ".#.#.*#.",
        ".#...#..",
        ".#*#.#.*",
        "...#....",
        "##.###.#",
        "*......G",
        ".#.#*#..",
    )

    val examples = listOf(
        StudioExample(
            title = "Game Kejar Permata",
            emoji = "🕹️",
            description = "Kendalikan Bito dengan tombol. Ambil semua permata lalu ke bendera!",
            map = gameMap,
            program = program(
                events = mapOf(
                    EventKind.BUTTON_UP to { move() },
                    EventKind.BUTTON_LEFT to { left() },
                    EventKind.BUTTON_RIGHT to { right() },
                    EventKind.ON_GEM to { pick(); say("Hore! 💎") },
                ),
            ) { say("Pakai tombol ⬅ ⬆ ➡ untuk bermain!") },
        ),
        StudioExample(
            title = "Cerita Bito",
            emoji = "📖",
            description = "Cerita interaktif: sentuh Bito dan dia akan menari.",
            program = program(
                events = mapOf(
                    EventKind.TAP_ROBOT to { loop(4) { right() }; say("Wiii! ♪") },
                    EventKind.BUTTON_UP to { move(); say("Aku jalan-jalan!") },
                ),
            ) {
                say("Halo! Aku Bito.")
                waitABit()
                say("Sentuh aku untuk melihatku menari!")
            },
        ),
        StudioExample(
            title = "Pelangi",
            emoji = "🌈",
            description = "Animasi melukis enam baris warna, bolak-balik seperti ular.",
            program = program {
                val colors = listOf(PaintColor.RED, PaintColor.ORANGE, PaintColor.YELLOW, PaintColor.GREEN, PaintColor.BLUE, PaintColor.PURPLE)
                colors.forEachIndexed { i, c ->
                    loop(SIZE - 1) { paint(c); move() }
                    paint(c)
                    if (i < colors.lastIndex) {
                        if (i % 2 == 0) { right(); move(); right() } else { left(); move(); left() }
                    }
                }
            },
        ),
        StudioExample(
            title = "Bingkai",
            emoji = "🖼️",
            description = "Perulangan bersarang menggambar tepi kanvas.",
            program = program { loop(4) { loop(SIZE - 1) { paint(PaintColor.BLUE); move() }; right() } },
        ),
        StudioExample(
            title = "Tiga Kotak",
            emoji = "🟧",
            description = "Satu fungsi menggambar kotak, dipanggil tiga kali.",
            program = program(fnA = { loop(4) { loop(2) { paint(PaintColor.ORANGE); move() }; right() } }) {
                callA()
                loop(4) { move() }
                callA()
                right()
                loop(4) { move() }
                callA()
            },
        ),
    )
}
