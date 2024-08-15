package ftcDashboard

import java.awt.Graphics
import java.awt.GraphicsEnvironment
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JPanel

interface IFTCDashboard {
    fun sendTelemetryPacket(telemetryPacket: ITelemetryPacket)
}

class FTCDashboard(ppi: Double) : IFTCDashboard {
    inner class Panel(val ppi: Double) : JPanel() {
        init {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val fonts = ge.availableFontFamilyNames
            for (i in fonts) {
                println("$i ")
            }

            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {

                    if (e.button != MouseEvent.BUTTON1) {
                        return super.mousePressed(e)
                    }
                    val packet = TelemetryPacket()
                    packet.fieldOverlay()
                        .setFill("#0000ff")
                        .drawImage("resources/bg.png", 0.0, 0.0, 144.0, 144.0)
                        .drawGrid(0.0, 0.0, 144.0, 144.0, 7, 7)
                        .fillRect(-20.0, -20.0, 40.0, 40.0)
                        .setFill("#ff0000")
                        .fillText("Text", 0.0, 144.0, "${1 * ppi}px FreeSans", 0.0)

                    sendTelemetryPacket(packet)
                }
            })
        }

        override fun paintComponent(g: Graphics) {
            println("rendering")
            super.paintComponent(g)
//            val g2d = graphics as Graphics2D
            val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2d = bufferedImage.createGraphics()
            g2d.translate(width / 2, height / 2)
            val canvas = currentPacket.fieldOverlay()
            val ops = canvas.getOperations()
            for (op in ops) {
                op.draw(g2d, ppi)
            }
            g.drawImage(bufferedImage, 0, 0, null)
            println("done")
        }
    }

    val panel: Panel = Panel(ppi)
    private var currentPacket: ITelemetryPacket = TelemetryPacket()
    override fun sendTelemetryPacket(telemetryPacket: ITelemetryPacket) {
//        TODO("Not yet implemented")
        currentPacket = telemetryPacket
//        SwingUtilities.invokeAndWait { panel.repaint() }
        println("Sent telemetry packet")
        panel.repaint()
    }
}