/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.delellis.quicknotes.activity.main;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.ItemClickListener;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_CREATED;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_TITLE;
import static ar.com.delellis.quicknotes.activity.main.NoteAdapter.SORT_BY_UPDATED;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.nextcloud.android.sso.helper.SingleAccountHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.activity.about.AboutActivity;
import ar.com.delellis.quicknotes.activity.editor.EditorActivity;
import ar.com.delellis.quicknotes.activity.error.ErrorActivity;
import ar.com.delellis.quicknotes.activity.login.LoginActivity;
import ar.com.delellis.quicknotes.activity.main.SortingOrderDialogFragment.OnSortingOrderListener;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.IResponseCallback;
import ar.com.delellis.quicknotes.model.Capabilities;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;
import ar.com.delellis.quicknotes.util.CapabilitiesService;

public class MainActivity extends AppCompatActivity implements MainView, OnSortingOrderListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int INTENT_ADD = 100;
    private static final int INTENT_EDIT = 200;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private MaterialCardView homeToolbar;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private FloatingActionButton fab;

    private MainPresenter presenter;
    private NoteAdapter noteAdapter;
    private ItemClickListener itemClickListener;

    private NavigationView navigationView;

    private Set<Tag> tags = new TreeSet<>();

    private List<String> colors = new ArrayList<>();

    private ApiProvider mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int sortRule = preferences.getInt(getString(R.string.setting_sort_by), SORT_BY_UPDATED);
        boolean pinnedFirst = preferences.getBoolean(getString(R.string.setting_pinned_first), true);
        boolean gridViewEnabled = preferences.getBoolean(getString(R.string.setting_grid_view_enabled), true);

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new StaggeredGridLayoutManager(gridViewEnabled ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        presenter = new MainPresenter(this);

        itemClickListener = ((view, position) -> {
            Note note = noteAdapter.get(position);

            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("note", note);
            intent.putExtra("tags", new ArrayList<>(tags));

            startActivityForResult(intent, INTENT_EDIT);
        });

        noteAdapter = new NoteAdapter(this, itemClickListener);
        recyclerView.setAdapter(noteAdapter);

        noteAdapter.setSortRule(sortRule);
        noteAdapter.setFirstPinned(pinnedFirst);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> presenter.getNotes());

        fab = findViewById(R.id.add);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("tags", (Serializable) tags);
            startActivityForResult(intent, INTENT_ADD);
        });

        toolbar = findViewById(R.id.toolbar);
        homeToolbar = findViewById(R.id.home_toolbar);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                noteAdapter.getFilter().filter(query);
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            if (toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });

        setSupportActionBar(toolbar);

        homeToolbar.setOnClickListener(view -> updateToolbars(false));

        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        sortButton.setOnClickListener(view -> openSortingOrderDialogFragment(getSupportFragmentManager(), noteAdapter.getSortRule()));

        drawerLayout = findViewById(R.id.drawerLayout);
        AppCompatImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        AppCompatImageView viewButton = findViewById(R.id.view_mode);
        viewButton.setOnClickListener(view -> {
            boolean gridEnabled = layoutManager.getSpanCount() == 1;
            onGridIconChosen(gridEnabled);
        });

        updateSortingIcon(sortRule);
        updateGridIcon(gridViewEnabled);

        mApi = new ApiProvider(getApplicationContext());
        presenter.getNotes();

        navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateNavigationMenu(List<Note> notes) {
        boolean hasPinned = false;
        boolean hasSharedWithYou = false;
        boolean hasSharedWithOthers = false;

        for (Note note : notes) {
            if (note.getIsPinned()) {
                hasPinned = true;
            }
            if (!note.getShareBy().isEmpty()) {
                hasSharedWithYou = true;
            }
            if (!note.getShareWith().isEmpty()) {
                hasSharedWithOthers = true;
            }
        }

        navigationView.getMenu().findItem(R.id.pinned).setVisible(hasPinned);
        navigationView.getMenu().findItem(R.id.shared_with_others).setVisible(hasSharedWithOthers);
        navigationView.getMenu().findItem(R.id.shared_with_you).setVisible(hasSharedWithYou);

        MenuItem tagsItem = navigationView.getMenu().findItem(R.id.tags_outer);
        tagsItem.setVisible(!tags.isEmpty());

        SubMenu subMenu = tagsItem.getSubMenu();
        subMenu.clear();
        for (Tag tag : tags) {
            subMenu.add(R.id.tags, tag.getId(), 0, tag.getName())
                    .setIcon(R.drawable.ic_tag_grey)
                    .setCheckable(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.close();

        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (item.getItemId() == R.id.switch_account) {
            switch_account();
        } else if (item.getItemId() == R.id.all) {
            noteAdapter.getFilter().filter("");
        } else if (item.getItemId() == R.id.pinned) {
            noteAdapter.getPinnedFilter().filter("");
        } else if (item.getItemId() == R.id.shared_with_others) {
            noteAdapter.getSharedWithOthersFilter().filter("");
        } else if (item.getItemId() == R.id.shared_with_you) {
            noteAdapter.getIsSharedFilter().filter("");
        } else if (item.getGroupId() == R.id.tags) {
            noteAdapter.getTagFilter().filter(item.getTitle());
        }

        return true;
    }

    private void updateToolbars(boolean disableSearch) {
        homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (disableSearch) {
            searchView.setQuery(null, true);
        }
        searchView.setIconified(disableSearch);
    }

    private void switch_account() {
        SingleAccountHelper.setCurrentAccount(this, null);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void openSortingOrderDialogFragment(FragmentManager supportFragmentManager, int sortOrder) {
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);

        SortingOrderDialogFragment.newInstance(sortOrder).show(fragmentTransaction, SortingOrderDialogFragment.SORTING_ORDER_FRAGMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_ADD && resultCode == RESULT_OK) {
            presenter.getNotes();
        } else if (requestCode == INTENT_EDIT && resultCode == RESULT_OK) {
            presenter.getNotes();
        }
    }

    @Override
    public void showLoading() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onGetResult(List<Note> note_list) {
        noteAdapter.setNoteList(note_list);

        // Fill tags.
        tags.clear();
        for (Note note: note_list) {
            tags.addAll(note.getTags());
        }
        HashSet<Tag> hTags = new HashSet<>(tags);
        tags.clear();
        tags.addAll(hTags);

        // Fill colors
        colors.clear();
        for (Note note: note_list) {
            colors.add(note.getColor());
        }
        HashSet<String> hColors = new HashSet<>(colors);
        colors.clear();
        colors.addAll(hColors);

        // Update nav bar.
        updateNavigationMenu(note_list);
    }

    @Override
    public void onErrorLoading(String errorMessage) {
        CapabilitiesService capabilitiesService = new CapabilitiesService(this);
        capabilitiesService.refresh(new IResponseCallback() {
            @Override
            public void onComplete() {
                Capabilities capabilities = capabilitiesService.getCapabilities();
                if (capabilities.isMaintenanceEnabled()) {
                    Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
                    intent.putExtra("errorMessage", getString(R.string.error_maintenance_mode));
                    startActivity(intent);
                    finish();
                } else if (capabilities.getQuicknotesVersion().isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("errorMessage", getString(R.string.error_not_installed));
                    startActivity(intent);
                    finish();
                } else {
                    String errorDetail = errorMessage != null && !errorMessage.isEmpty() ? errorMessage : getString(R.string.error_unknown);
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    intent.putExtra("errorMessage", errorDetail);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onSortingOrderChosen(int sortSelection) {
        noteAdapter.setSortRule(sortSelection);
        updateSortingIcon(sortSelection);

        preferences.edit().putInt(getString(R.string.setting_sort_by), sortSelection).apply();
    }

    public void updateSortingIcon(int sortSelection) {
        AppCompatImageView sortButton = findViewById(R.id.sort_mode);
        switch (sortSelection) {
            case SORT_BY_TITLE:
                sortButton.setImageResource(R.drawable.ic_alphabetical_asc);
                break;
            case SORT_BY_CREATED:
            case SORT_BY_UPDATED:
                sortButton.setImageResource(R.drawable.ic_modification_asc);
                break;
        }
    }

    public void onGridIconChosen(boolean gridEnabled) {
        layoutManager.setSpanCount(gridEnabled ? 2 : 1);
        updateGridIcon(gridEnabled);

        preferences.edit().putBoolean(getString(R.string.setting_grid_view_enabled), gridEnabled).apply();
    }

    public void updateGridIcon(boolean gridEnabled) {
        AppCompatImageView viewButton = findViewById(R.id.view_mode);
        viewButton.setImageResource(gridEnabled ? R.drawable.ic_view_list : R.drawable.ic_view_module);
    }

}