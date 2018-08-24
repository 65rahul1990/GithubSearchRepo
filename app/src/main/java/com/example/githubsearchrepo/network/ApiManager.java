package com.example.githubsearchrepo.network;

import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public class ApiManager {
    private static final ApiManager instance = new ApiManager();

    private final String baseUrl = "https://api.github.com/";

    public static ApiManager getInstance(){
        return instance;
    }


    private <T> T createRetrofitService(Class<T> service){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(MyLoggingInterceptor.provideOkHttpLogging());

        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(service);
    }

    private interface GitSearchApiClient{
        @GET("search/repositories")
        Call<ResponseBody> getRepos(@QueryMap Map<String, Object> queryMap);

        @GET
        Call<ResponseBody> getContributors(@Url String url);

        @GET
        Call<ResponseBody> getOwnerRepos(@Url String url);
    }

    private GitSearchApiClient getService(){
        return createRetrofitService(GitSearchApiClient.class);
    }

    public Call<ResponseBody> searchRequest(Map<String, Object> queryMap){
        return getService().getRepos(queryMap);
    }

    public Call<ResponseBody> contributorsRequest(String baseUrl){
        return getService().getContributors(baseUrl);
    }

    public Call<ResponseBody> getOwnerReposRequest(String baseUrl){
        return getService().getOwnerRepos(baseUrl);
    }

}
