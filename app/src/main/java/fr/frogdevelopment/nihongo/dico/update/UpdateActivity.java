package fr.frogdevelopment.nihongo.dico.update;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import fr.frogdevelopment.nihongo.R;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.update_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, UpdateFragment.newInstance(getIntent().getExtras()))
                    .commitNow();
        }
    }
}
