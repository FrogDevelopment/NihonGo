/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;

public class TestInputActivity extends TestAbstractActivity {

	@BindView(R.id.test_input_scroll)
	ScrollView mScrollView;

	@BindView(R.id.test_input_to_find)
	TextView toFindView;

	@BindView(R.id.test_input_answer)
	EditText answerView;

	public TestInputActivity() {
		super(R.layout.activity_test_input);
	}

	@OnClick(R.id.test_input_validate_button)
	void onClickValidate(View v) {
		validate(answerView.getText());
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
