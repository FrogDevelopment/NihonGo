package fr.frogdevelopment.nihongo.dico;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Row;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dico.details.DetailsActivity;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;

import static fr.frogdevelopment.nihongo.dico.DicoAdapter.ViewHolder;

public class DicoFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnTouchListener {

    private static final int LOADER_ID = 100;

    private boolean isContextActionBar = false;
    protected boolean isSortByLetter = true;
    protected boolean isFilterByFavorite = false;

    protected DicoAdapter dicoAdapter;

    public DicoFragment() {
        // Empty constructor required for fragment subclasses
    }

    private Type mType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = (Type) getArguments().getSerializable("type");

        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_dico, container, false);
//        rootView.setBackgroundResource(mType.background);

        ButterKnife.inject(this, rootView);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
        int resource;
        switch (mType) {
            case WORD:
                resource = R.layout.row_dico_word;
                break;
            case EXPRESSION:
                resource = R.layout.row_dico_expression;
                break;

            default:
                throw new IllegalStateException("Type inconnue !");
        }
        dicoAdapter = new DicoAdapter(getActivity(), resource);
        setListAdapter(dicoAdapter);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey("query")) {
            return new CursorLoader(getActivity(), NihonGoContentProvider.URI_SEARCH, DicoContract.COLUMNS, null, new String[]{args.getString("query")}, DicoContract.SORT_LETTER + " ASC");
        }

        String selection = isFilterByFavorite ? DicoContract.FAVORITE + "=1" : null;

        String sortOrder = (isSortByLetter ? "" : DicoContract.TAGS + ",") + DicoContract.SORT_LETTER + "," + DicoContract.INPUT + " ASC";

        return new CursorLoader(getActivity(), mType.uri, DicoContract.COLUMNS, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            // There are no results
            Toast.makeText(getActivity(), R.string.no_results, Toast.LENGTH_LONG).show();
        }

        dicoAdapter.swapCursor(data, isSortByLetter);

        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        dicoAdapter.swapCursor(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setOnTouchListener(this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (dicoAdapter.isLetterHeader(position)) {
                    return true;
                }

                ((ListView) parent).setItemChecked(position, ((ListView) parent).isItemChecked(position));
                return false;
            }
        });

        getListView().setMultiChoiceModeListener(new MultiChoiceModeListener() {

            private int rowSelectedNumber = 0;
            final private Set<Integer> selectedRows = new HashSet<>();

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.actions_dico, menu);
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
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setSelector(R.drawable.dico_selector);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.dico, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);

        switch (mType) {
            case WORD:
                searchMenuItem.setVisible(true);
                searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        reset();
                        return true;
                    }
                });
                SearchView searchView = (SearchView) searchMenuItem.getActionView();
                // Get the SearchView and set the searchable configuration
                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                // Assumes current activity is the searchable activity
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
                searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
                searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

                break;

            case EXPRESSION:
                searchMenuItem.setVisible(false);
                break;
        }
    }

    private void reset() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                isSortByLetter = !isSortByLetter;
                getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
                item.setTitle(isSortByLetter ? R.string.action_dico_sort_tag : R.string.action_dico_sort_letter);
                break;

            case R.id.action_new:
                Intent intent;
                intent = new Intent(getActivity(), InputActivity.class);
                intent.putExtra("type", mType);

                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case R.id.action_filter_by_favorite:
                isFilterByFavorite = !isFilterByFavorite;
                item.setChecked(isFilterByFavorite);
                getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
                break;

            default:
                return false;
        }

        return true;
    }


    private void onUpdate(final int position) {
        final Item item = (Item) dicoAdapter.getItem(position);

        startActivity(item.getUpdateIntent(getActivity(), mType));
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void onDelete(final ActionMode actionMode, final Set<Integer> selectedRows) {
        final int nbSelectedRows = selectedRows.size();
        // Ask the user if they want to delete
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_title)
                .setMessage(getResources().getQuantityString(R.plurals.delete_confirmation, nbSelectedRows, nbSelectedRows))
                .setPositiveButton(R.string.positive_button_continue,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (nbSelectedRows == 1) {
                                    final Item item = (Item) dicoAdapter.getItem(selectedRows.iterator().next());
                                    Uri uri = Uri.parse(mType.uri + "/" + item.id);
                                    getActivity().getContentResolver().delete(uri, null, null);
                                } else {
                                    StringBuilder inList = new StringBuilder(nbSelectedRows * 2);
                                    final String[] selectionArgs = new String[nbSelectedRows];
                                    int i = 0;
                                    Item item;
                                    for (Integer position : selectedRows) {
                                        if (i > 0) {
                                            inList.append(",");
                                        }
                                        inList.append("?");

                                        item = (Item) dicoAdapter.getItem(position);
                                        selectionArgs[i] = item.id;
                                        i++;
                                    }

                                    final String selection = "_ID IN (" + inList.toString() + ")";
                                    getActivity().getContentResolver().delete(mType.uri, selection, selectionArgs);
                                }
                                Toast.makeText(getActivity(), R.string.delete_done, Toast.LENGTH_LONG).show();
                                actionMode.finish();
                            }

                        })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private GestureDetector mGestureDetector;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

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
                    intent.putParcelableArrayListExtra("items", items);
                    intent.putExtra("position", i);
                    intent.putExtra("type", mType);

                    startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    return false;
                }

                return false;
            }
            return false;
        }
    }
}
