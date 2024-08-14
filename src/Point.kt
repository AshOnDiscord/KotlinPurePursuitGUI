import java.awt.Graphics2D
import kotlin.math.atan2
import kotlin.math.sqrt

class Point(val x: Double, val y: Double) {
    fun draw(g: Graphics2D, ppi: Int, size: Double) {
        val ppiPoint = scale(ppi.toDouble())
        val x = ppiPoint.x
        val y = ppiPoint.y
        g.fillOval((x - size / 2).toInt(), (y - size / 2).toInt(), size.toInt(), size.toInt())
    }

    fun add(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    fun subtract(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    fun multiply(other: Point): Point {
        return Point(x * other.x, y * other.y)
    }

    fun divide(other: Point): Point {
        return Point(x / other.x, y / other.y)
    }

    fun scale(factor: Double): Point {
        return Point(x * factor, y * factor)
    }

    fun distanceTo(other: Point): Double {
        val xDiff = x - other.x
        val yDiff = y - other.y
        return sqrt(xDiff * xDiff + yDiff * yDiff)
    }

    fun angleTo(other: Point): Double {
        return atan2(other.y - y, other.x - x)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Point) return false
        return x == other.x && y == other.y
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}