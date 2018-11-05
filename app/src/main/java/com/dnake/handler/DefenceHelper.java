package com.dnake.handler;

import com.dnake.v700.security;
import com.dnake.v700.slaves;
import com.dnake.v700.sound;

public class DefenceHelper {

    public  static  boolean setDefence(int defence) {
        if(security.checkSecurityWithDefence(defence)) {
            security.setDefence(defence);
            slaves.setMarks(0x01);
            security.broadcastDefence();
            return true;
        } else {
            sound.play(sound.passwd_err, false);
            return false;
        }
    }

    public static void setWithdraw() {
        security.setDefence(security.WITHDRAW);
        slaves.setMarks(0x01);
        security.clearAlarm();
    }

}
