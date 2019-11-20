package fr.frogdevelopment.nihongo.dico;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Row;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.dico.details.DetailsActivity;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

import static android.R.anim.fade_in;
import static android.R.anim.fade_out;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static fr.frogdevelopment.nihongo.R.layout.dialog_help_dico;
import static fr.frogdevelopment.nihongo.R.layout.fragment_dico;
import static fr.frogdevelopment.nihongo.R.layout.row_dico_expression;
import static fr.frogdevelopment.nihongo.R.layout.row_dico_word;
import static fr.frogdevelopment.nihongo.data.Type.EXPRESSION;
import static fr.frogdevelopment.nihongo.dico.DicoAdapter.ViewHolder;

public class DicoFragment extends ListFragment {

    private boolean isContextActionBar = false;

    private Type mType;
    private DicoAdapter mDicoAdapter;
    private DicoViewModel mDicoViewModel;

    private FloatingActionButton mFabAdd;
    private boolean mIsSearchQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = requireArguments();
        mType = (Type) arguments.getSerializable("type");

        if (mType == null) {
            throw new IllegalStateException("Type required");
        }

        mDicoAdapter = new DicoAdapter(requireActivity(), mType == EXPRESSION ? row_dico_expression : row_dico_word);
        setListAdapter(mDicoAdapter);

        mDicoViewModel = new ViewModelProvider(this).get(DicoViewModel.class);

        mIsSearchQuery = arguments.containsKey("query");
        if (mIsSearchQuery) {
            searchData(arguments.getString("query"));
        } else {
            fetchData(false);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(fragment_dico, container, false);

        mFabAdd = rootView.findViewById(R.id.fab_add);
        mFabAdd.setOnClickListener(view -> onShowDetails());

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean doNotShow = PreferencesHelper.getInstance(requireContext()).getBoolean(Preferences.HELP_DICO);
        if (!doNotShow) {
            HelpDialog.show(getParentFragmentManager(), dialog_help_dico, true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        GestureDetector gestureDetector = new GestureDetector(requireActivity(), gestureListener);
        ListView listView = getListView();
        listView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        listView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(multiChoiceListener);
        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (mDicoAdapter.isLetterHeader(position)) {
                return true;
            }

            ((ListView) parent).setItemChecked(position, ((ListView) parent).isItemChecked(position));
            return false;
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_IDLE:
                        mFabAdd.show(true);
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        mFabAdd.hide(true);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dico, menu);

        menu.findItem(R.id.dico_action_filter_by_favorite).setVisible(!mIsSearchQuery);

        MenuItem searchMenuItem = menu.findItem(R.id.dico_menu_search);
        searchMenuItem.setVisible(true);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mFabAdd.hide(true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                int title = mType == EXPRESSION ? R.string.drawer_item_expression : R.string.drawer_item_word;
                requireActivity().setTitle(title);
                fetchData(false);
                mIsSearchQuery = false;
                requireActivity().invalidateOptionsMenu();
                mFabAdd.show(true);
                return true;
            }
        });

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        }
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dico_action_filter_by_favorite:
                boolean isFiltered = item.isChecked();
                fetchData(!isFiltered);
                item.setChecked(!isFiltered);
                break;

            case R.id.dico_help:
                HelpDialog.show(getParentFragmentManager(), dialog_help_dico);
                break;

            default:
                return false;
        }

        return true;
    }

    private void fetchData(boolean isFilteredByFavorite) {
        mDicoViewModel.getAllByType(mType, isFilteredByFavorite).observe(this, rows -> mDicoAdapter.setRows(rows));
    }

    private void searchData(String query) {
        mDicoViewModel.search(mType, query).observe(this, rows -> {
            hideKeyboard();
            mDicoAdapter.setRows(rows);
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        }
    }

    private void onUpdate(final int position) {
        final Item item = (Item) mDicoAdapter.getItem(position);

        Intent intent = new Intent(getActivity(), InputActivity.class);
        intent.putExtra("type", mType);
        intent.putExtra("item", item);

        startActivity(intent);
        requireActivity().overridePendingTransition(fade_in, fade_out);
    }

    private void onShowDetails() {
//            Intent intent;
//            intent = new Intent(getActivity(), InputActivity.class);
//            intent.putExtra("type", mType);
//
//            startActivity(intent);
//            requireActivity().overridePendingTransition(fade_in, fade_out);
    }

    private void onDelete(final ActionMode actionMode, final Set<Integer> selectedRows) {
        final int nbSelectedRows = selectedRows.size();
        // Ask the user if they want to delete
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.delete_title)
                .setMessage(getResources().getQuantityString(R.plurals.delete_confirmation, nbSelectedRows, nbSelectedRows))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Item item;
                    Item[] items = null;
                    for (Integer position : selectedRows) {
                        item = (Item) mDicoAdapter.getItem(position);
                        items = ArrayUtils.add(items, item);
                    }
                    mDicoViewModel.delete(items);
                    Snackbar.make(requireActivity().findViewById(R.id.dico_layout), R.string.delete_done, Snackbar.LENGTH_LONG).show();
                    actionMode.finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private MultiChoiceModeListener multiChoiceListener = new MultiChoiceModeListener() {

        private int rowSelectedNumber = 0;
        final private Set<Integer> selectedRows = new HashSet<>();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            requireActivity().getMenuInflater().inflate(R.menu.dico_context, menu);
            isContextActionBar = true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.action_update).setVisible(rowSelectedNumber == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    onDelete(actionMode, selectedRows);
                    break;

                case R.id.action_update:
                    onUpdate(selectedRows.iterator().next());
                    actionMode.finish();
                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            rowSelectedNumber = 0;
            selectedRows.clear();
            isContextActionBar = false;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (mDicoAdapter.isLetterHeader(position)) {
                return;
            }

            if (checked) {
                rowSelectedNumber++;
                selectedRows.add(position);
            } else {
                selectedRows.remove(position);
                rowSelectedNumber--;
            }
            mode.setTitle(getResources().getQuantityString(R.plurals.action_selection, rowSelectedNumber, rowSelectedNumber));
            mode.invalidate();
        }
    };


    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (!isContextActionBar) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                final int position = getListView().pointToPosition(x, y);
                if (position < 0) {
                    return false;
                }

                final Row row = mDicoAdapter.getItem(position);

                if (row instanceof Item) {
                    View view = getViewByPosition(position, getListView());
                    if (view == null) {
                        return false;
                    }
                    ViewHolder holder = (ViewHolder) view.getTag();

                    holder.switchKanjiKana();

                    return true;
                }
                return false;
            }
            return false;
        }

        private View getViewByPosition(int pos, ListView listView) {
            final int firstListItemPosition = listView.getFirstVisiblePosition();
            final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

            if (pos < firstListItemPosition || pos > lastListItemPosition) {
                return listView.getAdapter().getView(pos, null, listView);
            } else {
                final int childIndex = pos - firstListItemPosition + listView.getHeaderViewsCount();
                return listView.getChildAt(childIndex);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (!isContextActionBar) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                final int position = getListView().pointToPosition(x, y);
                if (position < 0) {
                    return false;
                }

                final Row row = mDicoAdapter.getItem(position);

                if (row instanceof Item) {
                    ArrayList<Item> items = new ArrayList<>(mDicoAdapter.getItems());
                    Item item = (Item) row;
                    int i = items.indexOf(item);

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                    intent.putParcelableArrayListExtra("items", items);
                    intent.putExtra("position", i);
                    intent.putExtra("type", mType);

                    startActivity(intent);
                    requireActivity().overridePendingTransition(fade_in, fade_out);

                    return false;
                }

                return false;
            }
            return false;
        }
    };

}
