package com.litroenade.yunjiweather.widget

import java.util.concurrent.atomic.AtomicInteger

class WeatherWidgetBroadcastFinisher private constructor(
    private val remainingCount: AtomicInteger,
    private val onFinished: Runnable
) {

    fun markFinished() {
        if (remainingCount.decrementAndGet() == 0) {
            onFinished.run()
        }
    }

    companion object {
        @JvmStatic
        fun create(totalCount: Int, onFinished: Runnable): WeatherWidgetBroadcastFinisher {
            require(totalCount >= 0) { "totalCount must not be negative" }
            if (totalCount == 0) {
                onFinished.run()
            }
            return WeatherWidgetBroadcastFinisher(AtomicInteger(totalCount), onFinished)
        }
    }
}
