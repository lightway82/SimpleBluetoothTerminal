package life.biomedis.simplebluetoothterminal

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.widget.*
import org.jetbrains.anko.*
import android.view.View
import android.widget.ArrayAdapter


const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var sendButton: Button
    private lateinit var selectDeviceButton: Button
    private lateinit var scrollArea: ScrollView
    private lateinit var resultLogText: TextView
    private lateinit var commandText: EditText
    private lateinit var clearLogBtn: Button
    private lateinit var infoArea: TextView
    private lateinit var deviceSpinner: Spinner

    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var scanReceiver: BroadcastReceiver
    private lateinit var scanFinishedReceiver: BroadcastReceiver

    private val deviceList: MutableList<BluetoothDevice> = arrayListOf()

    private lateinit var scanningProgressDlg: ProgressDialog
    private lateinit var bondedDevicesAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findWidgets()
        initWidgets()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        checkBluetooth(bluetoothAdapter)

        scanReceiver = createBroadcastReceiverForScan()
        scanFinishedReceiver = createBroadcastReceiverForFinishedScan()

        initDeviceSpinner()

    }

    private fun initDeviceSpinner() {
        val bondedDevices: MutableList<String> = MutableList(1, { "-" })
        (bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()).forEach { dev ->
            bondedDevices += "${dev.name}-${dev.address}"
        }


        // адаптер
        bondedDevicesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bondedDevices).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            deviceSpinner.adapter = this

        }

        with(deviceSpinner) {
            setSelection(0)
            prompt = "Кэшированные устройства"
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    toast(bondedDevices[position])
                    setDeviceWidgetsState("", true)
                }
            }
        }
    }

    private fun createBroadcastReceiverForScan(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                // Когда найдено новое устройство
                if (BluetoothDevice.ACTION_FOUND == action) {
                    // Получаем объект BluetoothDevice из интента
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    deviceList.add(device)
                }
            }
        }
    }

    private fun createBroadcastReceiverForFinishedScan(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                toast("Scanning complete")
                unregisterReceiver(scanFinishedReceiver)
                unregisterReceiver(scanReceiver)
                scanningProgressDlg.dismiss()


                if (deviceList.isEmpty()) toast("No devices found")
                else {
                    bondedDevicesAdapter.clear()
                    bondedDevicesAdapter.add("-")
                    bondedDevicesAdapter.addAll(deviceList.map { it.name })
                    bondedDevicesAdapter.notifyDataSetChanged()
                    selector("Available devices", deviceList.map { it.name + "\n" + it.address })
                    { _, i ->
                        toast("Selected device is ${deviceList[i].let { it.name + "\n" + it.address }}")
                        deviceSpinner.setSelection(i)
                        setDeviceWidgetsState("", true)
                    }
                }
            }
        }
    }

    /**
     * Ищет доступные для подключения устройства
     */
    private fun scanFoDevices() {
        toast("Prepare scan")
        if (!hasBluetoothDevice()) {
            error("Не удалось запустить сканирование устройств Bluetooth. Возможно устройство не доступно или отсутствует.")
            setDeviceWidgetsState("Bluetooth is disabled!", false)
            toast("Error scan")
            return
        }

        registerReceiver(scanFinishedReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        registerReceiver(scanReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        deviceList.clear()
        scanningProgressDlg = indeterminateProgressDialog(message = "Scan in progress", title = "Scanning for devices")
        bluetoothAdapter!!.startDiscovery()
        toast("Start scan")
    }


    private fun checkBluetooth(bluetoothAdapter: BluetoothAdapter?) {

        if (bluetoothAdapter == null) setDeviceWidgetsState("Bluetooth device not found!", false)
        else {
            if (!hasBluetoothDevice()) {

                setDeviceWidgetsState("Bluetooth is disabled!", false)
                // Bluetooth выключен. Предложим пользователю включить его.
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else setDeviceWidgetsState("Select target bluetooth device", true)
        }

    }

    private fun setDeviceWidgetsState(selectedDeviceText: String, enableSelectDeviceBtn: Boolean) {
        infoArea.text = selectedDeviceText
        selectDeviceButton.isEnabled = enableSelectDeviceBtn
    }

    /**
     * Доступно ли устройство bluetooth
     */
    private fun hasBluetoothDevice() = bluetoothAdapter?.isEnabled ?: false

    private fun initWidgets() {
        resultLogText.text = ""
        infoArea.text = ""

        sendButton.onClick { doSendCommand() }

        selectDeviceButton.onClick { scanFoDevices() }

        clearLogBtn.setOnClickListener {
            commandText.text.clear()
            resultLogText.text = ""
        }
    }

    private fun findWidgets() {
        sendButton = find(R.id.sendBtn)
        selectDeviceButton = find(R.id.selectDeviceBtn)
        scrollArea = find(R.id.resultScroll)
        resultLogText = find(R.id.resultLog)
        commandText = find(R.id.sendBytesString)
        clearLogBtn = find(R.id.clearLogBtn)
        infoArea = find(R.id.selectedDeviceInfo)
        deviceSpinner = find(R.id.deviceSpinner)
    }


    /**
     * Добавляет комманду и результат в поле лога
     */
    inline private fun appendLog(command: String, response: String) = resultLogText.append("==== Command ====\n$command\n==== Response ====\n$response\n\n")

    /**
     * Действие по отправке команды  и приему ответа
     */
    private fun doSendCommand() {
        appendLog(commandText.text.toString(), "Это наш милый результат")
    }


    private inline fun isMessageFromActivateBluetoothActivityAndResultOK(requestCode: Int, resultCode: Int) = requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        info(" Результат $requestCode $resultCode")
        when {
            isMessageFromActivateBluetoothActivityAndResultOK(requestCode, resultCode) -> setDeviceWidgetsState("Select target bluetooth device", true)
        }

    }

}

