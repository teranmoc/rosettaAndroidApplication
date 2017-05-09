package jeremie.ceri.m2.rosetta;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jérémie on 24/12/2016.
 */
public class SocketClient {
    private Socket socket = null;
    private String ip;
    private int port;
    private volatile int status;
    private volatile String msg;
    public SocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    public int connexion(final Context c)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v("DEBUG", "Connexion à : " + SocketClient.this.ip + ":" + SocketClient.this.port);
                    socket = new Socket(SocketClient.this.ip, SocketClient.this.port);
                    // emission.send();
                    if(!socket.isConnected())
                    {
                        Log.v("DEBUG", "bug connexion");
                        SocketClient.this.status = -1;
                    }
                    else {
                        SocketClient.this.status = 0;
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.v("DEBUG", "Erreur connexion");
                    SocketClient.this.status = -2;
                    //Toast.makeText(SocketClient.this.c, "Erreur de connexion ...", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        }).start();
        Log.v("DEBUG", "Valeur de status : " + this.status);
        return status;
    }
    public void sendOrder(String order) {
        DataOutputStream osw;
        try {
            osw = new DataOutputStream (this.socket.getOutputStream());
            osw.writeBytes(order);
        } catch (Exception e) {
            Log.v("DEBUG", "Catch send Order Socket Client");
        }
    }
    public String getResponseForRecognition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Log.v("DEBUG", "ici 1");
                    msg = "";
                    while(msg.isEmpty()) {
                        msg = in.readLine();
                        Log.v("DEBUG", "[Loop] msg : " + msg);
                    }
                    Log.v("DEBUG", "ici 2");
                    in.close();
                } catch (Exception e) {
                    Log.v("DEBUG", "catch getResponseForRecognition : " + e.getMessage());
                    e.printStackTrace();
                    msg = "Error receiving response:  " + e.getMessage();
                }
            }
        }).start();
        Log.v("DEBUG", "msg : " + this.msg);
        //return this.msg;
        return "ceci est un test";
    }
    public void closeSocket() {
        if(socket != null) {
            try {
                this.sendOrder("exit");     // il faut absolument envoyer "exit" au robot pour reset la connexion de son côté !
                socket.close();
            }
            catch(IOException e) {
            }
        }
    }
}
