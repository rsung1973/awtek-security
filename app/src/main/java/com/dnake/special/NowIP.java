package com.dnake.special;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.security;
import com.dnake.v700.utils;

@SuppressLint("SimpleDateFormat")
public class NowIP {
	public static int enable = 0;
	public static String to = "sip:911@192.168.12.40";
	public static String to2 = "sip:911@192.168.12.41";
	public static int to2_delay = -1;
	public static int auto_call = 0;
	public static String call_url = "sip:911@192.168.12.40";
	public static int timeout = 10;
	public static int heartbeat_en = 0;
	public static String heartbeat_data = "123456780000";
	public static String alarm_data[] = null;

	public static String url = "/dnake/cfg/nowip.xml";

	public static void load() {
		if (NowIP.alarm_data == null) {
			NowIP.alarm_data = new String[security.MAX];
			NowIP.alarm_ts = new long[security.MAX];
			for (int i = 0; i < security.MAX; i++) {
				NowIP.alarm_data[i] = "010012345678000000101000";
				NowIP.alarm_ts[i] = 0;
			}
		}

		dxml p = new dxml();
		if (p.load(url)) {
			NowIP.enable = p.getInt("/nowip/enable", 0);
			NowIP.to = p.getText("/nowip/to", NowIP.to);
			NowIP.to2 = p.getText("/nowip/to2", NowIP.to2);
			NowIP.to2_delay = p.getInt("/nowip/to2_delay", 0);
			NowIP.auto_call = p.getInt("/nowip/auto_call", 0);
			NowIP.call_url = p.getText("/nowip/call_url", NowIP.call_url);
			NowIP.timeout = p.getInt("/nowip/timeout", 10);

			NowIP.heartbeat_en = p.getInt("/nowip/heartbeat_en", 1);
			NowIP.heartbeat_data = p.getText("/nowip/heartbeat_data", NowIP.heartbeat_data);

			for (int i = 0; i < security.MAX; i++)
				NowIP.alarm_data[i] = p.getText("/nowip/zone" + i + "/d", NowIP.alarm_data[i]);
		} else
			save();
	}

	public static void save() {
		dxml p = new dxml();

		p.setInt("/nowip/enable", NowIP.enable);
		p.setText("/nowip/to", NowIP.to);
		p.setText("/nowip/to2", NowIP.to2);
		p.setInt("/nowip/to2_delay", NowIP.to2_delay);
		p.setInt("/nowip/auto_call", NowIP.auto_call);
		p.setText("/nowip/call_url", NowIP.call_url);
		p.setInt("/nowip/timeout", NowIP.timeout);

		p.setInt("/nowip/heartbeat_en", NowIP.heartbeat_en);
		p.setText("/nowip/heartbeat_data", NowIP.heartbeat_data);

		for (int i = 0; i < security.MAX; i++)
			p.setText("/nowip/zone" + i + "/d", NowIP.alarm_data[i]);

		p.save(url);
	}

	public static long ts = 0;

	public static void process() {
		if (NowIP.enable != 0) {
			if (NowIP.heartbeat_en != 0 && Math.abs(System.currentTimeMillis() - NowIP.ts) >= NowIP.timeout * 1000) {
				NowIP.ts = System.currentTimeMillis();

				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setText("/params/to", NowIP.to);
				p.setText("/params/data/ATM/version", "1.5");
				p.setInt("/params/data/ATM/type", 0);
				p.setText("/params/data/ATM/data", NowIP.heartbeat_data);
				p.setText("/params/data/ATM/mac", utils.getLocalMac());

				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Date dt = new Date(System.currentTimeMillis());
				p.setText("/params/data/ATM/time", fmt.format(dt));

				req.to("/talk/sip/sendto", p.toString());
				if (NowIP.to2_delay >= 0) {
					p.setText("/params/to", NowIP.to2);
					req.to("/talk/sip/sendto", p.toString());
				}
			}

			if (NowIP.to2_delay >= 0) {
				for (int i = 0; i < security.MAX; i++) {
					if (NowIP.alarm_ts[i] != 0 && Math.abs(System.currentTimeMillis() - NowIP.alarm_ts[i]) >= NowIP.to2_delay * 1000) {
						NowIP.alarm_send(i, NowIP.alarm_ts[i], NowIP.to2);
						NowIP.alarm_ts[i] = 0;
					}
				}
			}
		}
	}

	public static void alarm_send(int zone, long ts, String to) {
		dxml p = new dxml();
		dmsg req = new dmsg();

		p.setText("/params/to", NowIP.to);
		p.setText("/params/data/ATM/version", "1.5");
		p.setInt("/params/data/ATM/type", 1);
		p.setText("/params/data/ATM/data", NowIP.alarm_data[zone]);
		p.setText("/params/data/ATM/mac", utils.getLocalMac());

		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		Date dt = new Date(ts);
		p.setText("/params/data/ATM/time", fmt.format(dt));

		req.to("/talk/sip/sendto", p.toString());
	}

	public static long alarm_ts[] = null;

	public static void alarm(int zone) {
		if (NowIP.enable == 0)
			return;

		if (NowIP.auto_call != 0) {
			dxml p = new dxml();
			dmsg req = new dmsg();
			p.setText("/params/url", NowIP.call_url);
			req.to("/talk/sip/call", p.toString());

			p.setText("/params/host", "NowIP");
			req.to("/talk/sip/ring", p.toString());
		}

		NowIP.alarm_send(zone, System.currentTimeMillis(), NowIP.to);
		if (NowIP.to2_delay >= 0)
			NowIP.alarm_ts[zone] = System.currentTimeMillis();
	}
}
