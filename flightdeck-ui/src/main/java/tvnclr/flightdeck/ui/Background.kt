package tvnclr.flightdeck.ui

import android.animation.ValueAnimator
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Animated page backgrounds the user can pick in the app settings. */
enum class FdBackgroundStyle { ORBS, AURORA, HORIZON, RADAR, DEPTH, PLAIN }

val LocalFdBackgroundStyle = staticCompositionLocalOf { FdBackgroundStyle.ORBS }

private const val TWO_PI = (2 * PI).toFloat()

/**
 * Page background. [scroll] is the page scroll offset in px (used by DEPTH and HORIZON);
 * [hero] marks the main screen (RADAR only sweeps there). Motion stops when the system
 * "Remove animations" setting is on.
 */
@Composable
fun FdBackground(
    modifier: Modifier = Modifier,
    scroll: () -> Float = { 0f },
    hero: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
  val c = Fd.colors
  val style = LocalFdBackgroundStyle.current
  val animate = remember { ValueAnimator.areAnimatorsEnabled() }
  val moving = animate && style != FdBackgroundStyle.PLAIN && style != FdBackgroundStyle.DEPTH
  val period =
      when (style) {
        FdBackgroundStyle.RADAR -> 9_000
        FdBackgroundStyle.HORIZON -> 40_000
        FdBackgroundStyle.AURORA -> 70_000
        else -> 60_000
      }
  val t: State<Float> =
      if (moving) {
        rememberInfiniteTransition(label = "fd-bg")
            .animateFloat(0f, 1f, infiniteRepeatable(tween(period, easing = LinearEasing), RepeatMode.Restart), label = "fd-bg-t")
      } else {
        remember { mutableFloatStateOf(0.12f) }
      }

  Box(modifier.fillMaxSize().background(c.background)) {
    if (style != FdBackgroundStyle.PLAIN) {
      Canvas(Modifier.fillMaxSize()) {
        when (style) {
          FdBackgroundStyle.ORBS -> drawOrbs(t.value, c.circles)
          FdBackgroundStyle.AURORA -> drawAurora(t.value, c.accent, c.isDark)
          FdBackgroundStyle.HORIZON -> drawHorizon(t.value, scroll(), c)
          FdBackgroundStyle.RADAR -> drawRadar(t.value, hero && animate, c)
          FdBackgroundStyle.DEPTH -> drawDepth(scroll(), c.circles)
          FdBackgroundStyle.PLAIN -> Unit
        }
      }
    }
    content()
  }
}

// ── Orbs: large circles wandering across most of the screen ─────────────────

private class Orbit(val x: Float, val y: Float, val r: Float, val ax: Float, val ay: Float, val kx: Int, val ky: Int, val phase: Float)

private val orbits =
    listOf(
        Orbit(0.30f, 0.22f, 0.42f, 0.28f, 0.18f, 1, 2, 0.00f),
        Orbit(0.72f, 0.40f, 0.34f, 0.30f, 0.26f, 2, 1, 0.30f),
        Orbit(0.35f, 0.65f, 0.28f, 0.32f, 0.22f, 1, 1, 0.60f),
        Orbit(0.68f, 0.82f, 0.46f, 0.26f, 0.16f, 1, 2, 0.15f),
    )

/** Lissajous position; integer frequencies keep the loop seamless. */
private fun DrawScope.orbit(o: Orbit, t: Float): Offset {
  val a = TWO_PI * (t + o.phase)
  return Offset((o.x + o.ax * sin(a * o.kx)) * size.width, (o.y + o.ay * cos(a * o.ky)) * size.height)
}

private fun DrawScope.drawOrbs(t: Float, color: Color) {
  val unit = minOf(size.width, size.height)
  for (o in orbits) drawCircle(color, radius = o.r * unit, center = orbit(o, t))
}

// ── Aurora: soft accent-colored glows drifting and overlapping ───────────────

private fun hueShift(color: Color, degrees: Float): Color {
  val hsv = FloatArray(3)
  android.graphics.Color.colorToHSV(color.toArgb(), hsv)
  hsv[0] = (hsv[0] + degrees + 360f) % 360f
  return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun DrawScope.drawAurora(t: Float, accent: Color, dark: Boolean) {
  val unit = minOf(size.width, size.height)
  val alpha = if (dark) 0.22f else 0.16f
  val tints = listOf(accent, hueShift(accent, 45f), hueShift(accent, -45f))
  orbits.take(3).forEachIndexed { i, o ->
    val center = orbit(o, t)
    val radius = unit * (0.75f + 0.15f * sin(TWO_PI * (t * 2 + o.phase)))
    drawCircle(
        Brush.radialGradient(listOf(tints[i].copy(alpha = alpha), Color.Transparent), center = center, radius = radius),
        radius = radius,
        center = center,
    )
  }
}

// ── Horizon: an artificial horizon slowly rolling and pitching ──────────────

private fun DrawScope.drawHorizon(t: Float, scroll: Float, c: FdColors) {
  val w = size.width
  val h = size.height
  val roll = 7f * sin(TWO_PI * t)
  val pitch = h * 0.05f * sin(TWO_PI * t * 2 + 0.6f)
  val cx = w / 2
  val cy = h * 0.42f + pitch - scroll * 0.2f
  val step = h * 0.09f
  val stroke = 1.5f * density
  rotate(roll, pivot = Offset(cx, cy)) {
    // ground
    drawRect(c.circles, topLeft = Offset(-w, cy), size = Size(w * 3, h * 3))
    // horizon line
    drawLine(c.accent.copy(alpha = 0.35f), Offset(-w, cy), Offset(w * 2, cy), strokeWidth = 2f * density)
    // pitch ladder
    for (k in -6..6) {
      if (k == 0) continue
      val y = cy - k * step
      val half = if (k % 2 == 0) w * 0.16f else w * 0.08f
      drawLine(c.textSecondary.copy(alpha = 0.16f), Offset(cx - half, y), Offset(cx + half, y), strokeWidth = stroke)
    }
  }
  // fixed aircraft reference
  val wing = w * 0.07f
  val ref = c.accent.copy(alpha = 0.28f)
  val y = h * 0.42f - scroll * 0.2f
  drawLine(ref, Offset(cx - wing * 2, y), Offset(cx - wing * 0.6f, y), strokeWidth = 3f * density)
  drawLine(ref, Offset(cx + wing * 0.6f, y), Offset(cx + wing * 2, y), strokeWidth = 3f * density)
  drawCircle(ref, radius = 3f * density, center = Offset(cx, y))
}

// ── Radar: range rings, with a slow sweep on the main screen ────────────────

private fun DrawScope.drawRadar(t: Float, sweep: Boolean, c: FdColors) {
  val unit = minOf(size.width, size.height)
  val center = Offset(size.width * 0.82f, size.height * 0.16f)
  val ring = c.textSecondary.copy(alpha = 0.10f)
  for (i in 1..6) drawCircle(ring, radius = unit * 0.24f * i, center = center, style = Stroke(1.5f * density))
  drawLine(ring, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = density)
  drawLine(ring, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = density)
  if (sweep) {
    rotate(360f * t, pivot = center) {
      drawCircle(
          Brush.sweepGradient(
              0f to Color.Transparent,
              0.82f to Color.Transparent,
              1f to c.accent.copy(alpha = 0.22f),
              center = center,
          ),
          radius = unit * 1.6f,
          center = center,
      )
    }
  }
}

// ── Depth: still layers that move at different speeds while scrolling ───────

private class Dot(val x: Float, val y: Float, val r: Float)

private val layers =
    listOf(
        0.08f to listOf(Dot(0.15f, 0.20f, 0.30f), Dot(0.85f, 0.70f, 0.38f), Dot(0.40f, 1.30f, 0.34f)),
        0.22f to listOf(Dot(0.80f, 0.15f, 0.22f), Dot(0.10f, 0.85f, 0.26f), Dot(0.70f, 1.45f, 0.20f)),
        0.45f to listOf(Dot(0.55f, 0.45f, 0.14f), Dot(0.92f, 1.05f, 0.16f), Dot(0.05f, 1.60f, 0.12f)),
    )

private fun DrawScope.drawDepth(scroll: Float, color: Color) {
  val unit = minOf(size.width, size.height)
  val span = size.height * 2f
  layers.forEachIndexed { i, (speed, dots) ->
    val alpha = 0.55f + 0.225f * i
    translate(top = 0f) {
      for (d in dots) {
        // wrap so long pages always have shapes on screen
        var y = (d.y * size.height - scroll * speed) % span
        if (y < -unit) y += span
        drawCircle(color.copy(alpha = color.alpha * alpha), radius = d.r * unit, center = Offset(d.x * size.width, y))
      }
    }
  }
}
