/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.conjugation;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.ConjugationContract;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.dico.input.InputUtils;

import static fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider.URI_CONJUGATION;

public class ConjugationActivity extends Activity {

	// Components
	@InjectView(R.id.conjugation_word)
	TextView    mWord;
	@InjectView(R.id.conjugation_group)
	RadioGroup  mGroup;
	@InjectView(R.id.conjugation_group_1)
	RadioButton mGroup1;
	@InjectView(R.id.conjugation_group_2)
	RadioButton mGroup2;
	@InjectView(R.id.conjugation_group_3)
	RadioButton mGroup3;
	@InjectView(R.id.conjugation_dico)
	EditText    mDico;
	@InjectView(R.id.conjugation_masu)
	EditText    mMasu;
	@InjectView(R.id.conjugation_te)
	EditText    mTe;
	@InjectView(R.id.conjugation_nai)
	EditText    mNai;
	@InjectView(R.id.conjugation_ta)
	EditText    mTa;

	// Initial Data
	protected String                    idUpdate;
	private   ConjugationContract.Group groupSave;
	private   String                    dicoSave;
	protected String                    masuSave;
	private   String                    teSave;
	private   String                    naiSave;
	private   String                    taSave;
	protected String                    wordId;

	protected boolean isUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_conjugation);
//        setTitle(R.string.menu_subitem_word); FIXME

		ButterKnife.inject(this);

		chekUpdate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.input, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_validate:
				validate();
				return true;

			case R.id.action_cancel:
				reset();
				return true;

			case android.R.id.home:
				back();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		back();
	}


	private void back() {
		NavUtils.navigateUpFromSameTask(this);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void chekUpdate() {
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			return;// fixme
		}

		wordId = bundle.getString(ConjugationContract.WORD_ID);
		String word = bundle.getString(DicoContract.INPUT);
		mWord.setText(word);

		if (bundle.containsKey(ConjugationContract._ID)) {
			fillDataToUpdate();
		} else {
			emptyData();
		}
	}

	private void fillDataToUpdate() {
		Bundle bundle = getIntent().getExtras();

		isUpdate = true;
		idUpdate = bundle.getString(ConjugationContract._ID);

		String tmp = bundle.getString(ConjugationContract.GROUP);
		groupSave = ConjugationContract.Group.valueOf(tmp);
		switch (groupSave) {
			case I:
				mGroup1.setSelected(true);
				break;
			case II:
				mGroup2.setSelected(true);
				break;
			case III:
				mGroup3.setSelected(true);
				break;
			default:
				mGroup.clearCheck();
		}

		dicoSave = bundle.getString(ConjugationContract.DICO);
		mDico.setText(dicoSave);

		masuSave = bundle.getString(ConjugationContract.MASU);
		mMasu.setText(masuSave);

		teSave = bundle.getString(ConjugationContract.TE);
		mTe.setText(teSave);

		naiSave = bundle.getString(ConjugationContract.NAI);
		mNai.setText(naiSave);

		taSave = bundle.getString(ConjugationContract.TA);
		mTa.setText(taSave);
	}

	private void emptyData() {
		isUpdate = false;
		idUpdate = "";
		groupSave = null;
		dicoSave = "";
		masuSave = "";
		teSave = "";
		naiSave = "";
		taSave = "";
	}

	private void reset() {
		mGroup.clearCheck();

		mDico.setText(dicoSave);
		mDico.setError(null);

		mMasu.setText(masuSave);
		mMasu.setError(null);

		mTe.setText(teSave);
		mTe.setError(null);

		mNai.setText(naiSave);
		mNai.setError(null);

		mTa.setText(taSave);
		mTa.setError(null);
	}

	private void validate() {
		boolean isNoError = true;

		mGroup3.setError(null);
		switch (mGroup.getCheckedRadioButtonId()) {
			case R.id.conjugation_group_1:
				break;
			case R.id.conjugation_group_2:
				break;
			case R.id.conjugation_group_3:
				break;
			default:
				mGroup3.setError(getResources().getString(R.string.input_error_empty));
		}

		String dicoText = mDico.getText().toString();
		if (dicoText.isEmpty()) {
			isNoError = false;
			mDico.setError(getResources().getString(R.string.input_error_all_empty)); //fixme
		} else {
			if (InputUtils.isOnlyJapanese(dicoText)) {
				mDico.setError(null);
			} else {
				isNoError = false;
				mDico.setError(getResources().getString(R.string.input_error_japanese));
			}
		}

		String masuText = mMasu.getText().toString();
		if (masuText.isEmpty()) {
			isNoError = false;
			mMasu.setError(getResources().getString(R.string.input_error_all_empty)); //fixme
		} else {
			if (InputUtils.isOnlyJapanese(dicoText)) {
				mMasu.setError(null);
			} else {
				isNoError = false;
				mMasu.setError(getResources().getString(R.string.input_error_japanese));
			}
		}

		String teText = mTe.getText().toString();
		if (InputUtils.isOnlyJapanese(teText)) {
			mTe.setError(null);
		} else {
			isNoError = false;
			mTe.setError(getResources().getString(R.string.input_error_japanese));
		}
		String naiText = mNai.getText().toString();
		if (InputUtils.isOnlyJapanese(naiText)) {
			mNai.setError(null);
		} else {
			isNoError = false;
			mNai.setError(getResources().getString(R.string.input_error_japanese));
		}
		String taText = mTa.getText().toString();
		if (InputUtils.isOnlyJapanese(taText)) {
			mTa.setError(null);
		} else {
			isNoError = false;
			mTa.setError(getResources().getString(R.string.input_error_japanese));
		}

		if (isNoError) {
			saveOrUpdate();
		} else {
			Toast.makeText(this, R.string.input_error_fields, Toast.LENGTH_LONG).show();
		}
	}

	private void saveOrUpdate() {
		if (isUpdate) {
			updateById();
		} else {
			insert();
			reset();
		}

		back();
	}

	private String getGroup() {
		switch (mGroup.getCheckedRadioButtonId()) {
			case R.id.conjugation_group_1:
				return ConjugationContract.Group.I.code;
			case R.id.conjugation_group_2:
				return ConjugationContract.Group.II.code;
			case R.id.conjugation_group_3:
				return ConjugationContract.Group.III.code;
			default:
				throw  new IllegalStateException("Unknow group :(");
		}
	}

	private void updateById() {
		final String where = ConjugationContract._ID + "=?";
		final String[] selectionArgs = {idUpdate};

		final ContentValues values = new ContentValues();
		values.put(ConjugationContract.GROUP, getGroup());
		values.put(ConjugationContract.DICO, mDico.getText().toString());
		values.put(ConjugationContract.MASU, mMasu.getText().toString());
		values.put(ConjugationContract.TE, mTe.getText().toString());
		values.put(ConjugationContract.NAI, mNai.getText().toString());
		values.put(ConjugationContract.TA, mTa.getText().toString());
		values.put(ConjugationContract.WORD_ID, wordId);

		getContentResolver().update(URI_CONJUGATION, values, where, selectionArgs);

		// TOAST
		Toast.makeText(this, R.string.input_update_OK, Toast.LENGTH_LONG).show();
	}

	private void insert() {
		final ContentValues values = new ContentValues();
		values.put(ConjugationContract.GROUP, getGroup());
		values.put(ConjugationContract.DICO, mDico.getText().toString());
		values.put(ConjugationContract.MASU, mMasu.getText().toString());
		values.put(ConjugationContract.TE, mTe.getText().toString());
		values.put(ConjugationContract.NAI, mNai.getText().toString());
		values.put(ConjugationContract.TA, mTa.getText().toString());
		values.put(ConjugationContract.WORD_ID, wordId);

		getContentResolver().insert(URI_CONJUGATION, values);

		// TOAST
		Toast.makeText(this, R.string.input_save_OK, Toast.LENGTH_LONG).show();
	}
}
