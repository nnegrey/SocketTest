package com.example.android.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by micha on 5/30/2016.
 */
public class MainActivity extends Activity implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "MainActivity";
    public static long TIME = 0;

    @BindView(R.id.serverButton)
    Button buttonServer;

    @BindView(R.id.clientButton)
    Button buttonClient;

    @BindView(R.id.responseTextView)
    TextView response;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private boolean weConnect = false;
    private BroadcastReceiver receiver = null;
    WifiP2pDevice device;
    private boolean checkPeers = true;
    public static long connectTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        buttonServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weConnect = false;
                discoverPeers();
            }
        });

        buttonClient.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                connectTime = System.currentTimeMillis();
                weConnect = true;
                discoverPeers();
            }
        });
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(TAG, "onConnectionInfoAvailable");

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            new ServerSocketAsyncTask(getBaseContext()).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Intent serviceIntent = new Intent(MainActivity.this, ClientSocketService.class);
            serviceIntent.setAction(ClientSocketService.ACTION_SEND_FILE);
            serviceIntent.putExtra(ClientSocketService.EXTRAS_DATA, "http://mwong56-thesis.herokuapp.com/");
            serviceIntent.putExtra(ClientSocketService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(ClientSocketService.EXTRAS_GROUP_OWNER_PORT, 8988);
            startService(serviceIntent);
        }

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

        if (!weConnect || !checkPeers) {
            Log.d(TAG, "Returnning from onPeersAvailable");
            return;
        }

        if (peers.getDeviceList().size() == 0) {
            Log.d(MainActivity.TAG, "No devices found");
            return;
        }

        for (WifiP2pDevice p : peers.getDeviceList()) {
            device = p;
            Log.d(TAG, "Found device: " + p.deviceName);
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = p.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            Toast.makeText(this, "Connecting to: " + p.deviceName, Toast.LENGTH_SHORT).show();
            connect(config);
            checkPeers = false;
            return;
        }

    }

    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d(TAG, "Manager: Connect success");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                checkPeers = true;
            }
        });
    }

    public void deviceDisconnected() {
        Toast.makeText(getBaseContext(), "Device disconnected.", Toast.LENGTH_SHORT).show();
        device = null;
        checkPeers = true;
    }
}
