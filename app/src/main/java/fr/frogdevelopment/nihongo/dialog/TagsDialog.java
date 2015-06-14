package fr.frogdevelopment.nihongo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

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

    public static void show(FragmentManager fragmentManager, TagDialogListener listener, List<String> items, ArrayList<Integer> selectedItems) {
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

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_tags, null);

        CharSequence[] items = getArguments().getCharSequenceArray("items");
        mSelectedItems = new ArrayList<>();
        int nbItems = items.length;
        List<Integer> ll = getArguments().getIntegerArrayList("selectedItems");
        boolean[] toto = null;
        if (ll != null) {
            toto = new boolean[nbItems];
            for (Integer i = 0; i < nbItems; i++) {
                if (ll.contains(i)) {
                    toto[i] = true;
                    mSelectedItems.add(i);
                } else {
                    toto[i] = false;
                }
            }
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.param_tags_dialog_title);
        builder.setView(dialogView);

        builder.setMultiChoiceItems(items, toto, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    mSelectedItems.add(which);
                } else if (mSelectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    mSelectedItems.remove(Integer.valueOf(which));
                }
            }
        });

        // Set the action buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Collections.sort(mSelectedItems);
                listener.get().onReturnValue(mSelectedItems);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

}
