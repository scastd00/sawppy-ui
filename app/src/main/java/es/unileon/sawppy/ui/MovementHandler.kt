package es.unileon.sawppy.ui

import android.bluetooth.BluetoothSocket
import org.intellij.lang.annotations.Language
import java.util.concurrent.atomic.AtomicReference

/**
 * Class that handles the movement of the robot.
 */
class MovementHandler {
	var bluetoothSocket: BluetoothSocket? = null
	private val actionToSend: AtomicReference<Action> = AtomicReference(Action.MANUAL)

	// Todo: first action must be Manual Control (Action(207)) then Stop (Action(105))

	/**
	 * Sends a command to the rover.
	 *
	 * @param type The command type to send.
	 */
	fun performAction(type: Action) {
		println("Moving ${type.signalToSend}")

		this.sendSignal(
			"""
				{
					"movement": "${type.signalToSend}"
				}
			""".trimIndent()
		)
	}

	/**
	 * Sends a signal to stop the rover.
	 */
	fun stop() {
		println("Stopping")
		this.performAction(Action.STOP)
	}

	/**
	 * Changes the control mode of the rover to manual.
	 */
	fun manualControl() {
		this.sendSignal(
			"""
				{
					"control": "manual"
				}
			""".trimIndent()
		)
	}

	/**
	 * Changes the control mode of the rover to automatic.
	 */
	fun automaticControl() {
		this.sendSignal(
			"""
				{
					"control": "automatic"
				}
			""".trimIndent()
		)
	}

	/**
	 * Sends a signal to the rover.
	 *
	 * @param signal The signal to send.
	 */
	private fun sendSignal(@Language("JSON") signal: String) {
		if (this.bluetoothSocket == null || !this.bluetoothSocket!!.isConnected) {
			println("No bluetooth connection")
			return
		}

		this.bluetoothSocket?.outputStream?.write(signal.toByteArray())
	}

	/**
	 * Handles the end of the connection.
	 */
	fun end() {
		this.stop()
		this.bluetoothSocket?.close()
	}
}
