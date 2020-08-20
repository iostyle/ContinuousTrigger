package com.iostyle.continuoustrigger;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.iostyle.trigger.ContinuousTrigger;
import com.iostyle.trigger.Trigger;
import com.iostyle.trigger.UtilKt;

public class JavaMainActivity extends AppCompatActivity {

    private ContinuousTrigger trigger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Trigger t0 = new Trigger();
        t0.setId("test1");
        t0.setTimeout(2000);

        Trigger t1 = new Trigger();
        t1.setId("test2");
        t1.setChokeMode(true);

        Trigger t2 = new Trigger();
        t2.setId("test3");
        t2.setTimeout(2000);

        //name为可选参数 用于在任意位置获取trigger实例
        trigger = new ContinuousTrigger.Builder("myTrigger")
                .with(t0)
                .with(t1)
                .with(t2)
                .create();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (trigger != null)
                            trigger.attach("test1", new Trigger.Strike() {
                                @Override
                                public void strike() {
                                    Log.e("trigger", "test1");
                                }
                            });
                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UtilKt.getTriggerInstance("myTrigger").attach("test2", new Trigger.Strike() {
                            @Override
                            public void strike() {
                                Log.e("trigger", "test2");
                                new AlertDialog.Builder(JavaMainActivity.this).setMessage("test2")
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                if (trigger != null)
                                                    trigger.next();
                                            }
                                        }).show();
                            }
                        });
                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (trigger != null)
                            trigger.attach("test3", new Trigger.Strike() {
                                @Override
                                public void strike() {
                                    Log.e("trigger", "test3");
                                    UtilKt.removeTriggerInstance("myTrigger");
                                }
                            });
                    }
                });
            }
        }).start();
    }
}
