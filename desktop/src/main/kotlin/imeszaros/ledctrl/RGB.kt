package imeszaros.ledctrl

import org.eclipse.swt.graphics.RGB as SWTRGB

data class RGB(val r: Int, val g: Int, val b: Int) {

    override fun toString() = "$r;$g;$b"

    companion object {

        fun fromSWT(rgb: SWTRGB) = RGB(rgb.red, rgb.green, rgb.blue)
    }
}