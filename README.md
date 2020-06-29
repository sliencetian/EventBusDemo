# EventBusDemo
一个类实现简易版EventBus，使用方式与EventBus完全一样，仅用于分析EventBus实现方式，具体相关细节未完善
```kotlin
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
```
