package app.maximus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.maximus.ui.theme.Palette

/**
 * The MAXIMUS mascot: a knight in German gothic plate (fluted breastplate, pointed sabatons)
 * under a great helm (Kübelhelm) crested with buffalo horns, resting both gauntlets on a longsword.
 * Vector art on a 160 × 374 unit canvas (x ∈ [40, 200], y ∈ [−24, 350]), mirror-symmetric about x = 120.
 * Every plate gets its own diagonal steel gradient, which reads as polished metal on the dark ground.
 */
object KnightArt {
    enum class Role { STEEL, DARK, EDGE, BLADE, BLACK }
    class Part(val role: Role, val path: String)

    const val MIN_X = 40f
    const val MIN_Y = -24f
    const val WIDTH = 160f
    const val HEIGHT = 374f

    val parts = listOf(
        Part(Role.STEEL, "M97.0,236.0 L117.0,236.0 L117.0,280.0 L99.0,280.0 Z"),
        Part(Role.STEEL, "M143.0,236.0 L123.0,236.0 L123.0,280.0 L141.0,280.0 Z"),
        Part(Role.STEEL, "M95.0,279.0 L118.0,279.0 L119.0,292.0 L108.0,298.0 L96.0,292.0 Z"),
        Part(Role.STEEL, "M145.0,279.0 L122.0,279.0 L121.0,292.0 L132.0,298.0 L144.0,292.0 Z"),
        Part(Role.DARK, "M95.0,285.0 L90.0,281.0 L89.0,292.0 L96.0,292.0 Z"),
        Part(Role.DARK, "M145.0,285.0 L150.0,281.0 L151.0,292.0 L144.0,292.0 Z"),
        Part(Role.STEEL, "M99.0,297.0 L117.0,297.0 L116.0,332.0 L101.0,332.0 Z"),
        Part(Role.STEEL, "M141.0,297.0 L123.0,297.0 L124.0,332.0 L139.0,332.0 Z"),
        Part(Role.STEEL, "M99.0,331.0 L117.0,331.0 L118.0,340.0 L84.0,346.0 L98.0,338.0 Z"),
        Part(Role.STEEL, "M141.0,331.0 L123.0,331.0 L122.0,340.0 L156.0,346.0 L142.0,338.0 Z"),
        Part(Role.STEEL, "M90.0,186.0 L150.0,186.0 L151.0,197.0 L89.0,197.0 Z"),
        Part(Role.STEEL, "M88.0,196.0 L152.0,196.0 L153.0,207.0 L87.0,207.0 Z"),
        Part(Role.STEEL, "M86.0,206.0 L154.0,206.0 L155.0,217.0 L85.0,217.0 Z"),
        Part(Role.STEEL, "M88.0,216.0 L117.0,216.0 L117.0,240.0 L93.0,244.0 Z"),
        Part(Role.STEEL, "M152.0,216.0 L123.0,216.0 L123.0,240.0 L147.0,244.0 Z"),
        Part(Role.EDGE, "M90.0,226.0 L117.0,226.0"),
        Part(Role.EDGE, "M150.0,226.0 L123.0,226.0"),
        Part(Role.STEEL, "M90.0,108.0 L150.0,108.0 L147.0,150.0 L140.0,188.0 L100.0,188.0 L93.0,150.0 Z"),
        Part(Role.EDGE, "M120.0,110.0 L120.0,186.0"),
        Part(Role.EDGE, "M110.0,114.0 L112.0,150.0 L115.0,184.0"),
        Part(Role.EDGE, "M130.0,114.0 L128.0,150.0 L125.0,184.0"),
        Part(Role.EDGE, "M100.0,114.0 L104.0,150.0 L110.0,184.0"),
        Part(Role.EDGE, "M140.0,114.0 L136.0,150.0 L130.0,184.0"),
        Part(Role.EDGE, "M100.0,176.0 L140.0,176.0"),
        Part(Role.DARK, "M99.0,184.0 L141.0,184.0 L142.0,190.0 L98.0,190.0 Z"),
        Part(Role.STEEL, "M116.0,183.0 L124.0,183.0 L124.0,191.0 L116.0,191.0 Z"),
        Part(Role.STEEL, "M102.0,98.0 L138.0,98.0 L146.0,110.0 L94.0,110.0 Z"),
        Part(Role.STEEL, "M64.0,140.0 L84.0,140.0 L86.0,178.0 L70.0,180.0 Z"),
        Part(Role.STEEL, "M176.0,140.0 L156.0,140.0 L154.0,178.0 L170.0,180.0 Z"),
        Part(Role.STEEL, "M66.0,176.0 L88.0,174.0 L92.0,188.0 L78.0,196.0 L64.0,190.0 Z"),
        Part(Role.STEEL, "M174.0,176.0 L152.0,174.0 L148.0,188.0 L162.0,196.0 L176.0,190.0 Z"),
        Part(Role.DARK, "M64.0,182.0 L56.0,178.0 L58.0,192.0 L66.0,190.0 Z"),
        Part(Role.DARK, "M176.0,182.0 L184.0,178.0 L182.0,192.0 L174.0,190.0 Z"),
        Part(Role.STEEL, "M72.0,190.0 L90.0,186.0 L110.0,200.0 L104.0,212.0 Z"),
        Part(Role.STEEL, "M168.0,190.0 L150.0,186.0 L130.0,200.0 L136.0,212.0 Z"),
        Part(Role.STEEL, "M92.0,104.0 L100.0,112.0 L96.0,132.0 L84.0,148.0 L62.0,150.0 L54.0,136.0 L58.0,118.0 L72.0,106.0 Z"),
        Part(Role.STEEL, "M148.0,104.0 L140.0,112.0 L144.0,132.0 L156.0,148.0 L178.0,150.0 L186.0,136.0 L182.0,118.0 L168.0,106.0 Z"),
        Part(Role.EDGE, "M58.0,134.0 L70.0,138.0 L92.0,132.0"),
        Part(Role.EDGE, "M182.0,134.0 L170.0,138.0 L148.0,132.0"),
        Part(Role.EDGE, "M62.0,146.0 L74.0,148.0 L90.0,142.0"),
        Part(Role.EDGE, "M178.0,146.0 L166.0,148.0 L150.0,142.0"),
        Part(Role.BLADE, "M115.0,226.0 L125.0,226.0 L124.0,318.0 L120.0,334.0 L116.0,318.0 Z"),
        Part(Role.EDGE, "M120.0,228.0 L120.0,322.0"),
        Part(Role.STEEL, "M84.0,221.0 L156.0,221.0 L156.0,229.0 L84.0,229.0 Z"),
        Part(Role.STEEL, "M84.0,219.0 L80.0,217.0 L78.0,233.0 L84.0,231.0 Z"),
        Part(Role.STEEL, "M156.0,219.0 L160.0,217.0 L162.0,233.0 L156.0,231.0 Z"),
        Part(Role.DARK, "M116.0,206.0 L124.0,206.0 L124.0,222.0 L116.0,222.0 Z"),
        Part(Role.STEEL, "M102.0,200.0 L119.0,202.0 L119.0,218.0 L104.0,220.0 L99.0,212.0 Z"),
        Part(Role.STEEL, "M138.0,200.0 L121.0,202.0 L121.0,218.0 L136.0,220.0 L141.0,212.0 Z"),
        Part(Role.EDGE, "M104.0,206.0 L118.0,207.0"),
        Part(Role.EDGE, "M136.0,206.0 L122.0,207.0"),
        Part(Role.EDGE, "M103.0,212.0 L118.0,213.0"),
        Part(Role.EDGE, "M137.0,212.0 L122.0,213.0"),
        Part(Role.STEEL, "M128.0,196.0 L127.4,199.1 L125.7,201.7 L123.1,203.4 L120.0,204.0 L116.9,203.4 L114.3,201.7 L112.6,199.1 L112.0,196.0 L112.6,192.9 L114.3,190.3 L116.9,188.6 L120.0,188.0 L123.1,188.6 L125.7,190.3 L127.4,192.9 Z"),
        Part(Role.STEEL, "M101.0,27.0 L90.0,22.0 L80.0,15.0 L72.0,5.0 L68.0,-7.0 L70.0,-20.0 L76.0,-8.0 L82.0,1.0 L92.0,8.0 L104.0,13.0 L112.0,21.0 Z"),
        Part(Role.STEEL, "M139.0,27.0 L150.0,22.0 L160.0,15.0 L168.0,5.0 L172.0,-7.0 L170.0,-20.0 L164.0,-8.0 L158.0,1.0 L148.0,8.0 L136.0,13.0 L128.0,21.0 Z"),
        Part(Role.STEEL, "M90.0,40.0 L95.0,26.0 L108.0,20.0 L132.0,20.0 L145.0,26.0 L150.0,40.0 L150.0,94.0 L143.0,102.0 L97.0,102.0 L90.0,94.0 Z"),
        Part(Role.DARK, "M114.0,21.0 L126.0,21.0 L126.0,101.0 L114.0,101.0 Z"),
        Part(Role.STEEL, "M116.0,22.0 L124.0,22.0 L124.0,100.0 L116.0,100.0 Z"),
        Part(Role.STEEL, "M90.0,46.0 L150.0,46.0 L150.0,52.0 L90.0,52.0 Z"),
        Part(Role.BLACK, "M95.0,55.0 L113.0,55.0 L113.0,61.0 L95.0,61.0 Z"),
        Part(Role.BLACK, "M145.0,55.0 L127.0,55.0 L127.0,61.0 L145.0,61.0 Z"),
        Part(Role.BLACK, "M95.0,64.0 L113.0,64.0 L113.0,68.0 L95.0,68.0 Z"),
        Part(Role.BLACK, "M145.0,64.0 L127.0,64.0 L127.0,68.0 L145.0,68.0 Z"),
        Part(Role.BLACK, "M98.5,76.5 L101.5,76.5 L101.5,79.5 L98.5,79.5 Z"),
        Part(Role.BLACK, "M141.5,76.5 L138.5,76.5 L138.5,79.5 L141.5,79.5 Z"),
        Part(Role.BLACK, "M104.5,76.5 L107.5,76.5 L107.5,79.5 L104.5,79.5 Z"),
        Part(Role.BLACK, "M135.5,76.5 L132.5,76.5 L132.5,79.5 L135.5,79.5 Z"),
        Part(Role.BLACK, "M98.5,84.5 L101.5,84.5 L101.5,87.5 L98.5,87.5 Z"),
        Part(Role.BLACK, "M141.5,84.5 L138.5,84.5 L138.5,87.5 L141.5,87.5 Z"),
        Part(Role.BLACK, "M104.5,84.5 L107.5,84.5 L107.5,87.5 L104.5,87.5 Z"),
        Part(Role.BLACK, "M135.5,84.5 L132.5,84.5 L132.5,87.5 L135.5,87.5 Z"),
        Part(Role.EDGE, "M95.0,26.0 L145.0,26.0")
    )
}

@Composable
fun KnightMascot(modifier: Modifier = Modifier, description: String? = null) {
    val paths = remember { KnightArt.parts.map { it.role to PathParser().parsePathString(it.path).toPath() } }
    val outline = Color(0xFF08090A)
    val recess = Color(0xFF363B41)
    val a11y = if (description != null) Modifier.semantics { contentDescription = description } else Modifier
    Canvas(modifier = modifier.aspectRatio(KnightArt.WIDTH / KnightArt.HEIGHT).then(a11y)) {
        val s = size.width / KnightArt.WIDTH
        withTransform({
            scale(s, s, pivot = Offset.Zero)
            translate(-KnightArt.MIN_X, -KnightArt.MIN_Y)
        }) {
            val line = Stroke(width = 1.4f, join = StrokeJoin.Round, cap = StrokeCap.Round)
            for ((role, path) in paths) {
                val b = path.getBounds()
                when (role) {
                    KnightArt.Role.STEEL -> {
                        drawPath(path, Brush.linearGradient(listOf(Palette.SteelLight, Color(0xFF9AA3AC), Color(0xFF4E555D)), b.topLeft, b.bottomRight))
                        drawPath(path, outline, style = line)
                    }
                    KnightArt.Role.BLADE -> {
                        drawPath(path, Brush.horizontalGradient(listOf(Color(0xFFF2F5F7), Color(0xFFB6BEC6), Palette.SteelDeep), b.left, b.right))
                        drawPath(path, outline, style = line)
                    }
                    KnightArt.Role.DARK -> { drawPath(path, recess); drawPath(path, outline, style = line) }
                    KnightArt.Role.BLACK -> drawPath(path, Color(0xFF050506))
                    KnightArt.Role.EDGE -> drawPath(path, Color(0xFF2A2E33), style = Stroke(width = 1.2f, cap = StrokeCap.Round))
                }
            }
        }
    }
}
