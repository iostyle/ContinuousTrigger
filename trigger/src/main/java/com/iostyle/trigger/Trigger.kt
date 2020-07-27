package com.iostyle.trigger

class Trigger {
    var id: String? = null

    //超时时间 -1为持续等待
    var timeout: Long = -1L

    //已失效
    var invalid = false

    var strike: Strike? = null

    //阻塞模式
    var chokeMode = false

    interface Strike {
        fun strike()
    }
}