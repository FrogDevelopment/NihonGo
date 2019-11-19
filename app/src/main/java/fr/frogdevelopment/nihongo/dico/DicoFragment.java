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

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static fr.frogdevelopment.nihongo.dico.DicoAdapter.ViewHolder;

public class DicoFragment extends ListFragment {

    private boolean isContextActionBar = false;
    private boolean isFilterByFavorite = false;

    private DicoViewModel mDicoViewModel;
    private DicoAdapter dicoAdapter;

    private Type mType;
    private FloatingActionButton mFabAdd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = requireArguments();
        mType = (Type) arguments.getSerializable("type");

        if (mType == null) {
            throw new IllegalStateException("Type required");
        }

        int resource = mType == Type.EXPRESSION ? R.layout.row_dico_expression : R.layout.row_dico_word;
        dicoAdapter = new DicoAdapter(requireActivity(), resource);
        setListAdapter(dicoAdapter);

        mDicoViewModel = new ViewModelProvider(this).get(DicoViewModel.class);
        mDicoViewModel.getAllByType(mType, isFilterByFavorite).observe(this, rows -> dicoAdapter.setRows(rows));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_dico, container, false);

        mFabAdd = rootView.findViewById(R.id.fab_add);
        mFabAdd.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(getActivity(), InputActivity.class);
            intent.putExtra("type", mType);

            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });


        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean doNotShow = PreferencesHelper.getInstance(requireContext()).getBoolean(Preferences.HELP_DICO);
        if (!doNotShow) {
            HelpDialog.show(getFragmentManager(), R.layout.dialog_help_dico, true);
        }

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        mFabAdd.show(true);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        mFabAdd.hide(true);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Uri uri;
//        String selection;
//        String[] selectionArgs;
//        String sortOrder;
//
//        if (id == LOADER_FILTER_ID && args != null) {
//            uri = mType == Type.WORD ? URI_SEARCH_WORD : URI_SEARCH_EXPRESSION;
//            selection = null;
//            selectionArgs = new String[]{args.getString("query")};
//            sortOrder = DicoContract.SORT_LETTER + " ASC";
//        } else {
//            uri = mType.uri;
//            selection = isFilterByFavorite ? DicoContract.BOOKMARK + "=1" : null;
//            selectionArgs = null;
//            sortOrder = (isSortByLetter ? "" : DicoContract.TAGS + ",") + DicoContract.SORT_LETTER + "," + DicoContract.INPUT + " ASC";
//        }
//
//        return new CursorLoader(requireActivity(), uri, DicoContract.COLUMNS, selection, selectionArgs, sortOrder);
//    }
//
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        dicoAdapter.swapCursor(data, isSortByLetter);
//        LoaderManager.getInstance(this).destroyLoader(loader.getId());
//
//        if (loader.getId() == LOADER_DICO_ID && dicoAdapter.getCount() == 0) {
//            new MaterialAlertDialogBuilder(requireContext())
//                    .setMessage(R.string.help_dico_start)
//                    .setPositiveButton(android.R.string.yes, (dialog, id) -> ((MainActivity) requireActivity()).selectItemAtIndex(R.id.navigation_lessons))
//                    .setNegativeButton(android.R.string.no, null)
//                    .create()
//                    .show();
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        GestureDetector mGestureDetector = new GestureDetector(getActivity(), gestureListener);
        ListView listView = getListView();
        listView.setOnTouchListener((v, event) -> {
            v.performClick();
            return mGestureDetector.onTouchEvent(event);
        });
        listView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(multiChoiceListener);
        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (dicoAdapter.isLetterHeader(position)) {
                return true;
            }

            ((ListView) parent).setItemChecked(position, ((ListView) parent).isItemChecked(position));
            return false;
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.dico, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.dico_menu_search);
        searchMenuItem.setVisible(true);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
//                LoaderManager.getInstance(DicoFragment.this).restartLoader(currentQuery, null, DicoFragment.this);
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
                isFilterByFavorite = !isFilterByFavorite;
                item.setChecked(isFilterByFavorite);
//                LoaderManager.getInstance(this).restartLoader(currentQuery, getArguments(), this);
                break;

            case R.id.dico_help:
                HelpDialog.show(getParentFragmentManager(), R.layout.dialog_help_dico);
                break;

            default:
                return false;
        }

        return true;
    }

    private void onUpdate(final int position) {
        final Item item = (Item) dicoAdapter.getItem(position);

        Intent intent = new Intent(getActivity(), InputActivity.class);
        intent.putExtra("type", mType);
        intent.putExtra("item", item);

        startActivity(intent);
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                        item = (Item) dicoAdapter.getItem(position);
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
            if (dicoAdapter.isLetterHeader(position)) {
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

                final Row row = dicoAdapter.getItem(position);

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

                final Row row = dicoAdapter.getItem(position);

                if (row instanceof Item) {
                    ArrayList<Item> items = new ArrayList<>(dicoAdapter.getItems());
                    Item item = (Item) row;
                    int i = items.indexOf(item);

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                    intent.putParcelableArrayListExtra("items", items);
                    intent.putExtra("position", i);
                    intent.putExtra("type", mType);

                    startActivity(intent);
                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    return false;
                }

                return false;
            }
            return false;
        }
    };
}
