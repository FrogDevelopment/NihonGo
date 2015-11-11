/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.dialog.TagsDialog;

public class TestParametersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TagsDialog.TagDialogListener {

    private static final int LOADER_ID = 700;

    static final String TYPE_TEST     = "isTestJapanese";
    static final String QUANTITY      = "count";
    static final String DISPLAY_KANJI = "isDisplayKanji";

    @Bind(R.id.test_param_type_selection)
    TextView mTypeSelected;

    @Bind(R.id.test_param_method_selection)
    TextView mMethodSelected;

    @Bind(R.id.test_param_quantity_selection)
    TextView mQuantitySelected;

    @Bind(R.id.test_param_kanji)
    Switch mKanjiSwitch;

    @Bind(R.id.test_param_tag_selection)
    TextView mTagSelected;

    @Bind(R.id.test_button_start)
    Button startButton;

    private int          selectedType     = -1;
    private int          selectedMethod   = -1;
    private String       selectedQuantity = null;
    private List<String> items            = new ArrayList<>();
    private ArrayList<Integer> mSelectedItems;
    private String[]           mSelectedTags;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_parameters, container, false);

        ButterKnife.bind(this, rootView);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(NihonGoContentProvider.URI_WORD + "/TAGS");
        return new CursorLoader(getActivity(), uri, new String[]{DicoContract.TAGS}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Set<String> uniqueItems = new HashSet<>();
        while (data.moveToNext()) {
            String row = data.getString(0);
            String[] tags = row.split(",");
            uniqueItems.addAll(Arrays.asList(tags));
        }

        items = new ArrayList<>(uniqueItems);

        Collections.sort(items);

        data.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @OnClick(R.id.test_param_type)
    public void onClickType(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_type_selection)
                .setItems(R.array.param_types, (dialog, which) -> {
                    selectedType = which;
                    mTypeSelected.setText(getResources().getStringArray(R.array.param_types)[which]);

                    boolean displayKanji = selectedType > 1;
                    if (displayKanji) {
                        mKanjiSwitch.setVisibility(View.VISIBLE);
                        mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);
                    } else {
                        mKanjiSwitch.setVisibility(View.INVISIBLE);
                    }
                    startButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity != null);
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_method)
    public void onClickMethod(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_method_selection)
                .setItems(R.array.param_methods, (dialog, which) -> {
                    selectedMethod = which;
                    mMethodSelected.setText(getResources().getStringArray(R.array.param_methods)[which]);

                    mKanjiSwitch.setText(selectedMethod == 1 && selectedType == 3 ? R.string.param_kanji_write : R.string.param_kanji_display);

                    startButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity != null);
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_quantity)
    void onClickQuantity(View v) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.param_quantity_selection)
                .setItems(R.array.param_quantities, (dialog, which) -> {
                    selectedQuantity = getResources().getStringArray(R.array.param_quantities)[which];
                    mQuantitySelected.setText(selectedQuantity);

                    startButton.setEnabled(selectedType > -1 && selectedMethod > -1 && selectedQuantity != null);
                })
                .create()
                .show();
    }

    @OnClick(R.id.test_param_tag)
    void onClickTag() {
        TagsDialog.show(getFragmentManager(), this, items, mSelectedItems);
    }

    @Override
    public void onReturnValue(ArrayList<Integer> selectedItems) {
        mSelectedItems = selectedItems;
        mSelectedTags = null;

        for (Integer selectedIndex : mSelectedItems) {
            String selectedTag = items.get(selectedIndex);
            mSelectedTags = ArrayUtils.add(mSelectedTags, selectedTag);
        }

        mTagSelected.setText(StringUtils.join(mSelectedTags, ", "));
    }

    @OnClick(R.id.test_button_start)
    void onClickButtonStart() {
        Intent intent;
        switch (selectedMethod) {
            case 0:
                intent = new Intent(getActivity(), TestSelectActivity.class);
                break;

            case 1:
                intent = new Intent(getActivity(), TestInputActivity.class);
                break;

            default:
                // fixme
                throw new IllegalStateException("fixme");
        }

        Bundle options = new Bundle();
        options.putInt(TYPE_TEST, selectedType);
        options.putInt(QUANTITY, Integer.valueOf(selectedQuantity));
        options.putBoolean(DISPLAY_KANJI, mKanjiSwitch.isChecked());
        options.putStringArray("tags", mSelectedTags);
        intent.putExtras(options);

        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
