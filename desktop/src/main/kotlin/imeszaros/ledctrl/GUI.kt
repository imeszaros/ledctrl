package imeszaros.ledctrl

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.ImageData
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.*
import java.util.function.Consumer
import kotlin.concurrent.thread

class GUI {

    init {
        Display.setAppName("LED Controller")
        Display.setAppVersion("1.0.0")
    }

    val display = Display.getDefault()!!.apply {
        runtimeExceptionHandler = Consumer {
            it.printStackTrace()

            val activeShell = activeShell
            val parent = activeShell ?: Shell(this)

            val box = MessageBox(parent, SWT.ICON_ERROR or SWT.OK)
            box.text = "Error"
            box.message = it.localizedMessage
            box.open()

            if (activeShell == null) {
                parent.dispose()
            }
        }
    }

    private val images = mutableMapOf<String, Image>()
    private val colors = mutableMapOf<RGB, Color>()

    private val rootShell = Shell(display)

    private val trayMenu = Menu(rootShell, SWT.POP_UP).apply {

        MenuItem(this, SWT.PUSH).apply {
            text = "Open"
            addListener(SWT.Selection) { openAppWindow() }
        }

        separator()

        MenuItem(this, SWT.PUSH).apply {
            text = "Exit"
            addListener(SWT.Selection) { rootShell.dispose() }
        }
    }

    private val trayItem = TrayItem(display.systemTray, SWT.NONE).apply {
        toolTipText = "LED Controller - Right click for options."
        image = image("/icon-32.png")

        addListener(SWT.MenuDetect) { trayMenu.visible = true }
        addListener(SWT.Selection) { openAppWindow() }
    }

    private var appWindow: Window? = null

    fun loop() {
        while (!rootShell.isDisposed) {
            if (!display.readAndDispatch()) {
                display.sleep()
            }
        }

        trayItem.dispose()

        images.values.forEach(Image::dispose)
        colors.values.forEach(Color::dispose)

        display.dispose()
    }

    fun image(path: String): Image = images.computeIfAbsent(path) { p ->
        Image(display, ImageData(javaClass.getResourceAsStream(p)))
    }

    fun grayImage(path: String): Image = images.computeIfAbsent("gray:$path") {
        Image(display, image(path), SWT.IMAGE_GRAY)
    }

    private fun openAppWindow() {
        with (appWindow) {
            if (this == null || shell.isDisposed) {
                appWindow = Window(this@GUI).apply {
                    shell.open()
                }
            } else {
                shell.setActive()
            }
        }
    }

    companion object {

        fun init() = thread(name = "User Interface Thread") {
            GUI().loop()
        }
    }
}