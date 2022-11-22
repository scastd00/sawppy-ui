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
import kotlinx.android.synthetic.main.activity_main.buttonStop
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
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return
		} else {
			this.bluetoothAdapter.bondedDevices
		}

		// Todo: We search by MAC or by UUID?
		val device: BluetoothDevice? = pairedDevices?.firstOrNull {
			it.address.equals(BLUETOOTH_MAC, true)
		}

		device?.let {
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

		this.movementHandler.stop()
		this.bluetoothThread.cancel()
	}

	/**
	 * Called when the forward button is clicked.
	 *
	 * @param view The view that was clicked.
	 */
	fun moveForward(view: View) = this.movementHandler.performAction(ActionType.FORWARD)

	/**
	 * Called when the backward button is clicked.
	 *
	 * @param view The view that was clicked.
	 */
	fun moveBackward(view: View) = this.movementHandler.performAction(ActionType.BACKWARD)

	/**
	 * Called when the left button is clicked.
	 *
	 * @param view The view that was clicked.
	 */
	fun moveLeft(view: View) = this.movementHandler.performAction(ActionType.LEFT)

	/**
	 * Called when the right button is clicked.
	 *
	 * @param view The view that was clicked.
	 */
	fun moveRight(view: View) = this.movementHandler.performAction(ActionType.RIGHT)

	/**
	 * Called when the stop button is clicked.
	 *
	 * @param view The view that was clicked.
	 */
	fun stop(view: View) = this.movementHandler.stop()

	/**
	 * Called when the manual control button is clicked.
	 *
	 * @param view The view.
	 *
	 * Enables all the control buttons.
	 */
	fun manualControl(view: View) {
		this.movementHandler.manualControl()
		this.manualControlButton.isEnabled = false
		this.automaticControlButton.isEnabled = true
		this.enableButtons()
	}

	/**
	 * Called when the automatic control button is clicked.
	 *
	 * @param view The view.
	 *
	 * Disables all the control buttons.
	 */
	fun automaticControl(view: View) {
		this.disableButtons()
		this.manualControlButton.isEnabled = true
		this.automaticControlButton.isEnabled = false
		this.movementHandler.automaticControl()
	}

	/**
	 * Enables all the control buttons.
	 *
	 * See "https://devexperto.com/kotlin-android-extensions" for better understanding of the syntax.
	 */
	@SuppressLint("UseCompatLoadingForDrawables")
	private fun enableButtons() {
		arrayOf(
			this.buttonForward,
			this.buttonBackwards,
			this.buttonLeft,
			this.buttonRight,
			this.buttonStop
		).forEach {
			it.isEnabled = true
			ViewCompat.setBackgroundTintList(
				it, ColorStateList.valueOf(
					this.getColor(R.color.enabled_control_button)
				)
			)
		}
	}

	/**
	 * Disables all the control buttons.
	 */
	@SuppressLint("UseCompatLoadingForDrawables")
	private fun disableButtons() {
		arrayOf(
			this.buttonForward,
			this.buttonBackwards,
			this.buttonLeft,
			this.buttonRight,
			this.buttonStop
		).forEach {
			it.isEnabled = false
			ViewCompat.setBackgroundTintList(
				it, ColorStateList.valueOf(
					this.getColor(R.color.disabled_control_button)
				)
			)
		}
	}

	/**
	 * Thread that initializes the Bluetooth connection.
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	private inner class BluetoothInitializationThread(device: BluetoothDevice) : Thread() {
		private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
			if (ActivityCompat.checkSelfPermission(
					this@MainActivity,
					Manifest.permission.BLUETOOTH_CONNECT
				) == PackageManager.PERMISSION_DENIED
			) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
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
				return@lazy null
			}
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
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return
			}
			bluetoothAdapter.cancelDiscovery()

			mmSocket?.let {
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
	}
}
