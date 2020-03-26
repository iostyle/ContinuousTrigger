package com.iostyle.continuoustrigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.iostyle.trigger.ContinuousTrigger
import com.iostyle.trigger.Trigger
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var trigger: ContinuousTrigger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        trigger = ContinuousTrigger.Builder()
            .with(
                Trigger().also {
                    it.id = "test1"
                    it.timeout = 2000
                }
            )
            .with(
                Trigger().also {
                    it.id = "test2"
                    // 应用于dialog的阻塞模式
                    it.chokeMode = true
                }
            )
            .with(
                Trigger().also {
                    it.id = "test3"
                    it.timeout = 2000
                }
            )
            .create()

        GlobalScope.launch {
            delay(1500)
            withContext(Dispatchers.Main) {
                trigger?.attach("test1", object : Trigger.Strike {
                    override fun strike() {
                        Log.e("trigger", "test1")
                    }
                })
            }
        }

        trigger?.attach("test2", object : Trigger.Strike {
            override fun strike() {
                Log.e("trigger", "test2")
                AlertDialog.Builder(this@MainActivity).setMessage("test2")
                    .setOnDismissListener {
                        trigger?.next()
                    }.show()
            }
        })

        GlobalScope.launch {
            delay(6000)
            withContext(Dispatchers.Main) {
                trigger?.attach("test3", object : Trigger.Strike {
                    override fun strike() {
                        Log.e("trigger", "test3")
                    }
                })
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        trigger?.clear()
    }
}
