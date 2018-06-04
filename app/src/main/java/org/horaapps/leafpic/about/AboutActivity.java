package org.horaapps.leafpic.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
//import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
//import android.text.method.LinkMovementMethod;
import android.widget.ScrollView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

//import org.horaapps.leafpic.BuildConfig;
import org.horaapps.leafpic.R;
//import org.horaapps.leafpic.activities.DonateActivity;
//import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.ApplicationUtils;
import org.horaapps.leafpic.util.ChromeCustomTabs;
//import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedTextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
//import butterknife.OnClick;

//import static org.horaapps.leafpic.util.ServerConstants.GITHUB_CALVIN;
//import static org.horaapps.leafpic.util.ServerConstants.GITHUB_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.GITHUB_GILBERT;
//import static org.horaapps.leafpic.util.ServerConstants.GITHUB_LEAFPIC;
//import static org.horaapps.leafpic.util.ServerConstants.GOOGLE_ABOUT_CALVIN;
//import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_CROWDIN;
//import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_ISSUES;
//import static org.horaapps.leafpic.util.ServerConstants.LEAFPIC_LICENSE;
//import static org.horaapps.leafpic.util.ServerConstants.MAIL_CALVIN;
//import static org.horaapps.leafpic.util.ServerConstants.MAIL_DONALD;
//import static org.horaapps.leafpic.util.ServerConstants.MAIL_GILBERT;
//import static org.horaapps.leafpic.util.ServerConstants.TWITTER_ABOUT_DONALD;
import static org.horaapps.leafpic.util.ServerConstants.TWITTER_ABOUT_GILBERT;

public class AboutActivity extends ThemedActivity implements ContactListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.about_version_item_sub) ThemedTextView appVersion;
    @BindView(R.id.aboutAct_scrollView) ScrollView aboutScrollView;

    @BindView(R.id.list_contributors) RecyclerView rvContributors;

    private ChromeCustomTabs chromeTabs;


    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        chromeTabs = new ChromeCustomTabs(AboutActivity.this);

        initUi();
    }

    @Override
    public void onDestroy() {
        chromeTabs.destroy();
        super.onDestroy();
    }



    private void mail(String mail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + mail));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.send_mail_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        appVersion.setText(ApplicationUtils.getAppVersion());



        ArrayList<Contributor> contributors = new ArrayList<>(1);

        /* Truong */
        Contributor Truong = new Contributor("Dương Nhật Trường", "1420261", R.drawable.truong_profile);
        contributors.add(Truong);

        /* Trung1 */
        Contributor Trung1 = new Contributor("Trần Thành Trung", "1420146", R.drawable.trung1_profile);
        contributors.add(Trung1);

        /* Trung2 */
        Contributor Trung2 = new Contributor("Phan Việt Trung", "1420145", R.drawable.trung2_profile);
        contributors.add(Trung2);


        ContributorsAdapter contributorsAdapter = new ContributorsAdapter(this, contributors, this);
        rvContributors.setHasFixedSize(true);
        rvContributors.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvContributors.setAdapter(contributorsAdapter);


    }

    @CallSuper
    @Override
    public void updateUiElements() {
        super.updateUiElements();
        toolbar.setBackgroundColor(getPrimaryColor());
        toolbar.setNavigationIcon(getToolbarIcon(GoogleMaterial.Icon.gmd_arrow_back));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setScrollViewColor(aboutScrollView);
        setStatusBarColor();
        setNavBarColor();


    }

    @Override
    public void onContactClicked(Contact contact) {
        chromeTabs.launchUrl(contact.getValue());
    }

    @Override
    public void onMailClicked(String mail) {
        mail(mail);
    }
}
