/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.lessons;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.frogdevelopment.nihongo.ConnectionHelper;
import fr.frogdevelopment.nihongo.Preferences;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.lessons.billing.IabHelper;
import fr.frogdevelopment.nihongo.lessons.billing.IabResult;
import fr.frogdevelopment.nihongo.lessons.billing.Inventory;
import fr.frogdevelopment.nihongo.lessons.billing.Purchase;
import fr.frogdevelopment.nihongo.lessons.billing.SkuDetails;

// fixme : tester connection internet avant appel
// todo : proposer rafraîchissement de la vue
public class LessonsFragment extends ListFragment {

	private static final String LOG_TAG = "NIHON_GO";

	// http://loopj.com/android-async-http/
	private static final AsyncHttpClient CLIENT = new AsyncHttpClient();

	private static final String   BASE_URL              = "http://legall.benoit.free.fr/nihon_go/";
	private static final String   AVAILABLE_LESSONS_URL = "%s/available_lessons.json";
	private static final String   PACK_FILE             = "%s.tsv"; // ex : pack_01.tsv
	private static final String[] LANGUAGES             = {"fr_FR", "en_US"};
	private static final String   DEFAULT_LANGUAGE      = "en_US";
	private static final String   PACK_1                = "pack_01";
	private static final String   DEVELOPER_PAYLOAD     = "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ";

	//    private boolean checkFirst() {
//        SharedPreferences settings = getSharedPreferences(Preferences.PREFS_NAME, 0);
//        return settings.getBoolean(Preferences.FIRST_LOAD, true);
//    }
//
//    private void setIsNotFirst() {
//        SharedPreferences settings = getSharedPreferences(Preferences.PREFS_NAME, 0);
//        final SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean(Preferences.FIRST_LOAD, false);
//        // Commit the edits!
//        editor.apply();
//    }

	private LessonAdapter adapter;
	private String        myLocale;


	public LessonsFragment() {
	}

	// The helper object
	private IabHelper mHelper;

	private List<Lesson> lessons;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myLocale = Locale.getDefault().toString();
		if (!ArrayUtils.contains(LANGUAGES, myLocale)) {
			myLocale = DEFAULT_LANGUAGE;
		}

		initPacksPresent();

		new TestConnectionTask(this).execute();
	}

	private Set<String> packs;

	private void initPacksPresent() {
		SharedPreferences settings = getSharedPreferences();
		String packsSaved = settings.getString(Preferences.PACKS.value, "");
		packs = new HashSet<>(Arrays.asList(packsSaved.split(";")));
	}

	private SharedPreferences getSharedPreferences() {
		return getActivity().getSharedPreferences(Preferences.PREFS_NAME.value, 0);
	}

	private boolean checkPackPresent(String pack) {
		return packs.contains(pack);
	}

	private void setPackPresent(String pack) {
		packs.add(pack);

		String packsSaved = StringUtils.join(packs, ";");

		SharedPreferences settings = getSharedPreferences();
		final SharedPreferences.Editor editor = settings.edit();
		editor.putString(Preferences.PACKS.value, packsSaved);
		editor.apply();
	}

	private class TestConnectionTask extends AsyncTask<String, Void, Boolean> {

		private final WeakReference<LessonsFragment> reference;

		public TestConnectionTask(LessonsFragment fragment) {
			this.reference = new WeakReference<>(fragment);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return ConnectionHelper.hasActiveInternetConnection(getActivity());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				reference.get().getAvailableProducts();
			} else {
				reference.get().setEmptyText("Pas de connexion");
			}

		}
	}

	private void inProgress(boolean wait) {
		getActivity().setProgressBarIndeterminateVisibility(wait);
		getListView().setEnabled(!wait);
	}

	private void getAvailableProducts() {

		inProgress(true);

		String urlLessonsAvailable = String.format(AVAILABLE_LESSONS_URL, BASE_URL);
		Log.d(LOG_TAG, "Calling : " + urlLessonsAvailable);
		CLIENT.get(urlLessonsAvailable, new JsonHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray response) {
				Log.e(LOG_TAG, "KO", throwable);
				Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
				inProgress(false);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

				List<String> availableProducts = new ArrayList<>();

				for (int index = 0, nbItem = response.length(); index < nbItem; index++) {
					try {
						String productId = response.getString(index);
						availableProducts.add(productId);
					} catch (JSONException e) {
						Log.e(LOG_TAG, "KO", e);
					}
				}

				getProducts(availableProducts);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					JSONArray jsonArray = response.getJSONArray(myLocale);

					List<String> availableProducts = new ArrayList<>();
					for (int index = 0, nbItem = jsonArray.length(); index < nbItem; index++) {
						try {
							String productId = jsonArray.getString(index);
							availableProducts.add(productId);
						} catch (JSONException e) {
							Log.e(LOG_TAG, "KO", e);
						}
					}

					getProducts(availableProducts);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void getProducts(final List<String> availableProducts) {

        /* fixme :
         * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsp1jGc8Qjp0I/gi1CQDac4BlGa5NFUwLV1N3GYIZ8WHUbytlvYssYQWT9Mz7bMYb0NcAGgivACMUZWZGeHdwcfppzGgegjnwmaJyC3X+bhUtRBQpLDM4wUl62PvCxukBpRJI/iBQZzciU9teEBMMMEjqHbHloK6z7qPDI7NsaCAP+vGarSICx9UBABgj/OPz4YDX3UcBGM49XTVKSB6Xo7j3TXeYC/LptXZSXG1RXTMQyt5O/ZwvTgG71C+KkzHv70K/7+JfdRS0DKkjSfvtw8YYOWJJE6O1ZeoaE2useOBHb7Z+RpggQEeRAt63kvC/p/X4JKz6YFeukoIdwQrzAwIDAQAB";

		// Create the helper, passing it our context and the public key to verify signatures with
		Log.d(LOG_TAG, "Creating IAB helper.");
		mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set this to false).
		mHelper.enableDebugLogging(false);

		// Start setup. This is asynchronous and the specified listener will be called once setup completes.
		Log.d(LOG_TAG, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(LOG_TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					Log.e(LOG_TAG, "**** TrivialDrive Error: Problem setting up in-app billing: " + result);
					return;
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null) return;

				// IAB is fully set up. Now, let's get an inventory of stuff we own.
				Log.d(LOG_TAG, "Setup successful. Querying inventory.");

				mHelper.queryInventoryAsync(true, availableProducts, new IabHelper.QueryInventoryFinishedListener() {
					//                mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
					public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
						if (result.isFailure()) {
							// fixme handle error
							return;
						}

						// update the UI
						lessons = new ArrayList<>();

						for (SkuDetails skuDetails : inventory.getAllSkuDetails()) {
							if (PACK_1.equals(skuDetails.getSku())) { // pack gratuit
								lessons.add(new Lesson(skuDetails, true, checkPackPresent(skuDetails.getSku())));
							} else {
								// FIXME lorsque deployé dans Google Play
//                                lessons.add(new Lesson(skuDetails, true, checkPackPresent(skuDetails.getSku())));
								lessons.add(new Lesson(skuDetails, inventory.hasPurchase(skuDetails.getSku()), checkPackPresent(skuDetails.getSku())));
							}
						}

						Collections.sort(lessons);

						adapter = new LessonAdapter(getActivity(), lessons);
						setListAdapter(adapter);
						inProgress(false);
					}
				});
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		final Lesson lesson = lessons.get(position);

		if (lesson.isBought)
			if (checkPackPresent(lesson.sku))
				new AlertDialog.Builder(getActivity())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.lesson_already_present)
						.setMessage(R.string.lesson_continue)
						.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								fetchData(lesson);
							}
						})
						.setNegativeButton(getString(R.string.no), null)
						.show();
			else
				fetchData(lesson);
		else
			buyLessonsPackage(lesson);
	}

	private void fetchData(final Lesson lesson) {

		final String url = BASE_URL + String.format(PACK_FILE, lesson.sku);

		Log.d(LOG_TAG, "Calling : " + url);
		inProgress(true);
		CLIENT.get(url, new FileAsyncHttpResponseHandler(getActivity()) {

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
				Log.e(LOG_TAG, "KO", throwable);
				Toast.makeText(getActivity(), R.string.options_error_fetch_data, Toast.LENGTH_LONG).show();
				inProgress(false);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, final File file) {
				Log.d(LOG_TAG, "File downloaded");
				insertData(file, lesson);
				setPackPresent(lesson.sku);
			}
		});
	}

	private void insertData(File file, final Lesson lesson) {
		adapter.setEnabled(false);

		try (Reader in = new FileReader(file)) {
			String col_input = myLocale + "_input";
			String col_details = myLocale + "_details";
			String col_tags = myLocale + "_tags";

			String[] insert = new String[7];
			ArrayList<ContentProviderOperation> ops = new ArrayList<>();
			CSVParser parse = CSVFormat.TDF.withHeader().withSkipHeaderRecord().parse(in);
			for (CSVRecord record : parse.getRecords()) {

				// 0 : INPUT
				String input = StringUtils.capitalize(record.get(col_input));

				if (StringUtils.isBlank(input)) {
					continue;
				}

				insert[0] = input;

				// 0 bis : SORT_LETTER
				// Normalizer.normalize(source, Normalizer.Form.NFD) renvoi une chaine unicode décomposé.
				// C'est à dire que les caractères accentués seront décomposé en deux caractères (par exemple "à" se transformera en "a`").
				// Le replaceAll("[\u0300-\u036F]", "") supprimera tous les caractères unicode allant de u0300 à u036F,
				// c'est à dire la plage de code des diacritiques (les accents qu'on a décomposé ci-dessus donc).
				insert[1] = Normalizer.normalize(input.substring(0, 1), Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]", "");

				// 1 : KANJI
				insert[2] = record.get("kanji");

				// 2 : KANA
				insert[3] = record.get("kana");

				// 3 : DETAILS
				insert[4] = record.get(col_details);

				// 4 : TYPE
				insert[5] = record.get("type");

				// 5 : TAGS
				insert[6] = record.get(col_tags);

				ops.add(ContentProviderOperation
								.newInsert(NihonGoContentProvider.URI_WORD)
								.withValue(DicoContract.INPUT, insert[0])
								.withValue(DicoContract.SORT_LETTER, insert[1])
								.withValue(DicoContract.KANJI, insert[2])
								.withValue(DicoContract.KANA, insert[3])
								.withValue(DicoContract.DETAILS, insert[4])
								.withValue(DicoContract.TYPE, insert[5])
								.withValue(DicoContract.TAGS, insert[6])
								.build()
				);
			}

			getActivity().getContentResolver().applyBatch(NihonGoContentProvider.AUTHORITY, ops);

			lesson.isPresent = true;

		} catch (RemoteException | OperationApplicationException | IOException e) {
			Log.e(LOG_TAG, "Error while fetching data", e);
		} finally {
			adapter.setEnabled(true);
			getListView().invalidateViews();
		}

		Toast.makeText(getActivity(), getActivity().getString(R.string.lesson_donwload_sucess, lesson.title), Toast.LENGTH_LONG).show();
		inProgress(false);
	}

	private void buyLessonsPackage(final Lesson lesson) {
		if (mHelper != null) mHelper.flagEndAsync();
		mHelper.launchPurchaseFlow(getActivity(), lesson.sku, 10001, new IabHelper.OnIabPurchaseFinishedListener() {
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
				if (result.isFailure()) {
					Log.d(LOG_TAG, "Error purchasing: " + result);

					return;
				}

				// fixme : Security Recommendation: When you receive the purchase response from Google Play, make sure to check the returned data signature, the orderId,
				// and the developerPayload string in the Purchase object to make sure that you are getting the expected values.
				// You should verify that the orderId is a unique value that you have not previously processed,
				// and the developerPayload string matches the token that you sent previously with the purchase request.
				// As a further security precaution, you should perform the verification on your own secure server.
				purchase.getOrderId();

				if (!DEVELOPER_PAYLOAD.equals(purchase.getDeveloperPayload())) {
					Toast.makeText(getActivity(), "Erreur ?", Toast.LENGTH_LONG).show();
					return;
				}

				if (purchase.getSku().equals(lesson.sku)) {
					fetchData(lesson);
				} else {
					Toast.makeText(getActivity(), "Erreur ?", Toast.LENGTH_LONG).show();
				}
			}
		}, DEVELOPER_PAYLOAD);
	}

	public static class Lesson implements Comparable<Lesson> {
		public final String  sku;
		public final String  title;
		public final String  description;
		public final String  price;
		public       boolean isBought;
		public       boolean isPresent;

		public Lesson(SkuDetails skuDetails, boolean isBought, boolean isPresent) {
			this.sku = skuDetails.getSku();
			// todo : à améliorer : suppression du nom de l'application (Nihon Go!) du titre
			this.title = skuDetails.getTitle().substring(0, skuDetails.getTitle().length() - 12);
			this.description = skuDetails.getDescription();
			this.price = skuDetails.getPrice();
			this.isBought = isBought;
			this.isPresent = isPresent;
		}

		@Override
		public String toString() {
			return title;
		}

		@Override
		public int compareTo(@NonNull Lesson another) {
			return sku.compareTo(another.sku);
		}
	}


}
