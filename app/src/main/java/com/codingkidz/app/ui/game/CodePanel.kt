package com.codingkidz.app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.codingkidz.app.engine.CodeExporter
import com.codingkidz.app.engine.CodeLanguage
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.TokenKind
import com.codingkidz.app.ui.theme.CodeTextStyle
import com.codingkidz.app.ui.theme.LocalGameColors

/** Menampilkan program sebagai kode teks (Python/JavaScript), menyorot baris yang sedang berjalan. */
@Composable
fun CodePanel(program: Program, activeBlockId: Long?, language: CodeLanguage = CodeLanguage.PYTHON, modifier: Modifier = Modifier) {
    val colors = LocalGameColors.current
    val lines = remember(program, language) { CodeExporter.export(program, language) }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.codeBackground)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 10.dp),
    ) {
        Text(
            if (language == CodeLanguage.PYTHON) "# Kode Python dari blokmu" else "// Kode JavaScript dari blokmu",
            style = CodeTextStyle,
            color = colors.codeComment,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        lines.forEach { line ->
            val active = line.blockId != null && line.blockId == activeBlockId
            val text = buildAnnotatedString {
                append("    ".repeat(line.indent))
                line.tokens.forEach { token ->
                    val color = when (token.kind) {
                        TokenKind.KEYWORD -> colors.codeKeyword
                        TokenKind.FUNCTION -> colors.codeFunction
                        TokenKind.NUMBER -> colors.codeNumber
                        TokenKind.STRING -> colors.codeString
                        TokenKind.PLAIN -> colors.codePlain
                        TokenKind.COMMENT -> colors.codeComment
                    }
                    withStyle(SpanStyle(color = color, fontWeight = if (token.kind == TokenKind.KEYWORD) FontWeight.Bold else null)) {
                        append(token.text)
                    }
                }
            }
            Text(
                text,
                style = CodeTextStyle,
                softWrap = false,
                modifier = Modifier
                    .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                    .padding(horizontal = 14.dp),
            )
        }
    }
}
