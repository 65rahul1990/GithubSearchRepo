package com.example.githubsearchrepo.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import okhttp3.ResponseBody;

public class DBManager {
    private static final DBManager ourInstance = new DBManager();
    private Realm realm;

    public static DBManager getInstance() {
        return ourInstance;
    }

    private DBManager() {
        realm = Realm.getDefaultInstance();
    }

    public void createRepos(ResponseBody body){
        String jsonString = null;
        try {
            jsonString = body.string();
            realm.beginTransaction();
            GithubMainEntity githubMainEntity = realm.where(GithubMainEntity.class).findFirst();
            if (githubMainEntity != null && githubMainEntity.isValid()){
                githubMainEntity.deleteFromRealm();
            }
            realm.createOrUpdateObjectFromJson(GithubMainEntity.class, jsonString);
            realm.commitTransaction();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<ContributeEntity> createContributeResponse(ResponseBody responseBody){
        ArrayList<ContributeEntity> contributerEntityArrayList = new ArrayList<>();
        contributerEntityArrayList.clear();
        try {
            String jsonString = responseBody.string();
            JSONArray jsonArray = new JSONArray(jsonString);
            realm.beginTransaction();
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContributeEntity contributerEntity = realm.createOrUpdateObjectFromJson(ContributeEntity.class, jsonObject);
                contributerEntityArrayList.add(contributerEntity);
            }

            realm.commitTransaction();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return contributerEntityArrayList;
    }


    public void createReposFromJsonResponse(ResponseBody responseBody){
        try {
            String jsonString = responseBody.string();
            JSONArray jsonArray = new JSONArray(jsonString);
            realm.beginTransaction();
            realm.createOrUpdateAllFromJson(GithubItemEntity.class, jsonArray);
            realm.commitTransaction();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
