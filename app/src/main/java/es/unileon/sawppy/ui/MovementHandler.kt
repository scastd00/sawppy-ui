package es.unileon.sawppy.ui

import android.bluetooth.BluetoothSocket
import org.intellij.lang.annotations.Language

/**
 * Class that handles the movement of the robot.
 */
class MovementHandler {
	var bluetoothSocket: BluetoothSocket? = null

	/**
	 * Sends a command to the rover.
	 *
	 * @param type The command type to send.
	 */
	fun performAction(type: ActionType) {
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
		this.performAction(ActionType.STOP)
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
