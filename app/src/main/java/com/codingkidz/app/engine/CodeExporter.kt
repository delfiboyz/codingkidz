package com.codingkidz.app.engine

enum class TokenKind { KEYWORD, FUNCTION, NUMBER, STRING, PLAIN, COMMENT }

data class Token(val text: String, val kind: TokenKind)

data class CodeLine(val indent: Int, val tokens: List<Token>, val blockId: Long? = null)

@kotlinx.serialization.Serializable
enum class CodeLanguage(val label: String) { PYTHON("Python"), JAVASCRIPT("JavaScript") }

/**
 * Menerjemahkan blok menjadi kode teks (Python atau JavaScript) dengan nama perintah berbahasa
 * Indonesia, sebagai jembatan dari pemrograman visual ke kode sungguhan.
 */
object CodeExporter {

    fun export(program: Program, language: CodeLanguage = CodeLanguage.PYTHON): List<CodeLine> =
        when (language) {
            CodeLanguage.PYTHON -> Python(program).build()
            CodeLanguage.JAVASCRIPT -> JavaScript(program).build()
        }

    fun asPlainText(program: Program, language: CodeLanguage = CodeLanguage.PYTHON): String =
        export(program, language).joinToString("\n") { line ->
            "    ".repeat(line.indent) + line.tokens.joinToString("") { it.text }
        }

    private fun kw(t: String) = Token(t, TokenKind.KEYWORD)
    private fun fn(t: String) = Token(t, TokenKind.FUNCTION)
    private fun num(t: Any) = Token("$t", TokenKind.NUMBER)
    private fun str(t: String) = Token("\"${t.replace("\"", "'")}\"", TokenKind.STRING)
    private fun plain(t: String) = Token(t, TokenKind.PLAIN)
    private fun comment(t: String) = Token(t, TokenKind.COMMENT)

    private abstract class Emitter(val program: Program) {
        val out = mutableListOf<CodeLine>()
        fun line(indent: Int, vararg tokens: Token, id: Long? = null) {
            out += CodeLine(indent, tokens.toList(), id)
        }
        abstract fun build(): List<CodeLine>
    }

    private class Python(program: Program) : Emitter(program) {
        private val fnNames = listOf("fungsi_a", "fungsi_b")

        override fun build(): List<CodeLine> {
            if (program.usesVariable) line(0, plain("angka = "), num(0))
            program.functions.forEachIndexed { i, body ->
                if (body.isEmpty()) return@forEachIndexed
                line(0, kw("def "), fn(fnNames[i]), plain("():"))
                list(body, 1)
                line(0)
            }
            program.events.filter { it.body.isNotEmpty() }.forEach { e ->
                line(0, kw("@ketika"), plain("("), str(e.kind.code), plain(")"), id = e.kind.hatId)
                line(0, kw("def "), fn("saat_${e.kind.code}"), plain("():"), id = e.kind.hatId)
                list(e.body, 1)
                line(0)
            }
            if (program.main.isEmpty()) line(0, comment("# susun blok untuk mulai"))
            else list(program.main, 0)
            return out
        }

        private fun list(blocks: List<Block>, indent: Int) {
            if (blocks.isEmpty()) line(indent, kw("pass")) else blocks.forEach { block(it, indent) }
        }

        private fun block(b: Block, i: Int) {
            when (b) {
                is MoveBlock -> line(i, fn("maju"), plain("()"), id = b.id)
                is TurnLeftBlock -> line(i, fn("belok_kiri"), plain("()"), id = b.id)
                is TurnRightBlock -> line(i, fn("belok_kanan"), plain("()"), id = b.id)
                is PickBlock -> line(i, fn("ambil"), plain("()"), id = b.id)
                is PaintBlock -> line(i, fn("cat"), plain("("), str(b.color.codeName), plain(")"), id = b.id)
                is CallBlock -> line(i, fn(fnNames.getOrElse(b.fn) { "fungsi" }), plain("()"), id = b.id)
                is SetVarBlock -> line(i, plain("angka = "), num(b.value), id = b.id)
                is ChangeVarBlock -> line(i, plain(if (b.delta >= 0) "angka += " else "angka -= "), num(kotlin.math.abs(b.delta)), id = b.id)
                is MoveVarBlock -> line(i, fn("maju"), plain("(angka)"), id = b.id)
                is SayBlock -> line(i, fn("katakan"), plain("("), str(b.text), plain(")"), id = b.id)
                is WaitBlock -> line(i, fn("tunggu"), plain("("), num(1), plain(")"), id = b.id)
                is RepeatBlock -> {
                    line(i, kw("for "), plain("i "), kw("in "), fn("range"), plain("("), num(b.times), plain("):"), id = b.id)
                    list(b.body, i + 1)
                }
                is RepeatUntilBlock -> {
                    line(i, kw("while not "), fn("di_finis"), plain("():"), id = b.id)
                    list(b.body, i + 1)
                }
                is IfBlock -> {
                    line(i, kw("if "), fn(b.condition.py), plain("():"), id = b.id)
                    list(b.then, i + 1)
                    b.otherwise?.let {
                        line(i, kw("else"), plain(":"), id = b.id)
                        list(it, i + 1)
                    }
                }
            }
        }
    }

    private class JavaScript(program: Program) : Emitter(program) {
        private val fnNames = listOf("fungsiA", "fungsiB")

        override fun build(): List<CodeLine> {
            if (program.usesVariable) line(0, kw("let "), plain("angka = "), num(0), plain(";"))
            program.functions.forEachIndexed { i, body ->
                if (body.isEmpty()) return@forEachIndexed
                line(0, kw("function "), fn(fnNames[i]), plain("() {"))
                list(body, 1)
                line(0, plain("}"))
                line(0)
            }
            program.events.filter { it.body.isNotEmpty() }.forEach { e ->
                line(0, fn("ketika"), plain("("), str(e.kind.jsCode), plain(", () => {"), id = e.kind.hatId)
                list(e.body, 1)
                line(0, plain("});"))
                line(0)
            }
            if (program.main.isEmpty()) line(0, comment("// susun blok untuk mulai"))
            else list(program.main, 0)
            return out
        }

        private fun list(blocks: List<Block>, indent: Int) = blocks.forEach { block(it, indent) }

        private fun call(i: Int, name: String, id: Long, vararg args: Token) {
            line(i, fn(name), plain("("), *args, plain(");"), id = id)
        }

        private fun block(b: Block, i: Int) {
            when (b) {
                is MoveBlock -> call(i, "maju", b.id)
                is TurnLeftBlock -> call(i, "belokKiri", b.id)
                is TurnRightBlock -> call(i, "belokKanan", b.id)
                is PickBlock -> call(i, "ambil", b.id)
                is PaintBlock -> call(i, "cat", b.id, str(b.color.codeName))
                is CallBlock -> call(i, fnNames.getOrElse(b.fn) { "fungsi" }, b.id)
                is SetVarBlock -> line(i, plain("angka = "), num(b.value), plain(";"), id = b.id)
                is ChangeVarBlock -> line(i, plain(if (b.delta >= 0) "angka += " else "angka -= "), num(kotlin.math.abs(b.delta)), plain(";"), id = b.id)
                is MoveVarBlock -> line(i, fn("maju"), plain("(angka);"), id = b.id)
                is SayBlock -> call(i, "katakan", b.id, str(b.text))
                is WaitBlock -> call(i, "tunggu", b.id, num(1))
                is RepeatBlock -> {
                    line(i, kw("for "), plain("("), kw("let "), plain("i = 0; i < "), num(b.times), plain("; i++) {"), id = b.id)
                    list(b.body, i + 1)
                    line(i, plain("}"))
                }
                is RepeatUntilBlock -> {
                    line(i, kw("while "), plain("(!"), fn("diFinis"), plain("()) {"), id = b.id)
                    list(b.body, i + 1)
                    line(i, plain("}"))
                }
                is IfBlock -> {
                    line(i, kw("if "), plain("("), fn(b.condition.js), plain("()) {"), id = b.id)
                    list(b.then, i + 1)
                    b.otherwise?.let {
                        line(i, plain("} "), kw("else"), plain(" {"), id = b.id)
                        list(it, i + 1)
                    }
                    line(i, plain("}"))
                }
            }
        }
    }
}
