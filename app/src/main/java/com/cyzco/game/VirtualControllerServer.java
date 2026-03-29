package com.cyzco.game;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import android.app.GameManager;
import android.app.GameState;
import android.util.Log;

import java.net.SocketException;
import java.nio.channels.Selector;

public class VirtualControllerServer
{
    // Server constant variables
    private final String HOST = "0.0.0.0";
    private final int PORT = 12345;
    private final int UPDATE_RATE = 1/120;
    private final Socket socket;
    private final Selector selector;
    
    // Server variables
    private static final String TAG = "VirtualControllerServer";
    boolean connected = false;

    // Constructor
    public VirtualControllerServer(Socket socket, Selector selector)
    {
        this.socket = socket;
        this.selector = selector;
        connected = true;
        Log.d("VirtualControllerServer", "VirtualControllerServer: connection = True ::: " + connected);
    }


    private void readClient(Socket socket, Selector selector)
    {
        try
        {
            String data = socket.getOutputStream().toString();
            String[] splitData = data.split("\n");

        }

        catch (ConnectException e) {
            connected = false;
        }

        catch (Exception e) {
            connected = false;
        }
    }

}