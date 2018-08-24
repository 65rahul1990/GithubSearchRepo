package com.example.githubsearchrepo.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.events.RepoDetailsEvent;
import com.example.githubsearchrepo.database.GithubItemEntity;
import com.example.githubsearchrepo.database.RepoOwnerDetailEntity;

import org.greenrobot.eventbus.EventBus;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by yellappa on 18/1/18.
 */

public class RepoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
   private RealmResults<GithubItemEntity> githubItemEntities;
   private Context context;
   private Realm realm;

    public RepoListAdapter(Activity activity, RealmResults<GithubItemEntity> githubItemEntities){
        context = activity;
        realm = Realm.getDefaultInstance();
        updateAdapter(githubItemEntities);

    }

    public void updateAdapter(RealmResults<GithubItemEntity> githubItemEntities){
      this.githubItemEntities = githubItemEntities;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.repo_list_item, parent, false);
        return new RepoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final RepoViewHolder repoViewHolder = (RepoViewHolder) holder;
        GithubItemEntity githubItemEntity = githubItemEntities.get(position);
        if(githubItemEntity == null){
            return;
        }

        String name = githubItemEntity.getName();
        String fullName = githubItemEntity.getFull_name();
        int watcherCount = githubItemEntity.getWatchers_count();
        int commitCount = githubItemEntity.getForks_count();
        int starCount = githubItemEntity.getStargazers_count();

            RepoOwnerDetailEntity ownerEntity = githubItemEntity.getOwner();
            String url = ownerEntity.getAvatar_url();
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.user_placeholder)
                            .error(R.drawable.user_placeholder))
                    .into(repoViewHolder.imageView);

        repoViewHolder.tvName.setText(name);
        repoViewHolder.tvFullName.setText(fullName);
        repoViewHolder.tvWatcherCount.setText(String.valueOf(watcherCount));
        repoViewHolder.tvCommitCount.setText(String.valueOf(commitCount));
        repoViewHolder.tvStarCount.setText(String.valueOf(starCount));

        repoViewHolder.itemView.setOnClickListener(v -> {
            EventBus.getDefault().post(new RepoDetailsEvent(githubItemEntity));
        });
    }

    @Override
    public int getItemCount() {
        if (githubItemEntities == null){
            return 0;
        }
        return githubItemEntities.size();
    }

    public static class RepoViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvFullName) TextView tvFullName;
        @BindView(R.id.tvWatcherCount) TextView tvWatcherCount;
        @BindView(R.id.tvCommitCount) TextView tvCommitCount;
        @BindView(R.id.thumbnail) ImageView imageView;
        @BindView(R.id.tvStarCount) TextView tvStarCount;

        public RepoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
