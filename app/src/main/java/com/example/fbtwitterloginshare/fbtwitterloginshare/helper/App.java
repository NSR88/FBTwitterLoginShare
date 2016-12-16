package com.example.fbtwitterloginshare.fbtwitterloginshare.helper;

import android.app.Application;
import android.content.Context;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

	// Note: Your consumer key and secret should be obfuscated in your source code before shipping.
	private static final String TWITTER_KEY = "tE6VxZ8mTB70Rdu4tlXaBcGu3";
	private static final String TWITTER_SECRET = "XttOfEpUI0wDuYx2CAJRhwo9LLNY1devK0FWGzMDBvz857NyAr";
	public static final String TAG = App.class
			.getSimpleName();

	private static Context mContext;
	public static App instance;
	private static void checkInstance() {
		if (instance == null)
			throw new IllegalStateException("Application not created yet!");
	}

	public static App getInstance() {
		checkInstance();
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig));
		mContext = this;
		instance = this;
	}

	public static Context getContext() {
		return mContext;
	}



	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

	}


}