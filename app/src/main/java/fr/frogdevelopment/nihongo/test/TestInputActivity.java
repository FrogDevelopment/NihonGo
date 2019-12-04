package fr.frogdevelopment.nihongo.test;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Details;

public class TestInputActivity extends TestAbstractActivity {

    private ScrollView mScrollView;
    private TextView toFindView;
    private EditText answerView;

    public TestInputActivity() {
        super(R.layout.test_input_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScrollView = findViewById(R.id.test_input_scroll);
        toFindView = findViewById(R.id.test_input_to_find);
        answerView = findViewById(R.id.test_input_answer);
        Button validateButton = findViewById(R.id.test_input_validate_button);
        validateButton.setOnClickListener(v -> onClickValidate());
    }

    private void onClickValidate() {
        Editable answer = answerView.getText();
        if (TextUtils.isEmpty(answer)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.test_input_empty)
                    .setPositiveButton(android.R.string.yes, (dialog, id) -> validate(answer))
                    .setNegativeButton(android.R.string.no, null)
                    .create()
                    .show();
        } else {
            validate(answer);
        }
    }

    @Override
    protected void next(Details row) {
        mScrollView.smoothScrollTo(0, 0);
        answerView.setText(null);

        String answer = null;
        String toFind = null;

        switch (typeTest) {

            case 0: // Kanji -> Hiragana
                if (row.kanji.contains("、")) {
                    String[] kanjis = row.kanji.split("、");
                    toFind = kanjis[new Random().nextInt(2) - 1];
                } else
                    toFind = row.kanji;

                answer = row.kana;
                break;

            case 1: // Hiragana -> Kanji
                toFind = row.kana;
                answer = row.kanji;
                break;

            case 2: // Japanese -> French
                answer = row.input;

                if (isDisplayKanji && StringUtils.isNoneBlank(row.kanji)) {
                    toFind = row.kanji;
                } else {
                    toFind = row.kana;
                }
                break;

            case 3: // French -> Japanese
                toFind = row.input;

                if (isDisplayKanji && StringUtils.isNoneBlank(row.kanji)) {
                    answer = row.kanji;
                } else {
                    answer = row.kana;
                }
                break;
        }

        toFindView.setText(toFind);
        results.add(new Result(toFind, answer, true));
    }

}
