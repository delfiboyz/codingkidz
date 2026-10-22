package com.codingkidz.app.content

import com.codingkidz.app.engine.Block
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.Dir
import com.codingkidz.app.engine.IdGen
import com.codingkidz.app.engine.Level
import com.codingkidz.app.engine.MoveBlock
import com.codingkidz.app.engine.PickBlock
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.RepeatBlock
import com.codingkidz.app.engine.TurnLeftBlock
import com.codingkidz.app.engine.TurnRightBlock
import kotlin.random.Random

/**
 * Membuat satu labirin baru setiap hari dari tanggal (deterministik), jadi semua anak
 * mendapat tantangan yang sama di hari yang sama tanpa perlu internet.
 */
object DailyChallenge {
    const val ID_PREFIX = "daily-"
    private const val SIZE = 7

    fun idFor(epochDay: Long) = "$ID_PREFIX$epochDay"

    fun isDaily(levelId: String) = levelId.startsWith(ID_PREFIX)

    fun dayOf(levelId: String): Long? = levelId.removePrefix(ID_PREFIX).toLongOrNull()

    fun forDay(epochDay: Long): Level {
        var attempt = 0
        while (true) {
            generate(Random(epochDay * 7919 + attempt))?.let { (map, solution) ->
                return Level(
                    id = idFor(epochDay),
                    title = "Tantangan Harian",
                    instruction = "Labirin baru setiap hari! Ambil semua permata lalu menuju bendera.",
                    map = map,
                    blocks = setOf(BlockType.MOVE, BlockType.TURN_LEFT, BlockType.TURN_RIGHT, BlockType.PICK, BlockType.REPEAT),
                    solution = solution,
                    hints = listOf(
                        "Ikuti jalan dari Bito sampai bendera, satu belokan setiap kali.",
                        "Kalau ada beberapa Maju berturut-turut, ringkas dengan blok Ulangi.",
                    ),
                    funFact = "Datang lagi besok untuk labirin yang berbeda dan jaga terus runtutan harianmu!",
                )
            }
            attempt++
        }
    }

    private sealed interface Move {
        data object Forward : Move
        data object Left : Move
        data object Right : Move
        data object Pick : Move
    }

    private fun generate(rnd: Random): Pair<List<String>, Program>? {
        val start = Pos(rnd.nextInt(SIZE), rnd.nextInt(SIZE))
        val startDir = Dir.entries[rnd.nextInt(4)]
        var dir = startDir
        var cur = start
        val path = mutableListOf(start)
        val visited = mutableSetOf(start)
        val moves = mutableListOf<Move>()
        val targetLength = 9 + rnd.nextInt(5)
        var forwardCount = 0
        var stuck = 0

        fun canEnter(p: Pos, from: Pos): Boolean {
            if (p.x !in 0 until SIZE || p.y !in 0 until SIZE || p in visited) return false
            // Jalan selebar satu petak: petak baru tidak boleh menempel pada jalan lama selain asalnya.
            return Dir.entries.map { p + it }.none { it != from && it in visited }
        }

        while (forwardCount < targetLength) {
            val segment = 1 + rnd.nextInt(3)
            var advanced = 0
            repeat(segment) {
                val next = cur + dir
                if (canEnter(next, cur)) {
                    cur = next
                    visited += cur
                    path += cur
                    moves += Move.Forward
                    forwardCount++
                    advanced++
                }
            }
            if (forwardCount >= targetLength) break
            val turnLeft = rnd.nextBoolean()
            val options = if (turnLeft) listOf(dir.left(), dir.right()) else listOf(dir.right(), dir.left())
            val newDir = options.firstOrNull { canEnter(cur + it, cur) }
            if (newDir == null) {
                if (advanced == 0 && ++stuck > 3) return null
                if (!canEnter(cur + dir, cur)) return null
                continue
            }
            moves += if (newDir == dir.left()) Move.Left else Move.Right
            dir = newDir
        }
        if (path.size < 8) return null

        val candidates = path.drop(1).dropLast(1).shuffled(rnd)
        val gems = candidates.take(2 + rnd.nextInt(2)).toSet()
        val goal = path.last()

        // Sisipkan Ambil tepat setelah Maju yang mendarat di permata.
        val withPicks = mutableListOf<Move>()
        var pos = start
        var d = startDir
        for (m in moves) {
            withPicks += m
            when (m) {
                Move.Forward -> {
                    pos += d
                    if (pos in gems) withPicks += Move.Pick
                }
                Move.Left -> d = d.left()
                Move.Right -> d = d.right()
                Move.Pick -> Unit
            }
        }

        val rows = (0 until SIZE).map { y ->
            buildString {
                for (x in 0 until SIZE) {
                    val p = Pos(x, y)
                    append(
                        when {
                            p == start -> when (startDir) {
                                Dir.NORTH -> '^'
                                Dir.EAST -> '>'
                                Dir.SOUTH -> 'v'
                                Dir.WEST -> '<'
                            }
                            p == goal -> 'G'
                            p in gems -> '*'
                            p in visited -> '.'
                            else -> '#'
                        },
                    )
                }
            }
        }
        return rows to compress(withPicks)
    }

    /** Ubah daftar langkah jadi program; 3+ Maju berturut-turut diringkas jadi Ulangi. */
    private fun compress(moves: List<Move>): Program {
        val ids = IdGen()
        val blocks = mutableListOf<Block>()
        var i = 0
        while (i < moves.size) {
            val m = moves[i]
            if (m == Move.Forward) {
                var run = 0
                while (i + run < moves.size && moves[i + run] == Move.Forward) run++
                if (run >= 3) {
                    val id = ids.next()
                    blocks += RepeatBlock(id, run, listOf(MoveBlock(ids.next())))
                } else {
                    repeat(run) { blocks += MoveBlock(ids.next()) }
                }
                i += run
                continue
            }
            blocks += when (m) {
                Move.Left -> TurnLeftBlock(ids.next())
                Move.Right -> TurnRightBlock(ids.next())
                Move.Pick -> PickBlock(ids.next())
                Move.Forward -> error("unreachable")
            }
            i++
        }
        return Program(blocks)
    }
}
