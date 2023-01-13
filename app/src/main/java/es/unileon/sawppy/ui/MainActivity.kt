package es.unileon.sawppy.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.automaticControlButton
import kotlinx.android.synthetic.main.activity_main.buttonBackwards
import kotlinx.android.synthetic.main.activity_main.buttonForward
import kotlinx.android.synthetic.main.activity_main.buttonLeft
import kotlinx.android.synthetic.main.activity_main.buttonRight
import kotlinx.android.synthetic.main.activity_main.buttonStop
import kotlinx.android.synthetic.main.activity_main.connectButton
import kotlinx.android.synthetic.main.activity_main.disconnectButton
import kotlinx.android.synthetic.main.activity_main.manualControlButton
import java.io.IOException
import java.util.UUID

/**
 * Main activity of the application.
 */
class MainActivity : AppCompatActivity() {
	private val movementHandler: MovementHandler = MovementHandler.INSTANCE
	private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private val imageButtons: Array<ImageButton> by lazy {
		arrayOf(
			this.buttonForward,
			this.buttonBackwards,
			this.buttonLeft,
			this.buttonRight,
			this.buttonStop
		)
	}

	/**
	 * Called when the activity is created.
	 *
	 * @param savedInstanceState The saved instance state.
	 * @see AppCompatActivity.onCreate
	 *
	 * Retrieves the Bluetooth device and creates a new BluetoothInitializationThread to connect to it.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_main)
		this.prepareTouchListeners()

		this.stateOfButtons(true)
		this.setColorToButton(this.connectButton, R.color.connect_button)
		this.setColorToButton(this.disconnectButton, R.color.disconnect_button, 0.4F)

		if (!this.bluetoothAdapter.isEnabled) {
			try {
				this.bluetoothAdapter.enable()
			} catch (_: SecurityException) {
			}
		}
	}

	private fun moveForward() = this.movementHandler.setAction(Action.FORWARD)
	private fun moveBackward() = this.movementHandler.setAction(Action.BACKWARD)
	private fun moveLeft() = this.movementHandler.setAction(Action.LEFT)
	private fun moveRight() = this.movementHandler.setAction(Action.RIGHT)
	private fun stop() = this.movementHandler.setAction(Action.STOP)

	/**
	 * Establishes the Stop action to be sent by the MovementHandler and clears the
	 * background of all buttons.
	 */
	private fun sendIgnored() {
		this.movementHandler.setAction(Action.IGNORE_SIGNAL)
		this.stateOfButtons(true)
	}

	/**
	 * Clears the background of all buttons and sets the background of the given button.
	 *
	 * @param view The button to set the background.
	 */
	private fun keepPressedButtonBackground(view: View) {
		this.stateOfButtons(true)
		this.setColorToButton(view, R.color.control_button)
	}

	/**
	 * Called when the manual control button is clicked.
	 *
	 * @param view The view.
	 *
	 * Enables all the control buttons.
	 */
	fun manualControl(view: View) {
		this.movementHandler.setAction(Action.MANUAL)
		this.switchControlButtons()
		this.stateOfButtons(true)
	}

	/**
	 * Called when the automatic control button is clicked.
	 *
	 * @param view The view.
	 *
	 * Disables all the control buttons.
	 */
	fun automaticControl(view: View) {
		this.movementHandler.setAction(Action.AUTO)
		this.switchControlButtons()
		this.stateOfButtons(false)
	}

	/**
	 * Alternates between the manual and automatic control buttons.
	 */
	private fun switchControlButtons() {
		this.manualControlButton.isEnabled = !this.manualControlButton.isEnabled
		this.automaticControlButton.isEnabled = !this.automaticControlButton.isEnabled
	}

	/**
	 * Enables or disables all the control buttons and sets the background color
	 * depending on the state.
	 *
	 * @param enabled The state of the buttons.
	 *
	 * See "https://devexperto.com/kotlin-android-extensions" for better understanding of the syntax.
	 */
	private fun stateOfButtons(enabled: Boolean) {
		this.imageButtons.forEach {
			it.isEnabled = enabled

			if (enabled) setColorToButton(it, R.color.control_button, 0.75F)
			else setColorToButton(it, R.color.control_button, 0.4F)
		}
	}

	/**
	 * Assigns the action to be performed when a button is pressed.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private fun prepareTouchListeners() {
		this.imageButtons.forEach {
			it.setOnTouchListener { view, event ->
				when (event.action) {
					MotionEvent.ACTION_DOWN -> {
						when (view.id) {
							R.id.buttonForward -> this.moveForward()
							R.id.buttonBackwards -> this.moveBackward()
							R.id.buttonLeft -> this.moveLeft()
							R.id.buttonRight -> this.moveRight()
							R.id.buttonStop -> this.stop()
						}

						this.keepPressedButtonBackground(view)
					}

					MotionEvent.ACTION_UP -> this.sendIgnored()
				}

				true
			}
		}
	}

	/**
	 * Helper method to set the background color of a button.
	 *
	 * @param view The button.
	 * @param color The color to set. If not specified, the color is set to transparent.
	 */
	private fun setColorToButton(view: View, color: Int, opacity: Float = 1f) {
		view.backgroundTintList = ColorStateList.valueOf(this.getColor(color))
		view.alpha = opacity - 0.1f
	}

	/**
	 * Finds the Bluetooth device and creates a new BluetoothInitializationThread to connect to it.
	 *
	 * Internet resources:
	 *  - https://developer.android.com/guide/topics/connectivity/bluetooth/connect-bluetooth-devices
	 *  - https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data
	 */
	fun connect(view: View) {
		try {
			this.bluetoothAdapter.cancelDiscovery()
			val bluetoothSocket: BluetoothSocket =
				this.bluetoothAdapter
					.getRemoteDevice(BLUETOOTH_MAC)
					.createInsecureRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_UUID))

			// Connect to the remote device through the socket. This call blocks
			// until it succeeds or throws an exception.
			bluetoothSocket.connect()

			// The connection attempt succeeded. Perform work associated with
			// the connection in a separate thread.
			this.movementHandler.bluetoothSocket = bluetoothSocket
			this.movementHandler.start()
			this.switchConnectionButtons()
		} catch (e: SecurityException) {
			Log.e(TAG, "Error while creating the Bluetooth socket", e)
		} catch (e: IOException) {
			Log.e(TAG, "Error while connecting to the Bluetooth device", e)
		}
	}

	fun disconnect(view: View) {
		this.movementHandler.stop()
		this.switchConnectionButtons()
	}

	private fun switchConnectionButtons() {
		this.connectButton.isEnabled = !this.connectButton.isEnabled
		this.disconnectButton.isEnabled = !this.disconnectButton.isEnabled

		if (this.connectButton.isEnabled) {
			this.setColorToButton(this.connectButton, R.color.connect_button)
			this.setColorToButton(this.disconnectButton, R.color.disconnect_button, 0.4F)
		} else {
			this.setColorToButton(this.connectButton, R.color.connect_button, 0.4F)
			this.setColorToButton(this.disconnectButton, R.color.disconnect_button)
		}
	}

	fun goToStatsActivity(view: View) {
		val intent = Intent(this, StatsActivity::class.java)
		startActivity(intent)
		overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
	}
}
