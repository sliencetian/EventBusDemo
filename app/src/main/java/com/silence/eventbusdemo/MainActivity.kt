package com.silence.eventbusdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.silence.eventbus.EventBus
import com.silence.eventbus.EventMethod
import com.silence.eventbus.ThreadMode

class DataModel(val msg: String)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.register(this)
    }

    @EventMethod(ThreadMode.UI)
    fun dataReceive(dataModel: DataModel) {
        Toast.makeText(this, dataModel.msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        EventBus.unRegister(this)
        super.onDestroy()
    }

    fun post(view: View) {
        EventBus.post(DataModel("send success"))
    }

}