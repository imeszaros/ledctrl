package imeszaros.ledctrl

object Protocol {

    const val DELAY = 3000L

    const val DELIMITER = "\r\n"

    object Request {

        const val HELLO = "HELLO"
        const val MODES = "MODES"
        const val MODE = "MODE?"

        fun mode(mode: Int) = "MODE:$mode"
        fun rgb(rgb: RGB) = "RGB:$rgb"
    }

    object Response {

        const val VERSION = "LedController-V1"
        const val OK = "OK"
    }
}