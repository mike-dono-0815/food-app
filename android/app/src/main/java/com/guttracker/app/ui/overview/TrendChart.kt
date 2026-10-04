package com.guttracker.app.ui.overview

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guttracker.app.ui.theme.AppColors
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TrendChart(
    points: List<DayPoint>,
    valueOf: (DayPoint) -> Double?,
    minVal: Double,
    maxVal: Double,
    colorScale: List<Color>,
    gridValues: List<Int>,
) {
    val muted = AppColors.TextMuted.toArgb()
    val lineColor = AppColors.TextSecondary
    val gridColor = AppColors.Gridline
    val vacationColor = AppColors.VacationBand

    val textSizePx = with(LocalDensity.current) { 10.sp.toPx() }
    val axisPaint = remember(textSizePx) {
        Paint().apply { color = muted; textSize = textSizePx; isAntiAlias = true; textAlign = Paint.Align.RIGHT }
    }
    val dateLabelPaint = remember(textSizePx) {
        Paint().apply { color = muted; textSize = textSizePx; isAntiAlias = true }
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val leftPad = 34.dp.toPx()
        val bottomPad = 26.dp.toPx()
        val plotW = size.width - leftPad
        val plotH = size.height - bottomPad
        val n = points.size
        if (n < 2) return@Canvas

        fun x(i: Int) = leftPad + plotW * i / (n - 1)
        fun y(v: Double) = plotH - plotH * (v - minVal).toFloat() / (maxVal - minVal).toFloat()

        // vacation shading — contiguous runs
        var runStart = -1
        for (i in 0..n) {
            val isVac = i < n && points[i].isVacation
            if (isVac && runStart == -1) runStart = i
            if (!isVac && runStart != -1) {
                drawRect(
                    color = vacationColor,
                    topLeft = Offset(x(runStart) - (plotW / (n - 1) / 2), 0f),
                    size = androidx.compose.ui.geometry.Size((x(i - 1) - x(runStart)) + plotW / (n - 1), plotH),
                )
                runStart = -1
            }
        }

        // gridlines + axis labels
        gridValues.forEach { v ->
            val yy = y(v.toDouble())
            drawLine(gridColor, Offset(leftPad, yy), Offset(size.width, yy), strokeWidth = 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(v.toString(), leftPad - 8.dp.toPx(), yy + 8f, axisPaint)
        }

        // area + line, broken at null gaps
        var segment = mutableListOf<Pair<Int, Double>>() // index, value
        fun flushSegment() {
            if (segment.size >= 2) {
                val path = androidx.compose.ui.graphics.Path()
                segment.forEachIndexed { idx, (i, v) ->
                    val px = x(i); val py = y(v)
                    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                val areaPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(x(segment.last().first), plotH)
                    lineTo(x(segment.first().first), plotH)
                    close()
                }
                drawPath(areaPath, color = Color(0x1A9A978C))
                drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
            segment = mutableListOf()
        }
        points.forEachIndexed { i, p ->
            val v = valueOf(p)
            if (v != null) segment.add(i to v) else flushSegment()
        }
        flushSegment()

        // dots
        points.forEachIndexed { i, p ->
            val v = valueOf(p) ?: return@forEachIndexed
            val colorIndex = (v - minVal).roundToInt().coerceIn(0, colorScale.size - 1)
            drawCircle(colorScale[colorIndex], radius = 3.dp.toPx(), center = Offset(x(i), y(v)))
        }

        // date labels: first / last only — a middle label collided with these on narrow screens
        val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        drawContext.canvas.nativeCanvas.drawText(points.first().date.format(fmt), leftPad, size.height, dateLabelPaint)
        val endPaint = Paint(dateLabelPaint).apply { textAlign = Paint.Align.RIGHT }
        drawContext.canvas.nativeCanvas.drawText(points.last().date.format(fmt), size.width, size.height, endPaint)
    }
}
