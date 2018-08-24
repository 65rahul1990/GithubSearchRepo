package com.example.githubsearchrepo.database;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GithubMainEntity extends RealmObject {
   @PrimaryKey
   public int total_count;
    public boolean incomplete_results;
    public RealmList<GithubItemEntity> items;
    public RealmList<GithubItemEntity> getItems() {
        return items;
    }
}
