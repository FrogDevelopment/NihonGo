/*
 * Copyright (c) Frog Development 2015.
 */

package fr.frogdevelopment.nihongo.dico;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.frogdevelopment.nihongo.MainActivity;
import fr.frogdevelopment.nihongo.R;
import fr.frogdevelopment.nihongo.contentprovider.DicoContract;
import fr.frogdevelopment.nihongo.data.Item;
import fr.frogdevelopment.nihongo.data.Row;
import fr.frogdevelopment.nihongo.data.Type;
import fr.frogdevelopment.nihongo.dialog.HelpDialog;
import fr.frogdevelopment.nihongo.dico.details.DetailsActivity;
import fr.frogdevelopment.nihongo.dico.input.InputActivity;
import fr.frogdevelopment.nihongo.preferences.Preferences;
import fr.frogdevelopment.nihongo.preferences.PreferencesHelper;

import static fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider.URI_SEARCH_EXPRESSION;
import static fr.frogdevelopment.nihongo.contentprovider.NihonGoContentProvider.URI_SEARCH_WORD;
import static fr.frogdevelopment.nihongo.dico.DicoAdapter.ViewHolder;

public class DicoFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_DICO_ID   = 100;
	private static final int LOADER_FILTER_ID = 110;

	private boolean isContextActionBar = false;
	private boolean isSortByLetter     = true;
	private boolean isFilterByFavorite = false;

	private DicoAdapter dicoAdapter;

	private Type mType;
	private int  currentQuery;
	private FloatingActionButton mFabAdd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_dico, container, false);

		mFabAdd = rootView.findViewById(R.id.fab_add);
		mFabAdd.setOnClickListener(view -> {
			Intent intent;
			intent = new Intent(getActivity(), InputActivity.class);
			intent.putExtra("type", mType);

			startActivity(intent);
			getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		});

		Bundle arguments = getArguments();
		mType = (Type) arguments.getSerializable("type");

		currentQuery = arguments.containsKey("query") ? LOADER_FILTER_ID : LOADER_DICO_ID;
		getLoaderManager().initLoader(currentQuery, arguments, this);

		int resource;
		switch (mType) {
			case WORD:
				resource = R.layout.row_dico_word;
				break;
			case EXPRESSION:
				resource = R.layout.row_dico_expression;
				break;

			default:
				throw new IllegalStateException("Unknown Type : " + mType);
		}

		dicoAdapter = new DicoAdapter(getActivity(), resource);
		setListAdapter(dicoAdapter);

		setHasOptionsMenu(true);

		return rootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		boolean doNotShow = PreferencesHelper.getInstance(getActivity()).getBoolean(Preferences.HELP_DICO);
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri;
		String selection;
		String[] selectionArgs;
		String sortOrder;

		if (id == LOADER_FILTER_ID) {
			uri = mType == Type.WORD ? URI_SEARCH_WORD : URI_SEARCH_EXPRESSION;
			selection = null;
			selectionArgs = new String[]{args.getString("query")};
			sortOrder = DicoContract.SORT_LETTER + " ASC";
		} else {
			uri = mType.uri;
			selection = isFilterByFavorite ? DicoContract.BOOKMARK + "=1" : null;
			selectionArgs = null;
			sortOrder = (isSortByLetter ? "" : DicoContract.TAGS + ",") + DicoContract.SORT_LETTER + "," + DicoContract.INPUT + " ASC";
		}

		return new CursorLoader(getActivity(), uri, DicoContract.COLUMNS, selection, selectionArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		dicoAdapter.swapCursor(data, isSortByLetter);
		getLoaderManager().destroyLoader(loader.getId());

		if (loader.getId() == LOADER_DICO_ID && dicoAdapter.getCount() == 0) {
			new AlertDialog.Builder(getActivity())
					.setMessage(R.string.help_dico_start)
					.setPositiveButton(android.R.string.yes, (dialog, id) -> ((MainActivity) getActivity()).selectItemAtIndex(R.id.navigation_lessons))
					.setNegativeButton(android.R.string.no, null)
					.create()
					.show();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		dicoAdapter.swapCursor(null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		GestureDetector mGestureDetector = new GestureDetector(getActivity(), gestureListener);
		getListView().setOnTouchListener((v, event) -> mGestureDetector.onTouchEvent(event));
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(multiChoiceListener);
		getListView().setOnItemLongClickListener((parent, view1, position, id) -> {
			if (dicoAdapter.isLetterHeader(position)) {
				return true;
			}

			((ListView) parent).setItemChecked(position, ((ListView) parent).isItemChecked(position));
			return false;
		});

		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the options menu from XML
		inflater.inflate(R.menu.dico, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.dico_menu_search);

		searchMenuItem.setVisible(true);
		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				getLoaderManager().restartLoader(currentQuery, null, DicoFragment.this);
				return true;
			}
		});

		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
		searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.dico_action_sort:
				isSortByLetter = !isSortByLetter;
				getLoaderManager().restartLoader(currentQuery, getArguments(), this);
				item.setTitle(isSortByLetter ? R.string.action_dico_sort_tag : R.string.action_dico_sort_letter);
				break;

			case R.id.dico_action_filter_by_favorite:
				isFilterByFavorite = !isFilterByFavorite;
				item.setChecked(isFilterByFavorite);
				getLoaderManager().restartLoader(currentQuery, getArguments(), this);
				break;

			case R.id.dico_help:
				HelpDialog.show(getFragmentManager(), R.layout.dialog_help_dico);
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
		getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void onDelete(final ActionMode actionMode, final Set<Integer> selectedRows) {
		final int nbSelectedRows = selectedRows.size();
		// Ask the user if they want to delete
		new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.ic_warning)
				.setTitle(R.string.delete_title)
				.setMessage(getResources().getQuantityString(R.plurals.delete_confirmation, nbSelectedRows, nbSelectedRows))
				.setPositiveButton(R.string.positive_button_continue, (dialog, which) -> {
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
					Snackbar.make(getActivity().findViewById(R.id.dico_layout), R.string.delete_done, Snackbar.LENGTH_LONG).show();
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
			getActivity().getMenuInflater().inflate(R.menu.dico_context, menu);
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
	};
}
