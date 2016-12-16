package com.example.fbtwitterloginshare.fbtwitterloginshare;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.yoyomap.app.R;
import com.yoyomap.app.constants.AppGlobalConstants;
import com.yoyomap.app.helper.App;
import com.yoyomap.app.helper.AppSharedPreferences;
import com.yoyomap.app.helper.CommonUtilities;
import com.yoyomap.app.helper.ConnectivityUtil;
import com.yoyomap.app.helper.DataHelper;
import com.yoyomap.app.helper.Logs;
import com.yoyomap.app.helper.Toaster;
import com.yoyomap.app.receivers.SchedulerCellSite;
import com.yoyomap.app.receivers.SchedulerEventReceiver;
import com.yoyomap.app.services.GPSLoggerService;
import com.yoyomap.app.services.SchedulerEventService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by root on 30/10/15.
 */
public class AccountManagementActivity extends Activity implements View.OnClickListener{
    Context ctx;
    private static final String class_name = "AccountManagementActivity";
    private TextView twitter_value_txt, fb_value_txt;

    private LinearLayout fb_layout, twitter_layout;

    private String fb_username = "";
    private String twitter_username = "";

    private boolean isFacebookAuthenticated = false, isTwitterAuthenticated = false;

    private String social_media_email_str = "", social_media_userid_str = "", social_media_username_str = "", social_media_profile_pic_url = "", social_media_cover_pic_url = "", social_media_city = "", social_media_country="", social_user_friendly_name="", social_media_firstname = "", social_media_lastname = "", social_user_auth_token = "";

    private CallbackManager mCallbackManager;
    private int loginVia;
    TwitterAuthClient mTwitterAuthClient = new TwitterAuthClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            ActionBar actionBar = getActionBar();

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.actionbar_standard_with_back);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);


            FacebookSdk.sdkInitialize(getApplicationContext());
//
            LayoutInflater mInflater = LayoutInflater.from(this);
            mCustomView = actionBar.getCustomView();

            ctx = AccountManagementActivity.this;

            setContentView(R.layout.activity_account_management);

            initVbs();
//            updateVbs();
            initializeViews(mCustomView);
            updateViews();
            clickEvents();


            FacebookSdk.sdkInitialize(this.getApplicationContext());

            mCallbackManager = CallbackManager.Factory.create();


            LoginManager.getInstance().registerCallback(mCallbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(final LoginResult loginResult) {
                            Log.d("Success", "Login");
                            loginVia = AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID;

                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject user,
                                                GraphResponse response) {
                                            // Application code

//                                    {Response:  responseCode: 200, graphObject: {"id":"1693435637554955","name":"Adisoft Indore","email":"testthe17@gmail.com","gender":"male","birthday":"11\/15\/1988","location":{"id":"112027728823762","name":"Indore, India"}}, error: null}
                                            Log.v("LoginActivity", response.toString());
                                            social_media_username_str = user.optString("name");
                                            social_media_userid_str = user.optString("id");
                                            if (user.has("email")) {
                                                social_media_email_str = user.optString("email").trim();
                                            }
                                            social_media_profile_pic_url = "http://graph.facebook.com/" + social_media_userid_str + "/picture";
                                            if (user.has("first_name")) {
                                                social_media_firstname = user.optString("first_name");
                                            }
                                            if (user.has("last_name")) {
                                                social_media_lastname = user.optString("last_name");
                                            }
                                            try {
                                                JSONObject json = response.getJSONObject();
                                                JSONObject locationJsonObj = new JSONObject();
                                                if (json.has("first_name")) {
                                                    social_user_friendly_name = json.getString("first_name");
                                                } else {
                                                    social_user_friendly_name = "";
                                                }
                                                if (json.has("location")) {
                                                    locationJsonObj = json.getJSONObject("location");
                                                    if (locationJsonObj.has("name")) {
                                                        String locationName = locationJsonObj.getString("name"); // this will return you the album's name.
                                                        social_media_city = locationName.split(", ")[0];
                                                        social_media_country = locationName.split(", ")[1];
                                                    }
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Log.v("LoginActivity", user.toString());
                                            social_user_auth_token = loginResult.getAccessToken().getToken();
//                                            getFBCoverImageURL(social_media_userid_str, loginResult.getAccessToken());


                                            makeURLForUpdateSNInfoAndFireWebService(1);


                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender, birthday, location, first_name");
                            request.setParameters(parameters);
                            request.executeAsync();


                        }

                        @Override
                        public void onCancel() {

                            Toaster.popLongToast(ctx, "Login Cancel");
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Toaster.popLongToast(ctx, exception.getMessage());

                        }
                    });


            if(isFacebookAuthenticated || isTwitterAuthenticated) {
                makeURLToGetUserSnDetailsAndFireRequest();
            }



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private void initVbs() {
        isFacebookAuthenticated = AppSharedPreferences.loadIsFBAuthenticated(ctx);
        isTwitterAuthenticated = AppSharedPreferences.loadIsTwitterAuthenticated(ctx);
        mTwitterAuthClient = new TwitterAuthClient();

    }

    private void initializeViews(View ActionBarView) {
        try {
            twitter_value_txt = (TextView)findViewById(R.id.twitter_value_txt);
            fb_value_txt = (TextView)findViewById(R.id.fb_value_txt);

            fb_layout = (LinearLayout)findViewById(R.id.fb_layout);
            twitter_layout = (LinearLayout)findViewById(R.id.twitter_layout);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private void updateViews() {

    }

    private void clickEvents() {

        try {
            fb_layout.setOnClickListener(this);
            twitter_layout.setOnClickListener(this);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.fb_layout:
//                    if(CommonUtilities.isFacebookLoggedIn())
//                        LoginManager.getInstance().logOut();

                    resetVariablesIfSocialSignupFailed();

                    if(isFacebookAuthenticated) {
                        // show toast
//                        Toaster.popShortToast(ctx, getResources().getString(R.string.popup_authenticated_via_fb));
                        showUnlinkConfirmationPopup(getResources().getString(R.string.popup_authenticated_via_fb));
                    } else {
                        // call fb for authentication and then call update_user_sn
                        if(CommonUtilities.isOnline(ctx)) {
                            //authentication process for fb
                            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_birthday", "public_profile", "user_location"));

                        } else {
                            Toaster.popInternetUnavailableToast(ctx);
                        }

                    }


                    break;
                case R.id.twitter_layout:

                    resetVariablesIfSocialSignupFailed();

                    if(isTwitterAuthenticated) {
                        // show toast
//                        Toaster.popShortToast(ctx, getResources().getString(R.string.popup_authenticated_via_twitter));
                        showUnlinkConfirmationPopup(getResources().getString(R.string.popup_authenticated_via_twitter));
                    } else {
                        // call fb for authentication and then call update_user_sn
                        if(CommonUtilities.isOnline(ctx)) {
                            //authentication process for twitter

                            loginVia = AppGlobalConstants.TWITTER_LOGIN_STATUS_ID;

                            loginWithTwitterUsingFabric();

                        } else {
                            Toaster.popInternetUnavailableToast(ctx);
                        }

                    }
                    break;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    // for twitter

    private void loginWithTwitterUsingFabric() {


        try {

            mTwitterAuthClient.authorize(AccountManagementActivity.this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> twitterSessionResult) {
                    Log.d("twitterLogin", "Logged with twitter");
                    TwitterSession session = twitterSessionResult.data;

                    TwitterAuthToken authToken = session.getAuthToken();
                    String token = authToken.token;

                    social_user_auth_token = token;

                    //                User user = session.getAuthToken().showUser(accessToken.getUserId());
                    social_media_username_str = session.getUserName();
                    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                    twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<com.twitter.sdk.android.core.models.User>() {
                        @Override
                        public void success(Result<com.twitter.sdk.android.core.models.User> userResult) {
                            String name = userResult.data.name;
                            String profilebannerurl = userResult.data.profileBannerUrl;
                            String profileurl = userResult.data.profileImageUrl;
                            String location = userResult.data.location;
                            social_media_userid_str = userResult.data.idStr;
                            social_media_username_str = name;
                            if (name != null) {
                                social_user_friendly_name = name;
                            } else {
                                social_user_friendly_name = "";
                            }
                            if (profileurl != null) {
                                social_media_profile_pic_url = profileurl;
                            }
                            if (profilebannerurl != null) {
                                social_media_cover_pic_url = profilebannerurl;
                            }
                        }

                        @Override
                        public void failure(com.twitter.sdk.android.core.TwitterException e) {

                        }
                    });
                    TwitterAuthClient authClient = new TwitterAuthClient();
                    authClient.requestEmail(session, new Callback<String>() {
                        @Override
                        public void success(Result<String> result) {
                            Log.e("RESULT", "User EMAIL:" + result.data.toString());
                            social_media_email_str = result.data.toString().trim();
                            makeURLForUpdateSNInfoAndFireWebService(AppGlobalConstants.TWITTER_LOGIN_STATUS_ID);

                        }

                        @Override
                        public void failure(com.twitter.sdk.android.core.TwitterException e) {
                            Log.e("RESULT", "FAIL:" + e.getMessage().toString());
                            makeURLForUpdateSNInfoAndFireWebService(AppGlobalConstants.TWITTER_LOGIN_STATUS_ID);
                        }
                    });
                    //
                }

                @Override
                public void failure(com.twitter.sdk.android.core.TwitterException e) {
                    Log.e("twitterLogin", "Failed login with twitter");
                    Logs.setLogException(class_name, "loginWithTwitterUsingFabric, failure", e);
                }
            });
        } catch (Exception e) {
            Logs.setLogException(class_name, "loginWithTwitterUsingFabric", e);
        }


    }






    private void onSuccessfullyLogout() {

        if(CommonUtilities.isFacebookLoggedIn())
            LoginManager.getInstance().logOut();

//					if(AppSharedPreferences.loadLoginViaFacebook)
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }



    private void makeURLToGetUserSnDetailsAndFireRequest() {






        try {
            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_GET_USER_SN); //like /app/index/file.php
//    uri.appendPath("login")//
            uri.appendQueryParameter("user_id", AppSharedPreferences.loadUserIDPreference(ctx));


            uri.appendQueryParameter("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx));
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);


            requestVolleyForGettingUserSnDetailsUsingVolley(
                    AppGlobalConstants.WEBSERVICE_GET_USER_SN,
                    uri.toString());


        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLToGetUserDetailsAndFireRequest()", e);
        }

    }

    private void requestVolleyForGettingUserSnDetailsUsingVolley(final String method_name, String url) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ArrayList<String> list = new ArrayList<String>();

        final ProgressDialog pDialogVolley = new ProgressDialog(ctx);
        // pDialogVolley.setCancelable(false);
        if(ConnectivityUtil.isConnectedFast(ctx)) {
            pDialogVolley.setMessage(ctx.getResources().getString(R.string.please_wait));
        } else {
            pDialogVolley.setMessage(ctx.getResources().getString(R.string.loader_please_wait_low_connectivity));

        }
        pDialogVolley.show();
        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Http Response", method_name + ": " + response);
                        responseString[0] = response;
                        Log.d("TAG", response.toString());
                        pDialogVolley.hide();
                        try {




                            parseJsonUserSnDetails(response);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                pDialogVolley.hide();
                Toaster.popLowNetworkConnectionToast(ctx);

            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }



    private boolean parseJsonUserSnDetails(String json) {


        boolean isSuccesful= false;
        try {


            JSONArray yoyo_details_array = new JSONArray(json);

            for (int i = 0; i < yoyo_details_array.length(); i++) {
                JSONObject specific_yoyo_detail_jsonobject = yoyo_details_array
                        .getJSONObject(i);
                String SocialNetwork_id = specific_yoyo_detail_jsonobject
                        .getString("SocialNetwork_id");
                String sn_user_name = specific_yoyo_detail_jsonobject
                        .getString("sn_user_name");

                if(SocialNetwork_id.equalsIgnoreCase("1")) {
                    twitter_username = sn_user_name;
                    twitter_value_txt.setText(twitter_username);

                } else if(SocialNetwork_id.equalsIgnoreCase("0")) {
                    fb_username = sn_user_name;
                    fb_value_txt.setText(fb_username);
                }


            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJson()", e);
        }
        return isSuccesful;

    }

    private void resetVariablesIfSocialSignupFailed() {
        social_media_cover_pic_url = "";
        social_media_country = "";
        social_media_email_str = "";
        social_media_profile_pic_url = "";
        social_media_city = "";
        social_media_userid_str = "";
        social_media_username_str = "";
        social_user_friendly_name = "";
        social_media_firstname = "";
        social_media_lastname = "";
        social_user_auth_token = "";
        loginVia = AppGlobalConstants.NORMAL_LOGIN_STATUS_ID;

    }

    public void makeURLForUpdateSNInfoAndFireWebService(int loginVia) {
        try {

            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_UPDATE_USER_SN); //like /app/index/file.php

            uri.appendQueryParameter("user_id", AppSharedPreferences.loadUserIDPreference(ctx));
            uri.appendQueryParameter("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx));
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);
            if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                uri.appendQueryParameter("SocialNetwork_id", "0");
            } else {
                uri.appendQueryParameter("SocialNetwork_id", "1");

            }
            uri.appendQueryParameter("sn_email", social_media_email_str);
            uri.appendQueryParameter("sn_user_id", social_media_userid_str);
            uri.appendQueryParameter("sn_user_name", social_media_username_str);
            uri.appendQueryParameter("sn_email", social_media_email_str);
//
            uri.appendQueryParameter("sn_first_name", social_media_firstname);
            uri.appendQueryParameter("sn_last_name", social_media_lastname);
            uri.appendQueryParameter("sn_country", social_media_country);
            uri.appendQueryParameter("sn_location", social_media_city);
            uri.appendQueryParameter("auth_token", social_user_auth_token);




            requestVolleyForUpdateSNInfo(
                    AppGlobalConstants.WEBSERVICE_UPDATE_USER_SN,
                    uri.toString(),loginVia);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForRegistration()", e);
        }
    }

    private void requestVolleyForUpdateSNInfo(final String method_name, String url, final int loginVia) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        url = CommonUtilities.encodeURLSpecialCharacters(url);
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ProgressDialog pDialog = new ProgressDialog(ctx);
        pDialog.setCancelable(false);

        pDialog.setMessage(ctx.getResources().getString(R.string.please_wait));
        pDialog.show();

        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseString[0] = response;
                        Log.d("Http Response", method_name + ": " + response);


                        try {


                            if (!parseJsonForUpdateSNInfo(response)) {

//                                if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
//                                    LoginManager.getInstance().logOut();
//
//                                }
//
//
                            } else if (parseJsonForUpdateSNInfo(response)) {

                              // perform the success case

                                if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                                    AppSharedPreferences.saveFBAuthenticatedPreference(ctx,true);
                                    fb_value_txt.setText(social_media_username_str);

                                    AppSharedPreferences.saveFBAuthAccessTokenPreference(ctx,social_user_auth_token);

                                } else {
                                    AppSharedPreferences.saveIsTwitterAuthenticatedPreference(ctx, true);
                                    twitter_value_txt.setText(social_media_username_str);

                                    AppSharedPreferences.saveTwitterAuthAccessTokenPreference(ctx, social_user_auth_token);
                                }

                                resetVariablesIfSocialSignupFailed();
//                                LoginManager.getInstance().logOut();
                                isFacebookAuthenticated = AppSharedPreferences.loadIsFBAuthenticated(ctx);
                                isTwitterAuthenticated = AppSharedPreferences.loadIsTwitterAuthenticated(ctx);

                            }

                            pDialog.hide();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toaster.popInternetUnavailableToast(ctx);
                pDialog.hide();

                Toaster.popLowNetworkConnectionToast(ctx);
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }
   public void makeURLForUnlinkSNInfoAndFireWebService(int loginVia) {
        try {

            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_UPDATE_USER_SN); //like /app/index/file.php

            uri.appendQueryParameter("user_id", AppSharedPreferences.loadUserIDPreference(ctx));
            uri.appendQueryParameter("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx));
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);
            if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                uri.appendQueryParameter("SocialNetwork_id", "0");
            } else {
                uri.appendQueryParameter("SocialNetwork_id", "1");

            }
            uri.appendQueryParameter("clear", "");




            requestVolleyForUnlinkSNInfo(
                    AppGlobalConstants.WEBSERVICE_UPDATE_USER_SN,
                    uri.toString(), loginVia);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForUnlinkSNInfoAndFireWebService()", e);
        }
    }

    private void requestVolleyForUnlinkSNInfo(final String method_name, String url, final int loginVia) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ProgressDialog pDialog = new ProgressDialog(ctx);
        pDialog.setCancelable(false);
        pDialog.setMessage(ctx.getResources().getString(R.string.please_wait));
        pDialog.show();

        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseString[0] = response;
                        Log.d("Http Response", method_name + ": " + response);


                        try {


                            if (parseJsonForUpdateSNInfo(response)) {


                              // perform the success case

                                if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                                    AppSharedPreferences.saveFBAuthenticatedPreference(ctx,false);
                                    fb_value_txt.setText("");

                                    AppSharedPreferences.saveFBAuthAccessTokenPreference(ctx, "");
                                    if(CommonUtilities.isFacebookLoggedIn())
                                        LoginManager.getInstance().logOut();
                                } else {
                                    AppSharedPreferences.saveIsTwitterAuthenticatedPreference(ctx, false);
                                    twitter_value_txt.setText("");
                                    AppSharedPreferences.saveTwitterAuthAccessTokenPreference(ctx, "");
                                    CookieSyncManager.createInstance(ctx);
                                    CookieManager cookieManager = CookieManager.getInstance();
                                    cookieManager.removeSessionCookie();
                                    Twitter.getSessionManager().clearActiveSession();
                                    Twitter.logOut();
                                }


                                isFacebookAuthenticated = AppSharedPreferences.loadIsFBAuthenticated(ctx);
                                isTwitterAuthenticated = AppSharedPreferences.loadIsTwitterAuthenticated(ctx);

                            }

                            pDialog.hide();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toaster.popInternetUnavailableToast(ctx);
                pDialog.hide();

                Toaster.popLowNetworkConnectionToast(ctx);
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }





    private boolean parseJsonForUpdateSNInfo(String json) {

//        {"Warning":"This email has already been reigstered.."}


//        {
//            user_id: "3221128008",
//                    user_name: "yoyor_3221128008",
//                temp_password: "17e9483284ae60a02785c95c81f0adf1",
//                at: "e5938b4ad543632e17f107eedb985b10433b536e9951bacdd9fbac6a616b27c0",
//                expire_at: "2015-10-31 01:33:57"
//        }
        boolean isSuccesfulRegistration = false;
        try {






            JSONObject reader = new JSONObject(json);
            if(reader.has("Warning")) {
                Toaster.popLongToast(ctx, reader.getString("Warning"));
            } else if(reader.has("Error")) {
                Toaster.popLongToast(ctx, reader.getString("Error"));
            } else if(reader.has("Success")) {
                Toaster.popLongToast(ctx, reader.getString("Success"));
                isSuccesfulRegistration = true;
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJsonForUpdateSNInfo", e);
        }
        return isSuccesfulRegistration;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }



    private void showUnlinkConfirmationPopup(final String fbOrTwitter) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

if(fbOrTwitter.equalsIgnoreCase(ctx.getResources().getString(R.string.popup_authenticated_via_twitter))) {
    makeURLForUnlinkSNInfoAndFireWebService(AppGlobalConstants.TWITTER_LOGIN_STATUS_ID);
} else if(fbOrTwitter.equalsIgnoreCase(ctx.getResources().getString(R.string.popup_authenticated_via_fb))) {
    makeURLForUnlinkSNInfoAndFireWebService(AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID);
}
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(fbOrTwitter).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }


    public static void stopEventService() {
        try {
            App.getContext().stopService(
                    new Intent(App.getContext(), SchedulerEventService.class));
            Intent myIntent = new Intent(App.getContext(),
                    SchedulerEventReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    App.getContext(), 0, myIntent, 0);

            AlarmManager alarmManager = (AlarmManager) App.getContext()
                    .getSystemService(ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            Logs.setLogException("SplashActivity", "stopEventService()", e);
        }

    }

    public static void stopGPSService() {
        try {
            App.getContext().stopService(
                    new Intent(App.getContext(), GPSLoggerService.class));
            Intent myIntent = new Intent(App.getContext(),
                    SchedulerCellSite.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    App.getContext(), 0, myIntent, 0);

            AlarmManager alarmManager = (AlarmManager) App.getContext()
                    .getSystemService(ALARM_SERVICE);

            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            Logs.setLogException("SplashActivity", "stopEventService()", e);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        App.updateLocation();
        updateViews();
    }
}
