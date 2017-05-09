package jeremie.ceri.m2.rosetta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    SocketClient c;
    SharedPreferences sp;
    Button recoBtn;

    static String _ipRob = "";
    static int _portRob = -1;
    static int _portStream = -1;

    //Evite la surcharge des sockets
    static String lastMsg = "";


    //Vidéo
    WebView webView;
    String url;

    //JoyStick
    RelativeLayout layout_joystick;
    JoyStickClass js;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.recoBtn = (Button) findViewById(R.id.recoBtn);
        // lecture des préférences stockées dans un SharedPreferences
        this.sp = getBaseContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        if(!this.sp.contains("ipRob") || !this.sp.contains("portRob")) {  // s'ils n'existent pas, on en crée par défaut
            this.sp.edit()
                    .putString("ipRob", "192.168.2.50")              // @IP du robot sur son réseau Wifi
                    .putInt("portRob",2155)                          // port de commandes
                    .putInt("portStream", 8080)                      // port du stream vidéo
                    .apply();
        }

        // Connexion à la Raspberry Pi
        this.c = new SocketClient(sp.getString("ipRob", "192.168.2.50"), sp.getInt("portRob", 2155));
        if(this.c.connexion(this) != 0) {
            Toast.makeText(this, "L'application n'est pas connectée au robot", Toast.LENGTH_SHORT).show();
        }

        //Récupération du flux vidéo et construction de l'URL http://IP:PORT/stream
        url = "http://" + sp.getString("ipRob", "192.168.2.50") + ":"+sp.getInt("portStream", 8080)+"/stream";
        webView = (WebView)findViewById(R.id.webview);
        webView.setInitialScale(100);
        webView.post(new Runnable()
        {
            @Override
            public void run() {
                url += "?width="+webView.getWidth()+"&height="+webView.getHeight();
                webView.loadUrl(url);
            }
        });
        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext(), layout_joystick, R.drawable.ic_button_bg);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(25);

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction() == MotionEvent.ACTION_MOVE)
                {
                    if(js.get8Direction() == JoyStickClass.STICK_NONE)
                    {
                        if(lastMsg.equals("stop")) return true;
                        MainActivity.this.sendOrder("stop");
                        return true;
                    }
                    double tv =  js.getDistance()/1.5;
                    String v="N";
                    switch (js.get8Direction())
                    {
                        case JoyStickClass.STICK_UP:        v="A" ;break;
                        case JoyStickClass.STICK_UPRIGHT:   v="AD";break;
                        case JoyStickClass.STICK_RIGHT:     v="D" ;break;
                        case JoyStickClass.STICK_DOWNRIGHT: v="RD";break;
                        case JoyStickClass.STICK_DOWN:      v="R" ;break;
                        case JoyStickClass.STICK_DOWNLEFT:  v="RG";break;
                        case JoyStickClass.STICK_LEFT:      v="G" ;break;
                        case JoyStickClass.STICK_UPLEFT:    v="AG";break;
                        default:
                    }
                    String msg = v+Integer.toString(tv <= 100 ? new Double(tv).intValue() : 100);
                    if(msg.equals(lastMsg)) return true;
                    MainActivity.this.sendOrder(msg);
                    lastMsg = msg;

                    TextView com = (TextView) findViewById(R.id.comMsg);
                    com.setText(msg);
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    if(lastMsg.equals("stop")) return true;
                    MainActivity.this.sendOrder("stop");
                }
                return true;
            }
        });

        this.recoBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.v("DEBUG", "Envoi de l'ordre capture");
                MainActivity.this.c.sendOrder("capture");

                // attente du retour
                String ret = MainActivity.this.c.getResponseForRecognition();
                Log.v("DEBUG", "Retour : " + ret);

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //gère le clic sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch (item.getItemId()){
            case R.id.setting:  // Appel de la fenêtre de configuration de l'application
                i = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(i, 4);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Traitement du retour des activités
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1) {   // le bouton Config a été actionné
            Log.v("DEBUG", "Nouvelles valeurs détectées, on se reconnecte au robot");
            this.c.closeSocket();
            this.c = new SocketClient(sp.getString("ipRob", "192.168.2.50"), sp.getInt("portRob", 2155));
            if(this.c.connexion(this) != 0) {
                Toast.makeText(this, "L'application n'est pas connectée au robot", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.v("DEBUG", "resultCode : " + resultCode);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.c.closeSocket();
    }
    private void sendOrder(String order) {
        this.c.sendOrder(order);
    }
}