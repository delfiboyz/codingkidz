package com.codingkidz.app.engine

/**
 * Peta permainan. Dibuat dari baris teks:
 *
 * - `#` tembok, `.` lantai
 * - `>` `<` `^` `v` posisi awal robot beserta arah hadapnya
 * - `G` bendera tujuan, `*` permata
 * - `b` petak biru, `o` petak oranye
 *
 * Pola target (untuk level melukis) memakai kode warna [PaintColor.code], `.` berarti kosong.
 */
class Board(
    val width: Int,
    val height: Int,
    private val tiles: List<Tile>,
    val start: Pos,
    val startDir: Dir,
    val goal: Pos?,
    val gems: Set<Pos>,
    val target: Map<Pos, PaintColor>? = null,
) {
    fun inBounds(p: Pos) = p.x in 0 until width && p.y in 0 until height

    fun tile(p: Pos): Tile = if (inBounds(p)) tiles[p.y * width + p.x] else Tile.WALL

    fun isOpen(p: Pos) = tile(p) != Tile.WALL

    val isPaintLevel: Boolean get() = target != null

    companion object {
        fun parse(rows: List<String>, goalAtStart: Boolean = false, target: List<String>? = null): Board {
            require(rows.isNotEmpty()) { "Peta kosong" }
            val width = rows.maxOf { it.length }
            val height = rows.size
            val tiles = ArrayList<Tile>(width * height)
            var start: Pos? = null
            var startDir = Dir.EAST
            var goal: Pos? = null
            val gems = mutableSetOf<Pos>()
            rows.forEachIndexed { y, row ->
                for (x in 0 until width) {
                    val c = row.getOrElse(x) { '#' }
                    val p = Pos(x, y)
                    tiles += when (c) {
                        '#' -> Tile.WALL
                        'b' -> Tile.BLUE
                        'o' -> Tile.ORANGE
                        else -> Tile.FLOOR
                    }
                    when (c) {
                        '>' -> { start = p; startDir = Dir.EAST }
                        '<' -> { start = p; startDir = Dir.WEST }
                        '^' -> { start = p; startDir = Dir.NORTH }
                        'v' -> { start = p; startDir = Dir.SOUTH }
                        'G' -> goal = p
                        '*' -> gems += p
                    }
                }
            }
            val s = requireNotNull(start) { "Peta tidak punya posisi awal robot" }
            if (goalAtStart) goal = s
            val targetMap = target?.let { t ->
                buildMap {
                    t.forEachIndexed { y, row ->
                        row.forEachIndexed { x, c -> PaintColor.fromCode(c)?.let { put(Pos(x, y), it) } }
                    }
                }
            }
            return Board(width, height, tiles, s, startDir, goal, gems, targetMap)
        }

        /** Kanvas kosong untuk Studio Kreasi. */
        fun canvas(width: Int, height: Int): Board = Board(
            width = width,
            height = height,
            tiles = List(width * height) { Tile.FLOOR },
            start = Pos(0, 0),
            startDir = Dir.EAST,
            goal = null,
            gems = emptySet(),
        )
    }
}
