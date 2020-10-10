package imeszaros.ledctrl

fun main() {
    Device.tryLast()?.let { dev ->
        dev.rgb(Config.staticColor())
        dev.mode(Config.mode())
    }
    GUI.init().join()
    Device.close()
    Config.save()
}