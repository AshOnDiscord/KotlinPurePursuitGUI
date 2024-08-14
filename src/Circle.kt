import java.awt.Graphics2D

class Circle(val center: Point, val radius: Double) {
    fun draw(g: Graphics2D, ppi: Int) {
        val x = (center.x - radius) * ppi
        val y = (center.y - radius) * ppi
        val diameter = (2 * radius) * ppi
        g.drawOval(x.toInt(), y.toInt(), diameter.toInt(), diameter.toInt())
        center.draw(g, ppi,2.0)
    }

    fun contains(p: Point): Boolean {
        return center.distanceTo(p) <= radius
    }

    fun boundingContains(p: Point): Boolean {
        val xMin = center.x - radius
        val xMax = center.x + radius
        val yMin = center.y - radius
        val yMax = center.y + radius

        return p.x in xMin..xMax && p.y in yMin..yMax
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Circle) return false
        return center == other.center && radius == other.radius
    }

    override fun toString(): String {
        return "($center, r=$radius)"
    }

    override fun hashCode(): Int {
        var result = center.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }
}