package com.example.fbtwitterloginshare.fbtwitterloginshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.yoyomap.app.CustomizedViews.CustomEditText;
import com.yoyomap.app.Models.ChannelsModel;
import com.yoyomap.app.Models.TrendingyoyosModel;
import com.yoyomap.app.R;
import com.yoyomap.app.constants.AppGlobalConstants;
import com.yoyomap.app.helper.App;
import com.yoyomap.app.helper.AppSharedPreferences;
import com.yoyomap.app.helper.CommonUtilities;
import com.yoyomap.app.helper.ConnectivityUtil;
import com.yoyomap.app.helper.Logs;
import com.yoyomap.app.helper.Toaster;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PublishyoyosWithChannelsActivity extends Activity implements View.OnClickListener{

    private Context ctx;
    private ImageButton twitter_share_btn, fb_share_btn;

    private LoginManager loginManager;
    private CallbackManager fbCallbackManager;
    private TwitterAuthClient mTwitterAuthClient = new TwitterAuthClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = PublishyoyosWithChannelsActivity.this;


        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_publish_yoyos);
        initVbs();
        initViews();
        initFacebookVbs();
        initTwitterVbs();
        updateViews();
        clickEvents();

        Log.d("test","post_yoyo bAnonymous = "+ComposeyoyoActivity.bAnonymous);

    }

    private void initVbs() {
        try {
            } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initViews() {
        twitter_share_btn = (ImageButton)findViewById(R.id.twitter_share_btn);
        fb_share_btn = (ImageButton)findViewById(R.id.fb_share_btn);
}


    private void initFacebookVbs() {
        fbCallbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
    }

    private void initTwitterVbs() {

        mTwitterAuthClient = new TwitterAuthClient();
    }


    private void updateViews() {



        if(AppSharedPreferences.loadIsTwitterAuthenticated(ctx)) {
            twitter_share_btn.setBackgroundResource(R.drawable.twitter_composing_b);
        }

        if(AppSharedPreferences.loadIsFBAuthenticated(ctx)) {
            fb_share_btn.setBackgroundResource(R.drawable.fb_composing_b);
        }


    }

    private void clickEvents() {
        twitter_share_btn.setOnClickListener(this);
        fb_share_btn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.twitter_share_btn:
                if(AppSharedPreferences.loadIsTwitterAuthenticated(ctx)) {

                    showShareConfirmationPopup(ctx.getResources().getString(R.string.popup_share_twitter_confirmation_msg));



                } else {
                    showShareErrorMsgPopup(ctx.getResources().getString(R.string.popup_share_twitter_error_msg));
                }

                break;
            case R.id.fb_share_btn:

                if(AppSharedPreferences.loadIsFBAuthenticated(ctx)) {
                    showShareConfirmationPopup(ctx.getResources().getString(R.string.popup_share_fb_confirmation_msg));

                } else {
                    showShareErrorMsgPopup(ctx.getResources().getString(R.string.popup_share_fb_error_msg));
                }
                break;

            default:
                break;
        }
    }


    private void faceBookLogInNdShareContent() {
        try {
            loginManager.logInWithPublishPermissions(this,
                    Collections.singletonList("publish_actions"));
            loginManager.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResult) {
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    postToFb(loginResult.getAccessToken());
                                }
                            });
                    request.executeAsync();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException e) {

                }
            });
        } catch (Exception e) {
            Logs.setLogException(class_name,"faceBookLogInNdShareContent",e);
        }
    }


    private void postToFb(AccessToken accessToken) {
        final ProgressDialog pDialogVolley = new ProgressDialog(ctx);
        // pDialogVolley.setCancelable(false);
        pDialogVolley.setMessage("Jiving...");
        if (!pDialogVolley.isShowing()) {
            pDialogVolley.show();
        }

        Bundle parameters = new Bundle(1);
        parameters.putString("message", content);
//        Request request = new Request(Session.getActiveSession(),
//                "feed", _postParameter, HttpMethod.POST, callback);


        GraphRequest request = new GraphRequest(accessToken, "me/feed", parameters,
//        GraphRequest request = new GraphRequest(accessToken, "feed", parameters,
                HttpMethod.POST, new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse graphResponse) {
                pDialogVolley.dismiss();

                FacebookRequestError error = graphResponse.getError();

                if (error == null) {
                    Toast.makeText(ctx, "yoyod to Facebook",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("facebook error", error.getErrorMessage());
                    Toast.makeText(ctx, ctx.getResources().getString(R.string.toast_error_sharing),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        request.executeAsync();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fbCallbackManager.onActivityResult(requestCode, resultCode, data);

        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);

    }


    private void twitterLogInNdShareContent() {
        final TwitterCore twitterCore = TwitterCore.getInstance();
        final TwitterSession twitterSession = twitterCore.getSessionManager().getActiveSession();
        if (twitterSession == null || twitterSession.getUserId() <= 0) {
            mTwitterAuthClient.authorize(PublishyoyosWithChannelsActivity.this, new Callback<TwitterSession>() {

                @Override
                public void success(Result<TwitterSession> result) {
                    if (content != null && content.length() > 140)
                        content = content.substring(0,137) + "...";
                    postTweet(twitterCore, result.data, content);
                }

                @Override
                public void failure(TwitterException e) {
                    Log.e("fail", e.getMessage());
                }
            });
        } else {
            if (content != null && content.length() > 140)
                content = content.substring(0,137) + "...";
            postTweet(twitterCore, twitterSession, content);
        }
    }

    private void postTweet(TwitterCore twitterCore, TwitterSession twitterSession, String tweetText) {

        final ProgressDialog progressDialog = new ProgressDialog(ctx);

        progressDialog.setMessage("Jiving...");
        progressDialog.show();
        TwitterApiClient apiClient = twitterCore.getApiClient(twitterSession);
        apiClient.getStatusesService().update(tweetText, null, null, null, null, null, null, null,
                new Callback<Tweet>() {

                    @Override
                    public void success(Result<Tweet> tweetResult) {
                        progressDialog.dismiss();
                        Toast.makeText(PublishyoyosWithChannelsActivity.this, "yoyod to Twitter", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        if (e != null) {
                            Log.e("TwitterException", e.getMessage());
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                        Toast.makeText(PublishyoyosWithChannelsActivity.this, ctx.getResources().getString(R.string.toast_error_sharing), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showShareConfirmationPopup(final String fbOrTwitter) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        if(fbOrTwitter.equalsIgnoreCase(ctx.getResources().getString(R.string.popup_share_twitter_confirmation_msg))) {
                            if(CommonUtilities.isOnline(ctx)) {

//                        twitter_share_btn.setBackgroundResource(R.drawable.twitter_composing_b);
//                        Toaster.popShortToast(ctx,ctx.getResources().getString(R.string.toast_this_feature_will_be_arrived_very_soon));
                                twitterLogInNdShareContent();
                            } else {
                                Toaster.popInternetUnavailableToast(ctx);
                            }
                        } else if(fbOrTwitter.equalsIgnoreCase(ctx.getResources().getString(R.string.popup_share_fb_confirmation_msg))) {
                            if(CommonUtilities.isOnline(ctx)) {

//                        fb_share_btn.setBackgroundResource(R.drawable.fb_composing_b);

//                        Toaster.popShortToast(ctx,ctx.getResources().getString(R.string.toast_this_feature_will_be_arrived_very_soon));
                                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                if (accessToken == null || accessToken.isExpired() ||
                                        !accessToken.getPermissions().contains("publish_actions")) {
                                    faceBookLogInNdShareContent();
                                } else {
                                    postToFb(accessToken);
                                }

                            } else {
                                Toaster.popInternetUnavailableToast(ctx);
                            }
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(fbOrTwitter).setPositiveButton("OK", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

}
