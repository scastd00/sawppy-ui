package es.unileon.sawppy.ui

import android.bluetooth.BluetoothSocket
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.timer

/**
 * Class that handles the movement of the rover.
 */
class MovementHandler {
	var bluetoothSocket: BluetoothSocket? = null
	private val actionToSend: AtomicReference<Action> = AtomicReference()
	private val taskExecutor = timer(
		name = "RoverDataSender-Timer",
		daemon = true,
		initialDelay = 0,
		period = 100
	) {
		var action = actionToSend.get()

		// First action to send to the rover is always Manual, then it is set to Stop.
		if (action == null) {
			action = Action.MANUAL
			actionToSend.set(Action.STOP)
		}

		sendBytes(action.toString())
	}

	/**
	 * Writes the data string to the Bluetooth socket.
	 *
	 * @param data The data to send.
	 */
	private fun sendBytes(data: String) {
		this.bluetoothSocket?.outputStream?.write(data.toByteArray())
	}

	/**
	 * Sets the action to send to the rover.
	 *
	 * @param action The action to send.
	 */
	fun setAction(action: Action) {
		println("Action set: $action")
		this.actionToSend.set(action)
	}

	/**
	 * Handles the end of the connection.
	 */
	fun end() {
		this.actionToSend.set(Action.STOP) // Set the action in case a new task is executed (last).
		this.taskExecutor.cancel() // Cancel the timer.
		this.sendBytes(Action.STOP.toString()) // Send stop signal to the rover ensure that it stops.
		this.bluetoothSocket?.close() // Close the bluetooth socket.
	}
}
