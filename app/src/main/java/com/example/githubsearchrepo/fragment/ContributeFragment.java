package com.example.githubsearchrepo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.adapter.RepoListAdapter;
import com.example.githubsearchrepo.database.ContributeEntity;
import com.example.githubsearchrepo.database.DBManager;
import com.example.githubsearchrepo.database.GithubItemEntity;
import com.example.githubsearchrepo.network.ApiManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContributeFragment extends Fragment {
    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.dataLoadProgress)
    ContentLoadingProgressBar dataLoadProgress;
    @BindView(R.id.img_contributor_avatar)
    ImageView contributorAvatar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.repository_list)
    RecyclerView recyclerView;

    private Realm realm;
    private RealmResults<GithubItemEntity> githubItemEntities;
    private RepoListAdapter repoListAdapter;
    private ContributeEntity contributorEntity;

    public static ContributeFragment newInstance(ContributeEntity contributeEntity){
        ContributeFragment contributeFragment = new ContributeFragment();
        contributeFragment.contributorEntity = contributeEntity;
        return contributeFragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contributor_details_fragment, container, false);
        ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataLoadProgress.hide();
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        githubItemEntities = realm.where(GithubItemEntity.class).equalTo("owner.id", contributorEntity.getId()).findAll().sort("watchers_count", Sort.DESCENDING);
        repoListAdapter = new RepoListAdapter(getActivity(), githubItemEntities);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(repoListAdapter);

        String name = contributorEntity.getLogin();
        String url = contributorEntity.getAvatar_url();
        collapsingToolbar.setTitle(name);

        Glide.with(getActivity())
                .asBitmap()
                .load(url)
                .apply(RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(contributorAvatar);

        contributorDetailsFetch(contributorEntity.getRepos_url());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void reloadAdapter(){
        githubItemEntities = realm.where(GithubItemEntity.class).equalTo("owner.id", contributorEntity.getId()).findAll().sort("watchers_count", Sort.DESCENDING);
        repoListAdapter.updateAdapter(githubItemEntities);
    }

    private void contributorDetailsFetch(String url){
        dataLoadProgress.show();
        Call<ResponseBody> request = ApiManager.getInstance().getOwnerReposRequest(url);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    DBManager.getInstance().createReposFromJsonResponse(response.body());
                    reloadAdapter();
                }
                dataLoadProgress.hide();
            }
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                dataLoadProgress.hide();
            }
        });
    }

}
