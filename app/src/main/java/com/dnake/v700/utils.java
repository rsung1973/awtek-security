package com.dnake.v700;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.content.Intent;

public class utils {

	public static Boolean eHome = false;

	public static String getLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
						return inetAddress.getHostAddress().toString();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLocalMac() {
		String mac_s = "";
		try {
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(utils.getLocalIp()));
			if (ne != null) {
				byte[] mac = ne.getHardwareAddress();
				if (mac != null)
					mac_s = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}

	public static void eHomeCard(Context ctx, String card) {
		Intent it = new Intent("com.dnake.broadcast");
		it.putExtra("event", "com.dnake.eHome.card");
		it.putExtra("card", card);
		ctx.sendBroadcast(it);
	}
}
