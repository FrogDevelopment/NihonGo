/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.Preferences;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

public class ParametersFragment extends Fragment {

    private static final String LOG_TAG = "NIHON_GO";

    @InjectView(R.id.options_erase)
    Button mButtonErase;

    public ParametersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

        ButterKnife.inject(this, view);
        return view;
    }

    @OnClick(R.id.options_erase)
    void onClickErase() {

        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.options_erase_confirmation)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

                        SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
                        final SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Preferences.PACKS.value, "");
                        editor.apply();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
