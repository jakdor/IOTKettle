package com.jakdor.iotkettle;

import android.util.Log;

/**
 * Helper class, keeps connection up despite app state
 */
class IOTHelper extends Thread{

    private IOTClient iotClient;

    private String received;
    private boolean notifyFlag = false;

    IOTHelper(IOTClient iotClient){
        this.iotClient = iotClient;
    }

    @Override
    public void run() {
        while (true){
            if(iotClient.isConnectionOK()) {
                received = iotClient.receive();
                if (received != null) {
                    notifyFlag = true;
                }
            }

            try{
                Thread.sleep(10);
            }catch (Exception e){
                Log.e("Exception", e.getMessage());
            }
        }
    }

    void changeIotClient(IOTClient iotClient){
        this.iotClient = iotClient;
    }

    void notifyHandled(){
        notifyFlag = false;
    }

    boolean isNotifyFlag() {
        return notifyFlag;
    }

    String getReceived() {
        return received;
    }
}
