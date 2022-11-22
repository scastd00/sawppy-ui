package es.unileon.sawppy.ui

import android.bluetooth.BluetoothSocket
import org.intellij.lang.annotations.Language

class MovementHandler {
	var bluetoothSocket: BluetoothSocket? = null

	fun move(type: MovementType) {
		println("Moving ${type.signalToSend}")

		this.sendSignal(
			"""
				{
					"movement": "${type.signalToSend}"
				}
			""".trimIndent()
		)
	}

	fun stop() {
		println("Stopping")

		this.sendSignal(
			"""
				{
					"movement": "stop"
				}
			""".trimIndent()
		)
	}

	fun manualControl() {
		this.sendSignal(
			"""
				{
					"control": "manual"
				}
			""".trimIndent()
		)
	}

	fun automaticControl() {
		this.sendSignal(
			"""
				{
					"control": "automatic"
				}
			""".trimIndent()
		)
	}

	private fun sendSignal(@Language("JSON") signal: String) {
		if (this.bluetoothSocket == null || !this.bluetoothSocket!!.isConnected) {
			println("No bluetooth connection")
			return
		}

		this.bluetoothSocket?.outputStream?.write(signal.toByteArray())
	}

	fun end() {
		this.stop()
		this.bluetoothSocket?.close()
	}
}
