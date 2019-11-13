package fr.frogdevelopment.nihongo.test;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;

public class TestInputActivity extends TestAbstractActivity {

    private ScrollView mScrollView;
    private TextView toFindView;
    private EditText answerView;

    public TestInputActivity() {
        super(R.layout.activity_test_input);
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
            new AlertDialog.Builder(this)
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
    protected void next(Item item) {
        mScrollView.smoothScrollTo(0, 0);
        answerView.setText(null);

        String answer = null;
        String toFind = null;

        switch (typeTest) {

            case 0: // Kanji -> Hiragana
                if (item.kanji.contains("、")) {
                    String[] kanjis = item.kanji.split("、");
                    toFind = kanjis[new Random().nextInt(2) - 1];
                } else
                    toFind = item.kanji;

                answer = item.kana;
                break;

            case 1: // Hiragana -> Kanji
                toFind = item.kana;
                answer = item.kanji;
                break;

            case 2: // Japanese -> French
                answer = item.input;

                if (isDisplayKanji && StringUtils.isNoneBlank(item.kanji)) {
                    toFind = item.kanji;
                } else {
                    toFind = item.kana;
                }
                break;

            case 3: // French -> Japanese
                toFind = item.input;

                if (isDisplayKanji && StringUtils.isNoneBlank(item.kanji)) {
                    answer = item.kanji;
                } else {
                    answer = item.kana;
                }
                break;
        }

        toFindView.setText(toFind);
        results.add(new Result(toFind, answer, true));
    }

}
