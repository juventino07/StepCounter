package com.example.cyrilleulmi.stepcounter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Created by cyrilleulmi on 11/21/2014.
 */
public class HsrLogger {
    public static void LogMessage(String stringToPost, Context context) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(context, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra("ch.appquest.taskname", "Step Counter");
        intent.putExtra("ch.appquest.logmessage", stringToPost);

        context.startActivity(intent);
    }
}
