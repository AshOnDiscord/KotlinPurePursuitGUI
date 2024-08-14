import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.SwingUtilities
import kotlin.math.abs

fun main(args: Array<String>) {
    val panel = Panel(5)

    val frame = JFrame("Hello, Kotlin/Swing")

    frame.contentPane.add(panel, BorderLayout.CENTER)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.size = Dimension(144 * 5, 144*5)
    frame.setLocationRelativeTo(null)
    frame.isVisible = true

}

class Panel(val ppi: Int) : JPanel() {
    var robot = Robot(14.0)
    var lastIntersection: Intersection? = null

    fun updatePos(e: MouseEvent?) {
        val x = e?.point?.x ?: 0
        val y = e?.point?.y ?: 0

        val x1 = x.toFloat() / ppi - 24 * 3 // grid is 6x6 tiles so this centers,
        val y1 = y.toFloat() / ppi - 24 * 3

//        println("($x1, $y1)")

        robot.position = Point(x1.toDouble(), y1.toDouble())

        repaint()
    }

    fun updateHeading(e: MouseEvent?) {
        val x = e?.point?.x ?: 0
        val y = e?.point?.y ?: 0

        val x1 = x.toFloat() / ppi - 24 * 3 // grid is 6x6 tiles so this centers,
        val y1 = y.toFloat() / ppi - 24 * 3

        val dx = x1 - robot.position.x
        val dy = y1 - robot.position.y

        robot.heading = Point(0.0, 0.0).angleTo(Point(dx, dy))

//        println(Math.toDegrees(robot.heading))

        repaint()
    }

    init {
        setBackground(Color.WHITE);

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                if (e?.button == MouseEvent.BUTTON1) {
                    updatePos(e)
                } else {
                    updateHeading(e)
                }
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                val isLeft = SwingUtilities.isLeftMouseButton(e)
                if (isLeft) {
                    updatePos(e)
                } else {
                    updateHeading(e)
                }
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.stroke = java.awt.BasicStroke(5 / ppi.toFloat())
        g2d.color = Color.GRAY

//        g2d.scale(ppi.toDouble(), ppi.toDouble())

        // draw grid
        for (i in 0..width step 24 * ppi) {
            g2d.drawLine(i, 0, i, height)
        }
        for (i in 0..height step 24 * ppi) {
            g2d.drawLine(0, i, width, i)
        }

        g2d.translate(24 * 3.0 * ppi, 24 * 3.0 * ppi)

        val path: List<Point> = listOf(
            Point(0.0, 0.0),
            Point(48.0, 0.0),
            Point(48.0, 48.0),
            Point(0.0, 48.0),
        )

        val segments = path.zipWithNext().map { LineSegment(it.first, it.second) }
        g2d.color = Color.RED
        segments.forEach { it.draw(g2d, ppi) }

        val lastSegmentIndexT = segments.indexOfFirst { lastIntersection?.line == it }
        val lastSegmentIndex = if (lastSegmentIndexT == -1) 0 else lastSegmentIndexT

        // grab lastsegment to end
        val slicedSegments = segments.slice(lastSegmentIndex..<segments.size)

        println("Sliced segments ($lastSegmentIndexT): $slicedSegments")

        val intersections = slicedSegments.flatMap { it.intersections(robot.getLookaheadCircle()) }.sortedWith(Comparator { i1, i2 ->
            val a1 = robot.position.angleTo(i1.point)
            val a2 = robot.position.angleTo(i2.point)
            val d1 = abs(Util.normalizeAngle(a1 - robot.heading))
            val d2 = abs(Util.normalizeAngle(a2 - robot.heading))
            if (d1 < d2) -1 else 1
        })
        g2d.color = Color.GREEN
        intersections.forEach { it.point.draw(g2d, ppi, 40.0 / ppi) }
//        println(intersections)

        if (intersections.size > 0) {
            lastIntersection = intersections[0]
        }

        g2d.color = Color(0, 0, 255, 128);
        g2d.stroke = java.awt.BasicStroke(10 / ppi.toFloat())
        robot.draw(g2d, ppi)
    }
}