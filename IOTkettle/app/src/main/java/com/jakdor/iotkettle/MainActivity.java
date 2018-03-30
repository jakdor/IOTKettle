package com.jakdor.iotkettle;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.app.NotificationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private IOTClient iotClient;
    private IOTHelper iotHelper;
    private SharedPreferences preferences;

    private TextView dummyTextView;
    private TextView timerDisplayTextView;
    private EditText ipTextEdit;

    private Bitmap notifyIcon;
    private Bitmap notifyIcon2;

    private String connectionString;
    private Context appContext;

    private int retryCounter = 0;
    private int notificationCounter = 0;

    private boolean timerFlag = false;
    private long timerStart = 0;
    private long timer = 0;

    /**
     * ChangeIP button listener
     */
    private final View.OnTouchListener changeIpButtonListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            String newIp = ipTextEdit.getText().toString();

            if(!connectionString.equals(newIp)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.ip_string), newIp);
                editor.apply();

                connectionString = newIp;
            }

            iotClient.kill();
            connect();

            notificationCounter = 0;
            iotHelper.changeIotClient(iotClient);

            return false;
        }
    };

    /**
     * Starts connection threat
     */
    private void connect(){
        dummyTextView.setText(R.string.status_connecting);
        iotClient = new IOTClient(connectionString);
        new Thread(iotClient).start();
    }

    /**
     * checks connection, restarts if needed
     */
    private void checkConnection(){
        if(!iotClient.isConnectionOK()){
            ++retryCounter;
            dummyTextView.setText(R.string.status_no_connection);
            if(retryCounter > 5) {
                iotClient.kill();
                connect();
                iotHelper.changeIotClient(iotClient);
                retryCounter = 0;
            }
        }
        else if(notificationCounter == 0){
            dummyTextView.setText(R.string.status_text);
        }
    }

    /**
     * GUI response for received data
     */
    private void receive(){
        if(iotHelper.isNotifyFlag()){
            iotHelper.notifyHandled();

            String received = iotHelper.getReceived();

            if(received.equals("start")){
                sendNotification("Czajnik uruchomiony", getTime(), false);
                dummyTextView.setText(R.string.status_working);
                timerFlag = true;
            }
            else if(received.equals("stop1")){
                sendNotification("Czajnik wyłączony", getTime(), true);
                dummyTextView.setText(R.string.status_ended);
                timerFlag = false;
            }
        }
    }

    /**
     * Elapsed time counter
     */
    private void timeCounter(){
        if(timerFlag){
            if(timerStart == 0){
                timerStart = System.nanoTime();
            }
            else{
                timer = System.nanoTime() - timerStart;
                timer = TimeUnit.SECONDS.convert(timer, TimeUnit.NANOSECONDS);
            }

            displayTimer();
        }
        else if(timer > 0){
            if(timerStart != 0) {
                timer = System.nanoTime() - timerStart;
                timer = TimeUnit.SECONDS.convert(timer, TimeUnit.NANOSECONDS);
                displayTimer();

                timerStart = 0;
            }
        }
    }

    /**
     * Displays elapsed time
     */
    private void displayTimer(){
        if(timer%60 < 10) {
            timerDisplayTextView.setText(String.format(
                    Locale.ENGLISH, "Czas działania: %1$d:0%2$d", timer/60, timer%60));
        }
        else{
            timerDisplayTextView.setText(String.format(
                    Locale.ENGLISH, "Czas działania: %1$d:%2$d", timer/60, timer%60));
        }
    }

    /**
     * Main loop, change check every 1000ms
     */
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            checkConnection();
            receive();
            timeCounter();

            timerHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Builds and displays Android notification
     */
    private void sendNotification(String title, String text, boolean type) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        if (type){
            long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
            mBuilder.setVibrate(pattern);
            mBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert);
            mBuilder.setLights(Color.RED, 500, 500);
            mBuilder.setLargeIcon(notifyIcon2);
        }
        else {
            mBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
            mBuilder.setLights(Color.BLUE, 500, 500);
            mBuilder.setLargeIcon(notifyIcon);
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationCounter++, mBuilder.build());
    }

    /**
     * Format current time for notifications
     */
    private String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = this;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        connectionString = preferences.getString(getString(R.string.ip_string), "192.168.1.188");

        dummyTextView = (TextView) findViewById(R.id.textView);
        timerDisplayTextView = (TextView) findViewById(R.id.timerDisplayTextView);
        ipTextEdit = (EditText) findViewById(R.id.editText);
        ipTextEdit.setText(connectionString);

        findViewById(R.id.button).setOnTouchListener(changeIpButtonListener);

        notifyIcon = BitmapFactory.decodeResource(getResources(), R.drawable.kettler);
        notifyIcon2 = BitmapFactory.decodeResource(getResources(), R.drawable.kettler2);

        connect();

        iotHelper = new IOTHelper(iotClient);
        new Thread(iotHelper).start();

        timerHandler.postDelayed(timerRunnable, 0);
    }
}
