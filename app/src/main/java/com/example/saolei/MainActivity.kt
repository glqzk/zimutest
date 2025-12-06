package com.example.saolei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GameScreen()
            }
        }
    }
}

data class Cell(
    val x: Int,
    val y: Int,
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var adjacent: Int = 0
)

class GameBoard(val width: Int, val height: Int, val mines: Int) {
    val cells: Array<Array<Cell>> = Array(height) { y -> Array(width) { x -> Cell(x, y) } }

    fun neighbors(x: Int, y: Int): List<Cell> {
        val res = mutableListOf<Cell>()
        for (dy in -1..1) for (dx in -1..1) {
            if (dx == 0 && dy == 0) continue
            val nx = x + dx
            val ny = y + dy
            if (nx in 0 until width && ny in 0 until height) res.add(cells[ny][nx])
        }
        return res
    }

    fun placeMines(seedX: Int, seedY: Int) {
        var placed = 0
        val rnd = Random(System.currentTimeMillis())
        while (placed < mines) {
            val x = rnd.nextInt(width)
            val y = rnd.nextInt(height)
            if ((x == seedX && y == seedY) || cells[y][x].isMine) continue
            cells[y][x].isMine = true
            placed++
        }
        for (y in 0 until height) for (x in 0 until width) {
            cells[y][x].adjacent = neighbors(x, y).count { it.isMine }
        }
    }

    fun floodReveal(x: Int, y: Int) {
        val stack = ArrayDeque<Cell>()
        val start = cells[y][x]
        if (start.isRevealed || start.isFlagged) return
        stack.add(start)
        while (stack.isNotEmpty()) {
            val c = stack.removeLast()
            if (c.isRevealed || c.isFlagged) continue
            c.isRevealed = true
            if (c.adjacent == 0 && !c.isMine) {
                neighbors(c.x, c.y).forEach { n ->
                    if (!n.isRevealed && !n.isFlagged) stack.add(n)
                }
            }
        }
    }
}

@Composable
fun GameScreen() {
    var board by remember { mutableStateOf(GameBoard(9, 9, 10)) }
    var started by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }

    fun reset(w: Int, h: Int, m: Int) {
        board = GameBoard(w, h, m)
        started = false
        gameOver = false
        win = false
    }

    fun checkWin() {
        var safe = 0
        for (y in 0 until board.height) for (x in 0 until board.width) {
            val c = board.cells[y][x]
            if (!c.isMine && c.isRevealed) safe++
        }
        win = safe == board.width * board.height - board.mines && !gameOver
    }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                gameOver -> "失败"
                win -> "胜利"
                else -> "进行中"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { reset(9, 9, 10) }) { Text("初级") }
            Button(onClick = { reset(16, 16, 40) }) { Text("中级") }
            Button(onClick = { reset(30, 16, 99) }) { Text("高级") }
        }
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(board.width),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            items(board.height * board.width) { idx ->
                val x = idx % board.width
                val y = idx / board.width
                val cell = board.cells[y][x]
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(2.dp)
                        .size(32.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .background(
                            when {
                                cell.isRevealed && cell.isMine -> Color.Red
                                cell.isRevealed -> Color(0xFFEEEEEE)
                                else -> Color(0xFFB0C4DE)
                            }, RoundedCornerShape(4.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { _ ->
                                    if (!gameOver && !win && !cell.isRevealed) {
                                        cell.isFlagged = !cell.isFlagged
                                    }
                                },
                                onTap = { _ ->
                                    if (gameOver || win) return@detectTapGestures
                                    if (!started) {
                                        board.placeMines(x, y)
                                        started = true
                                    }
                                    if (cell.isFlagged || cell.isRevealed) return@detectTapGestures
                                    if (cell.isMine) {
                                        cell.isRevealed = true
                                        gameOver = true
                                    } else {
                                        board.floodReveal(x, y)
                                        checkWin()
                                    }
                                }
                            )
                        }
                ) {
                    when {
                        cell.isFlagged && !cell.isRevealed -> Text("⚑", fontSize = 18.sp)
                        cell.isRevealed && cell.isMine -> Text("✹", fontSize = 18.sp)
                        cell.isRevealed && cell.adjacent > 0 -> Text(cell.adjacent.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { reset(board.width, board.height, board.mines) }) { Text("重开") }
    }
}

 
