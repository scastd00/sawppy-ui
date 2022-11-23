package es.unileon.sawppy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.activity_main.automaticControlButton
import kotlinx.android.synthetic.main.activity_main.buttonBackwards
import kotlinx.android.synthetic.main.activity_main.buttonForward
import kotlinx.android.synthetic.main.activity_main.buttonLeft
import kotlinx.android.synthetic.main.activity_main.buttonRight
import kotlinx.android.synthetic.main.activity_main.connectBluetoothButton
import kotlinx.android.synthetic.main.activity_main.manualControlButton
import java.io.IOException
import java.util.UUID

/**
 * Main activity of the application.
 */
class MainActivity : AppCompatActivity() {
	private val movementHandler: MovementHandler = MovementHandler()
	private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private lateinit var bluetoothThread: BluetoothInitializationThread

	/**
	 * Called when the activity is created.
	 *
	 * @param savedInstanceState The saved instance state.
	 * @see AppCompatActivity.onCreate
	 *
	 * Retrieves the Bluetooth device and creates a new BluetoothInitializationThread to connect to it.
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.setContentView(R.layout.activity_main)
		this.findAndConnectBluetoothDevice()
		this.prepareTouchListeners()
		this.movementHandler.start()
	}

	/**
	 * Finds the Bluetooth device and creates a new BluetoothInitializationThread to connect to it.
	 *
	 * Internet resources:
	 *  - https://developer.android.com/guide/topics/connectivity/bluetooth/connect-bluetooth-devices
	 *  - https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	private fun findAndConnectBluetoothDevice() {
		// Create a bluetooth connection
		val pairedDevices: Set<BluetoothDevice>? = if (ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.BLUETOOTH_CONNECT
			) == PackageManager.PERMISSION_DENIED
		) {
			return
		} else {
			this.bluetoothAdapter.bondedDevices
		}

		pairedDevices
			?.filter { it.address.equals(BLUETOOTH_MAC, true) }
			?.filter { it.uuids.any { uuid -> uuid.uuid.equals(BLUETOOTH_UUID) } }
			?.get(0)
			?.let {
				this.bluetoothThread = BluetoothInitializationThread(it)
				this.bluetoothThread.start()
			}
	}

	/**
	 * Called when the activity is destroyed.
	 *
	 * @see AppCompatActivity.onDestroy
	 *
	 * Stops the movement handler and closes the Bluetooth connection.
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onDestroy() {
		super.onDestroy()

		this.movementHandler.setAction(Action.STOP)
		this.bluetoothThread.cancel()
	}

	private fun moveForward() = this.movementHandler.setAction(Action.FORWARD)
	private fun moveBackward() = this.movementHandler.setAction(Action.BACKWARD)
	private fun moveLeft() = this.movementHandler.setAction(Action.LEFT)
	private fun moveRight() = this.movementHandler.setAction(Action.RIGHT)

	/**
	 * Establishes the Stop action to be sent by the MovementHandler and clears the
	 * background of all buttons.
	 */
	private fun stop() {
		this.movementHandler.setAction(Action.STOP)
		this.stateOfButtons(true)
	}

	/**
	 * Clears the background of all buttons and sets the background of the given button.
	 *
	 * @param view The button to set the background.
	 */
	private fun keepPressedButtonBackground(view: View) {
		this.stateOfButtons(true)
		ViewCompat.setBackgroundTintList(
			view, ColorStateList.valueOf(
				this.getColor(R.color.pressed_control_button)
			)
		)
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
	 * Alternates between the manual and automatic control buttons.
	 */
	private fun switchControlButtons() {
		this.manualControlButton.isEnabled = !this.manualControlButton.isEnabled
		this.automaticControlButton.isEnabled = !this.automaticControlButton.isEnabled
	}

	/**
	 * Called when the automatic control button is clicked.
	 *
	 * @param view The view.
	 *
	 * Disables all the control buttons.
	 */
	fun automaticControl(view: View) {
		this.stateOfButtons(false)
		this.switchControlButtons()
		this.movementHandler.setAction(Action.AUTO)
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
		arrayOf(
			this.buttonForward,
			this.buttonBackwards,
			this.buttonLeft,
			this.buttonRight
		).forEach {
			it.isEnabled = enabled
			ViewCompat.setBackgroundTintList(
				it, ColorStateList.valueOf(
					this.getColor(
						if (enabled) R.color.enabled_control_button else R.color.disabled_control_button
					)
				)
			)
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	private fun prepareTouchListeners() {
		arrayOf(
			this.buttonForward,
			this.buttonBackwards,
			this.buttonLeft,
			this.buttonRight
		).forEach {
			it.setOnTouchListener { view, event ->
				when (event.action) {
					MotionEvent.ACTION_DOWN -> {
						when (view.id) {
							R.id.buttonForward -> this.moveForward()
							R.id.buttonBackwards -> this.moveBackward()
							R.id.buttonLeft -> this.moveLeft()
							R.id.buttonRight -> this.moveRight()
						}

						this.keepPressedButtonBackground(view)
					}

					MotionEvent.ACTION_UP -> this.stop()
				}

				true
			}
		}
	}

	/**
	 * Thread that initializes the Bluetooth connection.
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	private inner class BluetoothInitializationThread(device: BluetoothDevice) : Thread() {
		private val bluetoothSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
			if (ActivityCompat.checkSelfPermission(
					this@MainActivity,
					Manifest.permission.BLUETOOTH_CONNECT
				) == PackageManager.PERMISSION_DENIED
			) {
				ActivityCompat.requestPermissions(
					this@MainActivity,
					arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
					1
				)

				return@lazy null
			}

			try {
				return@lazy device.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_UUID))
			} catch (e: SecurityException) {
				Log.e(TAG, "Socket's create() method failed. Permissions required by user", e)
			}

			return@lazy null
		}

		/**
		 * Requests for permission to use the Bluetooth connection and creates the BluetoothSocket.
		 */
		override fun run() {
			Log.d(TAG, "BluetoothInitializationThread started")

			// Cancel discovery because it otherwise slows down the connection.
			if (ActivityCompat.checkSelfPermission(
					this@MainActivity,
					Manifest.permission.BLUETOOTH_SCAN
				) == PackageManager.PERMISSION_DENIED
			) {
				return
			}
			bluetoothAdapter.cancelDiscovery()

			bluetoothSocket?.let {
				// Connect to the remote device through the socket. This call blocks
				// until it succeeds or throws an exception.
				it.connect()

				// The connection attempt succeeded. Perform work associated with
				// the connection in a separate thread.
				setSocket(it)
			}
		}

		/**
		 * Closes the client socket and causes the thread to finish.
		 */
		fun cancel() {
			try {
				// Since the socket that we created is assigned to the MovementHandler, we don't need to close it here.
				// We close it in the MovementHandler (to send a message prior to closing it)
				movementHandler.end()
			} catch (e: IOException) {
				Log.e(TAG, "Could not close the client socket", e)
			}
		}
	}

	/**
	 * Sets the socket to the MovementHandler.
	 *
	 * @param bluetoothSocket The socket.
	 */
	private fun setSocket(bluetoothSocket: BluetoothSocket) {
		this.movementHandler.bluetoothSocket = bluetoothSocket
		this.connectBluetoothButton.isEnabled = false
		this.connectBluetoothButton.text = this.getString(R.string.connected)
		this.movementHandler.start()
	}
}
