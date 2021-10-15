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
        private var name: String? = null
        private var triggerList = ConcurrentLinkedQueue<Trigger>()

        constructor()
        constructor(name: String?) {
            this.name = name
        }

        @TriggerDSL
        infix fun with(trigger: Trigger): Builder {
            triggerList.offer(trigger)
            return this
        }

        fun create(): ContinuousTrigger {
            return ContinuousTrigger(triggerList).also {
                it.next()
                if (!name.isNullOrBlank()) saveTriggerInstance(name!!, it)
            }
        }
    }

    //按序注册
    @TriggerDSL
    infix fun register(trigger: Trigger): ContinuousTrigger {
        if (debugMode) log("ContinuousTrigger register ${triggerList?.size}: ${trigger.id}")
        if (triggerList == null) triggerList = ConcurrentLinkedQueue<Trigger>()
        triggerList?.offer(trigger)
        if (blockNode == null) {
            if (debugMode) log("ContinuousTrigger autoStart")
            next()
        }
        return this
    }

    //绑定执行 为Java代码提供的重载
    @Synchronized
    fun attach(id: String, strike: Trigger.Strike) {
        attach(id, strike, false)
    }

    //绑定执行
    @Synchronized
    fun attach(id: String, strike: Trigger.Strike, adjustAttach: Boolean = false) {
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
            /**
             * 代码走到这里代表你attach的事件没有被绑定或者已经超时被跳过
             */
            if (adjustAttach)
                adjustAttach(id, strike)
        } else {
            /**
             * 代码走到这里意味着所有的事件都已经超时
             */
            if (adjustAttach)
                adjustAttach(id, strike)
        }
    }

    /**
     * 灵活绑定 内部调用用于已超时的补充或者id未注册场景
     * 外部使用的话如果不需要顺序控制则不需要注册 直接调用adjustAttach即可按调用顺序执行
     */
    @Synchronized
    fun adjustAttach(id: String, strike: Trigger.Strike, chokeMode: Boolean = false) {
        register(Trigger().also {
            it.id = id
            it.chokeMode = chokeMode
            it.strike = strike
        })
    }

    private fun tryWakeUp(id: String, strike: Trigger.Strike): Boolean {
        if (isCurrentNode(id)) {
            currentJob?.cancel()
            strike.strike()
            if (blockNode?.chokeMode == false) next()
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
            strike?.run {
                strike()
                if (!chokeMode) next()
            } ?: (
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