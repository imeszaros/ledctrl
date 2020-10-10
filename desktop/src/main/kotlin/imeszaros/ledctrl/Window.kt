package imeszaros.ledctrl

import com.fazecast.jSerialComm.SerialPort
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.ImageData
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class Window(private val gui: GUI) {

    private val deviceCombo: Combo
    private val modeCombo: Combo
    private val picker: Composite
    private val rScale: Scale
    private val gScale: Scale
    private val bScale: Scale

    private val imageData: ImageData
    private val imageEnabled: Image
    private val imageDisabled: Image

    private lateinit var ports: Array<SerialPort>

    private var executor: ThreadPoolExecutor = ThreadPoolExecutor(
        1, 1, 0L, MILLISECONDS, SynchronousQueue(), DiscardPolicy())

    val shell = Shell(gui.display, SWT.TITLE or SWT.BORDER or SWT.MIN or SWT.CLOSE).apply {

        addListener(SWT.Iconify) {
            gui.display.asyncExec {
                close()
            }
        }

        addListener(SWT.Dispose) {
            executor.shutdown()

            while (!executor.awaitTermination(1, SECONDS)) {
                /* no-op */
            }
        }

        images = arrayOf(
            gui.image("/icon-16.png"),
            gui.image("/icon-32.png"),
            gui.image("/icon-48.png"),
            gui.image("/icon-64.png"),
            gui.image("/icon-128.png"),
            gui.image("/icon-256.png"))

        text = "LED Controller"

        layout = GridLayout(2, false).apply {
            marginWidth = 10
            marginHeight = 10
        }

        Label(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                horizontalAlignment = SWT.RIGHT
            }

            text = "Select Port:"
        }

        Composite(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                grabExcessHorizontalSpace = true
                horizontalAlignment = SWT.FILL
            }

            layout = GridLayout(2, false).apply {
                marginWidth = 0
                marginHeight = 0
            }

            deviceCombo = Combo(this, SWT.DROP_DOWN or SWT.READ_ONLY).apply {
                addListener(SWT.Selection) {
                    onDeviceSelection()
                }
            }

            Button(this, SWT.PUSH).apply {
                text = "Refresh"

                addListener(SWT.Selection) {
                    refreshPorts()
                }
            }
        }

        Label(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                horizontalAlignment = SWT.RIGHT
            }

            text = "Select Mode:"
        }

        Composite(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                grabExcessHorizontalSpace = true
                horizontalAlignment = SWT.FILL
            }

            layout = GridLayout(2, false).apply {
                marginWidth = 0
                marginHeight = 0
            }

            modeCombo = Combo(this, SWT.DROP_DOWN or SWT.READ_ONLY).apply {
                addListener(SWT.Selection) {
                    onModeSelection()
                }
            }

            Label(this, SWT.NONE).apply {
                text = "Or pick a color:"
            }
        }

        placeholder()

        Composite(this, SWT.NONE).apply {
            layoutData = GridData().apply {
                grabExcessHorizontalSpace = true
                horizontalAlignment = SWT.FILL
            }

            layout = GridLayout(2, false).apply {
                marginWidth = 0
                marginHeight = 0
            }

            picker = Composite(this, SWT.BORDER).apply {
                layoutData = GridData(321, 281)

                imageEnabled = gui.image("/picker.png")
                imageDisabled = gui.grayImage("/picker.png")
                imageData = imageEnabled.imageData

                backgroundImage = imageDisabled

                addListener(SWT.MouseDown, this@Window::onColorPick)
                addListener(SWT.MouseMove, this@Window::onColorPick)
            }

            Group(this, SWT.NONE).apply {
                layoutData = GridData(SWT.FILL, SWT.FILL, true, true).apply {
                    verticalSpan = 4
                }

                layout = GridLayout()

                text = "About"

                Label(this, SWT.NONE).apply {
                    image = gui.image("/rabbit.png")
                    layoutData = GridData().apply {
                        grabExcessHorizontalSpace = true
                        grabExcessVerticalSpace = true
                        horizontalAlignment = SWT.CENTER
                        verticalAlignment = SWT.CENTER
                    }
                }

                Label(this, SWT.CENTER).apply {
                    text = "Version ${Display.getAppVersion()}\n- made with love, by Isti -"
                    layoutData = GridData().apply {
                        grabExcessHorizontalSpace = true
                        grabExcessVerticalSpace = true
                        horizontalAlignment = SWT.CENTER
                        verticalAlignment = SWT.CENTER
                    }
                }
            }

            rScale = Scale(this, SWT.HORIZONTAL).apply {
                layoutData = GridData().apply {
                    horizontalAlignment = SWT.FILL
                }

                maximum = 255

                addListener(SWT.Selection) {
                    onScaleChange()
                }
            }

            gScale = Scale(this, SWT.HORIZONTAL).apply {
                layoutData = GridData().apply {
                    horizontalAlignment = SWT.FILL
                }

                maximum = 255

                addListener(SWT.Selection) {
                    onScaleChange()
                }
            }

            bScale = Scale(this, SWT.HORIZONTAL).apply {
                layoutData = GridData().apply {
                    horizontalAlignment = SWT.FILL
                }

                maximum = 255

                addListener(SWT.Selection) {
                    onScaleChange()
                }
            }
        }

        refreshPorts()
        refreshDeviceSpecific()

        pack()
        center()
        open()
    }

    private fun refreshPorts() {
        ports = gui.display.compute { SerialPort.getCommPorts() }

        deviceCombo.removeAll()
        ports.forEach {
            deviceCombo.add("${it.descriptivePortName} (${it.portDescription})")
        }

        Device.current()?.let { dev ->
            ports.firstOrNull { it.systemPortName == dev.port.systemPortName }?.let {
                deviceCombo.select(ports.indexOf(it))
            }
        }
    }

    private fun refreshDeviceSpecific() {
        Device.current()?.let { dev ->
            modeCombo.removeAll()
            gui.display.compute { dev.modes() }.forEach { mode ->
                modeCombo.add(mode)
            }

            val mode = gui.display.compute { dev.mode() }
            modeCombo.select(mode.toInt())
            modeCombo.enabled = true

            picker.backgroundImage = imageEnabled

            Config.staticColor().let {
                rScale.selection = it.r
                gScale.selection = it.g
                bScale.selection = it.b
            }

            rScale.enabled = true
            gScale.enabled = true
            bScale.enabled = true
        } ?: run {
            modeCombo.deselectAll()
            modeCombo.enabled = false
            picker.backgroundImage = imageDisabled

            rScale.selection = 0
            gScale.selection = 0
            bScale.selection = 0

            rScale.enabled = false
            gScale.enabled = false
            bScale.enabled = false
        }
    }

    private fun onDeviceSelection() {
        Device.close()
        refreshDeviceSpecific()

        val port = ports[deviceCombo.selectionIndex]
        Device.switch(gui.display.compute { Device(port) })

        Device.current()?.let {
            Config.device(port.systemPortName)

            gui.display.compute { it.hello() }
            val box = MessageBox(shell, SWT.ICON_INFORMATION or SWT.OK)
            box.text = "Device Found"
            box.message = "Connected successfully."
            box.open()
        }

        refreshDeviceSpecific()
    }

    private fun onModeSelection() {
        val mode = modeCombo.selectionIndex

        Device.current()?.let {
            gui.display.compute {
                it.mode(mode)
            }
            Config.mode(mode)
        }
    }

    private fun onScaleChange() {
        if (!Device.connected()) {
            return
        }

        setRGB(RGB(rScale.selection, gScale.selection, bScale.selection))
    }

    private fun onColorPick(event: Event) {
        if (!Device.connected() || event.type == SWT.MouseMove && (event.stateMask and SWT.BUTTON1) == 0) {
            return
        }

        val rgb = RGB.fromSWT(imageData.palette.getRGB(imageData.getPixel(
                event.x.coerceIn(0, imageData.width - 1),
                event.y.coerceIn(0, imageData.height - 1))))

        rScale.selection = rgb.r
        gScale.selection = rgb.g
        bScale.selection = rgb.b

        setRGB(rgb)
    }

    private fun setRGB(rgb: RGB) {
        executor.submit {
            Device.current()?.let { dev ->
                dev.rgb(rgb)
                val mode = dev.mode()
                gui.display.asyncExec {
                    modeCombo.select(mode.toInt())
                }
                Config.staticColor(rgb)
            }
        }
    }
}