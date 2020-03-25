package com.iostyle.trigger

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 多线程连续触发器
 */
class ContinuousTrigger {

    private var triggerList: ConcurrentLinkedQueue<Trigger>? = null
    private var currentJob: Job? = null

    private var blockNode: Trigger? = null
    private var debugMode = false

    constructor()

    constructor(triggerList: ConcurrentLinkedQueue<Trigger>?) {
        this.triggerList = triggerList
    }

    class Builder {
        private var triggerList = ConcurrentLinkedQueue<Trigger>()

        fun with(trigger: Trigger): Builder {
            triggerList.offer(trigger)
            return this
        }

        fun create(): ContinuousTrigger {
            return ContinuousTrigger(triggerList).also {
                it.next()
            }
        }
    }

    //按序注册
    fun register(trigger: Trigger): ContinuousTrigger {
        if (debugMode) log("ContinuousTrigger register ${triggerList?.size}: ${trigger.id}")
        if (triggerList == null) triggerList = ConcurrentLinkedQueue<Trigger>()
        triggerList?.offer(trigger)
        if (blockNode == null) {
            if (debugMode) log("ContinuousTrigger autoStart")
            next()
        }
        return this
    }

    //绑定执行
    @Synchronized
    fun attach(id: String, strike: Trigger.Strike) {
        if (debugMode) log("ContinuousTrigger attach ${id}")

        //阻塞唤醒
        if (tryWakeUp(id, strike)) {
            if (debugMode) log("ContinuousTrigger blockNode wakeup")
        } else if (triggerList?.isEmpty() == false) {
            triggerList?.forEach {
                if (it.id == id) {
                    it.strike = strike
                    return
                }
            }
        }
    }

    private fun tryWakeUp(id: String, strike: Trigger.Strike): Boolean {
        if (isCurrentNode(id)) {
            strike.strike()
            return true
        }
        return false
    }

    private fun isCurrentNode(id: String): Boolean {
        return blockNode != null && blockNode!!.id == id
    }

    //下一步
    @Synchronized
    fun next() {
        currentJob?.cancel()
        blockNode = triggerList?.poll()
        if (debugMode) log("ContinuousTrigger next ${blockNode?.id}")
        blockNode?.run {
            if (invalid) {
                if (debugMode) log("ContinuousTrigger invalid $id")
                next()
                return
            }
            strike?.strike() ?: (
                    if (timeout > 0)
                        currentJob = GlobalScope.launch {
                            delay(timeout)
                            if (debugMode) log("ContinuousTrigger timeout $id")
                            withContext(Dispatchers.Main) {
                                next()
                            }
                        }
                    )
        } ?: clear()
    }

    fun cancel(id: String) {
        if (debugMode) log("ContinuousTrigger cancel $id")
        if (triggerList?.isEmpty() == false)
            kotlin.run looper@{
                triggerList?.forEach {
                    if (it.id == id) {
                        it.invalid = true
                        return@looper
                    }
                }
            }
        if (isCurrentNode(id)) {
            if (debugMode) log("ContinuousTrigger cancel next $id")
            next()
        }
    }

    //响应 关闭超时线程 持续等待
    fun response() {
        currentJob?.cancel()
    }

    //清空
    fun clear() {
        if (debugMode) log("ContinuousTrigger clear")
        blockNode = null
        currentJob?.cancel()
        triggerList?.clear()
    }

    fun log(content: String?) {
        content?.let {
            Log.e("Trigger", it)
        }
    }
}