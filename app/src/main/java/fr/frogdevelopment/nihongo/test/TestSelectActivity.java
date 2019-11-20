package fr.frogdevelopment.nihongo.test;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.model.Details;
import fr.frogdevelopment.nihongo.data.model.Row;

public class TestSelectActivity extends TestAbstractActivity {

    private static final int LOADER_ID_ITEMS_QCM = 720;

    private TextView mToFindView;
    private LinearLayout answers;

    private List<Details> itemsQCM = new ArrayList<>();

    public TestSelectActivity() {
        super(R.layout.activity_test_select);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToFindView = findViewById(R.id.test_select_to_find);
        answers = findViewById(R.id.test_select_answers);

        for (int i = 0; i < nbAnswer; i++) {
            Button button = new Button(this);
            answers.addView(button);
            button.setOnClickListener(view -> {
                Button answerButton = (Button) view;
                CharSequence testAnswer = answerButton.getText();

                validate(testAnswer);
            });
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle options) {

        // To find items
        if (id == TestAbstractActivity.LOADER_ID_ITEMS_TO_FIND) {
            return super.onCreateLoader(id, options);
        } else { // QCM items
            String selection = "INPUT != '~'"; // fixme

            switch (typeTest) {

                case 0: // Kanji -> Hiragana
                case 1: // Hiragana -> Kanji
                    // katakana exclude
                    selection += " AND KANJI IS NOT NULL AND KANJI != ''";
                    break;

                case 2: // Japanese -> French
                case 3: // French -> Japanese
                    break;
            }

            String[] selectionArgs = null;
            List<String> idsToFind = options.getStringArrayList("idsToFind");
            if (idsToFind != null) {
                StringBuilder inList = new StringBuilder(idsToFind.size());
                selectionArgs = new String[idsToFind.size()];
                int i = 0;
                for (String idDone : idsToFind) {
                    if (i > 0) {
                        inList.append(",");
                    }
                    inList.append("?");

                    selectionArgs[i] = idDone;
                    i++;
                }

                selection += "AND _ID NOT IN (" + inList.toString() + ")";
            }

            String sortOrder = "RANDOM() LIMIT " + quantityMax * nbAnswer;

            return new CursorLoader(this, NihonGoContentProvider.URI_WORD, DicoContract.COLUMNS, selection, selectionArgs, sortOrder);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // To find items
        if (loader.getId() == TestAbstractActivity.LOADER_ID_ITEMS_TO_FIND) {
            quantityMax = data.getCount();
            results = new ArrayList<>(quantityMax);

            displayQuantity();

            ArrayList<String> idsToFind = new ArrayList<>();
            Row row;
            while (data.moveToNext()) {
//                item = new Item(data);
//                itemsToFind.add(item);
//                idsToFind.add(item.id);
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList("idsToFind", idsToFind);

            data.close();

            LoaderManager.getInstance(this).restartLoader(LOADER_ID_ITEMS_QCM, bundle, this);

        } else {// QCM items
            while (data.moveToNext()) {
//                itemsQCM.add(new Item(data));
            }

            data.close();
            next(itemsToFind.get(currentItemIndex));
        }
    }

    @Override
    protected void next(Details row) {
        int testIndex = new Random().nextInt(nbAnswer);

        Button mResponseView;
        String toFind;
        String answer;

        for (int index = 0; index < nbAnswer; index++) {
            mResponseView = (Button) answers.getChildAt(index);

            if (testIndex != index) { // QCM
                answer = getButtonLabel(itemsQCM.remove(0));
            } else {// RESPONSE
                answer = getButtonLabel(row);
                toFind = getTestLabel(row);

                mToFindView.setText(toFind);

                results.add(new Result(toFind, answer, false));
            }

            mResponseView.setText(answer);
        }
    }

    private String getButtonLabel(Details row) {
        String answer = null;

        switch (typeTest) {

            case 0: // Kanji -> Hiragana
                answer = row.kana;
                break;

            case 1: // Hiragana -> Kanji
                if (row.kanji.contains("、")) {
                    String[] kanjis = row.kanji.split("、");
                    int i = 0;
                    while (i < 1) {
                        i = new Random().nextInt(2);
                    }
                    answer = kanjis[i - 1];
                } else
                    answer = row.kanji;
                break;

            case 2: // Japanese -> French
                answer = row.input;
                break;

            case 3: // French -> Japanese
                if (isDisplayKanji && StringUtils.isNotBlank(row.kanji)) {
                    answer = row.kanji;
                } else {
                    answer = row.kana;
                }
                break;
        }

        return answer;
    }

    private String getTestLabel(Details row) {
        String toFind = null;

        switch (typeTest) {
            case 0: // Kanji -> Hiragana
                if (row.kanji.contains("、")) {
                    String[] kanjis = row.kanji.split("、");
                    int i = 0;
                    while (i < 1) {
                        i = new Random().nextInt(2);
                    }
                    toFind = kanjis[i - 1];
                } else
                    toFind = row.kanji;
                break;

            case 1: // Hiragana -> Kanji
                toFind = row.kana;
                break;

            case 2: // Japanese -> French

                if (isDisplayKanji && StringUtils.isNoneBlank(row.kanji)) {
                    toFind = row.kanji;
                } else {
                    toFind = row.kana;
                }
                break;

            case 3: // French -> Japanese
                toFind = row.input;
                break;
        }

        return toFind;
    }

}
