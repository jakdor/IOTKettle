package com.jakdor.iotkettle;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles connection between app and device
 */
class IOTClient extends Thread {

    private String receiveMessage, connectIP;

    private Socket sock;
    private PrintWriter printWriter;
    private BufferedReader receiveRead;

    private boolean connectionOK = false;

    IOTClient(String connectIP){
        this.connectIP = connectIP;
    }

    @Override
    public void run() {
            this.connectionOK = connect();
            super.run();
    }

    /**
     * Opens connection socket
     */
    private boolean connect(){
        try {
            this.sock = new Socket(this.connectIP, 8889);

            OutputStream ostream = sock.getOutputStream();
            this.printWriter = new PrintWriter(ostream, true);

            InputStream istream = sock.getInputStream();
            this.receiveRead = new BufferedReader(new InputStreamReader(istream));
            return true;
        }
        catch (Exception e){
            Log.e("Exception", "Client connection problem: " + e.toString());
        }

        return false;
    }

    /**
     * Incoming data listener
     */
    String receive(){
        try {
            if ((this.receiveMessage = this.receiveRead.readLine()) != null)
            {
                return this.receiveMessage;
            }
        }
        catch (Exception e){
            Log.e("Exception", "Client receive problem: " + e.toString());
        }
        return null;
    }

    /**
     * Send data method
     */
    void send(String input){
        try {
            this.printWriter.println(input);
            this.printWriter.flush();
        }
        catch (Exception e){
            Log.e("Exception", "Client send problem: " + e.toString());
        }
    }

    boolean isConnectionOK(){
        return connectionOK;
    }

    /**
     * closes connection
     */
    void kill(){
        try {
            this.sock.close();
        }
        catch (Exception e){
            Log.e("Exception", "Client can't close connection: " + e.toString());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.kill();
        super.finalize();
    }
}