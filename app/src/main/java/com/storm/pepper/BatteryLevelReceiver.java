package com.storm.pepper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.storm.posh.BaseBehaviourLibrary;

public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BaseBehaviourLibrary behaviourLibrary = BaseBehaviourLibrary.getInstance();

        if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
//            some code...
            behaviourLibrary.setBatteryLow(true);
        } else if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
            behaviourLibrary.setBatteryLow(false);
        }

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = ((status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL));

        behaviourLibrary.setBatteryCharging(isCharging);
    }
}