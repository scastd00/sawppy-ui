package es.unileon.sawppy.ui

import android.bluetooth.BluetoothSocket
import java.util.Timer
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.timer

/**
 * Class that handles the movement of the rover.
 */
class MovementHandler {
	var bluetoothSocket: BluetoothSocket? = null
	private val actionToSend: AtomicReference<Action> = AtomicReference(Action.STOP)
	private lateinit var taskExecutor: Timer
	private var isRunning = false

	companion object {
		val INSTANCE: MovementHandler = MovementHandler()
	}

	/**
	 * Starts the movement of the rover.
	 */
	fun start() {
		if (isRunning) return

		isRunning = true
		taskExecutor = timer(
			name = "RoverDataSender-Timer",
			daemon = true,
			initialDelay = 0,
			period = 100
		) {
			val action = actionToSend.get()
			sendBytes(action.toString())

			// When a control action was sent, the rover will stop moving in the next iteration.
			if (action.isControl())
				actionToSend.set(Action.STOP)
		}
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
		if (!isRunning) return

		println("Action set: $action")
		this.actionToSend.set(action)
	}

	/**
	 * Handles the end of the connection.
	 */
	fun stop() {
		if (!isRunning) return

		this.actionToSend.set(Action.STOP) // Set the action in case a new task is executed (last).
		this.taskExecutor.cancel() // Cancel the timer.
		this.sendBytes(Action.STOP.toString()) // Send stop signal to the rover ensure that it stops.
		this.bluetoothSocket?.close() // Close the bluetooth socket.
		this.isRunning = false // Set the isRunning attribute flag to false.
	}
}
