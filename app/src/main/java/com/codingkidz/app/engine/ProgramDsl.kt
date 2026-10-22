package com.codingkidz.app.engine

/** Penghasil id blok yang unik dalam satu program. */
class IdGen(start: Long = 0L) {
    private var last = start
    fun next(): Long = ++last
}

@DslMarker
annotation class ProgramDslMarker

/** DSL ringkas untuk menulis program contoh, kunci jawaban, dan program awal level debug. */
@ProgramDslMarker
class BlockListBuilder(private val ids: IdGen) {
    private val blocks = mutableListOf<Block>()

    fun move() { blocks += MoveBlock(ids.next()) }
    fun left() { blocks += TurnLeftBlock(ids.next()) }
    fun right() { blocks += TurnRightBlock(ids.next()) }
    fun pick() { blocks += PickBlock(ids.next()) }
    fun paint(color: PaintColor) { blocks += PaintBlock(ids.next(), color) }
    fun callA() { blocks += CallBlock(ids.next(), 0) }
    fun callB() { blocks += CallBlock(ids.next(), 1) }
    fun setVar(value: Int) { blocks += SetVarBlock(ids.next(), value) }
    fun changeVar(delta: Int) { blocks += ChangeVarBlock(ids.next(), delta) }
    fun moveVar() { blocks += MoveVarBlock(ids.next()) }
    fun say(text: String) { blocks += SayBlock(ids.next(), text) }
    fun waitABit() { blocks += WaitBlock(ids.next()) }

    fun moves(n: Int) = repeat(n) { move() }

    fun loop(times: Int, body: BlockListBuilder.() -> Unit) {
        val id = ids.next()
        blocks += RepeatBlock(id, times, BlockListBuilder(ids).apply(body).build())
    }

    fun untilGoal(body: BlockListBuilder.() -> Unit) {
        val id = ids.next()
        blocks += RepeatUntilBlock(id, BlockListBuilder(ids).apply(body).build())
    }

    fun ifThen(condition: Condition, then: BlockListBuilder.() -> Unit) {
        val id = ids.next()
        blocks += IfBlock(id, condition, BlockListBuilder(ids).apply(then).build(), null)
    }

    fun ifElse(condition: Condition, then: BlockListBuilder.() -> Unit, otherwise: BlockListBuilder.() -> Unit) {
        val id = ids.next()
        val t = BlockListBuilder(ids).apply(then).build()
        val o = BlockListBuilder(ids).apply(otherwise).build()
        blocks += IfBlock(id, condition, t, o)
    }

    fun build(): List<Block> = blocks.toList()
}

fun program(
    fnA: (BlockListBuilder.() -> Unit)? = null,
    fnB: (BlockListBuilder.() -> Unit)? = null,
    events: Map<EventKind, BlockListBuilder.() -> Unit> = emptyMap(),
    main: BlockListBuilder.() -> Unit,
): Program {
    val ids = IdGen()
    val functions = listOfNotNull(fnA, fnB).map { BlockListBuilder(ids).apply(it).build() }
    val handlers = events.map { (kind, body) -> EventHandler(kind, BlockListBuilder(ids).apply(body).build()) }
    return Program(BlockListBuilder(ids).apply(main).build(), functions, handlers)
}
