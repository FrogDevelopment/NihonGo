/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.options;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.billing.FIXME;
import fr.frogdevelopment.nihongo.billing.IabHelper;
import fr.frogdevelopment.nihongo.billing.IabResult;
import fr.frogdevelopment.nihongo.billing.Purchase;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

// fixme utiliser les préférences XML ? http://developer.android.com/guide/topics/ui/settings.html
public class ParametersFragment extends Fragment {

    private static final String LOG_TAG = "NIHON_GO";

    @Bind(R.id.options_no_advertising_button)
    Button mNoAdvertisingButton;

    @Bind(R.id.options_no_advertising_text)
    TextView mNoAdvertisingText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options_parameters, container, false);

        ButterKnife.bind(this, view);

        boolean noAdvertising = PreferencesHelper.getInstance(getContext()).getBoolean(Preferences.NO_ADVERTISING);
        manageNoAdvertisingViews(noAdvertising);

        return view;
    }

    private void manageNoAdvertisingViews(boolean noAdvertising) {
        mNoAdvertisingButton.setEnabled(!noAdvertising);
        mNoAdvertisingText.setText(noAdvertising ? R.string.options_no_advertising_purchased : R.string.options_no_advertising_not_purchased);
    }

    @OnClick(R.id.options_erase)
    void onClickErase() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.options_erase_data_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    getActivity().getContentResolver().delete(NihonGoContentProvider.URI_ERASE, null, null);

                    PreferencesHelper.getInstance(getContext()).saveString(Preferences.PACKS, "");
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    @OnClick(R.id.options_reset_favorite)
    void onClickResetFavorite() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.options_erase_data_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    final ContentValues values = new ContentValues();
                    values.put(DicoContract.FAVORITE, "0");
                    getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_FAVORITE, values, null, null);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    @OnClick(R.id.options_reset_learned)
    void onClickResetLearned() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.options_erase_data_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    final ContentValues values = new ContentValues();
                    values.put(DicoContract.LEARNED, "0");
                    getActivity().getContentResolver().update(NihonGoContentProvider.URI_RESET_LEARNED, values, null, null);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }


    // The helper object
    private IabHelper mHelper;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @OnClick(R.id.options_no_advertising_restore)
    void checkPurchase() {

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(LOG_TAG, "Creating IAB helper.");
        mHelper = new IabHelper(getActivity(), FIXME.PUBLIC_KEY.value);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        final String sku = Preferences.NO_ADVERTISING.code;

        // Start setup. This is asynchronous and the specified listener will be called once setup completes.
        Log.d(LOG_TAG, "Starting setup.");
        mHelper.startSetup(result -> {
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
            mHelper.queryInventoryAsync(true, Collections.singletonList(sku), (result1, inventory) -> {
                if (result1.isFailure()) {
                    // fixme handle error
                    return;
                }

                // update the UI
                boolean noAdvertising = inventory.hasPurchase(sku);
                PreferencesHelper.getInstance(getContext()).saveBoolean(Preferences.NO_ADVERTISING, noAdvertising);
                manageNoAdvertisingViews(noAdvertising);
            });
        });
    }

    @OnClick(R.id.options_no_advertising_button)
    void buyNoAdvertising() {
        if (mHelper != null) mHelper.flagEndAsync();

        final String sku = Preferences.NO_ADVERTISING.code;

        mHelper.launchPurchaseFlow(getActivity(), sku, 10001, new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    Log.d(LOG_TAG, "Error purchasing: " + result);
                    // fixme meilleur message
                    Toast.makeText(getActivity(), "Erreur ?", Toast.LENGTH_LONG).show();
                    return;
                }

                // fixme
                // Security Recommendation: When you receive the purchase response from Google Play, make sure to check the returned data signature, the orderId,
                // and the developerPayload string in the Purchase object to make sure that you are getting the expected values.
                // You should verify that the orderId is a unique value that you have not previously processed,
                // and the developerPayload string matches the token that you sent previously with the purchase request.
                // As a further security precaution, you should perform the verification on your own secure server.
                // purchase.getOrderId();

                if (!FIXME.DEVELOPER_PAYLOAD.value.equals(purchase.getDeveloperPayload())) {
                    // fixme meilleur message
                    Toast.makeText(getActivity(), "Erreur ?", Toast.LENGTH_LONG).show();
                    return;
                }

                if (purchase.getSku().equals(sku)) {
                    PreferencesHelper.getInstance(getContext()).saveBoolean(Preferences.NO_ADVERTISING, true);
                    manageNoAdvertisingViews(true);
                } else {
                    // fixme meilleur message
                    Toast.makeText(getActivity(), "Erreur ?", Toast.LENGTH_LONG).show();
                }
            }
        }, FIXME.DEVELOPER_PAYLOAD.value);
    }
}
