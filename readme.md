PC case LED light controller with Arduino
=

The repository contains a firmware written in C++ and a desktop GUI written in Kotlin.

You can build the firmware with PlatformIO and use Gradle for the desktop app.

The hardware is the simplest:
- an Arduino using PWM to drive some power transistors which in turn drive a led strip
- the Arduino is connected internally to an USB port of the motherboard
- the desktop app talks to the Arduino through serial communication
- the led strip is powered by the PSU of the PC

There are two predefined animations and static color option. The desktop app starts minimized to the taskbar.
Start it together with Windows, and it restores the last settings automatically.