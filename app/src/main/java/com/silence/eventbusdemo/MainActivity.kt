package com.silence.eventbusdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.silence.eventbus.CustomEventBus
import com.silence.eventbus.EventMethod
import com.silence.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class DataModel(val msg: String)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CustomEventBus.register(this)
        EventBus.getDefault().register(this)
    }

    fun post(view: View) {
        EventBus.getDefault().post(DataModel("send success"))
    }

    @EventMethod(ThreadMode.UI)
    @Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun dataReceive(dataModel: DataModel) {
        Toast.makeText(this, dataModel.msg, Toast.LENGTH_LONG).show()
    }

    fun customPost(view: View) {
        CustomEventBus.post(DataModel("custom post send success"))
    }

    override fun onDestroy() {
        CustomEventBus.unRegister(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}