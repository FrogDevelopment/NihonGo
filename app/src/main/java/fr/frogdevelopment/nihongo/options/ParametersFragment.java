/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.BuildConfig;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.billing.FIXME;
import fr.frogdevelopment.nihongo.billing.IabBroadcastReceiver;
import fr.frogdevelopment.nihongo.billing.IabHelper;
import fr.frogdevelopment.nihongo.billing.IabResult;
import fr.frogdevelopment.nihongo.billing.Purchase;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

// fixme utiliser les préférences XML ? http://developer.android.com/guide/topics/ui/settings.html
// http://developer.android.com/training/in-app-billing/preparing-iab-app.html
public class ParametersFragment extends Fragment implements IabBroadcastReceiver.IabBroadcastListener {

	private static final String LOG_TAG = "NIHON_GO";

	@Bind(R.id.options_no_advertising_button)
	Button mNoAdvertisingButton;

	@Bind(R.id.options_no_advertising_text)
	TextView mNoAdvertisingText;

	// The helper object
	private IabHelper mHelper;

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	// Provides purchase notification while this app is running
	IabBroadcastReceiver mBroadcastReceiver;

	private boolean noAdvertisingPurchased;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

		ButterKnife.bind(this, view);

		// fixme gérer cas pas de connexion
		noAdvertisingPurchased = PreferencesHelper.getInstance(getContext()).getBoolean(Preferences.NO_ADVERTISING);

		updateUi();

		initIabHelper();

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}


	@OnClick(R.id.options_erase)
	void onClickErase() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_erase_data_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

					PreferencesHelper.getInstance(getContext()).saveString(Preferences.LESSONS, "");

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_erase_data_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_favorite)
	void onClickResetFavorite() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_reset_favorite_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					final ContentValues values = new ContentValues();
					values.put(DicoContract.FAVORITE, "0");
					getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_FAVORITE, values, null, null);

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_reset_favorite_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@OnClick(R.id.options_reset_learned)
	void onClickResetLearned() {
		new AlertDialog.Builder(getActivity())
				.setMessage(R.string.options_reset_learned_erase_confirmation)
				.setPositiveButton(android.R.string.ok, (dialog, id) -> {
					final ContentValues values = new ContentValues();
					values.put(DicoContract.LEARNED, "0");
					getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_LEARNED, values, null, null);

					Snackbar.make(getActivity().findViewById(R.id.parameters_layout), R.string.options_reset_learned_erase_success, Snackbar.LENGTH_LONG).show();
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create()
				.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// very important:
		if (mBroadcastReceiver != null) {
			getContext().unregisterReceiver(mBroadcastReceiver);
		}

		// very important:
		Log.d(LOG_TAG, "Destroying helper.");
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	@Override
	public void receivedBroadcast() {
		// Received a broadcast notification that the inventory of items has changed
		Log.d(LOG_TAG, "Received broadcast notification. Querying inventory.");
		mHelper.queryInventoryAsync(mGotInventoryListener);
	}

	private void initIabHelper() {
		// Create the helper, passing it our context and the public key to verify signatures with
		Log.d(LOG_TAG, "Creating IAB helper.");
		mHelper = new IabHelper(getActivity(), FIXME.PUBLIC_KEY.value);

		// enable debug logging (for a production application, you should set this to false).
		mHelper.enableDebugLogging(BuildConfig.DEBUG);

		// Start setup. This is asynchronous and the specified listener will be called once setup completes.
		Log.d(LOG_TAG, "Starting setup.");
		mHelper.startSetup(result -> {
			Log.d(LOG_TAG, "Setup finished.");

			if (!result.isSuccess()) {
				// Oh no, there was a problem.
				complain("Problem setting up in-app billing: " + result);
				return;
			}

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null) return;

			// Important: Dynamically register for broadcast messages about updated purchases.
			// We register the receiver here instead of as a <receiver> in the Manifest
			// because we always call getPurchases() at startup, so therefore we can ignore
			// any broadcasts sent while the app isn't running.
			// Note: registering this listener in an Activity is a bad idea, but is done here
			// because this is a SAMPLE. Regardless, the receiver must be registered after
			// IabHelper is setup, but before first call to getPurchases().
			mBroadcastReceiver = new IabBroadcastReceiver(ParametersFragment.this);
			IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
			getContext().registerReceiver(mBroadcastReceiver, broadcastFilter);

			// IAB is fully set up. Now, let's get an inventory of stuff we own.
			Log.d(LOG_TAG, "Setup successful.");
		});
	}

	@OnClick(R.id.options_no_advertising_restore)
	void checkPurchase() {
		Log.d(LOG_TAG, "Querying inventory.");
		mHelper.queryInventoryAsync(mGotInventoryListener);
	}

	@OnClick(R.id.options_no_advertising_button)
	void buyNoAdvertising() {
		Log.d(LOG_TAG, "Buy no advertising button clicked.");
		setWaitScreen(true);

		if (noAdvertisingPurchased) {
			complain("No need! You're subscribed to no advertising. Isn't that awesome?");
			return;
		}

		// launch the gas purchase UI flow.
		// We will be notified of completion via mPurchaseFinishedListener
		Log.d(LOG_TAG, "Launching purchase flow for no advertising.");

		mHelper.flagEndAsync();
		mHelper.launchPurchaseFlow(getActivity(), Preferences.NO_ADVERTISING.code, RC_REQUEST, mPurchaseFinishedListener, FIXME.DEVELOPER_PAYLOAD.value);
	}

	// updates UI to reflect model
	public void updateUi() {
		mNoAdvertisingButton.setEnabled(!noAdvertisingPurchased);
		mNoAdvertisingText.setText(noAdvertisingPurchased ? R.string.options_no_advertising_purchased : R.string.options_no_advertising_not_purchased);
	}

	// Enables or disables the "please wait" screen.
	void setWaitScreen(boolean set) {
//        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
	}

	void complain(String message) {
		Log.e(LOG_TAG, "**** TrivialDrive Error: " + message);
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(getContext());
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(LOG_TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}

	/**
	 * Verifies the developer payload of a purchase.
	 */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
// todo voir pour utiliser http://developer.android.com/tools/help/proguard.html
		return FIXME.DEVELOPER_PAYLOAD.value.equals(payload);
	}


	/**
	 * Listener that's called when we finish querying the items and subscriptions we own
	 **/
	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = (result, inventory) -> {
		Log.d(LOG_TAG, "Query inventory finished.");

		// Have we been disposed of in the meantime? If so, quit.
		if (mHelper == null) return;

		if (result.isFailure()) {
			complain("Failed to query inventory: " + result);
			return;
		}

		Log.d(LOG_TAG, "Query inventory was successful.");

         /*
        * Check for items we own. Notice that for each purchase, we check
        * the developer payload to see if it's correct! See
        * verifyDeveloperPayload().
        */

		// Do we have the premium upgrade?
		Purchase premiumPurchase = inventory.getPurchase(Preferences.NO_ADVERTISING.code);
		noAdvertisingPurchased = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
		PreferencesHelper.getInstance(getContext()).saveBoolean(Preferences.NO_ADVERTISING, noAdvertisingPurchased);
		Log.d(LOG_TAG, "User is " + (noAdvertisingPurchased ? "PREMIUM" : "NOT PREMIUM"));

		updateUi();
		setWaitScreen(false);
		Log.d(LOG_TAG, "Initial inventory query finished; enabling main UI.");
	};

	/**
	 * Callback for when a purchase is finished
	 **/
	private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(LOG_TAG, "Purchase finished: " + result + ", purchase: " + purchase);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null) return;

			if (result.isFailure()) {
				complain("Error purchasing: " + result);
				setWaitScreen(false);
				return;
			}

			if (!verifyDeveloperPayload(purchase)) {
				complain("Error purchasing. Authenticity verification failed.");
				setWaitScreen(false);
				return;
			}

			Log.d(LOG_TAG, "Purchase successful.");

			if (purchase.getSku().equals(Preferences.NO_ADVERTISING.code)) {
				// bought the premium upgrade!
				Log.d(LOG_TAG, "Purchase is premium upgrade. Congratulating user.");
				alert("Thank you for upgrading to premium!");
				noAdvertisingPurchased = true;

				PreferencesHelper.getInstance(getContext()).saveBoolean(Preferences.NO_ADVERTISING, noAdvertisingPurchased);
				updateUi();
				setWaitScreen(false);
			}
		}
	};
}
