package com.codingkidz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.codingkidz.app.content.Studio
import com.codingkidz.app.engine.Interpreter
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.Tile
import com.codingkidz.app.ui.theme.LocalGameColors

/** Pratinjau kecil sebuah karya Studio: peta buatan anak plus hasil lukisannya. */
@Composable
fun CanvasThumbnail(program: Program, map: List<String>?, modifier: Modifier = Modifier) {
    val board = remember(map) { Studio.board(map) }
    val finalState = remember(program, board) {
        val interpreter = Interpreter(board, freePlay = true)
        interpreter.run(program).finalState ?: interpreter.initialState()
    }
    val colors = LocalGameColors.current
    Canvas(modifier.aspectRatio(board.width.toFloat() / board.height)) {
        val tile = size.width / board.width
        val gap = tile * 0.06f
        drawRoundRect(colors.boardBackground, cornerRadius = CornerRadius(tile * 0.4f))
        for (y in 0 until board.height) for (x in 0 until board.width) {
            val p = Pos(x, y)
            val color = finalState.painted[p]?.let { Color(it.argb) } ?: when (board.tile(p)) {
                Tile.WALL -> colors.wall
                Tile.BLUE -> colors.blueTile
                Tile.ORANGE -> colors.orangeTile
                Tile.FLOOR -> if ((x + y) % 2 == 0) colors.floorA else colors.floorB
            }
            val topLeft = Offset(x * tile + gap, y * tile + gap)
            drawRoundRect(color, topLeft, Size(tile - gap * 2, tile - gap * 2), CornerRadius(tile * 0.18f))
            if (p in finalState.gemsLeft) drawCircle(colors.gem, radius = tile * 0.22f, center = topLeft + Offset(tile / 2 - gap, tile / 2 - gap))
            if (p == board.goal) drawCircle(colors.flag, radius = tile * 0.2f, center = topLeft + Offset(tile / 2 - gap, tile / 2 - gap))
        }
    }
}
