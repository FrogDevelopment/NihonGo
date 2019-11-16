package fr.frogdevelopment.nihongo.review;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.frogdevelopment.nihongo.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class RateAdapter extends ArrayAdapter<CharSequence> {

    private final LayoutInflater mInflater;

    RateAdapter(@NonNull Context context) {
        super(context, R.layout.dropdown_menu_popup_item_rate, context.getResources().getStringArray(R.array.param_learned));
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.dropdown_menu_popup_item_rate, parent, false);
        } else {
            view = convertView;
        }

        TextView labelView = view.findViewById(R.id.rate_label);
        final CharSequence item = getItem(position);
        labelView.setText(item);

        ImageView rate_0View = view.findViewById(R.id.rate_0);
        ImageView rate_1View = view.findViewById(R.id.rate_1);
        ImageView rate_2View = view.findViewById(R.id.rate_2);

        rate_0View.setVisibility(VISIBLE);
        rate_1View.setVisibility(VISIBLE);
        rate_2View.setVisibility(VISIBLE);

        switch (position) {
            case 0:
                rate_0View.setImageResource(R.drawable.ic_star_black_24dp);
                rate_1View.setImageResource(R.drawable.ic_star_border_black_24dp);
                rate_2View.setImageResource(R.drawable.ic_star_border_black_24dp);
                break;
            case 1:
                rate_0View.setImageResource(R.drawable.ic_star_black_24dp);
                rate_1View.setImageResource(R.drawable.ic_star_black_24dp);
                rate_2View.setImageResource(R.drawable.ic_star_border_black_24dp);
                break;
            case 2:
                rate_0View.setImageResource(R.drawable.ic_star_black_24dp);
                rate_1View.setImageResource(R.drawable.ic_star_black_24dp);
                rate_2View.setImageResource(R.drawable.ic_star_black_24dp);
                break;
            case 3:
                rate_0View.setVisibility(INVISIBLE);
                rate_1View.setVisibility(INVISIBLE);
                rate_2View.setVisibility(INVISIBLE);
                break;
        }

        return view;
    }
}
