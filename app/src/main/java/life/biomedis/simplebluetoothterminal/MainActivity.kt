package life.biomedis.simplebluetoothterminal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick


class MainActivity : AppCompatActivity() {

    private lateinit var sendButton: Button
    private lateinit var selectDeviceButton: Button
    private lateinit var scrollArea: ScrollView
    private lateinit var resultLogText: TextView
    private lateinit var commandText: EditText
    private lateinit var clearLogBtn: Button
    private lateinit var selectedDeviceInfo: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findWidgets()
        initWidgets()
    }

    private fun initWidgets() {
        resultLogText.text = ""

        sendButton.onClick { doSendCommand() }

        selectDeviceButton.onClick { showSelectDeviceDialog() }

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
        selectedDeviceInfo = find(R.id.selectedDeviceInfo)
    }


    /**
     * Показывает диалог выбора доступных устройств для коннекта
     */
    private fun showSelectDeviceDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Добавляет комманду и результат в поле лога
     */
    inline private fun appendLog(command: String, response: String) = resultLogText.append("==== Command ====\n$command\n==== Response ====\n$response\n\n")

    /**
     * Действие по отправке команды  и приему ответа
     */
    private fun doSendCommand(){
        appendLog(commandText.text.toString(), "Это наш милый результат" )
    }
}
