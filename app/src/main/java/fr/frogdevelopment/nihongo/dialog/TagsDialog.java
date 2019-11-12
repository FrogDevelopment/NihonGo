/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.frogdevelopment.nihongo.R;

public class TagsDialog extends DialogFragment {

	public interface TagDialogListener {
		void onReturnValue(ArrayList<Integer> selectedItems);
	}

	private WeakReference<TagDialogListener> listener;

	public static void show(FragmentManager fragmentManager, TagDialogListener listener, List<CharSequence> items, ArrayList<Integer> selectedItems) {
		TagsDialog dialog = new TagsDialog();
		dialog.listener = new WeakReference<>(listener);

		Bundle args = new Bundle();
		args.putCharSequenceArray("items", items.toArray(new CharSequence[items.size()]));
		args.putIntegerArrayList("selectedItems", selectedItems);
		dialog.setArguments(args);

		dialog.show(fragmentManager, "tagsDialog");
	}

	private ArrayList<Integer> mSelectedItems;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		CharSequence[] items = getArguments().getCharSequenceArray("items");
		mSelectedItems = new ArrayList<>();
		int nbItems = items.length;
		List<Integer> ll = getArguments().getIntegerArrayList("selectedItems");
		boolean[] checkedItems = null;
		if (ll != null) {
			checkedItems = new boolean[nbItems];
			for (Integer i = 0; i < nbItems; i++) {
				if (ll.contains(i)) {
					checkedItems[i] = true;
					mSelectedItems.add(i);
				} else {
					checkedItems[i] = false;
				}
			}
		}

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.param_tags_dialog_title);
		builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
			if (isChecked) {
				// If the user checked the item, add it to the selected items
				mSelectedItems.add(which);
			} else if (mSelectedItems.contains(which)) {
				// Else, if the item is already in the array, remove it
				mSelectedItems.remove(Integer.valueOf(which));
			}
		});

		// Set the action buttons
		builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
			Collections.sort(mSelectedItems);
			listener.get().onReturnValue(mSelectedItems);
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

}
