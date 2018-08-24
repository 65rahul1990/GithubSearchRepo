package com.example.githubsearchrepo.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RepoOwnerDetailEntity extends RealmObject {
    @PrimaryKey
    private String login;
    private int id;
    private String node_id;
    private String avatar_url;
    private String gravatar_id;
    private String url;
    private String html_url;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    private String type;
    private boolean site_admin;



    public String getAvatar_url() {
        return avatar_url;
    }



    public String getUrl() {
        return url;
    }



    public String getType() {
        return type;
    }


}

