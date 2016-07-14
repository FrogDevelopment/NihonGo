/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.test;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;


public class TestSelectActivity extends TestAbstractActivity {

	private static final int LOADER_ID_ITEMS_QCM = 720;

	@BindView(R.id.test_select_to_find)
	TextView mToFindView;

	@BindView(R.id.test_select_answers)
	LinearLayout answers;

	private List<Item> itemsQCM = new ArrayList<>();

	public TestSelectActivity() {
		super(R.layout.activity_test_select);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			Item item;
			while (data.moveToNext()) {
				item = new Item(data);
				itemsToFind.add(item);
				idsToFind.add(item.id);
			}

			Bundle bundle = new Bundle();
			bundle.putStringArrayList("idsToFind", idsToFind);

			data.close();

			getLoaderManager().restartLoader(LOADER_ID_ITEMS_QCM, bundle, this);

		} else {// QCM items
			while (data.moveToNext()) {
				itemsQCM.add(new Item(data));
			}

			data.close();
			next(itemsToFind.get(currentItemIndex));
		}
	}

	@Override
	protected void next(Item item) {
		int testIndex = new Random().nextInt(nbAnswer);

		Button mResponseView;
		String toFind;
		String answer;

		for (int index = 0; index < nbAnswer; index++) {
			mResponseView = (Button) answers.getChildAt(index);

			if (testIndex != index) { // QCM
				answer = getButtonLabel(itemsQCM.remove(0));
			} else {// RESPONSE
				answer = getButtonLabel(item);
				toFind = getTestLabel(item);

				mToFindView.setText(toFind);

				results.add(new Result(toFind, answer, false));
			}

			mResponseView.setText(answer);
		}
	}

	private String getButtonLabel(Item item) {
		String answer = null;

		switch (typeTest) {

			case 0: // Kanji -> Hiragana
				answer = item.kana;
				break;

			case 1: // Hiragana -> Kanji
				if (item.kanji.contains("、")) {
					String[] kanjis = item.kanji.split("、");
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

		return answer;
	}

	private String getTestLabel(Item item) {
		String toFind = null;

		switch (typeTest) {
			case 0: // Kanji -> Hiragana
				if (item.kanji.contains("、")) {
					String[] kanjis = item.kanji.split("、");
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

		return toFind;
	}

}
