package com.silence.eventbus

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Author silence.
 * Time：2020/6/22.
 */

enum class ThreadMode {
    DEFAULT, //当前线程
    UI, //主线程
    BACKGROUND//子线程
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EventMethod(val threadMode: ThreadMode = ThreadMode.DEFAULT)

object EventBus {

    private val eventBus = EventBusInternal()

    fun register(any: Any) {
        eventBus.register(any)
    }

    fun unRegister(any: Any) {
        eventBus.unRegister(any)
    }

    fun post(any: Any) {
        eventBus.post(any)
    }

}

internal class EventBusInternal {

    private val subscribeMethodFinder = SubscribeMethodFinder()
    //hashMap<订阅事件的对象的类签名，该类下的所有订阅事件集合>，方便用于解注册
    private val subscribesBySubscriber = HashMap<Class<*>, ArrayList<SubscribeMethod>>()
    //hashMap<订阅事件的参数类型，该事件类型的所有集合>，用于事件分发
    private val subscribesByEventType = HashMap<Class<*>, ArrayList<SubscribeMethod>>()

    private val uiHandler = Handler(Looper.getMainLooper())
    private val backgroundHandler by lazy {
        val handlerThread = HandlerThread("InvokeSubscriberThread")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    fun register(subscriber: Any) {
        val methodList = subscribeMethodFinder.findSubscribeMethod(subscriber)
        subscribesBySubscriber[subscriber.javaClass] = methodList
        methodList.forEach {
            var subscriberList = subscribesByEventType[it.eventType]
            if (subscriberList == null) {
                subscriberList = ArrayList()
                subscribesByEventType[it.eventType] = subscriberList
            }
            subscriberList.add(it)
        }
    }

    fun unRegister(subscriber: Any) {
        subscribesBySubscriber[subscriber.javaClass]?.forEach { methodBySubscribe ->
            val methodList = subscribesByEventType[methodBySubscribe.eventType]
            methodList?.forEach { methodByEventType ->
                if (methodByEventType.subscriber == subscriber.javaClass) {
                    methodList.remove(methodByEventType)
                }
            }
        }
        subscribesBySubscriber.remove(subscriber.javaClass)
    }

    fun post(event: Any) {
        subscribesByEventType[event.javaClass]?.forEach {
            when (it.threadMode) {
                ThreadMode.DEFAULT -> {
                    invokeSubscriber(it, event)
                }
                ThreadMode.UI -> {
                    uiHandler.post { invokeSubscriber(it, event) }
                }
                ThreadMode.BACKGROUND -> {
                    backgroundHandler.post { invokeSubscriber(it, event) }
                }
            }
        }
    }

    private fun invokeSubscriber(subscribeMethod: SubscribeMethod, event: Any) {
        try {
            subscribeMethod.method.invoke(subscribeMethod.subscriber, event)
        } catch (t: Throwable) {
            throw EventBusException("invokeSubscriber exception", t)
        }
    }
}

internal class SubscribeMethodFinder {

    private val ignoreModifiers = Modifier.ABSTRACT or Modifier.STATIC
    private val subscribeMethodCache = HashMap<Class<*>, ArrayList<SubscribeMethod>>()

    fun findSubscribeMethod(subscriber: Any): ArrayList<SubscribeMethod> {
        val subscriberClass = subscriber.javaClass
        var subscribeMethodList = subscribeMethodCache[subscriberClass]
        if (subscribeMethodList != null) {
            return subscribeMethodList
        }
        subscribeMethodList = findUsingReflection(subscriber)
        subscribeMethodCache[subscriberClass] = subscribeMethodList
        return subscribeMethodList
    }

    private fun findUsingReflection(subscriber: Any): ArrayList<SubscribeMethod> {
        val subscriberClass = subscriber.javaClass
        val subscribeMethodList = ArrayList<SubscribeMethod>()
        val methods = try {
            subscriberClass.declaredMethods
        } catch (t: Throwable) {
            try {
                subscriberClass.methods
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
        methods?.forEach { method ->
            val modifiers = method.modifiers
            //只解析被 EventMethod 标注的方法
            if (method.isAnnotationPresent(EventMethod::class.java)) {
                //校验注册方法的权限，
                if (modifiers and Modifier.PUBLIC != 0 && modifiers and ignoreModifiers == 0) {
                    //该方法只能有一个参数
                    if (method.parameterTypes.size == 1) {
                        method.getAnnotation(EventMethod::class.java)?.let { it ->
                            subscribeMethodList.add(
                                SubscribeMethod(
                                    subscriber, method,
                                    method.parameterTypes[0], it.threadMode
                                )
                            )
                        }
                    } else {
                        throw EventBusException("${method.name} 方法 参数各位超过一个")
                    }
                } else {
                    throw EventBusException("${method.name} 方法不是 public 的")
                }
            }
        }
        return subscribeMethodList
    }

}

internal class SubscribeMethod(
    val subscriber: Any,
    val method: Method,
    val eventType: Class<*>,
    val threadMode: ThreadMode
)

internal class EventBusException(message: String, cause: Throwable? = null) :
    Throwable(message, cause)