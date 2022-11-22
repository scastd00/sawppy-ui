package es.unileon.sawppy.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {
	private val movementHandler = MovementHandler()
	private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var bluetoothThread: BluetoothInitializationThread? = null

	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		// See https://developer.android.com/guide/topics/connectivity/bluetooth/connect-bluetooth-devices
		// And https://developer.android.com/guide/topics/connectivity/bluetooth/transfer-data

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
			this.bluetoothThread?.start()
		}
	}

	@RequiresApi(Build.VERSION_CODES.S)
	override fun onDestroy() {
		super.onDestroy()

		this.movementHandler.stop()
		this.bluetoothThread?.cancel()
	}

	fun moveForward(view: View) {
		this.movementHandler.move(MovementType.FORWARD)
	}

	fun moveBackward(view: View) {
		this.movementHandler.move(MovementType.BACKWARD)
	}

	fun moveLeft(view: View) {
		this.movementHandler.move(MovementType.LEFT)
	}

	fun moveRight(view: View) {
		this.movementHandler.move(MovementType.RIGHT)
	}

	fun stop(view: View) {
		this.movementHandler.stop()
	}

	fun manualControl(view: View) {
		this.movementHandler.manualControl()

		findViewById<Button>(R.id.buttonForward).isEnabled = true
		findViewById<Button>(R.id.buttonBackwards).isEnabled = true
		findViewById<Button>(R.id.buttonLeft).isEnabled = true
		findViewById<Button>(R.id.buttonRight).isEnabled = true
	}

	fun automaticControl(view: View) {
		findViewById<Button>(R.id.buttonForward).isEnabled = false
		findViewById<Button>(R.id.buttonBackwards).isEnabled = false
		findViewById<Button>(R.id.buttonLeft).isEnabled = false
		findViewById<Button>(R.id.buttonRight).isEnabled = false

		this.movementHandler.automaticControl()
	}

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

		// Closes the client socket and causes the thread to finish.
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

	private fun setSocket(bluetoothSocket: BluetoothSocket) {
		this.movementHandler.bluetoothSocket = bluetoothSocket
	}
}
