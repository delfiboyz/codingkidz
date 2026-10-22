package com.codingkidz.app

import com.codingkidz.app.engine.Action
import com.codingkidz.app.engine.Board
import com.codingkidz.app.engine.CodeExporter
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.Cursor
import com.codingkidz.app.engine.Interpreter
import com.codingkidz.app.engine.ListRef
import com.codingkidz.app.engine.MoveBlock
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.ProgramEditor
import com.codingkidz.app.engine.RepeatBlock
import com.codingkidz.app.engine.program
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InterpreterTest {
    private val corridor = Board.parse(listOf(">..G"))

    @Test
    fun bumpingIntoWallFails() {
        val board = Board.parse(listOf(">#G"))
        val result = Interpreter(board).run(program { move() })
        assertEquals(Outcome.HIT_WALL, result.outcome)
        assertEquals(Action.BUMP, result.steps.last().action)
    }

    @Test
    fun stopsAsSoonAsGoalReached() {
        val result = Interpreter(corridor).run(program { moves(10) })
        assertEquals(Outcome.SUCCESS, result.outcome)
        assertEquals(3, result.physicalSteps)
    }

    @Test
    fun endingBeforeGoalIsReported() {
        assertEquals(Outcome.NOT_AT_GOAL, Interpreter(corridor).run(program { move() }).outcome)
    }

    @Test
    fun pickingWithoutGemFails() {
        val board = Board.parse(listOf(">.*G"))
        assertEquals(Outcome.NO_GEM_HERE, Interpreter(board).run(program { move(); pick() }).outcome)
        assertEquals(Outcome.GEMS_LEFT, Interpreter(board).run(program { moves(3) }).outcome)
    }

    @Test
    fun infiniteLoopIsDetected() {
        val board = Board.parse(listOf(">.#G"))
        val result = Interpreter(board).run(program { untilGoal { right() } })
        assertEquals(Outcome.INFINITE_LOOP, result.outcome)
    }

    @Test
    fun emptyUntilLoopDoesNotHang() {
        val result = Interpreter(corridor).run(program { untilGoal { } })
        assertEquals(Outcome.INFINITE_LOOP, result.outcome)
    }

    @Test
    fun recursionIsBounded() {
        val result = Interpreter(corridor).run(program(fnA = { right(); callA() }) { callA() })
        assertEquals(Outcome.INFINITE_LOOP, result.outcome)
    }

    @Test
    fun conditionsAreRecorded() {
        val board = Board.parse(listOf(">*.*"))
        val result = Interpreter(board).run(program { loop(5) { move(); ifThen(Condition.ON_GEM) { pick() } } })
        assertEquals(Outcome.SUCCESS, result.outcome)
        val checks = result.steps.filter { it.action == Action.CHECK }.map { it.check }
        assertEquals(listOf(true, false, true), checks)
    }

    @Test
    fun paintingMatchesTarget() {
        val board = Board.parse(listOf(">.."), target = listOf("rr."))
        val result = Interpreter(board).run(program { loop(3) { paint(PaintColor.RED); move() } })
        assertEquals(Outcome.SUCCESS, result.outcome)
        val wrong = Interpreter(board).run(program { paint(PaintColor.BLUE); move(); paint(PaintColor.RED) })
        assertEquals(Outcome.PATTERN_MISMATCH, wrong.outcome)
    }

    @Test
    fun freePlayFinishes() {
        val result = Interpreter(Board.canvas(4, 4), freePlay = true).run(program { paint(PaintColor.RED); move() })
        assertEquals(Outcome.FINISHED, result.outcome)
        assertEquals(mapOf(Pos(0, 0) to PaintColor.RED), result.steps.last().state.painted)
    }

    @Test
    fun editorInsertsRemovesAndShifts() {
        var p = Program()
        p = ProgramEditor.insert(p, Cursor(ListRef.Main, 0), RepeatBlock(1, 3))
        p = ProgramEditor.insert(p, Cursor(ListRef.Body(1), 0), MoveBlock(2))
        p = ProgramEditor.insert(p, Cursor(ListRef.Main, 1), MoveBlock(3))
        assertEquals(3, p.blockCount())
        assertEquals(Cursor(ListRef.Body(1), 0), ProgramEditor.locate(p, 2))
        p = ProgramEditor.shift(p, 3, -1)
        assertEquals(3L, p.main.first().id)
        p = ProgramEditor.remove(p, 1)
        assertEquals(1, p.blockCount())
        assertTrue(!ProgramEditor.exists(p, ListRef.Body(1)))
    }

    @Test
    fun functionsGetCreatedOnDemand() {
        val p = ProgramEditor.insert(Program(), Cursor(ListRef.Function(1), 0), MoveBlock(5))
        assertEquals(2, p.functions.size)
        assertEquals(1, p.function(1).size)
    }

    @Test
    fun variablesDriveMovement() {
        val board = Board.parse(listOf(">...G"))
        val result = Interpreter(board).run(program { setVar(2); moveVar(); changeVar(2); moveVar() })
        assertEquals(Outcome.SUCCESS, result.outcome)
        assertEquals(4, result.finalState!!.number)
    }

    @Test
    fun zeroVariableDoesNotMove() {
        val board = Board.parse(listOf(">.G"))
        val result = Interpreter(board).run(program { setVar(0); moveVar() })
        assertEquals(Outcome.NOT_AT_GOAL, result.outcome)
    }

    @Test
    fun triggerEventsFireAfterMoving() {
        val board = Board.parse(listOf(">*.*"))
        val p = program(events = mapOf(com.codingkidz.app.engine.EventKind.ON_GEM to { pick() })) { loop(3) { move() } }
        val result = Interpreter(board).run(p)
        assertEquals(Outcome.SUCCESS, result.outcome)
        assertEquals(2, result.steps.count { it.action == Action.EVENT })
    }

    @Test
    fun eventsDoNotNest() {
        val board = Board.parse(listOf(">bbbbG"))
        // Handler yang maju ke petak biru lain tidak memicu event lagi (tanpa rekursi).
        val p = program(events = mapOf(com.codingkidz.app.engine.EventKind.ON_BLUE to { move() })) { move() }
        val result = Interpreter(board).run(p)
        assertEquals(2, result.finalState!!.pos.x)
    }

    @Test
    fun sayAndWaitAreRecorded() {
        val result = Interpreter(Board.canvas(3, 3), freePlay = true).run(program { say("Halo"); waitABit() })
        assertEquals("Halo", result.finalState!!.say)
        assertEquals(listOf(Action.SAY, Action.WAIT), result.steps.map { it.action })
    }

    @Test
    fun moveBlockIntoContainerAndBack() {
        var p = program { move(); loop(2) { left() } }
        val moveId = p.main[0].id
        val loopId = p.main[1].id
        p = ProgramEditor.move(p, moveId, Cursor(ListRef.Body(loopId), 0))
        assertEquals(1, p.main.size)
        assertEquals(moveId, (p.main[0] as RepeatBlock).body[0].id)
        p = ProgramEditor.move(p, moveId, Cursor(ListRef.Main, 1))
        assertEquals(listOf(loopId, moveId), p.main.map { it.id })
    }

    @Test
    fun cannotMoveContainerIntoItself() {
        val p = program { loop(2) { loop(2) { move() } } }
        val outer = p.main[0] as RepeatBlock
        val inner = outer.body[0]
        assertEquals(p, ProgramEditor.move(p, outer.id, Cursor(ListRef.Body(inner.id), 0)))
    }

    @Test
    fun moveWithinSameListAdjustsIndex() {
        val p = program { move(); left(); right() }
        val ids = p.main.map { it.id }
        val moved = ProgramEditor.move(p, ids[0], Cursor(ListRef.Main, 3))
        assertEquals(listOf(ids[1], ids[2], ids[0]), moved.main.map { it.id })
    }

    @Test
    fun eventsCountTowardsBlocks() {
        val p = program(events = mapOf(com.codingkidz.app.engine.EventKind.ON_GEM to { pick() })) { move() }
        assertEquals(3, p.blockCount())
    }

    @Test
    fun exportsJavaScript() {
        val code = CodeExporter.asPlainText(
            program(events = mapOf(com.codingkidz.app.engine.EventKind.ON_GEM to { pick() })) {
                setVar(2)
                loop(2) { moveVar(); ifElse(Condition.PATH_AHEAD, { move() }, { left() }) }
            },
            com.codingkidz.app.engine.CodeLanguage.JAVASCRIPT,
        )
        val expected = """
            let angka = 0;
            ketika("permata", () => {
                ambil();
            });

            angka = 2;
            for (let i = 0; i < 2; i++) {
                maju(angka);
                if (depanTerbuka()) {
                    maju();
                } else {
                    belokKiri();
                }
            }
        """.trimIndent()
        assertEquals(expected, code)
    }

    @Test
    fun exportsReadableCode() {
        val code = CodeExporter.asPlainText(
            program(fnA = { move() }) { loop(2) { callA(); ifElse(Condition.PATH_AHEAD, { move() }, { left() }) } },
        )
        val expected = """
            def fungsi_a():
                maju()

            for i in range(2):
                fungsi_a()
                if depan_terbuka():
                    maju()
                else:
                    belok_kiri()
        """.trimIndent()
        assertEquals(expected, code)
    }
}
