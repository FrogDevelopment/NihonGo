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
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.ListFragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.data.model.Row;
import fr.frogdevelopment.nihongo.dico.details.DetailsActivity;
import fr.frogdevelopment.nihongo.dico.update.UpdateActivity;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;
import static fr.frogdevelopment.nihongo.R.layout.dico_fragment;
import static fr.frogdevelopment.nihongo.R.string.delete;
import static fr.frogdevelopment.nihongo.R.string.delete_done;
import static fr.frogdevelopment.nihongo.R.string.delete_title;
import static fr.frogdevelopment.nihongo.R.string.drawer_item_entries;
import static fr.frogdevelopment.nihongo.dico.DicoAdapter.ViewHolder;

public class DicoFragment extends ListFragment {

    private boolean isContextActionBar = false;

    private DicoAdapter mDicoAdapter;
    private RowViewModel mRowViewModel;

    private FloatingActionButton mFabAdd;
    private boolean mIsSearchQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDicoAdapter = new DicoAdapter(requireActivity());
        setListAdapter(mDicoAdapter);

        mRowViewModel = new ViewModelProvider(this).get(RowViewModel.class);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(dico_fragment, container, false);

        FrameLayout contentView = rootView.findViewById(R.id.dico_content);
        contentView.addView(super.onCreateView(inflater, contentView, savedInstanceState));

        mFabAdd = rootView.findViewById(R.id.fab_add);
        mFabAdd.setOnClickListener(view -> onAddInput());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText(getString(R.string.dico_no_data));
        setListShown(false);

        GestureDetector gestureDetector = new GestureDetector(requireActivity(), gestureListener);
        ListView listView = getListView();
        listView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        listView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(multiChoiceListener);
        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (mDicoAdapter.isHeader(position)) {
                return false;
            }

            listView.setItemChecked(position, listView.isItemChecked(position));
            return true;
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_IDLE:
                        mFabAdd.show();
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        mFabAdd.hide();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        Bundle arguments = getArguments();
        boolean isOnBoarding = arguments != null && arguments.getBoolean("isOnBoarding");
        if (isOnBoarding) {
            showAddPrompt();
        }

        mIsSearchQuery = arguments != null && arguments.containsKey("query");
        if (mIsSearchQuery) {
            searchData(arguments.getString("query"));
        } else {
            fetchData(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dico, menu);

        menu.findItem(R.id.dico_action_filter_by_favorite).setVisible(!mIsSearchQuery);

        MenuItem searchMenuItem = menu.findItem(R.id.dico_menu_search);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mFabAdd.hide();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                requireActivity().setTitle(drawer_item_entries);
                fetchData(false);
                mIsSearchQuery = false;
                requireActivity().invalidateOptionsMenu();
                mFabAdd.show();
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
        searchView.setIconifiedByDefault(false);
        searchView.setImeOptions(IME_ACTION_SEARCH);
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
                DicoHelpDialog.show(getParentFragmentManager());
                break;

            default:
                return false;
        }

        return true;
    }

    private void fetchData(boolean isFilteredByFavorite) {
        mRowViewModel.getAll(isFilteredByFavorite).observe(getViewLifecycleOwner(), this::setData);
    }

    private void searchData(String query) {
        mRowViewModel.search(query).observe(getViewLifecycleOwner(), this::setData);
    }

    private void setData(List<Row> rows) {
        mDicoAdapter.setRows(rows);
        setListShown(true);
    }

    private void onUpdate(final int position) {
        final Row row = mDicoAdapter.getItem(position);

        Intent intent = new Intent(requireContext(), UpdateActivity.class);
        intent.putExtra("item_id", row.id);

        startActivity(intent);
    }

    private void onAddInput() {
        startActivity(new Intent(requireContext(), UpdateActivity.class));
    }

    private void onShowDetails(int position) {
        final Row row = mDicoAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("item_id", row.id);

        startActivity(intent);
    }

    private void onDelete(final ActionMode actionMode, final Set<Integer> selectedRows) {
        final int nbSelectedRows = selectedRows.size();
        // Ask the user if they want to delete
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_warning)
                .setTitle(delete_title)
                .setMessage(getResources().getQuantityString(R.plurals.delete_confirmation, nbSelectedRows, nbSelectedRows))
                .setPositiveButton(delete, (dialog, which) -> {
                    Row row;
                    Integer[] ids = null;
                    for (Integer position : selectedRows) {
                        row = mDicoAdapter.getItem(position);
                        ids = ArrayUtils.add(ids, row.id);
                    }
                    mRowViewModel.delete(ids);
                    Snackbar.make(requireActivity().findViewById(R.id.dico_layout), delete_done, Snackbar.LENGTH_LONG).show();
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
            if (mDicoAdapter.isHeader(position)) {
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

                if (row.id != null) {
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


                if (!mDicoAdapter.isHeader(position)) {
                    onShowDetails(position);

                    return false;
                }

                return false;
            }
            return false;
        }
    };

    private void showAddPrompt() {
        new MaterialTapTargetPrompt.Builder(requireActivity())
                .setTarget(R.id.fab_add)
                .setPrimaryText("Add a new entry")
                .setSecondaryText("Tap the + to start writing your first word")
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        showSearchPrompt();
                    }
                })
                .show();
    }

    private void showSearchPrompt() {
        new MaterialTapTargetPrompt.Builder(requireActivity())
                .setTarget(R.id.dico_menu_search)
                .setPrimaryText("Search a word")
                .setSecondaryText("Tap the icon to quickly filter your data")
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        showDrawerPrompt();
                    }
                })
                .setIcon(R.drawable.ic_baseline_search_24)
                .show();
    }

    private void showDrawerPrompt() {
        Toolbar toolbar = requireActivity().findViewById(androidx.appcompat.R.id.action_bar);
        if (toolbar != null) {
            new MaterialTapTargetPrompt.Builder(requireActivity())
                    .setTarget(toolbar.getChildAt(1))
                    .setPrimaryText("Open the drawer")
                    .setSecondaryText("And select Lessons to download some")
                    .setPromptStateChangeListener((prompt, state) -> {
                    })
                    .setAnimationInterpolator(new FastOutSlowInInterpolator())
                    .setIcon(R.drawable.ic_baseline_menu_24)
                    .show();
        }
    }

}
