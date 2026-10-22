package com.codingkidz.app.engine

/** Menunjuk satu daftar blok di dalam program. */
sealed interface ListRef {
    data object Main : ListRef
    data class Function(val index: Int) : ListRef
    data class Event(val kind: EventKind) : ListRef
    data class Body(val blockId: Long) : ListRef
    data class Then(val blockId: Long) : ListRef
    data class Else(val blockId: Long) : ListRef
}

/** Posisi sisip: blok baru akan masuk ke [ref] pada indeks [index]. */
data class Cursor(val ref: ListRef, val index: Int)

/** Operasi pohon program yang murni (tanpa efek samping) supaya mudah diuji dan di-undo. */
object ProgramEditor {

    fun listAt(program: Program, ref: ListRef): List<Block>? = when (ref) {
        ListRef.Main -> program.main
        is ListRef.Function -> program.function(ref.index)
        is ListRef.Event -> program.event(ref.kind)
        is ListRef.Body -> when (val b = find(program, ref.blockId)) {
            is RepeatBlock -> b.body
            is RepeatUntilBlock -> b.body
            else -> null
        }
        is ListRef.Then -> (find(program, ref.blockId) as? IfBlock)?.then
        is ListRef.Else -> (find(program, ref.blockId) as? IfBlock)?.otherwise
    }

    fun find(program: Program, id: Long): Block? = program.everyBlock().firstOrNull { it.id == id }

    /** Mencari daftar dan indeks tempat blok [id] berada. */
    fun locate(program: Program, id: Long): Cursor? {
        fun search(ref: ListRef, list: List<Block>): Cursor? {
            list.forEachIndexed { i, b ->
                if (b.id == id) return Cursor(ref, i)
                val found = when (b) {
                    is RepeatBlock -> search(ListRef.Body(b.id), b.body)
                    is RepeatUntilBlock -> search(ListRef.Body(b.id), b.body)
                    is IfBlock -> search(ListRef.Then(b.id), b.then)
                        ?: b.otherwise?.let { search(ListRef.Else(b.id), it) }
                    else -> null
                }
                if (found != null) return found
            }
            return null
        }
        search(ListRef.Main, program.main)?.let { return it }
        program.functions.forEachIndexed { i, f -> search(ListRef.Function(i), f)?.let { return it } }
        program.events.forEach { e -> search(ListRef.Event(e.kind), e.body)?.let { return it } }
        return null
    }

    /** Id semua blok kontainer di dalam (dan termasuk) blok [id] — tidak boleh menjadi tujuan pindah. */
    fun subtreeIds(program: Program, id: Long): Set<Long> {
        val root = find(program, id) ?: return emptySet()
        return (listOf(root) + root.children().flatMap { it.allBlocks() }).map { it.id }.toSet()
    }

    fun insert(program: Program, cursor: Cursor, block: Block): Program =
        modifyList(program, cursor.ref) { list ->
            val idx = cursor.index.coerceIn(0, list.size)
            list.toMutableList().apply { add(idx, block) }
        }

    fun remove(program: Program, id: Long): Program {
        val loc = locate(program, id) ?: return program
        return modifyList(program, loc.ref) { list -> list.filterNot { it.id == id } }
    }

    /**
     * Memindahkan blok (beserta isinya) ke [target]. Mengembalikan program semula bila
     * tujuannya berada di dalam blok itu sendiri.
     */
    fun move(program: Program, id: Long, target: Cursor): Program {
        val block = find(program, id) ?: return program
        val from = locate(program, id) ?: return program
        val ownerId = ownerOf(target.ref)
        if (ownerId != null && ownerId in subtreeIds(program, id)) return program
        val index = if (from.ref == target.ref && target.index > from.index) target.index - 1 else target.index
        val removed = remove(program, id)
        return insert(removed, Cursor(target.ref, index), block)
    }

    fun ownerOf(ref: ListRef): Long? = when (ref) {
        is ListRef.Body -> ref.blockId
        is ListRef.Then -> ref.blockId
        is ListRef.Else -> ref.blockId
        else -> null
    }

    fun replace(program: Program, id: Long, transform: (Block) -> Block): Program {
        fun mapList(list: List<Block>): List<Block> = list.map { b ->
            val updated = if (b.id == id) transform(b) else b
            mapChildren(updated, ::mapList)
        }
        return Program(
            main = mapList(program.main),
            functions = program.functions.map(::mapList),
            events = program.events.map { it.copy(body = mapList(it.body)) },
        )
    }

    /** Menggeser blok naik (-1) atau turun (+1) di dalam daftarnya. */
    fun shift(program: Program, id: Long, delta: Int): Program {
        val loc = locate(program, id) ?: return program
        return modifyList(program, loc.ref) { list ->
            val target = loc.index + delta
            if (target !in list.indices) list
            else list.toMutableList().apply { add(target, removeAt(loc.index)) }
        }
    }

    /** Apakah [ref] masih ada di program (misalnya setelah blok induknya dihapus). */
    fun exists(program: Program, ref: ListRef): Boolean = when (ref) {
        is ListRef.Event, is ListRef.Function, ListRef.Main -> true
        else -> listAt(program, ref) != null
    }

    private fun modifyList(program: Program, ref: ListRef, f: (List<Block>) -> List<Block>): Program =
        when (ref) {
            ListRef.Main -> program.copy(main = f(program.main))
            is ListRef.Function -> {
                val fns = program.functions.toMutableList()
                while (fns.size <= ref.index) fns += emptyList<Block>()
                fns[ref.index] = f(fns[ref.index])
                program.copy(functions = fns)
            }
            is ListRef.Event -> {
                val existing = program.events.firstOrNull { it.kind == ref.kind }
                val updated = EventHandler(ref.kind, f(existing?.body.orEmpty()))
                val events = if (existing == null) program.events + updated
                else program.events.map { if (it.kind == ref.kind) updated else it }
                program.copy(events = events.filter { it.body.isNotEmpty() })
            }
            is ListRef.Body, is ListRef.Then, is ListRef.Else -> {
                val ownerId = ownerOf(ref)!!
                replace(program, ownerId) { b ->
                    when {
                        ref is ListRef.Body && b is RepeatBlock -> b.copy(body = f(b.body))
                        ref is ListRef.Body && b is RepeatUntilBlock -> b.copy(body = f(b.body))
                        ref is ListRef.Then && b is IfBlock -> b.copy(then = f(b.then))
                        ref is ListRef.Else && b is IfBlock && b.otherwise != null -> b.copy(otherwise = f(b.otherwise))
                        else -> b
                    }
                }
            }
        }

    private fun mapChildren(b: Block, f: (List<Block>) -> List<Block>): Block = when (b) {
        is RepeatBlock -> b.copy(body = f(b.body))
        is RepeatUntilBlock -> b.copy(body = f(b.body))
        is IfBlock -> b.copy(then = f(b.then), otherwise = b.otherwise?.let(f))
        else -> b
    }
}
