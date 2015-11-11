/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;


public class TestSelectActivity extends TestAbstractActivity {

    @Bind(R.id.test_select_to_find)
    TextView mToFindView;

    private static final List<Integer> ANSWERS = new ArrayList<>(6);

    static {
        ANSWERS.add(R.id.test_select_answer_1);
        ANSWERS.add(R.id.test_select_answer_2);
        ANSWERS.add(R.id.test_select_answer_3);
        ANSWERS.add(R.id.test_select_answer_4);
        ANSWERS.add(R.id.test_select_answer_5);
        ANSWERS.add(R.id.test_select_answer_6);
    }

    public TestSelectActivity() {
        limit = ANSWERS.size();
        showMenuNext = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_select);

        ButterKnife.bind(this);
    }

    @OnClick({R.id.test_select_answer_1, R.id.test_select_answer_2, R.id.test_select_answer_3, R.id.test_select_answer_4, R.id.test_select_answer_5, R.id.test_select_answer_6})
    void onClickAnswers(View view) {
        Button answerButton = (Button) view;
        CharSequence testAnswer = answerButton.getText();

        validate(testAnswer);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.getCount() < limit) {
            finishTest();
        }

        int testIndex = new Random().nextInt(limit);

        int index = 0;
        Item item;
        Button mResponseView;
        String toFind = null;
        while (data.moveToNext()) {
            item = new Item(data);

            // AFFICHAGE
            mResponseView = (Button) findViewById(ANSWERS.get(index));
            String answer = null;
            switch (typeTest) {

                case 0: // Kanji -> Hiragana
                    answer = item.kana;
                    break;

                case 1: // Hiragana -> Kanji
                    if (item.kanji.contains("?")) {
                        String[] kanjis = item.kanji.split("?");
                        answer = kanjis[new Random().nextInt(2) - 1];
                    } else
                        answer = item.kanji;
                    break;

                case 2: // Japanese -> French
                    answer = item.input;
                    break;

                case 3: // French -> Japanese
                    if (isDisplayKanji && StringUtils.isNotBlank(item.kanji)) {
                        answer = item.kanji;
                    } else {
                        answer = item.kana;
                    }
                    break;
            }

            mResponseView.setText(answer);

            // REPONSE
            if (testIndex == index) {
                idsDone.add(item.id);
                currentDetails = item.details;

                switch (typeTest) {
                    case 0: // Kanji -> Hiragana
                        if (item.kanji.contains("?")) {
                            String[] kanjis = item.kanji.split("?");
                            toFind = kanjis[new Random().nextInt(2) - 1];
                        } else
                            toFind = item.kanji;
                        break;

                    case 1: // Hiragana -> Kanji
                        toFind = item.kana;
                        break;

                    case 2: // Japanese -> French

                        if (isDisplayKanji && StringUtils.isNoneBlank(item.kanji)) {
                            toFind = item.kanji;
                        } else {
                            toFind = item.kana;
                        }
                        break;

                    case 3: // French -> Japanese
                        toFind = item.input;
                        break;
                }


                mToFindView.setText(toFind);

                results.add(new Result(toFind, answer, false));
            }

            index++;
        }

        data.close();

        super.onLoadFinished(loader, data);
    }


}
