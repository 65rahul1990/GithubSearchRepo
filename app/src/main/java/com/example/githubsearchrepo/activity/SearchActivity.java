package com.example.githubsearchrepo.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.githubsearchrepo.events.ContributorDetailsEvent;
import com.example.githubsearchrepo.events.ProjectLinkEvent;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.events.RepoDetailsEvent;
import com.example.githubsearchrepo.fragment.ContributeFragment;
import com.example.githubsearchrepo.fragment.RepoDetailsFragment;
import com.example.githubsearchrepo.fragment.SearchRepoFragment;
import com.example.githubsearchrepo.fragment.WebViewFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flLayout, new SearchRepoFragment())
                    .commit();
        }

    }

    @Subscribe
    public void openDetailFragment(RepoDetailsEvent repoDetailsEvent){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flLayout, RepoDetailsFragment.newInstance(repoDetailsEvent.getGithubItemEntity()))
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void openDetailFragment(ContributorDetailsEvent contributorDetailsEvent){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flLayout, ContributeFragment.newInstance(contributorDetailsEvent.getContributerEntity()))
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void openWebView(ProjectLinkEvent projectLinkEvent){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flLayout,  WebViewFragment.newInstance(projectLinkEvent.getUrl()))
                .addToBackStack(null)
                .commit();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onStart() {
        super.onStart();
       EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



}
