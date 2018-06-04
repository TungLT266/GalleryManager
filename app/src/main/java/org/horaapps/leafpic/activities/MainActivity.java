package org.horaapps.leafpic.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.about.AboutActivity;
import org.horaapps.leafpic.activities.base.SharedMediaActivity;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.fragments.AlbumsFragment;
import org.horaapps.leafpic.fragments.EditModeListener;
import org.horaapps.leafpic.fragments.NothingToShowListener;
import org.horaapps.leafpic.fragments.RvMediaFragment;
import org.horaapps.leafpic.interfaces.MediaClickListener;
import org.horaapps.leafpic.timeline.TimelineFragment;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.DeviceUtils;
import org.horaapps.leafpic.util.LegacyCompatFileProvider;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.ItemListener;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ABOUT;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_ALBUMS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_ALL_MEDIA;
//import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_DONATE;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_HIDDEN_FOLDERS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_SETTINGS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_TIMELINE;
//import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NAVIGATION_ITEM_WALLPAPERS;
import static org.horaapps.leafpic.views.navigation_drawer.NavigationDrawer.NavigationItem;

public class MainActivity extends SharedMediaActivity implements
        MediaClickListener, AlbumsFragment.AlbumClickListener,
        NothingToShowListener, EditModeListener, ItemListener {

    public static final String ARGS_PICK_MODE = "pick_mode";

    private static final String SAVE_FRAGMENT_MODE = "fragment_mode";

    public @interface FragmentMode {
        int MODE_ALBUMS = 1001;
        int MODE_MEDIA = 1002;
        int MODE_TIMELINE = 1003;
    }

    @BindView(R.id.fab_camera) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout navigationDrawer;
    @BindView(R.id.home_navigation_drawer) NavigationDrawer navigationDrawerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.coordinator_main_layout) CoordinatorLayout mainLayout;

    private AlbumsFragment albumsFragment;
    private RvMediaFragment rvMediaFragment;

    private boolean pickMode = false;
    private Unbinder unbinder;

    @FragmentMode private int fragmentMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        initUi();
        pickMode = getIntent().getBooleanExtra(ARGS_PICK_MODE, false);

        if (savedInstanceState == null) {
            fragmentMode = FragmentMode.MODE_ALBUMS;
            initAlbumsFragment();
            setContentFragment();

            return;
        }


        restoreState(savedInstanceState);

        switch (fragmentMode) {

            case FragmentMode.MODE_MEDIA:
                rvMediaFragment = (RvMediaFragment) getSupportFragmentManager().findFragmentByTag(RvMediaFragment.TAG);
                rvMediaFragment.setListener(this);
                rvMediaFragment.setEditModeListener(this);
                rvMediaFragment.setNothingToShowListener(this);
                break;

            case FragmentMode.MODE_ALBUMS:
                albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
                albumsFragment.setListener(this);
                albumsFragment.setEditModeListener(this);
                albumsFragment.setNothingToShowListener(this);
                break;

            case FragmentMode.MODE_TIMELINE:
                TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag(TimelineFragment.TAG);
                timelineFragment.setEditModeListener(this);
                timelineFragment.setNothingToShowListener(this);
                setupUiForTimeline();
        }
    }

    private void setContentFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, albumsFragment, AlbumsFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void initAlbumsFragment() {
        albumsFragment = new AlbumsFragment();
        albumsFragment.setListener(this);
        albumsFragment.setEditModeListener(this);
        albumsFragment.setNothingToShowListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_FRAGMENT_MODE, fragmentMode);
        super.onSaveInstanceState(outState);
    }

    private void restoreState(@NonNull Bundle savedInstance) {
        fragmentMode = savedInstance.getInt(SAVE_FRAGMENT_MODE, FragmentMode.MODE_ALBUMS);
    }

    private void displayAlbums(boolean hidden) {
        fragmentMode = FragmentMode.MODE_ALBUMS;
        unlockNavigationDrawer();
        if (albumsFragment == null) initAlbumsFragment();
        albumsFragment.displayAlbums(hidden);
        setContentFragment();
    }

    public void displayMedia(Album album) {
        rvMediaFragment = RvMediaFragment.make(album);

        fragmentMode = FragmentMode.MODE_MEDIA;
        lockNavigationDrawer();

        rvMediaFragment.setListener(this);
        rvMediaFragment.setEditModeListener(this);
        rvMediaFragment.setNothingToShowListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, rvMediaFragment, RvMediaFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    public void displayTimeline(Album album) {
        TimelineFragment fragment = TimelineFragment.newInstance(album);

        fragmentMode = FragmentMode.MODE_TIMELINE;

        fragment.setEditModeListener(this);
        fragment.setNothingToShowListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment, TimelineFragment.TAG)
                .addToBackStack(null)
                .commit();

        setupUiForTimeline();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onMediaClick(Album album, ArrayList<Media> media, int position) {

        if (!pickMode) {
            Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, album);
            try {
                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
                startActivity(intent);
            } catch (Exception e) {

                intent.setAction(SingleMediaActivity.ACTION_OPEN_ALBUM_LAZY);
                intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media.get(position));
                startActivity(intent);
            }

        } else {

            Media m = media.get(position);
            Uri uri = LegacyCompatFileProvider.getUri(getApplicationContext(), m.getFile());
            Intent res = new Intent();
            res.setData(uri);
            res.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(RESULT_OK, res);
            finish();
        }
    }

    @Override
    public void changedNothingToShow(boolean nothingToShow) {
        enableNothingToSHowPlaceHolder(nothingToShow);
    }

    @Override
    public void changedEditMode(boolean editMode, int selected, int total, @Nullable View.OnClickListener listener, @Nullable String title) {
        if (editMode) {
            updateToolbar(
                    getString(R.string.toolbar_selection_count, selected, total),
                    GoogleMaterial.Icon.gmd_check, listener);
        } else if (inAlbumMode()) {
            showDefaultToolbar();
        } else {
            updateToolbar(title, GoogleMaterial.Icon.gmd_arrow_back, v -> goBackToAlbums());
        }
    }

    @Override
    public void onItemsSelected(int count, int total) {
        toolbar.setTitle(getString(R.string.toolbar_selection_count, count, total));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DeviceUtils.isPortrait(getResources())) {
            fab.setVisibility(View.VISIBLE);
            fab.animate().translationY(fab.getHeight() * 2).start();
        } else
            fab.setVisibility(View.GONE);
    }

    public void goBackToAlbums() {
        fragmentMode = FragmentMode.MODE_ALBUMS;
        unlockNavigationDrawer();
        getSupportFragmentManager().popBackStack();
        albumsFragment = (AlbumsFragment) getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
        selectNavigationItem(NAVIGATION_ITEM_ALL_ALBUMS);
        showDefaultToolbar();
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        setupNavigationDrawer();
        setupFAB();
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle
                (this, navigationDrawer, toolbar,
                        R.string.drawer_open, R.string.drawer_close);

        navigationDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationDrawerView.setListener(this);
        navigationDrawerView.setAppVersion(BuildConfig.VERSION_NAME);
    }

    private void setupFAB() {
        fab.setImageDrawable(new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_camera_alt).color(Color.WHITE));
        fab.setOnClickListener(v -> startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)));
    }

    private void closeDrawer() {
        navigationDrawer.closeDrawer(GravityCompat.START);
    }

    private void askPassword() {

        Security.authenticateUser(MainActivity.this, new Security.AuthCallBack() {
            @Override
            public void onAuthenticated() {
                closeDrawer();
                selectNavigationItem(NAVIGATION_ITEM_HIDDEN_FOLDERS);
                displayAlbums(true);
            }

            @Override
            public void onError() {
                Toast.makeText(getApplicationContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        new Thread(() -> {
            if (Prefs.getLastVersionCode() < BuildConfig.VERSION_CODE) {
                String titleHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s <b>%s</b></font>", getTextColor(), getString(R.string.changelog), BuildConfig.VERSION_NAME),
                        buttonHtml = String.format(Locale.ENGLISH, "<font color='%d'>%s</font>", getAccentColor(), getString(R.string.view).toUpperCase());
                Snackbar snackbar = Snackbar
                        .make(mainLayout, StringUtils.html(titleHtml), Snackbar.LENGTH_LONG)
                        .setAction(StringUtils.html(buttonHtml), view -> AlertDialogsHelper.showChangelogDialog(MainActivity.this));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getBackgroundColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    snackbarView.setElevation(getResources().getDimension(R.dimen.snackbar_elevation));
                snackbar.show();
                Prefs.setLastVersionCode(BuildConfig.VERSION_CODE);
            }
        }).start();
    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();

        toolbar.setPopupTheme(getPopupToolbarStyle());
        toolbar.setBackgroundColor(getPrimaryColor());



        setStatusBarColor();
        setNavBarColor();

        fab.setBackgroundTintList(ColorStateList.valueOf(getAccentColor()));
        fab.setVisibility(Hawk.get(getString(R.string.preference_show_fab), true) ? View.VISIBLE : View.GONE);
        mainLayout.setBackgroundColor(getBackgroundColor());


        setAllScrollbarsColor();

        navigationDrawerView.setTheme(getPrimaryColor(), getBackgroundColor(), getTextColor(), getIconColor());


        setRecentApp(getString(R.string.app_name));
    }

    private void setAllScrollbarsColor() {
        Drawable drawableScrollBar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_scrollbar);
        drawableScrollBar.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(), PorterDuff.Mode.SRC_ATOP));
    }

    private void updateToolbar(String title, IIcon icon, View.OnClickListener onClickListener) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(getToolbarIcon(icon));
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    private void showDefaultToolbar() {
        updateToolbar(
                getString(R.string.app_name),
                GoogleMaterial.Icon.gmd_menu,
                v -> navigationDrawer.openDrawer(GravityCompat.START));
    }

    public void enableNothingToSHowPlaceHolder(boolean status) {
        findViewById(R.id.nothing_to_show_placeholder).setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Deprecated
    public void checkNothing(boolean status) {

        ((TextView) findViewById(R.id.emoji_easter_egg)).setTextColor(getSubTextColor());
        ((TextView) findViewById(R.id.nothing_to_show_text_emoji_easter_egg)).setTextColor(getSubTextColor());

        if (status && Prefs.showEasterEgg()) {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.VISIBLE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_emoji_easter_egg).setVisibility(View.GONE);
            findViewById(R.id.nothing_to_show_placeholder).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationDrawerView.refresh();
    }

    //region MENU

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {

            case R.id.settings:
                SettingsActivity.startActivity(this);
                return true;









            default:

                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        if (inAlbumMode()) {
            if (!albumsFragment.onBackPressed()) {
                if (navigationDrawer.isDrawerOpen(GravityCompat.START)) closeDrawer();
                else finish();
            }

        } else if (inTimelineMode()) {
            goBackToAlbums();

        } else if (inMediaMode() && !rvMediaFragment.onBackPressed()) {
            goBackToAlbums();
        }
    }

    @Override
    public void onAlbumClick(Album album) {
        displayMedia(album);
    }

    public void onItemSelected(@NavigationItem int navigationItemSelected) {
        closeDrawer();
        switch (navigationItemSelected) {

            case NAVIGATION_ITEM_ALL_ALBUMS:
                displayAlbums(false);
                selectNavigationItem(navigationItemSelected);
                break;

            case NAVIGATION_ITEM_ALL_MEDIA:
                displayMedia(Album.getAllMediaAlbum());
                break;

            case NAVIGATION_ITEM_TIMELINE:
                displayTimeline(Album.getAllMediaAlbum());
                selectNavigationItem(navigationItemSelected);
                break;

            case NAVIGATION_ITEM_HIDDEN_FOLDERS:
                if (Security.isPasswordOnHidden()) {
                    askPassword();
                } else {
                    selectNavigationItem(navigationItemSelected);
                    displayAlbums(true);
                }
                break;



            case NavigationDrawer.NAVIGATION_ITEM_AFFIX:
                Intent i = new Intent(getBaseContext(), AffixActivity.class);
                startActivity(i);

                break;
            case NAVIGATION_ITEM_SETTINGS:
                SettingsActivity.startActivity(this);
                break;

            case NAVIGATION_ITEM_ABOUT:
                AboutActivity.startActivity(this);
                break;
        }
    }

    private void selectNavigationItem(@NavigationItem int navItem) {
        navigationDrawerView.selectNavItem(navItem);
    }

    private boolean inAlbumMode() {
        return fragmentMode == FragmentMode.MODE_ALBUMS;
    }

    private boolean inMediaMode() {
        return fragmentMode == FragmentMode.MODE_MEDIA;
    }

    private boolean inTimelineMode() {
        return fragmentMode == FragmentMode.MODE_TIMELINE;
    }

    private void lockNavigationDrawer() {
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void unlockNavigationDrawer() {
        navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setupUiForTimeline() {
        lockNavigationDrawer();
        updateToolbar(getString(R.string.timeline_toolbar_title), GoogleMaterial.Icon.gmd_arrow_back, v -> goBackToAlbums());
    }
}
