import java.awt.Graphics2D
import kotlin.math.cos
import kotlin.math.sin

class Robot(val lookahead: Double) {
    var position = Point(0.0, 0.0)
    var heading = 0.0
    val size = Point(16.0, 14.0);

    fun getLookaheadCircle(): Circle {
        return Circle(position, lookahead)
    }

    fun draw(g: Graphics2D, ppi: Int) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val topLeft =
            position.add(Point(cosH * -size.x / 2 - sinH * size.y / 2, sinH * -size.x / 2 + cosH * size.y / 2))
        val topRight = position.add(Point(cosH * size.x / 2 - sinH * size.y / 2, sinH * size.x / 2 + cosH * size.y / 2))
        val bottomLeft =
            position.add(Point(cosH * -size.x / 2 - sinH * -size.y / 2, sinH * -size.x / 2 + cosH * -size.y / 2))
        val bottomRight =
            position.add(Point(cosH * size.x / 2 - sinH * -size.y / 2, sinH * size.x / 2 + cosH * -size.y / 2))

        val xValues = doubleArrayOf(topLeft.x, topRight.x, bottomRight.x, bottomLeft.x)
        val yValues = doubleArrayOf(topLeft.y, topRight.y, bottomRight.y, bottomLeft.y)

        val xValuesInt = xValues.map { (it * ppi).toInt() }.toIntArray()
        val yValuesInt = yValues.map { (it * ppi).toInt() }.toIntArray()

        g.fillPolygon(xValuesInt, yValuesInt, 4)

        val lookaheadCircle = getLookaheadCircle()
        lookaheadCircle.draw(g, ppi)

        val aimVector = Point(cos(heading), sin(heading))
        val aimPoint = position.add(aimVector.scale(lookahead))
        val lineSegment = LineSegment(position, aimPoint)
        lineSegment.draw(g, ppi)
    }
}