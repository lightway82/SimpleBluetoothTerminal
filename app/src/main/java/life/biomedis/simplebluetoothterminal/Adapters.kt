package life.biomedis.simplebluetoothterminal

import android.R
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.jetbrains.anko.find


data class BluetoothDeviceDecorator(val device: BluetoothDevice?){
    override fun toString() = if(device==null) "-" else "${device.name}\n${device.address}"
}



class DevicesInfoAdapter(context: Context, devices: List<BluetoothDeviceDecorator>) : ArrayAdapter<BluetoothDeviceDecorator>(context, android.R.layout.simple_list_item_2, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val item:BluetoothDeviceDecorator = getItem(position)
        var targetView: View = convertView ?: createNewView()

       return targetView.apply{
            find<TextView>(android.R.id.text1)?.text = item.device?.name?:"-"
            find<TextView>(android.R.id.text2)?.text = item.device?.address?:""
        }
    }

    private fun createNewView() = LayoutInflater.from(context).inflate(R.layout.simple_list_item_2, null)
}
