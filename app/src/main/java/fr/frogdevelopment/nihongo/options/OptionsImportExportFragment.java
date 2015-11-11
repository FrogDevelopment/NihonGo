/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;

public class OptionsImportExportFragment extends Fragment {

    private static final String LOG_TAG = "NIHON_GO";

    private static final int FLAG_EXPORT = 0;
    private static final int FLAG_IMPORT = 1;

    @Bind(R.id.options_export_name)
    EditText mNameExportView;
    @Bind(R.id.options_export_select)
    Button   mButtonExport;
    @Bind(R.id.options_erase)
    Button   mButtonErase;

    public OptionsImportExportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options_import_export, container, false);

        ButterKnife.bind(this, view);

        // First of all we check if the external storage of the device is available for writing.
        // Remember that the external storage is not necessarily the sd card. Very often it is the device storage.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(getActivity(), "Sauvegarde impossible sur votre appareil", Toast.LENGTH_LONG).show();
//            return false; fixme
        }

        return view;
    }

    @OnClick({R.id.options_import, R.id.options_export_select})
    void onClickSelect(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            intent.setDataAndType(uri, "file/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.options_file_chooser_prompt)), view.getId() == R.id.options_import ? FLAG_IMPORT : FLAG_EXPORT);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(getActivity(), R.string.options_file_chooser_get_file_manager, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "Problème", Toast.LENGTH_LONG).show();
            return;
        }

        if (data != null) {
            // Get the path
            final String path = getPath(getActivity(), data.getData());
            File file = new File(path);

            switch (requestCode) {
                case FLAG_IMPORT:
                    importDataBase(file);
                    break;

                case FLAG_EXPORT:
                    exportDataBase(file);
                    break;

                default:
                    break;
            }
        }
    }

    @OnTextChanged(value = R.id.options_export_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onAfterTextChanged(Editable editable) {
        mButtonExport.setEnabled(editable.length() > 5);
    }

    @OnClick(R.id.options_export)
    void onClickExport() {
        String fileName = mNameExportView.getText().toString();

        exportDataBase(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));
    }

    private void exportDataBase(File exportFile) {
//        if (getLocalContentProvider().exportDatabase(exportFile)) {
//            Toast.makeText(getActivity(), getString(R.string.options_export_success, exportFile.getName()), Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(getActivity(), R.string.options_export_error, Toast.LENGTH_LONG).show();
//        }
    }

    private void importDataBase(File importFile) {
//        if (getLocalContentProvider().importDatabase(importFile)) {
//            Toast.makeText(getActivity(), R.string.options_import_success, Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(getActivity(), R.string.options_import_failed, Toast.LENGTH_LONG).show();
//        }
    }

    private NihonGoContentProvider getLocalContentProvider() {
        return (NihonGoContentProvider) getActivity().getContentResolver().acquireContentProviderClient(".NihonGoContentProvider").getLocalContentProvider();
    }

    private static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString(), e);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @OnClick(R.id.options_erase)
    void onClickErase() {
        getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);
    }
}
