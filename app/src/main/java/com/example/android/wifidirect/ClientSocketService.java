// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class ClientSocketService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_DATA = "data";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String TAG = "ClientSocketService";


    public ClientSocketService() {
        super("ClientSocketService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String data = intent.getExtras().getString(EXTRAS_DATA);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(MainActivity.
                        TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(TAG, "Search and connect: " + (System.currentTimeMillis() - MainActivity.connectTime));

                MainActivity.TIME = System.currentTimeMillis();
                Log.d(MainActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                BleRequest response = new BleRequest.Builder().url(data).build();

                StreamUtils.sendBytes(response.toJsonString().getBytes(), outputStream);
                Log.d(MainActivity.TAG, "Client: Data written");

                BleResponse bleResponse = BleResponse.parseResponse(new String(StreamUtils.readBytes(inputStream)));
                Log.d(TAG, "received response: " + bleResponse.getBody().length());
                Log.d(TAG, "Time: " + (System.currentTimeMillis() - MainActivity.TIME));
                inputStream.close();
                socket.close();


//                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
