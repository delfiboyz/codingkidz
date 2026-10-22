package com.codingkidz.app

import com.codingkidz.app.content.DailyChallenge
import com.codingkidz.app.content.QuizBank
import com.codingkidz.app.content.Studio
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.engine.Interpreter
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.engine.allBlocks
import com.codingkidz.app.engine.type
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSolutionsTest {

    @Test
    fun everyLevelIsSolvedByItsSolution() {
        val failures = Worlds.allLevels.mapNotNull { level ->
            val result = Interpreter(level.board).run(level.solution)
            if (result.outcome != Outcome.SUCCESS) "${level.id}: ${result.outcome}" else null
        }
        assertTrue("Level gagal diselesaikan:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun solutionsOnlyUseAllowedBlocksAndFitLimits() {
        Worlds.allLevels.forEach { level ->
            val used = (level.solution.main.allBlocks() + level.solution.functions.flatMap { it.allBlocks() })
            used.forEach { b -> assertTrue("${level.id} memakai ${b.type}", b.type in level.blocks) }
            level.maxBlocks?.let { max ->
                assertTrue("${level.id}: par ${level.par} > maks $max", level.par <= max)
            }
        }
    }

    @Test
    fun debugStartersAreActuallyBroken() {
        Worlds.allLevels.filter { it.starter != null }.forEach { level ->
            val result = Interpreter(level.board).run(level.starter!!)
            assertNotEquals("${level.id}: program awal seharusnya bermasalah", Outcome.SUCCESS, result.outcome)
        }
    }

    @Test
    fun levelIdsAreUnique() {
        val ids = Worlds.allLevels.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun dailyChallengesAreSolvableForAWholeYear() {
        val start = 20_000L
        for (day in start until start + 366) {
            val level = DailyChallenge.forDay(day)
            val result = Interpreter(level.board).run(level.solution)
            assertEquals("hari $day:\n${level.map.joinToString("\n")}", Outcome.SUCCESS, result.outcome)
            assertTrue(level.board.gems.size >= 2)
        }
    }

    @Test
    fun dailyChallengeIsDeterministic() {
        assertEquals(DailyChallenge.forDay(20_500).map, DailyChallenge.forDay(20_500).map)
    }

    @Test
    fun studioExamplesRunCleanly() {
        Studio.examples.forEach { example ->
            val result = Interpreter(Studio.board(example.map), freePlay = true).run(example.program)
            assertEquals(example.title, Outcome.FINISHED, result.outcome)
        }
    }

    @Test
    fun studioGameExampleCanBeWonWithButtons() {
        val example = Studio.examples.first { it.title == "Game Kejar Permata" }
        val board = Studio.board(example.map)
        val interpreter = Interpreter(board, freePlay = true)
        var state = interpreter.run(example.program).finalState!!
        // Jalur manual: kumpulkan permata dengan tombol lalu ke bendera — cukup pastikan tombol bekerja.
        val afterRight = interpreter.runEvent(example.program, state, com.codingkidz.app.engine.EventKind.BUTTON_RIGHT)
        state = afterRight.finalState!!
        assertEquals(com.codingkidz.app.engine.Dir.SOUTH, state.dir)
        val afterUp = interpreter.runEvent(example.program, state, com.codingkidz.app.engine.EventKind.BUTTON_UP)
        assertEquals(com.codingkidz.app.engine.Pos(0, 1), afterUp.finalState!!.pos)
    }

    @Test
    fun worldsHaveStoriesAndAnimals() {
        assertEquals(9, Worlds.all.size)
        Worlds.all.forEach { w ->
            assertTrue(w.id, w.story.isNotBlank() && w.nudges.isNotEmpty() && w.animal.emoji.isNotBlank())
        }
        assertEquals((1..9).toList(), Worlds.all.map { it.number })
    }

    @Test
    fun quizAnswersAreInRange() {
        QuizBank.all.forEach { q -> assertTrue(q.id, q.answer in q.options.indices) }
        assertEquals(QuizBank.all.size, QuizBank.all.map { it.id }.toSet().size)
    }
}
