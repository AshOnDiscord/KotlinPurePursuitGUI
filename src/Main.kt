import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import kotlin.math.abs

fun main() {
    val ppi = 7.0
    val pp = PurePursuit(ppi, 0.0)

    val frame = JFrame("Pure Pursuit")
    frame.size = Dimension((144 * ppi).toInt(), (144 * ppi).toInt())
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.contentPane.add(pp.panel)
    frame.isVisible = true

    pp.start()
}

class PurePursuit(ppi: Double, updateHertz: Double = -1.0) : OpMode(ppi, updateHertz) {
    val path: List<Point> = listOf(
        Point(0.0, 0.0),
        Point(48.0, 0.0),
        Point(48.0, -48.0),
        Point(0.0, -48.0)
    )
    val pathSegments: List<LineSegment> = path.zipWithNext().map { LineSegment(it.first, it.second) }

    var lastIntersection: Intersection = Intersection(path[0], pathSegments[0]) // start of the path
    var lastSegment: Int = 0 // prevent backtracking

    override fun init() {
        println("Initializing Pure Pursuit")
    }

    override fun loop(): Boolean {
        for (point in path) {
            draw(point, DrawData(Color.GRAY, 10.0))
        }
        for (segment in pathSegments) {
            draw(segment, DrawData(Color.GRAY, 2.0))
        }

        val distanceToFinal = robot.position.distanceTo(path.last())
        if (distanceToFinal < robot.lookahead) {
            if (distanceToFinal < 1.0) {
                println("Reached the end of the path ${path.last()} with distance $distanceToFinal (ending at ${robot.position})")
                return false
            }
            lastIntersection = Intersection(path.last(), pathSegments.last())
            draw(lastIntersection.point, DrawData(Color.CYAN, 10.0))
            driveTo(path.last())
            return true;
        }

        val remainingSegments = pathSegments.subList(lastSegment, pathSegments.size)
        val intersections = remainingSegments.flatMap { it.intersections(robot.lookaheadCircle) }
        // closest by angle from current heading
        val closestIntersection = intersections.minByOrNull { abs(getAngleDiff(it.point)) }
        for (intersection in intersections) {
            val color = if (intersection == closestIntersection) Color.GREEN else Color.RED
            draw(intersection.point, DrawData(color, 10.0))
        }
        val targetIntersection = closestIntersection ?: lastIntersection
        lastSegment = pathSegments.indexOf(targetIntersection.line)
        lastIntersection = targetIntersection
        driveTo(targetIntersection.point)
        return true
    }

    private fun driveTo(point: Point) {
        val angleDiff = getAngleDiff(point)
        val forwardPower = robot.position.distanceTo(point) / robot.lookahead
        drive(
            forwardPower,
            0.0,
            angleDiff
        ) // robot never strafes in pp since pp is a differential drive algorithm
    }

    private fun getAngleDiff(point: Point): Double {
        val angle = Utils.normalizeAngle(robot.position.angleTo(point))
        return Utils.normalizeAngle(angle - robot.heading)
    }
}