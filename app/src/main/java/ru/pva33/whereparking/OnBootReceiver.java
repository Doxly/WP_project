package ru.pva33.whereparking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Suppose start service on device boot process.
 * It is registered in AndroidManifest.xml
 * Created by pva on 21.01.16.
 */
public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, PvaParkingService.class);
            context.startService(serviceLauncher);
            Log.v(this.getClass().getName(), "Service loaded while device boot.");
        }
    }
}
