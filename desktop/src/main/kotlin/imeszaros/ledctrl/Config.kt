package imeszaros.ledctrl

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import java.nio.file.Files
import java.nio.file.Paths

object Config {

    private val configFile = Paths.get(System.getProperty("user.home")).resolve(".ledctrl")

    private var config = ConfigFactory.parseFile(configFile.toFile())
        .withFallback(
            ConfigFactory.parseMap(mapOf(
                Keys.DEVICE to "",
                Keys.MODE to 0,
                "${Keys.STATIC_COLOR}.r" to 0,
                "${Keys.STATIC_COLOR}.g" to 0,
                "${Keys.STATIC_COLOR}.b" to 0
            )))

    fun device() = config.getString(Keys.DEVICE).takeUnless { it.isBlank() }

    fun device(device: String) {
        config = config.withValue(Keys.DEVICE, fromAnyRef(device))
    }

    fun mode() = config.getInt(Keys.MODE)

    fun mode(mode: Int) {
        config = config.withValue(Keys.MODE, fromAnyRef(mode))
    }

    fun staticColor() = RGB(
        config.getInt("${Keys.STATIC_COLOR}.r"),
        config.getInt("${Keys.STATIC_COLOR}.g"),
        config.getInt("${Keys.STATIC_COLOR}.b"))

    fun staticColor(rgb: RGB) {
        config = config
            .withValue("${Keys.STATIC_COLOR}.r", fromAnyRef(rgb.r))
            .withValue("${Keys.STATIC_COLOR}.g", fromAnyRef(rgb.g))
            .withValue("${Keys.STATIC_COLOR}.b", fromAnyRef(rgb.b))
    }

    fun save() {
        Files.writeString(configFile, config.root().render(ConfigRenderOptions.concise()))
    }

    private object Keys {

        const val DEVICE = "device"
        const val MODE = "mode"
        const val STATIC_COLOR = "staticColor"
    }
}