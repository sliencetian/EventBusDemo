# EventBusDemo
一个类实现简易版EventBus，使用方式与EventBus完全一样，仅用于分析EventBus实现方式，具体相关细节未完善
```kotlin
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
```
