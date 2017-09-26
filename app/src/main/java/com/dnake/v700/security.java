package com.dnake.v700;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.dnake.security.AlarmLabel;
import com.dnake.security.WakeTask;
import com.dnake.special.NowIP;
import com.dnake.special.ipc;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class security extends Service {
	public static String url = "/dnake/cfg/security.xml";
	public static final int MAX = 16;

	public static final int WITHDRAW = 0;
	public static final int OUT = 1;
	public static final int HOME = 2;
	public static final int SLEEP = 3;

	public static final int M_3C = 0;
	public static final int M_NO = 1;
	public static final int M_NC = 2;
	public static final int M_BELL = 3;

	public static String passwd = "1234";

	public static int defence = 0;
	public static zone_c zone[] = null;
	public static int timeout = 100; // 布防时间

	public static class zone_c {
		public static final int NORMAL = 0;
		public static final int EMERGENCY = 1;
		public static final int H24 = 2;

		public int defence = 0;
		public int type = H24;
		public int delay = 0;
		public int sensor = 0;
		public int mode = 0; // 接口模式
		public int[] scene = new int[4];
	}

	public static void load() {
		if (zone == null) {
			zone = new zone_c[MAX];
			for (int i = 0; i < MAX; i++) {
				zone[i] = new zone_c();
				zone[i].scene[0] = 0;
				zone[i].scene[1] = 0;
				zone[i].scene[2] = 0;
				zone[i].scene[3] = 0;

				if (i < 8)
					zone[i].scene[0] = 1;
				if (i < 2)
					zone[i].scene[1] = 1;
				if (i < 4)
					zone[i].scene[2] = 1;
			}
		}

		dxml p = new dxml();
		if (p.load(url)) {
			passwd = p.getText("/security/passwd", passwd);
			timeout = p.getInt("/security/timeout", 100);
			defence = p.getInt("/security/defence", 0);
			for (int i = 0; i < MAX; i++) {
				String s = "/security/zone" + i;
				zone[i].defence = p.getInt(s + "/defence", 0);
				zone[i].type = p.getInt(s + "/type", 0);
				zone[i].delay = p.getInt(s + "/delay", 0);
				zone[i].sensor = p.getInt(s + "/sensor", 0);
				zone[i].mode = p.getInt(s + "/mode", 0);
				for (int j = 0; j < 4; j++)
					zone[i].scene[j] = p.getInt(s + "/scene" + j, 0);
			}
		} else
			save();
		sys.httpPasswd();
	}

	public static void save() {
		dxml p = new dxml();

		p.setText("/security/passwd", passwd);
		p.setInt("/security/defence", defence);
		p.setInt("/security/timeout", timeout);
		for (int i = 0; i < MAX; i++) {
			String s = new String("/security/zone" + i);
			p.setInt(s + "/defence", zone[i].defence);
			p.setInt(s + "/type", zone[i].type);
			p.setInt(s + "/delay", zone[i].delay);
			p.setInt(s + "/sensor", zone[i].sensor);
			p.setInt(s + "/mode", zone[i].mode);
			for (int j = 0; j < 4; j++)
				p.setInt(s + "/scene" + j, zone[i].scene[j]);
		}
		p.save(url);

		sys.httpPasswd();
		security.dBroadcast();
	}

	public static void load(dxml p) {
		if (zone == null)
			return;

		for (int i = 0; i < MAX; i++) {
			String s = "/params/zone" + i;
			zone[i].defence = p.getInt(s + "/defence", zone[i].defence);
			zone[i].type = p.getInt(s + "/type", zone[i].type);
			zone[i].delay = p.getInt(s + "/delay", zone[i].delay);
			zone[i].sensor = p.getInt(s + "/sensor", zone[i].sensor);
			zone[i].mode = p.getInt(s + "/mode", zone[i].mode);
			for (int j = 0; j < 4; j++)
				zone[i].scene[j] = p.getInt(s + "/scene" + j, zone[i].scene[j]);
		}

		if (p.getInt("/params/defence", -1) != -1) {
			int d = timeout;
			int st = p.getInt("/params/defence", defence);
			timeout = p.getInt("/params/timeout", timeout);

			if (st == 0)
				withdraw();

			setDefence(st);
			if (sys.talk.dcode == 0) // 主分机需要同步状态
				slaves.setMarks(0x01);
			if (timeout != d)
				save();
		}
	}

	//布防延时处理
	public static class dSound {
		public static long mTs = 0;
		public static dSound mSound = null;

		public static void stop() {
			mSound = null;
			mTs = 0;
		}

		public dSound() {
			mTs = System.currentTimeMillis();
		}

		private int max = 80;
		private int idx = 0;
		public void process() {
			idx++;
			if (idx >= max) {
				idx = 0;
				if (max > 20)
					max--;
				security.soundEvent(0);
			}

			if (mTs != 0 && Math.abs(System.currentTimeMillis() - mTs) >= timeout * 1000) {
				dSound.stop();
				security.soundEvent(1);
			}
			if (AlarmLabel.intent != null)
				dSound.stop();
		}
	}

	public static void setDefence(int st) {
		Boolean ok = (defence != st ? true : false);

		switch (st) {
		case WITHDRAW:
			for (int i = 0; i < MAX; i++)
				zone[i].defence = 0;
			break;

		case OUT:
			for (int i = 0; i < MAX; i++)
				zone[i].defence = zone[i].scene[0];
			break;

		case HOME:
			for (int i = 0; i < MAX; i++)
				zone[i].defence = zone[i].scene[1];
			break;

		case SLEEP:
			for (int i = 0; i < MAX; i++)
				zone[i].defence = zone[i].scene[2];
			break;
		}
		defence = st;

		if (ok) {
			dSound.stop();

			if (st > 0) {
				security.soundEvent(0);
				dSound.mSound = new dSound();
			} else
				security.soundEvent(2);

			CMS.sendDefence();
			CMS.d600SetZone();
			save();
		}
	}

	public static long[] mZoneTs = new long[security.MAX]; // 延时
	public static Boolean[] mSend = new Boolean[security.MAX]; // 是否网络发送

	public static Boolean mHave = false; //是否有报警触发
	public static long mAts = 0; // 最后一个报警触发时间
	public static int mIO[] = new int[MAX]; // 最新IO状态
	public static Boolean[] mIoSt = new Boolean[security.MAX]; // IO触发标记

	public static void process(int io[], int length) {
		if (length > security.MAX)
			length = security.MAX;

		for (int i = 0; i < length; i++) {
			if (io[i] == 0x10)
				continue;

			if (io[i] != 0) {
				if (zone[i].type == 1) { // 紧急报警
					CMS.sendAlarm(i);
				} else if (zone[i].type == 2 || (zone[i].defence > 0 && dSound.mTs == 0)) { // 报警
					if (mZoneTs[i] == 0)
						mZoneTs[i] = System.currentTimeMillis();
					if (mSend[i])
						mSend[i] = false;

					mIO[i] = io[i];
					mHave = true;
					mIoSt[i] = true;
					mAts = System.currentTimeMillis();
				}
			} else
				mIO[i] = 0;
		}
		if (security.mHave) {
			WakeTask.acquire();
			if (sys.talk.dcode == 0)
				slaves.setMarks(0x02); // 同步安防状态
		}
	}

	public static void withdraw() {
		mHave = false;
		for (int i = 0; i < security.MAX; i++) {
			mIO[i] = 0;
			mIoSt[i] = false;
			mSend[i] = false;
			mZoneTs[i] = 0;
		}
		ioctl.hooter(0);

		CMS.d600AlarmCancel();
	}

	public static long zoneDelayTs[] = { 0, 5, 15, 20, 25, 40, 60 };

	public static Boolean isZoneAlarm(int idx) {
		if (mIoSt[idx] && zone[idx].type == 1)
			return true;

		if (mIoSt[idx] && mZoneTs[idx] != 0) {
			long ts = Math.abs(System.currentTimeMillis() - mZoneTs[idx]);
			if (ts >= zoneDelayTs[zone[idx].delay] * 1000)
				return true;
		}
		return false;
	}

	//管理软件接口
	public static class CMS {
		public static void sendAlarm(int io) {
			if (sys.talk.dcode != 0)
				return;

			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/sp_io", 0);
			p.setInt("/params/io" + io, 1);
			req.to("/control/d600/alarm_trigger", p.toString()); // 600协议报警上报

			dxml p2 = new dxml();
			p2.setText("/params/event_url", "/msg/alarm/trigger");
			p2.setInt("/params/zone", io);
			p2.setInt("/params/data", 1);
			req.to("/talk/center/to", p2.toString()); // 700协议报警上报

			security.broadcast(io);
			security.mBroadcast(); // 取消静音

			NowIP.alarm(io);
		}

		public static void sendDefence() {
			if (sys.talk.dcode != 0)
				return;

			dmsg req = new dmsg();
			dxml p = new dxml();

			p.setText("/params/event_url", "/msg/alarm/defence");
			p.setInt("/params/defence", security.defence);
			p.setInt("/params/total", security.MAX);
			for (int i = 0; i < security.MAX; i++) {
				String s = "/params/io" + i;
				p.setInt(s, security.zone[i].defence);
			}
			req.to("/talk/center/to", p.toString());
		}

		public static void d600SetZone() { //600管理软件防区布撤防状态
			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/defence", defence);
			for (int i = 0; i < MAX; i++) {
				p.setInt("/params/zone" + i, zone[i].defence);
			}
			req.to("/control/d600/zone", p.toString());
		}

		public static void d600AlarmCancel() {
			if (sys.talk.dcode == 0) {
				dmsg req = new dmsg();
				req.to("/control/d600/alarm_cancel", null);
			}
		}
	}

	public static void broadcast(int io) {
		dmsg req = new dmsg();

		dxml p = new dxml();
		p.setText("/event/broadcast_url", "security");
		p.setInt("/event/data/io", io);
		p.setInt("/event/data/type", security.zone[io].type);
		p.setInt("/event/data/sensor", security.zone[io].sensor);
		p.setInt("/event/talk/build", sys.talk.building);
		p.setInt("/event/talk/unit", sys.talk.unit);
		p.setInt("/event/talk/floor", sys.talk.floor);
		p.setInt("/event/talk/family", sys.talk.family);
		req.to("/talk/broadcast/data", p.toString());
	}

	public static void nBroadcast() {
		if (sys.talk.dcode != 0)
			return;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/event/broadcast_url", "/security/notify");
		p.setInt("/event/build", sys.talk.building);
		p.setInt("/event/unit", sys.talk.unit);
		p.setInt("/event/floor", sys.talk.floor);
		p.setInt("/event/family", sys.talk.family);
		p.setInt("/event/defence", security.defence);
		req.to("/talk/broadcast/data", p.toString());
	}

	public static void run() {
		if (mHave) {
			for (int i = 0; i < security.MAX; i++) {
				if (mSend[i] == false && mIoSt[i] && isZoneAlarm(i)) {
					CMS.sendAlarm(i);
					mSend[i] = true;
				}
			}
			if (AlarmLabel.intent == null && Math.abs(System.currentTimeMillis() - mAts) < AlarmLabel.TIMEOUT) {
				WakeTask.acquire();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				security.alarmEvent();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ProcessThread pt;

	public static void start() {
		mHave = false;
		for (int i = 0; i < security.MAX; i++) {
			mIoSt[i] = false;
			mSend[i] = false;
			mZoneTs[i] = 0;
		}

		pt = new ProcessThread();
		Thread thread = new Thread(pt);
		thread.start();
	}

	public static class ProcessThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				security.run();
				slaves.process();
				NowIP.process();

				if (dSound.mSound != null)
					dSound.mSound.process();

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static Context ctx;
	private static Handler e_defence = null;
	private static Handler e_alarm = null;
	private static int s_defence = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		ctx = this;

		dmsg.start("/security");
		devent.setup();
		sys.load();
		ipc.load();
		NowIP.load();
		slaves.start();
		security.load();
		security.start();

		sound.load();
		ioctl.hooter(0);
		slaves.setMarks(0x03);

		dmsg req = new dmsg();
		req.to("/talk/slave/reset", null);

		e_defence = new Handler() {
			private MediaPlayer player = null;

			class PlayerOnCompletionListener implements OnCompletionListener {
				@Override
				public void onCompletion(MediaPlayer mp) {
					stopPlayer();
				}
			}

			private void stopPlayer() {
				if (player != null) {
					player.stop();
					player.release();
					player = null;
				}
			}

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (ctx != null) {
					// 模拟触摸事件，确保不会关屏
					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/data", 0);
					p.setInt("/params/apk", 1);
					req.to("/ui/touch/event", p.toString());

					if (s_defence == 0) { // 布防延时
						if (player == null) {
							player = sound.play(sound.defence_delay, false, new PlayerOnCompletionListener());
						} else if (!player.isPlaying()) {
							stopPlayer();
						}
					} else if (s_defence == 1) { // 布防成功
						stopPlayer();
						player = sound.play(sound.defence_on, false, new PlayerOnCompletionListener());
					} else if (s_defence == 2) {
						stopPlayer();
						player = sound.play(sound.defence_cancel, false, new PlayerOnCompletionListener());
					}
				}
			}
		};

		e_alarm = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				dmsg req = new dmsg();
				if (AlarmLabel.intent == null && req.to("/talk/active", null) == 200) {
					dxml p = new dxml();
					p.parse(req.mBody);

					if (p.getInt("/params/data", 0) == 0) {
						AlarmLabel.intent = new Intent(security.this, AlarmLabel.class);
						AlarmLabel.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(AlarmLabel.intent);
					}
				}
			}
		};

		security.dBroadcast();
		CMS.d600SetZone();

		devent.boot = true;
	}

	public static void soundEvent(int s) {
		s_defence = s;
		if (e_defence != null)
			e_defence.sendMessage(e_defence.obtainMessage());
	}

	public static void alarmEvent() {
		if (e_alarm != null)
			e_alarm.sendMessage(e_alarm.obtainMessage());
	}

	public static void dBroadcast() {
		if (ctx != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.security.data");
			it.putExtra("defence", defence);
			ctx.sendBroadcast(it);
		}
	}

	public static void mBroadcast() {
		if (ctx != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.talk.smute");
			it.putExtra("data", 0);
			ctx.sendBroadcast(it);
		}
	}
}
