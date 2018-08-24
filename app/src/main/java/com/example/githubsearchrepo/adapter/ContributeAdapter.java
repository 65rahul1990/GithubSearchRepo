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
import com.example.githubsearchrepo.events.ContributorDetailsEvent;
import com.example.githubsearchrepo.R;
import com.example.githubsearchrepo.database.ContributeEntity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ContributeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ContributeEntity> contributorEntities = new ArrayList<>();
    private Realm realm;

    public ContributeAdapter(Activity activity, ArrayList<ContributeEntity> contributorEntities){
        this.context = activity;
        realm = Realm.getDefaultInstance();
        updateAdapter(contributorEntities);
        this.contributorEntities.clear();
    }

    public void updateAdapter(ArrayList<ContributeEntity> contributorEntities){
        this.contributorEntities.addAll(contributorEntities);
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.grid_item, parent, false);
        return new ContributorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ContributorViewHolder viewHolder = (ContributorViewHolder) holder;
        ContributeEntity contributorEntity = contributorEntities.get(position);
        if(contributorEntity == null){
            return;
        }
        String url = contributorEntity.getAvatar_url();
        String name = contributorEntity.getLogin();
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder))
                .into(viewHolder.contributorAvatar);

        viewHolder.contributorName.setText(name);

        viewHolder.itemView.setOnClickListener(v -> {
            EventBus.getDefault().post(new ContributorDetailsEvent(contributorEntity));
        });
    }

    @Override
    public int getItemCount() {
        if (contributorEntities == null){
            return 0;
        }
        return contributorEntities.size();
    }

    public static class ContributorViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.img_contributor_avatar)
        ImageView contributorAvatar;
        @BindView(R.id.tv_contributor_name)
        TextView contributorName;

        public ContributorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
