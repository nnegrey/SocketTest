package com.example.android.wifidirect;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by micha on 5/30/2016.
 */
public class ServerSocketAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = ServerSocketAsyncTask.class.getSimpleName();
    private final Context context;

    public ServerSocketAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(TAG, "Server: connection done");


            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client -- should be URL request.
             */
            OutputStream outputStream = client.getOutputStream();
            InputStream inputStream = client.getInputStream();

            String request = new String(StreamUtils.readBytes(inputStream));
            Log.d(TAG, "Request: " + request);

            // Get HTTP Response
            OkHttpClient httpClient = new OkHttpClient();
            Request httpRequest = new Request.Builder().url(request).build();
            Response httpResponse = httpClient.newCall(httpRequest).execute();
            Log.d(TAG, "Http Response: " + httpResponse.body().string());

            // Write back to client
            StreamUtils.sendBytes(httpResponse.body().toString().getBytes(), outputStream);
            outputStream.close();
            // Close serverSocket
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
