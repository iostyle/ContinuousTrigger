package com.iostyle.trigger

class Trigger {
    var id: String? = null

    //超时时间 -1为持续等待
    var timeout: Long = -1L

    var invalid = false

    var strike: Strike? = null

    interface Strike {
        fun strike()
    }
}