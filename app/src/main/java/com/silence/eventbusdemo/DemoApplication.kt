package com.silence.eventbusdemo

import android.app.Application
import org.greenrobot.eventbus.EventBus

/**
 * Author silence.
 * Time：2020/6/30.
 * Desc：
 */
class DemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        EventBus.builder().addIndex(EventBusIndex()).installDefaultEventBus()
    }
}