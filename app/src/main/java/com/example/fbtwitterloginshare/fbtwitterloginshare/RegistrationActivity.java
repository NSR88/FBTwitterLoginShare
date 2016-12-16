package com.example.fbtwitterloginshare.fbtwitterloginshare;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jivemap.app.CustomizedViews.CustomButton;
import com.jivemap.app.CustomizedViews.CustomEditText;
import com.jivemap.app.R;
import com.jivemap.app.constants.AppGlobalConstants;
import com.jivemap.app.helper.App;
import com.jivemap.app.helper.AppSharedPreferences;
import com.jivemap.app.helper.CommonUtilities;
import com.jivemap.app.helper.ConnectivityUtil;
import com.jivemap.app.helper.ImageUtilities;
import com.jivemap.app.helper.Logs;
import com.jivemap.app.helper.Toaster;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;


public class RegistrationActivity extends Activity implements View.OnClickListener{


    private static final String class_name = "RegistrationActivity";

    private Context ctx;

    CustomButton btn_create_account, btn_signup_with_fb, btn_signup_with_twitter;

    private CallbackManager mCallbackManager;
    TwitterAuthClient mTwitterAuthClient = new TwitterAuthClient();

    // Shared Preferences
    private static SharedPreferences mtwitterSharedPreferences;

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

    private String social_media_email_str = "", social_media_userid_str = "", social_media_username_str = "", social_media_profile_pic_url = "", social_media_cover_pic_url = "", social_media_city = "", social_media_country="", social_user_friendly_name="", social_media_firstname = "", social_media_lastname = "", social_user_auth_token = "";

    private String updatedUserID = "", systemGeneratedPassword = "";

    private boolean username_already_updated = false;

    private TextView terms_conditions_txt;

    private boolean shouldValidateUsername = true;

    String accessTokenForTheFirstTime = "0";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            mCallbackManager.onActivityResult(requestCode, resultCode, data);
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            Logs.setLogException(class_name, "onActivityResult()", e);
        }
    }

    CustomEditText et_username,et_pswd,et_email, et_blank;

    private TextView registration_welcome_text;

    private String usernameStr, passwordStr, emailStr;

    private ProgressDialog pDialog;



//    twitter

    Twitter twitter;
    RequestToken requestToken = null;
    AccessToken accessToken;
    String oauth_url, oauth_verifier, profile_url;


    Dialog auth_dialog;
    SharedPreferences pref;

    int loginVia = AppGlobalConstants.NORMAL_LOGIN_STATUS_ID;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final String TAG = "GCMDemo";
    GoogleCloudMessaging gcm;
    String regid;
    String SENDER_ID = "57312500128";

    private String inputUserName = "";
    private String inputPassword = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);


            FacebookSdk.sdkInitialize(getApplicationContext());
            mCallbackManager = CallbackManager.Factory.create();
            // Shared Preferences
            mtwitterSharedPreferences = getApplicationContext().getSharedPreferences(
                    "MyPref", 0);

            setContentView(R.layout.activity_registration);

            ctx = RegistrationActivity.this;

            CommonUtilities.setupUI(findViewById(R.id.root_layout), this);
            loginVia = AppGlobalConstants.NORMAL_LOGIN_STATUS_ID;

//            AppSharedPreferences.saveIsSuccessfullyRegisteredPreference(ctx, false);
            initVbs();
            initializeViews();
            updateViews();
            clickEvents();





            if(CommonUtilities.isFacebookLoggedIn())
                LoginManager.getInstance().logOut();


            loginViaFacebook();

            mTwitterAuthClient = new TwitterAuthClient();

            initTwitter();

//            text_cSnippet.setText(addClickablePart(trendingJivesModel.getJive_cSnippet()), TextView.BufferType.SPANNABLE);



            if(checkPlayServices()){
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationId(ctx);
                if(regid.isEmpty()){
                    new RegisterBackground().execute();
                } else {
                    AppSharedPreferences.saveGCMDeviceIDPreference(ctx, regid);
                }

            }


        } catch (Exception e) {
            Logs.setLogException(class_name, "onCreate()", e);
        }


    }

    private void initVbs() {
        pDialog = new ProgressDialog(ctx);

    }

    private void initializeViews() {
        try {
            btn_create_account = (CustomButton)findViewById(R.id.btn_create_account);
            btn_signup_with_fb = (CustomButton)findViewById(R.id.btn_signup_with_fb);
            btn_signup_with_twitter = (CustomButton)findViewById(R.id.btn_signup_with_twitter);

            et_username = (CustomEditText)findViewById(R.id.et_username);
            et_username.setFilters(new InputFilter[] { filter });
            et_pswd = (CustomEditText)findViewById(R.id.et_pswd);
            et_email = (CustomEditText)findViewById(R.id.et_email);
            et_blank = (CustomEditText)findViewById(R.id.et_blank);

            registration_welcome_text = (TextView)findViewById(R.id.registration_welcome_text);
            terms_conditions_txt = (TextView)findViewById(R.id.terms_conditions_txt);

        } catch (Exception e) {
            Logs.setLogException(class_name, "initializeViews()", e);
        }
    }

    private void updateViews() {
        terms_conditions_txt.setPaintFlags(terms_conditions_txt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btn_create_account.setPaintFlags(btn_create_account.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btn_signup_with_fb.setPaintFlags(btn_signup_with_fb.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btn_signup_with_twitter.setPaintFlags(btn_signup_with_twitter.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }


    private void initTwitter() {

        try {
            pref = this.getPreferences(0);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("CONSUMER_KEY", ctx.getResources().getString(R.string.twitter_consumer_key));
            edit.putString("CONSUMER_SECRET", ctx.getResources().getString(R.string.twitter_consumer_key));
            edit.commit();

            twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(ctx.getResources().getString(R.string.twitter_consumer_key), ctx.getResources().getString(R.string.twitter_consumer_secret));
        } catch (Exception e) {
            Logs.setLogException(class_name, "initTwitter()", e);
        }
    }

    private void clickEvents() {
        try {
            btn_create_account.setOnClickListener(this);
            btn_signup_with_fb.setOnClickListener(this);
            btn_signup_with_twitter.setOnClickListener(this);
            terms_conditions_txt.setOnClickListener(this);
        } catch (Exception e) {
            Logs.setLogException(class_name, "clickEvents()", e);
        }
    }

    private void updateUIAccordingly(String toShow) {
        try {

            emailStr = social_media_email_str;
            et_email.setText(social_media_email_str);

            registration_welcome_text.setText(ctx.getResources().getString(R.string.registration_msg).replaceAll("XXX", social_user_friendly_name));

            et_email.setVisibility(View.INVISIBLE);
            if(toShow.equalsIgnoreCase("username")) {
                terms_conditions_txt.setVisibility(View.GONE);
                et_pswd.setVisibility(View.INVISIBLE);
                et_username.setVisibility(View.VISIBLE);
                btn_create_account.setText(ctx.getResources().getString(R.string.btn_update));
                et_username.setHint(ctx.getResources().getString(R.string.et_hint_set_username));
                et_email.setHint(ctx.getResources().getString(R.string.et_hint_set_email));
                et_pswd.setHint(ctx.getResources().getString(R.string.et_hint_set_password));
            } else if(toShow.equalsIgnoreCase("password")) {
                registration_welcome_text.setText(ctx.getResources().getString(R.string.plz_set_pswd_msg));
                terms_conditions_txt.setVisibility(View.GONE);
                et_pswd.setVisibility(View.VISIBLE);
                et_username.setVisibility(View.GONE);
                et_blank.setVisibility(View.INVISIBLE);
                btn_create_account.setText(ctx.getResources().getString(R.string.btn_update));
                et_username.setHint(ctx.getResources().getString(R.string.et_hint_set_username));
                et_email.setHint(ctx.getResources().getString(R.string.et_hint_set_email));
                et_pswd.setHint(ctx.getResources().getString(R.string.et_hint_set_password));
            } else {
                terms_conditions_txt.setVisibility(View.VISIBLE);
                et_email.setText("");
                et_pswd.setText("");
                et_username.setText("");
                et_pswd.setVisibility(View.VISIBLE);
                et_username.setVisibility(View.VISIBLE);
                et_email.setVisibility(View.VISIBLE);
            }




//        et_username.setText(social_media_username_str);


            btn_signup_with_fb.setVisibility(View.INVISIBLE);
            btn_signup_with_twitter.setVisibility(View.INVISIBLE);

            registration_welcome_text.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Logs.setLogException(class_name, "updateUIAccordingly()", e);
        }

    }

    private void loginViaFacebook() {
        try {


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
                                            getFBCoverImageURL(social_media_userid_str, loginResult.getAccessToken());


//                                            if (!social_media_email_str.equalsIgnoreCase("")) {
//                                                makeURLForRegistrationViaSocialMediaAndFireWebService();
                                                makeURLForAppAuthAndFireRequest();
//                                            } else {
//                                                updateUIAccordingly("all");
//                                            }

                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender, birthday, location, first_name");
                            request.setParameters(parameters);
                            request.executeAsync();


                        }

                        @Override
                        public void onCancel() {
                            // App code
                            Log.v("LoginActivity", "cancel");
                        }

                        @Override
                        public void onError(FacebookException exception) {


                            Log.e("FBError", exception.getMessage());

//                            if (exception instanceof FacebookAuthorizationException) {
//                                if (AccessToken.getCurrentAccessToken() != null) {
//                                    LoginManager.getInstance().logOut();
//                                }
//                            }
                            // App code
//                    Log.v("LoginActivity", exception.getCause().toString());
//                    Log.e("fb",Log.getStackTraceString(exception));
                            if (exception.getCause() != null) {
                                if (exception.getCause().toString().contains("CONNECTION")) {
                                    Toaster.popInternetUnavailableToast(ctx);
                                }
                            }
                        }
                    });


        } catch (Exception e) {
            Logs.setLogException(class_name, "loginViaFacebook()", e);
        }


    }


    @Override
    public void onClick(View view) {

        try {
            switch (view.getId()) {
                case R.id.btn_create_account:

                    //                loginVia = AppGlobalConstants.NORMAL_LOGIN_STATUS_ID;

                    try {
                        setErrorNull();

                        usernameStr = et_username.getText().toString().trim();
                        passwordStr = et_pswd.getText().toString().trim();
                        emailStr = et_email.getText().toString().trim();


                        //check validations

                        if(areInputsValid()) {

                            if(CommonUtilities.isOnline(ctx)) {
                                if(loginVia == AppGlobalConstants.NORMAL_LOGIN_STATUS_ID) {
                                    makeURLForAppAuthAndFireRequest();
                                } else {
                                    createUser();
                                }
                            } else {
                                Toaster.popInternetUnavailableToast(ctx);
                            }
                        }
                    } catch (Exception e) {
                        Logs.setLogException(class_name, "onClick(), btn_create_account", e);
                    }


                    break;
                case R.id.btn_signup_with_twitter:

                    try {
                        //                if(CommonUtilities.isOnline(ctx)) {
                        social_media_email_str = "";
                        social_media_username_str = "";
                        loginVia = AppGlobalConstants.TWITTER_LOGIN_STATUS_ID;

                        loginWithTwitterUsingFabric();
                    } catch (Exception e) {
                        Logs.setLogException(class_name, "onClick()", e);
                    }
                    break;
                case R.id.btn_signup_with_fb:

//                    loginViaFacebook();

                    if(CommonUtilities.isOnline(ctx)) {


                        if(CommonUtilities.isFacebookLoggedIn())
                            LoginManager.getInstance().logOut();

                        //authentication process for fb
                        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_birthday", "public_profile", "user_location"));

                    } else {
                        Toaster.popInternetUnavailableToast(ctx);
                    }
                    break;
                case R.id.terms_conditions_txt:

                    if(CommonUtilities.isOnline(ctx)) {
                        Intent mIntent = new Intent(ctx, UserWebsiteActivity.class);
                        mIntent.putExtra("website","https://jivemap.com/tnc.php");
                        startActivity(mIntent);
                    } else {
                        Toaster.popInternetUnavailableToast(ctx);
                    }
                    break;



                default:
                    break;
            }
        } catch (Exception e) {
            Logs.setLogException(class_name, "onClick()", e);
        }

    }


    private void dummyUserVerificationJustForNavigation() {
        try {
            if((loginVia == AppGlobalConstants.NORMAL_LOGIN_STATUS_ID)) {
                pDialog.hide();
                navigateToMyProfile();
            } else if(social_media_email_str.equalsIgnoreCase("")) {
                makeURLAndUpdateProfile("");
            } else {
                pDialog.hide();
                if(!usernameStr.equalsIgnoreCase("")) {
                    updateUIAccordingly("username");
                } else {
                    updateUIAccordingly("password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeURLForSocialRegistrationWithoutEmailBeingRetrieved() {
        try {

            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_CREATE_USER); //like /app/index/file.php
            uri.appendQueryParameter("email", emailStr);
            uri.appendQueryParameter("password", CommonUtilities.getSHA256(passwordStr));
            uri.appendQueryParameter("user_name", usernameStr);
            uri.appendQueryParameter("user_id", "1");
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);

            uri.appendQueryParameter("at", accessTokenForTheFirstTime);


            uri.appendQueryParameter("device_name", CommonUtilities.getUserDefinedDeviceID(ctx));
            uri.appendQueryParameter("device_token", AppSharedPreferences.loadGCMDeviceIDPreference(ctx));
            uri.appendQueryParameter("device_type", "0");

            uri.appendQueryParameter("app_country", "");
            uri.appendQueryParameter("reg_time_zone", CommonUtilities.getTimeZone());
            uri.appendQueryParameter("reg_utc_offset", "");
            uri.appendQueryParameter("reg_iso_language_code", "");


            uri.appendQueryParameter("user_friendly_name", social_user_friendly_name);
            if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                uri.appendQueryParameter("created_via", "0");
            } else {
                uri.appendQueryParameter("created_via", "1");
            }

            uri.appendQueryParameter("sn_user_id", social_media_userid_str);
            uri.appendQueryParameter("sn_user_name", social_media_username_str);
            uri.appendQueryParameter("sn_email", "");
//
            uri.appendQueryParameter("sn_first_name", social_media_firstname);
            uri.appendQueryParameter("sn_last_name", social_media_lastname);
            uri.appendQueryParameter("sn_country", social_media_country);
            uri.appendQueryParameter("sn_location", social_media_city);
            uri.appendQueryParameter("auth_token", social_user_auth_token);



            requestVolleyForNormalRegistration(
                    AppGlobalConstants.WEBSERVICE_CREATE_USER,
                    uri.toString());

//            String uriStr = "https://www.jivemap.com/dev/create_user.php?email=iphone99999999999999@gmail.com&user_id=1&at=0&created_via=1&device_type=1&device_name=iPhone%20Simulator&device_token=123&user_friendly_name=Iphone%209&sn_email=iphone99999999999999@gmail.com&sn_first_name=Iphone%209&sn_last_name=&sn_user_name=daleychappo26&auth_token=3727834216-QVrpCLycgVhs91csPKHj2mv5k3BmmZeyDBlikJ6&app_id=511688";
//                        requestVolleyForNormalRegistration(
//                    AppGlobalConstants.WEBSERVICE_CREATE_USER,
//                                uriStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForRegistration()", e);
        }
    }

    public void makeURLForNormalRegistration() {
        try {

            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_CREATE_USER); //like /app/index/file.php
            uri.appendQueryParameter("email", emailStr);
            uri.appendQueryParameter("password", CommonUtilities.getSHA256(passwordStr));
            uri.appendQueryParameter("user_name", usernameStr);
            uri.appendQueryParameter("user_id", "1");
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);

            uri.appendQueryParameter("at", accessTokenForTheFirstTime);


            uri.appendQueryParameter("device_name", CommonUtilities.getUserDefinedDeviceID(ctx));
            uri.appendQueryParameter("device_token", AppSharedPreferences.loadGCMDeviceIDPreference(ctx));
            uri.appendQueryParameter("device_type", "0");

            uri.appendQueryParameter("app_country", "");
            uri.appendQueryParameter("reg_time_zone", CommonUtilities.getTimeZone());
            uri.appendQueryParameter("reg_utc_offset", "");
            uri.appendQueryParameter("reg_iso_language_code", "");

            requestVolleyForNormalRegistration(
                    AppGlobalConstants.WEBSERVICE_CREATE_USER,
                    uri.toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForRegistration()", e);
        }
    }

    private void requestVolleyForNormalRegistration(final String method_name, String url) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        url = CommonUtilities.encodeURLSpecialCharacters(url);
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ArrayList<String> list = new ArrayList<String>();

        pDialog.setMessage(ctx.getResources().getString(R.string.please_wait));
        pDialog.show();

        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseString[0] = response;
                        Log.d("Http Response", method_name + ": " + response);


                        try {




                            if (!parseJsonForNormalRegistration(response)) {
                                pDialog.hide();
                            } else {
                                dummyUserVerificationJustForNavigation();

                            }


                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toaster.popLowNetworkConnectionToast(ctx);
                pDialog.hide();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }

    public void makeURLForRegistrationViaSocialMediaAndFireWebService() {
        try {

            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_CREATE_USER); //like /app/index/file.php
            if(!social_media_email_str.equalsIgnoreCase("")) {
                uri.appendQueryParameter("email", social_media_email_str);
            } else {
                uri.appendQueryParameter("email", emailStr);
            }
            uri.appendQueryParameter("user_id", "1");
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);

            uri.appendQueryParameter("at", accessTokenForTheFirstTime);
            uri.appendQueryParameter("user_friendly_name", social_user_friendly_name);
            if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                uri.appendQueryParameter("created_via", "0");
            } else {
                uri.appendQueryParameter("created_via", "1");
            }


            uri.appendQueryParameter("device_name", CommonUtilities.getUserDefinedDeviceID(ctx));
            uri.appendQueryParameter("device_token", AppSharedPreferences.loadGCMDeviceIDPreference(ctx));
            uri.appendQueryParameter("device_type", "0");

            uri.appendQueryParameter("app_country", "");
            uri.appendQueryParameter("reg_time_zone", CommonUtilities.getTimeZone());
            uri.appendQueryParameter("reg_utc_offset", "");
            uri.appendQueryParameter("reg_iso_language_code", "");

            uri.appendQueryParameter("sn_user_id", social_media_userid_str);
            uri.appendQueryParameter("sn_user_name", social_media_username_str);
            uri.appendQueryParameter("sn_email", social_media_email_str);
//
            uri.appendQueryParameter("sn_first_name", social_media_firstname);
            uri.appendQueryParameter("sn_last_name", social_media_lastname);
            uri.appendQueryParameter("sn_country", social_media_country);
            uri.appendQueryParameter("sn_location", social_media_city);
            uri.appendQueryParameter("auth_token", social_user_auth_token);




            requestVolleyForRegistrationViaSocialMedia(
                    AppGlobalConstants.WEBSERVICE_CREATE_USER,
                    uri.toString());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForRegistration()", e);
        }
    }

    private void requestVolleyForRegistrationViaSocialMedia(final String method_name, String url) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        url = CommonUtilities.encodeURLSpecialCharacters(url);
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ArrayList<String> list = new ArrayList<String>();

        pDialog.setMessage(ctx.getResources().getString(R.string.please_wait));
        pDialog.show();

        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseString[0] = response;
                        Log.d("Http Response", method_name + ": " + response);


                        try {




                            if(parseJsonForLoginViaSocialMedia(response)) {

                                dummyUserVerificationJustForNavigation();


                            } else {

                                if(loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                                    LoginManager.getInstance().logOut();

                                }
                                resetVariablesIfSocialSignupFailed();
                                pDialog.hide();
                            }


                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toaster.popLowNetworkConnectionToast(ctx);
                pDialog.hide();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }





    private boolean parseJsonForLoginViaSocialMedia(String json) {
        shouldValidateUsername = true;
//        {"Warning":"This email has already been reigstered.."}


//        {
//            user_id: "3221128008",
//                    user_name: "jiver_3221128008",
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
            } else if(reader.has("user_id")) {
                systemGeneratedPassword = reader.getString("temp_password");
                String user_id = reader.getString("user_id");






                if(!social_media_email_str.equalsIgnoreCase("")) {
                    usernameStr = ((reader.has("user_name")) ? reader.getString("user_name") : "");
//                    Toaster.popLongToast(ctx, ctx.getResources().getString(R.string.toast_successfully_registered));
                }
//                usernameStr = "";
                if(usernameStr.equalsIgnoreCase("") && (loginVia == AppGlobalConstants.TWITTER_LOGIN_STATUS_ID)) {
                    shouldValidateUsername = false;
                } else {
                    shouldValidateUsername = true;
                }


                isSuccesfulRegistration = true;
//                AppSharedPreferences.saveIsSuccessfullyRegisteredPreference(ctx, isSuccesfulRegistration);
                AppSharedPreferences.saveUserIDPreference(ctx, user_id);
                AppSharedPreferences.saveUserAccessTokenPreference(ctx, reader.getString("at"));

                updatedUserID = user_id;
//                CommonUtilities.writeUserIDToFile(ctx,user_id);

                AppSharedPreferences.saveMyNameToPreferences(ctx, social_user_friendly_name);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJsonForLoginViaSocialMedia", e);
        }
        return isSuccesfulRegistration;

    }

    private boolean parseJsonForNormalRegistration(String json) {

//        {"Warning":"This email has already been reigstered.."}


//        {
//            user_id: "3221127909",
//                    at: "bb577eafafb51d93393543928bf901b559fa3b6f060c6c0784a79ac7df9fbcef",
//                expire_at: "2015-10-17 02:07:59"
//        }

        boolean isSuccesfulRegistration = false;
        try {


            JSONObject reader = new JSONObject(json);
            if(reader.has("Warning")) {
                Toaster.popLongToast(ctx, reader.getString("Warning"));
//                et_email.setText("");
            } else if(reader.has("Error")) {
                Toaster.popLongToast(ctx, reader.getString("Error"));
            } else if(reader.has("user_id")) {

                String user_id = reader.getString("user_id");

//                Toaster.popLongToast(ctx, ctx.getResources().getString(R.string.toast_successfully_registered));
                isSuccesfulRegistration = true;

                AppSharedPreferences.saveUserIDPreference(ctx, user_id);
                updatedUserID = user_id;
                AppSharedPreferences.saveMyJiveIDToPreferences(ctx, usernameStr);
                AppSharedPreferences.saveUserAccessTokenPreference(ctx, reader.getString("at"));


                CommonUtilities.writeUserIDToFile(ctx, user_id);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJsonForNormalRegistration", e);
        }
        return isSuccesfulRegistration;

    }


    private void setErrorNull() {
        et_username.setError(null);
        et_pswd.setError(null);
        et_email.setError(null);
    }

    private boolean areInputsValid() {
        boolean areInputsValid = false;

        try {


            if(usernameStr.equalsIgnoreCase("") && shouldValidateUsername) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_username_mandatory));
                et_username.requestFocus();
            } else if((usernameStr.length() < 6) && (usernameStr.length() > 30) && shouldValidateUsername) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_invalid_username));
            } else if(CommonUtilities.containsSpecialCharacters(usernameStr) && shouldValidateUsername) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_invalid_username_special_chars));

            } else if((passwordStr.equalsIgnoreCase("") && ((loginVia == AppGlobalConstants.NORMAL_LOGIN_STATUS_ID) || username_already_updated)) || (!shouldValidateUsername && passwordStr.equalsIgnoreCase(""))) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_password_mandatory));
                et_pswd.requestFocus();
            } else if (emailStr.equalsIgnoreCase("")) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_email_mandatory));
                et_email.requestFocus();
            } else if (!CommonUtilities.isValidEmail(emailStr)) {
                Toaster.popShortToast(ctx, ctx.getResources().getString(R.string.toast_email_invalid_error));
                et_email.requestFocus();
            } else {
                // successful
                areInputsValid = true;
            }
//            } else {
//                // successful
//                areInputsValid = true;
//            }
        } catch (Exception e) {
            Logs.setLogException(class_name, "parseJsonForLoginViaSocialMedia", e);
        }


        return areInputsValid;
    }

// for twitter

    private void loginWithTwitterUsingFabric() {


        try {

            mTwitterAuthClient.authorize(RegistrationActivity.this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> twitterSessionResult) {
                    Log.d("twitterLogin", "Logged with twitter");
                    TwitterSession session = twitterSessionResult.data;

                    TwitterAuthToken authToken = session.getAuthToken();
                    String token = authToken.token;

                    social_user_auth_token = token;

                    //                User user = session.getAuthToken().showUser(accessToken.getUserId());
                    social_media_username_str = session.getUserName();
                    Log.d("twitter","username: session.getUserName() "+usernameStr);
                    TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                    twitterApiClient.getAccountService().verifyCredentials(false, false, new Callback<com.twitter.sdk.android.core.models.User>() {
                        @Override
                        public void success(Result<com.twitter.sdk.android.core.models.User> userResult) {
                            String name = userResult.data.name;
//                            Log.d("twitter","username: userResult.data.name "+userResult.data.name);
//                            Log.d("twitter","username: userResult.data.screenname "+userResult.data.screenName);
                            String profilebannerurl = userResult.data.profileBannerUrl;
                            String profileurl = userResult.data.profileImageUrl;
                            String location = userResult.data.location;
                            social_media_userid_str = userResult.data.idStr;
                            social_media_username_str = userResult.data.screenName;
                            if(name != null) {
                                social_user_friendly_name = name;
                            } else {
                                social_user_friendly_name = "";
                            }
                            if(profileurl != null) {
                                social_media_profile_pic_url = profileurl;
                            }
                            if(profilebannerurl != null) {
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
                            Log.e("RESULT", "User EMAIL:"+result.data.toString());
                            social_media_email_str = result.data.toString().trim();




                        }

                        @Override
                        public void failure(com.twitter.sdk.android.core.TwitterException e) {
                            Log.e("RESULT", "FAIL:"+e.getMessage().toString());

                            makeURLForAppAuthAndFireRequest();
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



    private void makeURLAndUpdateProfile(String toUpdate) {
        try {
            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_UPDATE_USER); //like /app/index/file.php
            uri.appendQueryParameter("user_id", AppSharedPreferences.loadUserIDPreference(ctx));
            uri.appendQueryParameter("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx));
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);
            if(toUpdate.equalsIgnoreCase("username")) {
                uri.appendQueryParameter("user_name", usernameStr);
            } else if(!toUpdate.equalsIgnoreCase("")) {
                uri.appendQueryParameter("old_password", CommonUtilities.getSHA256(systemGeneratedPassword));
                uri.appendQueryParameter("password", CommonUtilities.getSHA256(passwordStr));
            }
            if(!social_media_city.trim().equalsIgnoreCase("")) {
                uri.appendQueryParameter("location", social_media_city);
            }
            if(!social_media_country.trim().equalsIgnoreCase("")) {
                uri.appendQueryParameter("country", social_media_country);
            }
            if(!social_user_friendly_name.trim().equalsIgnoreCase("")) {
                uri.appendQueryParameter("user_friendly_name", social_user_friendly_name);
            }

            requestVolleyForUpdatingProfile(
                    AppGlobalConstants.WEBSERVICE_UPDATE_USER,
                    uri.build().toString(), toUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestVolleyForUpdatingProfile(final String method_name, String url, final String toUpdate) {
        // TODO Auto-generated method stub
        String tag_json_arry = "json_array_req: "+method_name;
        url = CommonUtilities.encodeURLSpecialCharacters(url);
        Log.d("Http Request", url);
        final String[] responseString = {""};
//        String url = "http://carbonchase.com/v1.1/get_favs.php?user_id=123&at=0&channels";
        final ArrayList<String> list = new ArrayList<String>();

        pDialog.setMessage(ctx.getResources().getString(R.string.please_wait));
        if(!social_media_email_str.trim().equalsIgnoreCase("")) {
            pDialog.show();
        }


        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseString[0] = response;
                        Log.d("Http Response", method_name + ": " + response);


                        try {




                            if (parseJsonUpdateProfile(response)) {
                                AppSharedPreferences.saveMyNameToPreferences(ctx, social_user_friendly_name);
                                if(toUpdate.equalsIgnoreCase("username")) {
                                    username_already_updated = true;
                                    updateUIAccordingly("password");
                                    pDialog.hide();
                                } else {
                                    new copyImageFileFromURLTask("profile").execute(ctx);
                                }

                            } else {

                                pDialog.hide();
                            }


                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }






                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
                Toaster.popLowNetworkConnectionToast(ctx);
                pDialog.hide();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(
                AppGlobalConstants.WEBSERVICE_TIMEOUT_VALUE_IN_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        App.getInstance().addToRequestQueue(req, tag_json_arry);


    }



    private boolean parseJsonUpdateProfile(String json) {

//        {"Warning":"This email has already been reigstered.."}


//        {
//            user_id: "3221127909",
//                    at: "bb577eafafb51d93393543928bf901b559fa3b6f060c6c0784a79ac7df9fbcef",
//                expire_at: "2015-10-17 02:07:59"
//        }

        boolean isUpdatedSuccessfully = false;
        try {


            JSONObject reader = new JSONObject(json);
            if (reader.has("System")) {
                Toaster.popLongToast(ctx, reader.getString("System"));
            } else if (reader.has("Warning")) {
                Toaster.popLongToast(ctx, reader.getString("Warning"));
            } else if (reader.has("Error")) {
                Toaster.popLongToast(ctx, reader.getString("Error"));
            } else if (reader.has("Success")) {
                isUpdatedSuccessfully = true;
                AppSharedPreferences.saveMyJiveIDToPreferences(ctx, usernameStr);
//                Toaster.popLongToast(ctx, reader.getString("Success"));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJsonUpdateProfile", e);
        }
        return isUpdatedSuccessfully;

    }

    private String blockCharacterSet = "~#^|$%&*!";

    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };


    /**
     * Function to login twitter
     * */
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(ctx.getResources().getString(R.string.twitter_consumer_key));
            builder.setOAuthConsumerSecret(ctx.getResources().getString(R.string.twitter_consumer_secret));
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mtwitterSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }




    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {

        return getSharedPreferences(RegistrationActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    class RegisterBackground extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(ctx);
                }
                regid = gcm.register(SENDER_ID);

                AppSharedPreferences.saveGCMDeviceIDPreference(ctx, regid);

                msg = "Dvice registered, registration ID=" + regid;
                Log.d("111", msg);

                // Persist the regID - no need to register again.
                storeRegistrationId(ctx, regid);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {

        }

        private void storeRegistrationId(Context context, String regId) {
            final SharedPreferences prefs = getGCMPreferences(context);
            int appVersion = getAppVersion(context);
            Log.i(TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        }
    }





    private class requestForUploadMedia extends AsyncTask<Context, Void, String> {

        File profile_img_file = new File("");
        private Context mContext;
        private String imageToUpload = "";
        public requestForUploadMedia(File profile_img_file, String imageToUpload) {
            this.profile_img_file = profile_img_file;
            this.imageToUpload = imageToUpload;
        }

        @Override
        protected String doInBackground(Context... ctx) {
            mContext = ctx[0];
//            http://carbonchase.com/v1.1/update_user.php?user_id=3&at=0&user_friendly_name=hihi&nick_name=Hi Hi&
// time_zone=CBT&utc_offset=1000&iso_language_code=RU&country=Russia&location=Mosco&profile_image=http://www.hi.com/hi.jpg&
// background_image=adfadfadfa.png&URLs=www.rusia.com&status=1&details=this is a hi account&
// password=c150ca2aef051801675a4a3ca83fe86dc8645591ada0a79c5be144365bc7e084&

            String update_user_details_json = "";


            try {
                if((imageToUpload.equalsIgnoreCase("profile") && !social_media_profile_pic_url.equalsIgnoreCase("")) || (imageToUpload.equalsIgnoreCase("background") && !social_media_cover_pic_url.equalsIgnoreCase(""))) {
//                if(!social_media_profile_pic_url.equalsIgnoreCase("") || !social_media_cover_pic_url.equalsIgnoreCase("")) {

                    RequestBody requestBody = null;
                    if(imageToUpload.equalsIgnoreCase("profile")) {

                        Log.d("upload_media",String.valueOf(profile_img_file.length()));

                        requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                                .addFormDataPart("user_id", AppSharedPreferences.loadUserIDPreference(mContext))
                                .addFormDataPart("app_id", AppGlobalConstants.APP_ID)
                                .addFormDataPart("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx[0]))
                                .addFormDataPart("profile_image", "test")
                                .addFormDataPart("fileToUpload", "file.png", RequestBody.create(MediaType.parse("image/*"), profile_img_file))
                                .build();
                        AppSharedPreferences.saveProfilePicURLPreference(mContext, social_media_profile_pic_url);
                    } else {
                        requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                                .addFormDataPart("user_id", AppSharedPreferences.loadUserIDPreference(mContext))
                                .addFormDataPart("app_id", AppGlobalConstants.APP_ID)
                                .addFormDataPart("at", AppSharedPreferences.loadUserAccessTokenPreference(ctx[0]))
                                .addFormDataPart("background_image", "test")
                                .addFormDataPart("fileToUpload", "file.png", RequestBody.create(MediaType.parse("image/*"), profile_img_file))
                                .build();
                        AppSharedPreferences.saveCoverPicURLPreference(mContext, social_media_cover_pic_url);
                    }

                    Request request = new Request.Builder()
                            .url(AppGlobalConstants.WEBSERVICE_BASE_URL_FULL_FOR_UPLOADING_MEDIA)
                            .post(requestBody)
                            .build();

                    OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(AppGlobalConstants.IMAGE_UPLOAD_SOCKET_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS); // connect timeout
                    client.setReadTimeout(AppGlobalConstants.IMAGE_UPLOAD_SOCKET_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);    // socket timeout
                    com.squareup.okhttp.Response response = client.newCall(request).execute();
                    return response.body().string();

                }
//                update_user_details_json = RequestToResponse.getResponseForThePOSTRequest_WithBlankParams(
//                        AppGlobalConstants.WEBSERVICE_UPLOAD_MEDIA,
//                        uri.build().toString());

                // survey_xml = loadXMLFromPreferences("survey_xml").trim();
            } catch (Exception e) {
                Logs.setLogException(class_name, "requestForUploadMedia()", e);
            }
            return update_user_details_json;
        }

        @Override
        protected void onPostExecute(String result) {
            // parseXmlDisplayForm(result);

            try {



                if(imageToUpload.equalsIgnoreCase("profile")) {
                    new copyImageFileFromURLTask("background").execute(ctx);
                } else {

                    pDialog.hide();
                    navigateToMyProfile();
                }





            } catch (Exception e) {
                // TODO Auto-generated catch block
                Logs.setLogException(class_name, "requestForUploadMedia()", e);
                pDialog.hide();
            }

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

    }
    class copyImageFileFromURLTask extends AsyncTask<Context, Void, File> {
        //        String param_to_update, paramValueToUpdate;
        Context mContext;
        private ProgressDialog pDialog;
        private String imageToUpload = "";

        public copyImageFileFromURLTask(String imageToUpload) {
            this.imageToUpload = imageToUpload;
        }

        @Override
        protected File doInBackground(Context... ctx) {
            mContext = ctx[0];

            File img_file = new File("");
            try {
                if(imageToUpload.equalsIgnoreCase("profile")) {
                    if (!social_media_profile_pic_url.equalsIgnoreCase("")) {
                        String update_user_details_json = "";
                        if (loginVia == AppGlobalConstants.TWITTER_LOGIN_STATUS_ID) {
                            String filepath = CommonUtilities.copyFromURLToFilePath(ctx[0], social_media_profile_pic_url, ImageUtilities.createImageFolderInAppStorageIfNotExists(), "profile_" + AppSharedPreferences.loadUserIDPreference(ctx[0]) + ".jpg");
                            img_file = new File(filepath);
                        } else {
                            String filepath = CommonUtilities.copyFacebookProfilePicFromURLToFilePath(social_media_userid_str,ctx[0], social_media_profile_pic_url, ImageUtilities.createImageFolderInAppStorageIfNotExists(), "profile_" + AppSharedPreferences.loadUserIDPreference(ctx[0]) + ".jpg");

                            img_file = new File(filepath);}

                    }
                } else {
                    if (!social_media_cover_pic_url.equalsIgnoreCase("")) {
                        String update_user_details_json = "";
                        if (loginVia == AppGlobalConstants.TWITTER_LOGIN_STATUS_ID) {
                            String filepath = CommonUtilities.copyFromURLToFilePath(ctx[0], social_media_cover_pic_url, ImageUtilities.createImageFolderInAppStorageIfNotExists(), "background_" + AppSharedPreferences.loadUserIDPreference(ctx[0]) + ".jpg");
                            img_file = new File(filepath);
                        } else {
                            String filepath = CommonUtilities.copyFromURLToFilePath(ctx[0], social_media_cover_pic_url, ImageUtilities.createImageFolderInAppStorageIfNotExists(), "background_" + AppSharedPreferences.loadUserIDPreference(ctx[0]) + ".jpg");

//                            String filepath = CommonUtilities.copyFacebookBGPicFromURLToFilePath(social_media_userid_str, ctx[0], social_media_cover_pic_url, ImageUtilities.createImageFolderInAppStorageIfNotExists(), "background_" + AppSharedPreferences.loadUserIDPreference(ctx[0]) + ".jpg");
                            img_file = new File(filepath);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // survey_xml = loadXMLFromPreferences("survey_xml").trim();
            return img_file;
        }

        @Override
        protected void onPostExecute(File img_file) {
            // parseXmlDisplayForm(result);

            try {


                new requestForUploadMedia(img_file, imageToUpload).execute(ctx);


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Logs.setLogException(class_name, "UpdateProfileAsyncTask()", e);
                pDialog.hide();
            }

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }
    }

    private void getFBCoverImageURL(final String fbUserID,final com.facebook.AccessToken loginResultAccessToken) {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    //Your implementation goes here
                    String URL = "https://graph.facebook.com/" + fbUserID + "?fields=cover&access_token=" + loginResultAccessToken.getToken();
                    social_user_auth_token = loginResultAccessToken.getToken();
                    Log.d("fb",loginResultAccessToken.getToken());
                    try {

//                        org.apache.http.client.HttpClient hc = new DefaultHttpClient();
//                        HttpGet get = new HttpGet(URL);
//                        HttpResponse rp = hc.execute(get);

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(URL)
                                .build();
                        com.squareup.okhttp.Response rp = client.newCall(request).execute();

                        if (rp.code() == HttpStatus.SC_OK) {
                            String result =rp.body().string();

                            JSONObject JODetails = new JSONObject(result);

                            if (JODetails.has("cover")) {
                                String getInitialCover = JODetails.getString("cover");

                                if (getInitialCover.equals("null")) {
                                    social_media_cover_pic_url = "";
                                } else {
                                    JSONObject JOCover = JODetails.optJSONObject("cover");

                                    if (JOCover.has("source")) {
                                        social_media_cover_pic_url = JOCover.getString("source");
                                    } else {
                                        social_media_cover_pic_url = "";
                                    }
                                }
                            } else {
                                social_media_cover_pic_url = "";
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

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

    private void navigateToMyProfile() {
        try {
            AppSharedPreferences.saveIsSuccessfullyRegisteredPreference(ctx, true);

            Intent mIntent = new Intent(ctx, MyProfileActivity.class);
            if(!shouldValidateUsername) {
                AppSharedPreferences.saveIsUserLoggedInPreference(ctx, true);
                mIntent = new Intent(ctx, TrendingJivesFragmentActivity.class);
            }
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            if (loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID) {
                AppSharedPreferences.saveSignupViaPreference(ctx, ctx.getResources().getString(R.string.value_fb));
//                LoginManager.getInstance().logOut(); // facebook logout

                AppSharedPreferences.saveFBAuthenticatedPreference(ctx, true);
                AppSharedPreferences.saveFBAuthAccessTokenPreference(ctx, social_user_auth_token);

            } else if (loginVia == AppGlobalConstants.TWITTER_LOGIN_STATUS_ID) {
                AppSharedPreferences.saveSignupViaPreference(ctx, ctx.getResources().getString(R.string.value_twitter));
                AppSharedPreferences.saveIsTwitterAuthenticatedPreference(ctx, true);
                AppSharedPreferences.saveTwitterAuthAccessTokenPreference(ctx, social_user_auth_token);

            } else {
                AppSharedPreferences.saveSignupViaPreference(ctx, ctx.getResources().getString(R.string.value_normal));

            }
            startActivity(mIntent);


            finish();
        } catch (Resources.NotFoundException e) {
            Logs.setLogException(class_name,"navigateToMyProfile()", e);
        }
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


    private void makeURLForAppAuthAndFireRequest() {

        try {
            Uri.Builder uri = new Uri.Builder();
            uri.scheme(AppGlobalConstants.SCHEME);//basicall http or https
            uri.encodedAuthority(AppGlobalConstants.WEBSERVICE_BASE_URL); //domain name like www.google.com
            uri.path(AppGlobalConstants.PREFIX_PATH + AppGlobalConstants.WEBSERVICE_APP_AUTH); //like /app/index/file.php
//    uri.appendPath("login")//
            uri.appendQueryParameter("app_id", AppGlobalConstants.APP_ID);




            requestVolleyForAppAuthUsingVolley(
                    AppGlobalConstants.WEBSERVICE_APP_AUTH,
                    uri.toString());


        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "makeURLForAppAuthAndFireRequest()", e);
        }

    }

    private void requestVolleyForAppAuthUsingVolley(final String method_name, String url) {
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




                            parseAppAuthDetails(response);
                            if(loginVia == AppGlobalConstants.NORMAL_LOGIN_STATUS_ID) {
                                createUser();
                            } else if((loginVia == AppGlobalConstants.FACEBOOK_LOGIN_STATUS_ID || loginVia == AppGlobalConstants.TWITTER_LOGIN_STATUS_ID) && (social_media_email_str.trim().equalsIgnoreCase(""))){
                                updateUIAccordingly("all");

                            } else {
                                makeURLForRegistrationViaSocialMediaAndFireWebService();
                            }

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



    private boolean parseAppAuthDetails(String json) {


        boolean isSuccesful= false;
        try {


            JSONObject app_auth_json_obj = new JSONObject(json);
            accessTokenForTheFirstTime= app_auth_json_obj
                    .getString("at");



        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logs.setLogException(class_name, "parseJson()", e);
        }
        return isSuccesful;

    }

    private void createUser() {
        try {
            setErrorNull();

            usernameStr = et_username.getText().toString().trim();
            passwordStr = et_pswd.getText().toString().trim();
            emailStr = et_email.getText().toString().trim();


            //check validations

//        if(areInputsValid()) {
//
//            if(CommonUtilities.isOnline(ctx)) {

            if (loginVia == AppGlobalConstants.NORMAL_LOGIN_STATUS_ID) {

                //                    Case 1....if normal registration

                makeURLForNormalRegistration();

            } else { // login via social
                //                                with email
                if (!social_media_email_str.equalsIgnoreCase("")) {

                    //                    Case 2.... registration via social (with email)


                    if(!passwordStr.equalsIgnoreCase("")) {
                        makeURLAndUpdateProfile("password");
                    } else {
                        makeURLAndUpdateProfile("username");
                    }


                } else {

                    //                    Case 3.... registration via social (without email)
                    makeURLForSocialRegistrationWithoutEmailBeingRetrieved();


                }
            }
//            } else {
//                Toaster.popInternetUnavailableToast(ctx);
//            }
//        }
        } catch (Exception e) {
            Logs.setLogException(class_name, "onClick(), btn_create_account", e);
        }

    }

    private void createUserViaFacebook() {

    }
    private void createUserViaTwitter() {

    }

}