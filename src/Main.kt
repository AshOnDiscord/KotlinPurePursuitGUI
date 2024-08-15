import ftcDashboard.FTCDashboard
import java.awt.Dimension
import javax.swing.JFrame

fun main() {
    val ppi = 7.0
    val dashboard = FTCDashboard(ppi)
    val purePursuit = PurePursuit(ppi)

    val frame = JFrame("Pure Pursuit")
    frame.size = Dimension((144 * ppi).toInt(), (144 * ppi).toInt())
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setLocationRelativeTo(null)
    frame.contentPane.add(dashboard.panel)
    frame.isVisible = true
}

