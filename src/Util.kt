import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Util {
    companion object {
        fun normalizeAngle(angle: Double): Double {
            return atan2(sin(angle), cos(angle))
        }
    }
}