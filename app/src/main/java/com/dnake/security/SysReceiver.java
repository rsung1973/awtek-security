package com.dnake.security;

import com.dnake.v700.security;
import com.dnake.v700.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SysReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent it) {
		String a = it.getAction();
		if (a.equals("android.intent.action.BOOT_COMPLETED")) {
			Intent intent = new Intent(ctx, security.class);
			ctx.startService(intent);
		} else if (a.equals("com.dnake.broadcast")) {
			String e = it.getStringExtra("event");
			if (e.equals("com.dnake.boot"))
				security.dBroadcast();
			else if (e.equals("com.dnake.talk.touch")) {
				WakeTask.refresh();
			} else if (e.equals("com.dnake.talk.eHome.setup")) {
				utils.eHome = it.getBooleanExtra("mode", false);
			}
		} else if (a.equals("com.dnake.doorAlarm")) {
			security.CMS.sendAlarm(1);
			/*String e = it.getStringExtra("event");
			if (e.equals("com.dnake.boot"))
				security.dBroadcast();
			else if (e.equals("com.dnake.talk.touch")) {
				WakeTask.refresh();
			} else if (e.equals("com.dnake.talk.eHome.setup")) {
				utils.eHome = it.getBooleanExtra("mode", false);
			}*/
		}
	}
}
