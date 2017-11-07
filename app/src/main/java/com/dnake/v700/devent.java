package com.dnake.v700;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.dnake.special.NowIP;
import com.dnake.special.ipc;

public class devent {
	private static List<devent> elist = null;
	public static Boolean boot = false;

	public String url;

	public devent(String url) {
		this.url = url;
	}

	public void process(String xml) {
	}

	public static void event(String url, String xml) {
		Boolean err = true;
		if (boot && elist != null) {
			devent e;

			Iterator<devent> it = elist.iterator();
			while (it.hasNext()) {
				e = it.next();
				if (url.equals(e.url)) {
					e.process(xml);
					err = false;
					break;
				}
			}
		}
		if (err)
			dmsg.ack(480, null);
	}

	public static void setup() {
		elist = new LinkedList<devent>();

		devent de;
		de = new devent("/security/run") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/security/version") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				String v = String.valueOf(sys.version_major)+"."+sys.version_minor+"."+sys.version_minor2;
				v = v+" "+sys.version_date+" "+sys.version_ex;
				p.setText("/params/version", v);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/security/io") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				int mcu = p.getInt("/params/mcu", 0);

				Log.d("security=>io",body);

                if (mcu == 1) {
                	if(security.zone!=null) {
						for (int i = 0; i < 8; i++) {
							if (security.zone[i] != null) {
								int status = p.getInt("/params/io" + i, 0x10);
								if (status != 0x10) {
									security.zone[i].currentStatus = status;
								}
							}
						}
					}
                }

                if (security.defence == security.WITHDRAW)
                    return;

				if (security.defenceStart == 0 || Math.abs(System.currentTimeMillis() - security.defenceStart) < security.timeout * 1000) {
					return;
				}

				if (mcu != 0 && sys.talk.dcode != 0) {
					//副机IO报警，直接同步到主分机
					dmsg req = new dmsg();
					p.setInt("/params/mcu", 0);
					req.to("/talk/slave/mcu_io", p.toString());
				} else {
					// io状态:  0: 正常状态    1:断开    2:闭合    0x10: 状态未发生变化
					int io[] = new int[8];
					Boolean bell = false;
					for(int i=0; i<8; i++) {
						io[i] = p.getInt("/params/io"+i, 0x10);
						if (security.zone[i].mode == security.M_NO && io[i] == 0x01) //常开，io=1为正常状态
							io[i] = 0x00;
						else if (security.zone[i].mode == security.M_NC && io[i] == 0x02) //常闭，io=2为正常状态
							io[i] = 0x00;
						else if (security.zone[i].mode == security.M_BELL && io[i] != 0x00 && io[i] != 0x10) {
							io[i] = 0x00;
							bell = true;
						}
					}
					security.process(io, 8);

					if (bell && Math.abs(System.currentTimeMillis()-sys.bell) >= 4000) {
						sys.bell = System.currentTimeMillis();
						sound.play(sound.bell, false);
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/security/conf") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				security.load(p);
			}
		};
		elist.add(de);

		de = new devent("/security/defence") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.parse(body);
				int d = p.getInt("/params/defence", -1);
				if (d != -1)
					security.setDefence(d);

				dxml p2 = new dxml();
				p2.setInt("/params/defence", security.defence);
				dmsg.ack(200, p2.toString());
			}
		};
		elist.add(de);

		de = new devent("/security/set_id") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				sys.load();
			}
		};
		elist.add(de);

		de = new devent("/security/slaves") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				slaves.load(p);
			}
		};
		elist.add(de);

		de = new devent("/security/slave/device") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				slaves.load(p);
			}
		};
		elist.add(de);

		de = new devent("/security/alarm") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				if (security.mHave)
					p.setInt("/params/have", 1);
				else
					p.setInt("/params/have", 0);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/security/broadcast/data") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				int build = p.getInt("/event/build", 0);
				int unit = p.getInt("/event/unit", 0);
				int floor = p.getInt("/event/floor", 0);
				int family = p.getInt("/event/family", 0);
				int mode = p.getInt("/event/mode", 0);
				if (sys.talk.dcode == 0 &&
				  security.mHave == false &&
				  build == sys.talk.building &&
				  unit == sys.talk.unit &&
				  floor == sys.talk.floor &&
				  family == sys.talk.family) {
					if (utils.eHome) {
						//中华电信eHome模式，不处理我们自己的流程。
						if (mode == 2) //小门口机
							utils.eHomeCard(security.ctx, p.getText("/event/card"));
						return;
					}

					if (mode == 2) { //小门口机
						if (security.defence == security.WITHDRAW)
							security.setDefence(security.OUT);
						else
							security.setDefence(security.WITHDRAW);
						security.save();
						security.nBroadcast();
					} else { //大门口机、围墙机只撤防
						if (security.defence != security.WITHDRAW) {
							security.setDefence(security.WITHDRAW);
							security.save();
							security.nBroadcast();
						}
					}
					slaves.setMarks(0x01);
				}
			}
		};
		elist.add(de);

		de = new devent("/security/web/nowip/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setInt("/params/enable", NowIP.enable);
				p.setText("/params/to", NowIP.to);
				p.setText("/params/to2", NowIP.to2);
				p.setInt("/params/to2_delay", NowIP.to2_delay);
				p.setInt("/params/auto_call", NowIP.auto_call);
				p.setText("/params/call_url", NowIP.call_url);
				p.setInt("/params/heartbeat_en", NowIP.heartbeat_en);
				p.setText("/params/heartbeat_data", NowIP.heartbeat_data);
				p.setInt("/params/timeout", NowIP.timeout);
				for(int i=0; i<security.MAX; i++)
					p.setText("/params/zone"+i+"/d", NowIP.alarm_data[i]);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/security/web/nowip/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				NowIP.enable = p.getInt("/params/enable", NowIP.enable);
				NowIP.to = p.getText("/params/to", NowIP.to);
				NowIP.to2 = p.getText("/params/to2", NowIP.to2);
				NowIP.to2_delay = p.getInt("/params/to2_delay", NowIP.to2_delay);
				NowIP.auto_call = p.getInt("/params/auto_call", NowIP.auto_call);
				NowIP.call_url = p.getText("/params/call_url", NowIP.call_url);
				NowIP.heartbeat_en = p.getInt("/params/heartbeat_en", NowIP.heartbeat_en);
				NowIP.heartbeat_data = p.getText("/params/heartbeat_data", NowIP.heartbeat_data);
				NowIP.timeout = p.getInt("/params/timeout", NowIP.timeout);
				for(int i=0; i<security.MAX; i++)
					NowIP.alarm_data[i] = p.getText("/params/zone"+i+"/d", NowIP.alarm_data[i]);

				if (NowIP.timeout < 10)
					NowIP.timeout = 10;

				NowIP.save();
			}
		};
		elist.add(de);

		de = new devent("/security/web/ipc/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setInt("/params/max", ipc.idx);
				for(int i=0; i<ipc.idx; i++) {
					p.setText("/params/r"+i+"/name", ipc.name[i]);
					p.setText("/params/r"+i+"/url", ipc.rtsp[i]);
				}
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/security/web/ipc/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				ipc.idx = p.getInt("/params/max", 0);
				for(int i=0; i<ipc.idx; i++) {
					ipc.name[i] = p.getText("/params/r"+i+"/name");
					ipc.rtsp[i] = p.getText("/params/r"+i+"/url");
				}
				ipc.save();
			}
		};
		elist.add(de);
	}
}
