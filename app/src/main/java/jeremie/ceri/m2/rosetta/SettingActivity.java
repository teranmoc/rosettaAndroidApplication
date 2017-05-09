package jeremie.ceri.m2.rosetta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Jérémie on 03/01/2017.
 */
public class SettingActivity extends AppCompatActivity {
    SharedPreferences sp;
    EditText ipRob;
    EditText portRob;
    EditText portStream;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_view);
        this.ipRob = (EditText) findViewById(R.id.ipRob);
        this.portRob = (EditText) findViewById(R.id.portRob);
        this.portStream = (EditText) findViewById(R.id.portStream);
        this.sp = getBaseContext().getSharedPreferences("PREFS", MODE_PRIVATE);

        this.ipRob.setText(this.sp.getString("ipRob", "192.168.2.50"));
        this.portRob.setText(String.valueOf(this.sp.getInt("portRob", 2155)));
        this.portStream.setText(String.valueOf(this.sp.getInt("portStream", 8080)));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);      // la configuration utilise le même menu que l'ajout d'une ville
        return true;
    }
    //gère le clic sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:     // Sauvegarde
                // on vérifie si la config a changé
                this.sp.edit()
                        .putString("ipRob", this.ipRob.getText().toString())
                        .putInt("portRob", Integer.parseInt(this.portRob.getText().toString()))
                        .putInt("portStream", Integer.parseInt(this.portStream.getText().toString()))
                        .apply();//*/
                Toast.makeText(this, "La nouvelle configuration a bien été sauvegardée. ", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                setResult(1, i);
                SettingActivity.this.finish();
                return true;
            case R.id.back_main:
                SettingActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
