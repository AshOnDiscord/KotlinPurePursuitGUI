import java.awt.Graphics2D

class LineSegment(val p1: Point, val p2: Point) : Line(p1, p2) {
    override fun intersections(circle: Circle): List<Intersection> {
        val intersections = super.intersections(circle)
        return intersections.filter { boundingContains(it.point) }
    }

    fun draw(g: Graphics2D, ppi: Int) {
        val ppiP1 = p1.scale(ppi.toDouble())
        val ppiP2 = p2.scale(ppi.toDouble())
        g.drawLine(ppiP1.x.toInt(), ppiP1.y.toInt(), ppiP2.x.toInt(), ppiP2.y.toInt())
    }

    fun length(): Double {
        return p1.distanceTo(p2)
    }

    override fun translate(p: Point): LineSegment {
        return LineSegment(p1.add(p), p2.add(p))
    }

    fun contains(p: Point): Boolean {
        val d1 = p1.distanceTo(p)
        val d2 = p2.distanceTo(p)
        return d1 + d2 == length()
    }

    fun boundingContains(p: Point): Boolean {
        val xMin = p1.x.coerceAtMost(p2.x)
        val xMax = p1.x.coerceAtLeast(p2.x)
        val yMin = p1.y.coerceAtMost(p2.y)
        val yMax = p1.y.coerceAtLeast(p2.y)

        return p.x in xMin..xMax && p.y in yMin..yMax
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LineSegment) return false
        return p1 == other.p1 && p2 == other.p2
    }

    override fun toString(): String {
        return "($p1, $p2)"
    }

    override fun hashCode(): Int {
        var result = p1.hashCode()
        result = 31 * result + p2.hashCode()
        return result
    }
}