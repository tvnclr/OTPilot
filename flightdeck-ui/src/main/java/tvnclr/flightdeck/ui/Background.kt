package tvnclr.flightdeck.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private class Orbit(val x: Float, val y: Float, val r: Float, val ax: Float, val ay: Float, val kx: Int, val ky: Int, val phase: Float)

private val orbits =
    listOf(
        Orbit(0.12f, 0.10f, 0.42f, 0.06f, 0.04f, 1, 2, 0.0f),
        Orbit(0.92f, 0.28f, 0.34f, 0.05f, 0.06f, 2, 1, 0.3f),
        Orbit(0.20f, 0.62f, 0.28f, 0.07f, 0.05f, 1, 1, 0.6f),
        Orbit(0.85f, 0.86f, 0.46f, 0.04f, 0.05f, 1, 2, 0.15f),
    )

/** Page background: solid base with a few large circles drifting very slowly (~90 s loop). */
@Composable
fun FdBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
  val c = Fd.colors
  val t by
      rememberInfiniteTransition(label = "fd-bg")
          .animateFloat(
              initialValue = 0f,
              targetValue = 1f,
              animationSpec = infiniteRepeatable(tween(90_000, easing = LinearEasing), RepeatMode.Restart),
              label = "fd-bg-t",
          )
  Box(modifier.fillMaxSize().background(c.background)) {
    Canvas(Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val unit = minOf(w, h)
      for (o in orbits) {
        val a = 2f * PI.toFloat() * (t + o.phase)
        val cx = (o.x + o.ax * sin(a * o.kx)) * w
        val cy = (o.y + o.ay * cos(a * o.ky)) * h
        drawCircle(color = c.circles, radius = o.r * unit, center = Offset(cx, cy))
      }
    }
    content()
  }
}

