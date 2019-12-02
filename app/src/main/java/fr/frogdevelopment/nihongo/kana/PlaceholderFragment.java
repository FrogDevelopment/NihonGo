package fr.frogdevelopment.nihongo.kana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import fr.frogdevelopment.nihongo.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.kana_placeholder_fragment, container, false);
        int position = requireArguments().getInt(ARG_SECTION_NUMBER);
        final ImageView imageView = root.findViewById(R.id.kana_imageView);
        imageView.setImageResource(position == 0 ? R.drawable.table_hiragana : R.drawable.table_katakana);
        return root;
    }
}