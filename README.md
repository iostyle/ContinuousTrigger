# ContinuousTrigger [![](https://jitpack.io/v/iostyle/ContinuousTrigger.svg)](https://jitpack.io/#iostyle/ContinuousTrigger)
多线程连续触发器 

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
		implementation 'com.github.iostyle:ContinuousTrigger:1.0.2'
	}
  
## Method
|方法|描述|
|--|--|
|with|Builder模式构造注册|
|create|Builder模式创建实例并初始化|
|register|实例化方式有序注册|
|attach|根据ID绑定触发器|
|next|下一步|
|cancel|根据ID取消对应触发器，如果是当前阻塞则自动执行下一个|
|response|响应并关闭超时线程|
|clear|清空|

  
## Example
```
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
                            it.timeout = 2000
                        }
                )
                .with(
                        Trigger().also { it.id = "test3" }
                ).create()
      
      //或者直接实例化使用
      trigger = ContinuousTrigger()
                .register(
                        Trigger().also {
                            it.id = "test1"
                            it.timeout = 2000
                        })
                .register(
                        Trigger().also {
                            it.id = "test2"
                            it.timeout = 2000
                        })
                .register(
                        Trigger().also {
                            it.id = "test3"
                        })
                        
       trigger?.attach("test3", object : Trigger.Strike {
            override fun strike() {
                trigger?.response()
                AlertDialog.Builder(this@MainActivity).setMessage("test3").setOnDismissListener {
                    trigger?.next()
                }.show()
            }
        })           
       
       GlobalScope.launch {
            delay(1500)
            withContext(Dispatchers.Main) {
                trigger?.attach("test1", object : Trigger.Strike {
                    override fun strike() {
                        trigger?.response()
                        AlertDialog.Builder(this@MainActivity).setMessage("test1").setOnDismissListener {
                            trigger?.next()
                        }.show()
                    }
                })
            }
        }


        GlobalScope.launch {
            delay(6000)
            withContext(Dispatchers.Main) {
                trigger?.attach("test2", object : Trigger.Strike {
                    override fun strike() {
                        trigger?.response()
                        AlertDialog.Builder(this@MainActivity).setMessage("test2").setOnDismissListener {
                            trigger?.next()
                        }.show()
                    }
                })
            }
        }

```
