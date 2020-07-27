package com.iostyle.trigger

import java.util.concurrent.ConcurrentHashMap

private var continuousTriggers: ConcurrentHashMap<String, ContinuousTrigger>? = null

fun getTriggerInstance(name: String): ContinuousTrigger? {
    return continuousTriggers?.get(name)
}

fun saveTriggerInstance(name: String, continuousTrigger: ContinuousTrigger) {
    if (continuousTriggers == null) {
        continuousTriggers = ConcurrentHashMap()
    }
    continuousTriggers?.put(name, continuousTrigger)
}

fun removeTriggerInstance(name: String): ContinuousTrigger? {
    return removeTriggerInstance(name, true)
}

fun removeTriggerInstance(name: String, clear: Boolean): ContinuousTrigger? {
    return continuousTriggers?.remove(name)?.apply { if (clear) clear() }
}

fun clearTriggers() {
    continuousTriggers?.forEach { it.value.clear() }
    continuousTriggers?.clear()
    continuousTriggers = null
}