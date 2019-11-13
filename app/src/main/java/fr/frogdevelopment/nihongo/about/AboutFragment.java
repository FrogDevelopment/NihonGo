package fr.frogdevelopment.nihongo.about;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import fr.frogdevelopment.nihongo.BuildConfig;
import fr.frogdevelopment.nihongo.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView version = rootView.findViewById(R.id.about_version);
        version.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));

        MovementMethod instance = LinkMovementMethod.getInstance();

        TextView hiragana = rootView.findViewById(R.id.about_hiragana_link);
        hiragana.setMovementMethod(instance);

        TextView katakana = rootView.findViewById(R.id.about_katakana_link);
        katakana.setMovementMethod(instance);

        return rootView;
    }

}
