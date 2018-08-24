package com.example.githubsearchrepo.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LicenseEntity extends RealmObject {
    @PrimaryKey
    public String key;
    public String name;
    public String spdx_id;
    public String url;
}
