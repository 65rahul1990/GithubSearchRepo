package com.example.githubsearchrepo.network;

import com.example.githubsearchrepo.BuildConfig;
import com.ihsanbal.logging.LoggingInterceptor;

import okhttp3.internal.platform.Platform;

public class MyLoggingInterceptor {
    public static LoggingInterceptor provideOkHttpLogging(){
        return new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .build();
    }
}
