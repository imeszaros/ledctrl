package imeszaros.ledctrl

import com.fazecast.jSerialComm.SerialPort
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.Closeable
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

class Device(val port: SerialPort) : Closeable {

    private val reader: Scanner
    private val writer: BufferedWriter

    init {
        port.baudRate = 9600
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0)
        port.openPort()

        Thread.sleep(Protocol.DELAY)

        reader = Scanner(BufferedInputStream(port.inputStream))
            .useDelimiter(Protocol.DELIMITER)

        writer = BufferedWriter(OutputStreamWriter(port.outputStream))
    }

    fun hello() {
        write(Protocol.Request.HELLO)

        check(read() == Protocol.Response.VERSION) {
            "Unknown device or incompatible protocol."
        }
    }

    fun modes(): List<String> {
        write(Protocol.Request.MODES)

        val modeCount = read().toInt()

        val modes = ArrayList<String>()
        for (i in 0 until modeCount) {
            modes += read()
        }

        return modes
    }

    fun mode(): String {
        write(Protocol.Request.MODE)
        return read()
    }

    fun mode(mode: Int) {
        write(Protocol.Request.mode(mode))

        check(read() == Protocol.Response.OK) {
            "Unable to set the requested mode."
        }
    }

    fun rgb(rgb: RGB) {
        write(Protocol.Request.rgb(rgb))

        check(read() == Protocol.Response.OK) {
            "Unable to set the requested color."
        }
    }

    @Override
    override fun close() {
        port.closePort()
    }

    private fun write(message: String) {
        writer.write(message)
        writer.write(Protocol.DELIMITER)
        writer.flush()
    }

    private fun read(): String {
        while (!reader.hasNextLine()) {
            reader.ioException()?.let {
                throw it
            }

            Thread.sleep(1)
        }

        val line = reader.nextLine()

        reader.ioException()?.let {
            throw it
        }

        return line
    }

    companion object {

        private var current: Device? = null

        fun connected() = current != null

        fun current() = current

        fun close() {
            current?.close()
            current = null
        }

        fun switch(device: Device?) {
            close()
            current = device
        }

        fun tryLast() = Config.device()?.let {
            switch(try {
                Device(SerialPort.getCommPort(it)).also {  dev -> dev.hello() }
            } catch (t: Throwable) {
                null
            })

            current
        }
    }
}