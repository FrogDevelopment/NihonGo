package fr.frogdevelopment.nihongo.options;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

// fixme utiliser les préférences XML ? http://developer.android.com/guide/topics/ui/settings.html
public class ParametersFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

        Button eraseButton = view.findViewById(R.id.options_erase);
        eraseButton.setOnClickListener(v -> onClickErase());
        Button favoriteButton = view.findViewById(R.id.options_reset_favorite);
        favoriteButton.setOnClickListener(v -> onClickResetFavorite());
        Button learnedButton = view.findViewById(R.id.options_reset_learned);
        learnedButton.setOnClickListener(v -> onClickResetLearned());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void onClickErase() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.options_erase_data_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    requireActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

                    PreferencesHelper.getInstance(requireActivity()).saveString(Preferences.LESSONS, "");

                    Snackbar.make(requireActivity().findViewById(R.id.parameters_layout), R.string.options_erase_data_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void onClickResetFavorite() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.options_reset_favorite_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    final ContentValues values = new ContentValues();
                    values.put(DicoContract.BOOKMARK, "0");
                    requireActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_FAVORITE, values, null, null);

                    Snackbar.make(requireActivity().findViewById(R.id.parameters_layout), R.string.options_reset_favorite_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void onClickResetLearned() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.options_reset_learned_erase_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    final ContentValues values = new ContentValues();
                    values.put(DicoContract.LEARNED, "0");
                    requireActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_LEARNED, values, null, null);

                    Snackbar.make(requireActivity().findViewById(R.id.parameters_layout), R.string.options_reset_learned_erase_success, Snackbar.LENGTH_LONG).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }
}
