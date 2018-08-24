package com.example.githubsearchrepo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.githubsearchrepo.events.ProjectLinkEvent;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.adapter.ContributeAdapter;
import com.example.githubsearchrepo.database.ContributeEntity;
import com.example.githubsearchrepo.database.DBManager;
import com.example.githubsearchrepo.database.GithubItemEntity;
import com.example.githubsearchrepo.database.RepoOwnerDetailEntity;
import com.example.githubsearchrepo.network.ApiManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RepoDetailsFragment extends Fragment {
    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_project_link)
    TextView tvProjectLink;
    @BindView(R.id.tv_description)
    TextView tvDescription;
    @BindView(R.id.dataLoadProgress)
    ContentLoadingProgressBar dataLoadProgress;
    @BindView(R.id.img_contributor_avatar)
    ImageView contributorAvatar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.tv_contributor) TextView tv_contributor;


    private Realm realm;
    private GithubItemEntity githubItemEntity;
    private ContributeAdapter contributerAdapter;
    private ArrayList<ContributeEntity> contributerEntityArrayList = new ArrayList<>();

    public static RepoDetailsFragment newInstance(GithubItemEntity githubItemEntity){
        RepoDetailsFragment repoDetailsFragment = new RepoDetailsFragment();
        repoDetailsFragment.githubItemEntity = githubItemEntity;
        return repoDetailsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.repo_details_fragment, container, false);
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


        String projectLink = githubItemEntity.getHtml_url();
        String description = githubItemEntity.getDescription();
        RepoOwnerDetailEntity ownerEntity = githubItemEntity.getOwner();
        String url = ownerEntity.getAvatar_url();
        String name = githubItemEntity.getName();
        collapsingToolbar.setTitle(name);

        Glide.with(getActivity())
                .asBitmap()
                .load(url)
                .apply(RequestOptions.circleCropTransform()
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder))
                .into(contributorAvatar);


        String text = "<a href='http://www.google.com'>" + projectLink + "</a>";
        tvProjectLink.setText(Html.fromHtml(text));
        tvDescription.setText(description);
        contributerEntityArrayList.clear();
        RealmResults<ContributeEntity> contributerEntities = realm.where(ContributeEntity.class).findAll();
        contributerEntityArrayList.addAll(contributerEntities);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        contributerAdapter = new ContributeAdapter(getActivity(), contributerEntityArrayList);
        recyclerView.setAdapter(contributerAdapter);

        getContributors(githubItemEntity.getContributors_url());
    }

    private void updateAdapter(){

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.llProjectLink)
    public void openLink(){
        String projectLink = githubItemEntity.getHtml_url();
        EventBus.getDefault().post(new ProjectLinkEvent(projectLink));
    }

    private void getContributors(String url){
        dataLoadProgress.show();
        Call<ResponseBody> request = ApiManager.getInstance().contributorsRequest(url);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                 ArrayList<ContributeEntity> contributerEntityArrayList = DBManager.getInstance().createContributeResponse(response.body());
                    contributerAdapter.updateAdapter(contributerEntityArrayList);
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
