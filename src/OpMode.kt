import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.round

interface IOpMode {
    val telemetry: ITelemetry

    fun init()
    fun loop(): Boolean // return false top stop, true or continue (can't get kotlin to do allow only `return;` without removing typing)
}

interface ITelemetry {
    fun addData(key: String, value: Any)
    fun clear()
    fun update()
}

class DrawData(val color: Color, val size: Double)

abstract class OpMode(ppi: Double, private val updateHertz: Double) : IOpMode {
    // Rendering
    inner class Panel(private val ppi: Double) : JPanel() {
        fun updatePos(e: MouseEvent) {
            val clickPoint = Point(e.x.toDouble(), e.y.toDouble())
            val mappedPoint = (clickPoint / ppi) + Point(-144.0 / 2, -144.0 / 2)
            robot.position = Point(mappedPoint.x, mappedPoint.y)
        }

        fun updateHeading(e: MouseEvent) {
            val clickPoint = Point(e.x.toDouble(), e.y.toDouble())
            val mappedPoint = (clickPoint / ppi) + Point(-144.0 / 2, -144.0 / 2)
            val heading = robot.position.angleTo(mappedPoint)
            robot.heading = heading
        }

        init {
            background = Color.WHITE

            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent?) {
                    if (e == null) return
                    if (e.button == MouseEvent.BUTTON1) updatePos(e) else updateHeading(e)
                }
            })

            addMouseMotionListener(object : MouseAdapter() {
                override fun mouseDragged(e: MouseEvent?) {
                    if (e == null) return
                    val isLeft = SwingUtilities.isLeftMouseButton(e)
                    if (isLeft) updatePos(e) else updateHeading(e)
                }
            })
        }

        var lastFrame = System.nanoTime()
        var deltaTime = 0.0

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D

            val now = System.nanoTime()
            val dt = now - lastFrame
            deltaTime = dt / 1e9;
            val fps = 1e9 / dt
            lastFrame = now

            renderGrid(g2d)
            g2d.translate(144 * ppi / 2, 144 * ppi / 2)

            // draw trail/path
            g2d.color = Color.BLUE
            renderTrail(g2d)

            val pointBufferCopy = pointBuffer.toList()
            for (point in pointBufferCopy) {
                render(g2d, point.first, point.second)
            }
            pointBuffer.clear()
            val lineBufferCopy = lineBuffer.toList()
            for (line in lineBufferCopy) {
                render(g2d, line.first, line.second)
            }
            lineBuffer.clear()
            val circleBufferCopy = circleBuffer.toList()
            for (circle in circleBufferCopy) {
                render(g2d, circle.first, circle.second)
            }
            circleBuffer.clear()
            render(g2d, robot)

            g2d.color = Color.BLACK
            val roundedFps = round(fps * 100) / 100.0
            g2d.drawString("FPS: $roundedFps", (-144 * ppi / 2 + 5).toFloat(), (-144 * ppi / 2 + 15).toFloat())
//            repaint()
        }

        private fun render(g2d: Graphics2D, point: Point, drawData: DrawData) {
            g2d.color = drawData.color
            val mappedPoint = point * ppi
            val size = drawData.size
            val origin = mappedPoint - Point(size, size) / 2.0
            g2d.fillOval(origin.x.toInt(), origin.y.toInt(), size.toInt(), size.toInt())
        }

        private fun render(g2d: Graphics2D, line: LineSegment, drawData: DrawData) {
            g2d.color = drawData.color
            g2d.stroke = BasicStroke(drawData.size.toFloat())
            val mappedP1 = line.p1 * ppi
            val mappedP2 = line.p2 * ppi
            g2d.drawLine(mappedP1.x.toInt(), mappedP1.y.toInt(), mappedP2.x.toInt(), mappedP2.y.toInt())
        }

        private fun render(g2d: Graphics2D, circle: Circle, drawData: DrawData) {
            g2d.color = drawData.color
            g2d.stroke = BasicStroke(drawData.size.toFloat())
            val mappedCenter = circle.center * ppi
            val mappedRadius = circle.radius * ppi
            val origin = mappedCenter - Point(mappedRadius, mappedRadius)
            g2d.drawOval(origin.x.toInt(), origin.y.toInt(), (mappedRadius * 2).toInt(), (mappedRadius * 2).toInt())
            render(g2d, mappedCenter / ppi, drawData) // I should write a non-scaled version or smt...
        }

        private fun render(g2d: Graphics2D, robot: Robot) {
            render(g2d, robot.position, DrawData(Color.BLUE, 10.0))
            render(g2d, robot.lookaheadCircle, DrawData(Color.BLUE, 2.0))
            val lookVector = Point(robot.lookahead, 0.0).rotate(robot.heading)
            val headingLine = LineSegment(robot.position, robot.position + lookVector)
            render(g2d, headingLine, DrawData(Color.BLUE, 2.0))
        }

        private fun renderGrid(g2d: Graphics2D) {
            val drawData = DrawData(Color.LIGHT_GRAY, 1.0)
            for (i in 0..144 step 24) {
                val line = LineSegment(Point(i.toDouble(), 0.0), Point(i.toDouble(), 144.0))
                render(g2d, line, drawData)
            }
            for (i in 0..144 step 24) {
                val line = LineSegment(Point(0.0, i.toDouble()), Point(144.0, i.toDouble()))
                render(g2d, line, drawData)
            }
        }

        private fun renderTrail(g2d: Graphics2D) {
            g2d.color = Color.ORANGE
            val robotBufferCopy = robotBuffer.toList()
            for (i in 1..<robotBufferCopy.size) {
                val p1 = robotBufferCopy[i - 1] * ppi
                val p2 = robotBufferCopy[i] * ppi
                g2d.drawLine(p1.x.toInt(), p1.y.toInt(), p2.x.toInt(), p2.y.toInt())
            }
        }
    }

    val panel = Panel(ppi)

    val pointBuffer: MutableList<Pair<Point, DrawData>> = mutableListOf()
    val lineBuffer: MutableList<Pair<LineSegment, DrawData>> = mutableListOf()
    val circleBuffer: MutableList<Pair<Circle, DrawData>> = mutableListOf()
    val robotBuffer: MutableList<Point> = mutableListOf()

    fun draw(point: Point, drawData: DrawData) {
        pointBuffer.add(Pair(point, drawData))
    }

    fun draw(line: LineSegment, drawData: DrawData) {
        lineBuffer.add(Pair(line, drawData))
    }

    fun draw(circle: Circle, drawData: DrawData) {
        circleBuffer.add(Pair(circle, drawData))
    }

    // FTC

    class Telemetry : ITelemetry {
        private val data = mutableMapOf<String, Any>()

        override fun addData(key: String, value: Any) {
            println("Telemetry.addData(key=$key, value=$value)")
            data[key] = value
        }

        override fun clear() {
            println("Telemetry.clear()")
            data.clear()
        }

        override fun update() {
            println("Telemetry.update()")
            // draw data
        }
    }

    override val telemetry = Telemetry()

    val robot: Robot = Robot(Point(16.0, 14.0), 14.0)

    fun drive(x: Double, y: Double, rx: Double) {
        robot.drive(x, y, rx)
    }

    fun start() {
        init()
        while (loop()) {
            update()
            if (updateHertz <= 0) {
                continue
            }
            val targetDelta = 1e9 / updateHertz
            val start = System.nanoTime()
            var current = System.nanoTime()
            while (current - start < targetDelta) {
                update()
                current = System.nanoTime()
            }
        }
    }

    private fun update() {
        robot.update(panel.deltaTime)
        robotBuffer.add(robot.position)
        SwingUtilities.invokeAndWait { panel.repaint() }
//        Thread.sleep(10)
    }
}