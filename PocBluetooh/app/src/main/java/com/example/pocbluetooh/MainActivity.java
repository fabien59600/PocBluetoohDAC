package com.example.pocbluetooh;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
    public ArrayAdapter mesNewDevicesArrayAdapter;

    public void sendData(BluetoothSocket socket, int data) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream(4);
        output.write(data)
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(output.toByteArray());
    }

    public int receiveData(BluetoothSocket socket) throws IOException{
        byte[] buffer = new byte[4];
        ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        InputStream inputStream = socket.getInputStream();
        inputStream.read(buffer);
        return input.read();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mesNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Enregistrement du filtre "BroadcastReceiver" qd la découverte est terminée
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mesNewDevicesArrayAdapter); // Association du "ArrayAdapter"

        if (!blueAdapter.isEnabled()) {
            blueAdapter.enable();
        }
        int i=0;

            doDiscovery();
            newDevicesListView.setAdapter(mesNewDevicesArrayAdapter);
        

    }

    private void doDiscovery()
    {
// Modifier le titre pour indiquer la phase "scanning"
        setTitle("Scan Bluetooth ...");
// Stopper la découverte si elle est déjà en cours
        if (blueAdapter.isDiscovering()) blueAdapter.cancelDiscovery();
// Démarrer la découverte des périphériques Bluetooth
        blueAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction(); // Lecture du message reçu depuis l'adaptateur BT
// Identification du message
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {// Quand un nouveau périphérique est découvert :
// Affectation du "BluetoothDevice" depuis l'Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mesNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            }
// Nouveau titre à la fin de la découverte
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                setTitle("Sélectionner le péripherique");
                if (mesNewDevicesArrayAdapter.getCount() == 0)
                {// Aucun périphérique découvert
                    mesNewDevicesArrayAdapter.add("Pas de péripherique trouvé");
                }
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Arrêt de la découverte
        if (blueAdapter!=null) blueAdapter.cancelDiscovery();
        // Elimination du "BroadcastReceiver"
        this.unregisterReceiver(mReceiver);
        //Arret du bluetooh
        blueAdapter.disable();
    }

}

class ConnectThread extends Thread{

    private final BluetoothDevice bTDevice;
    private final BluetoothSocket bTSocket;

    public ConnectThread(BluetoothDevice bTDevice, UUID UUID) {
        BluetoothSocket tmp = null;
        this.bTDevice = bTDevice;

        try {
            tmp = this.bTDevice.createRfcommSocketToServiceRecord(UUID);
        }
        catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not start listening for RFCOMM");
        }
        bTSocket = tmp;
    }

    public boolean connect() {

        try {
            bTSocket.connect();
        } catch(IOException e) {
            Log.d("CONNECTTHREAD","Could not connect: " + e.toString());
            try {
                bTSocket.close();
            } catch(IOException close) {
                Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }

    public boolean cancel() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }

}

class ServerConnectThread extends Thread{
    private BluetoothSocket bTSocket;

    public ServerConnectThread() { }

    public void acceptConnect(BluetoothAdapter bTAdapter, UUID mUUID) {
        BluetoothServerSocket temp = null;
        try {
            temp = bTAdapter.listenUsingRfcommWithServiceRecord("Service_Name", mUUID);
        } catch(IOException e) {
            Log.d("SERVERCONNECT", "Could not get a BluetoothServerSocket:" + e.toString());
        }
        while(true) {
            try {
                bTSocket = temp.accept();
            } catch (IOException e) {
                Log.d("SERVERCONNECT", "Could not accept an incoming connection.");
                break;
            }
            if (bTSocket != null) {
                try {
                    temp.close();
                } catch (IOException e) {
                    Log.d("SERVERCONNECT", "Could not close ServerSocket:" + e.toString());
                }
                break;
            }
        }
    }

    public void closeConnect() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            Log.d("SERVERCONNECT", "Could not close connection:" + e.toString());
        }
    }
}