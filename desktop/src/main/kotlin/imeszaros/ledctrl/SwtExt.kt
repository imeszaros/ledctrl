package imeszaros.ledctrl

import org.eclipse.swt.SWT
import org.eclipse.swt.custom.BusyIndicator
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.widgets.*
import java.lang.RuntimeException
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

fun Composite.placeholder() = Label(this, SWT.NONE)

fun Menu.separator(): MenuItem = MenuItem(this, SWT.SEPARATOR)

fun Shell.center() {
    val monitorBounds = monitor.bounds
    bounds = Rectangle(
        monitorBounds.x + monitorBounds.width / 2 - size.x / 2,
        monitorBounds.y + monitorBounds.height / 2 - size.y / 2,
        size.x, size.y)
}

fun <T> Display.compute(block: () -> T): T {
    val future = CompletableFuture<T>()

    BusyIndicator.showWhile(this) {
        thread(start = true) {
            try {
                future.complete(block.invoke())
            } catch (t: Throwable) {
                future.completeExceptionally(t)
            }
        }

        while (!future.isDone) {
            if (!readAndDispatch()) {
                Thread.sleep(10)
            }
        }
    }

    return try {
        future.get()
    } catch (e: RuntimeException) {
        throw e
    } catch (t: Throwable) {
        throw RuntimeException(t)
    }
}