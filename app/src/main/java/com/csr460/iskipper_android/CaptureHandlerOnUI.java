package com.csr460.iskipper_android;

import android.app.Activity;

import com.csr460.iSkipper.device.ReceivedPacketEvent;
import com.csr460.iSkipper.handler.CaptureHandler;
import com.csr460.iSkipper.support.Answer;
import com.csr460.iSkipper.support.AnswerPacket;
import com.csr460.iSkipper.support.AnswerPacketHashMap;
import com.csr460.iSkipper.support.Transcoding;

public class CaptureHandlerOnUI extends CaptureHandler {

    Activity activity;

    public CaptureHandlerOnUI(AnswerPacketHashMap hashMap, Activity mainActivity){
        super(hashMap, false, false);
        activity = mainActivity;
    }

    @Override
    public void onReceivedPacketEvent(ReceivedPacketEvent packetEvent) {
        super.onReceivedPacketEvent(packetEvent);
        AnswerPacketHashMap.AnswerStats stats = hashMap.getAnswerStats();
        activity.runOnUiThread(()->{
            MainActivity.showStatis(stats.getNumsA(), stats.getNumsB(), stats.getNumsC(), stats.getNumsD(), stats.getNumsE(), stats.getIDCount());
        });
    }
}
