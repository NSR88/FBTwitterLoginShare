package com.example.fbtwitterloginshare.fbtwitterloginshare.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.yoyomap.app.R;
import com.yoyomap.app.constants.AppGlobalConstants;
public class AppSharedPreferences {

    public static boolean loadIsUserLoggedIn(Context ctx) {
        boolean isUserLoggedIn = false;
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
            isUserLoggedIn = prefs.getBoolean("logged_in", false);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return isUserLoggedIn;
    }

    public static void saveIsUserLoggedInPreference(Context ctx, boolean isUserLoggedIn) {
        try {
			// MY_PREFS_NAME - a static String variable like:
			//public static final String MY_PREFS_NAME = "MyPrefsFile";
			SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE).edit();
			editor.putBoolean("logged_in", isUserLoggedIn);

			editor.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static boolean loadIsFBAuthenticated(Context ctx) {
        boolean isUserLoggedIn = false;
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref" + loadUserIDPreference(ctx), Context.MODE_PRIVATE);
            isUserLoggedIn = prefs.getBoolean("FBAuthenticated", false);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return isUserLoggedIn;
    }

    public static void saveFBAuthenticatedPreference(Context ctx, boolean isUserLoggedIn) {
        try {
            // MY_PREFS_NAME - a static String variable like:
            //public static final String MY_PREFS_NAME = "MyPrefsFile";
            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putBoolean("FBAuthenticated", isUserLoggedIn);

            editor.commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static void saveFBAuthAccessTokenPreference(Context ctx, String FBAuthAccessToken) {
        try {
            // MY_PREFS_NAME - a static String variable like:
            //public static final String MY_PREFS_NAME = "MyPrefsFile";
            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putString("FBAuthAccessToken", FBAuthAccessToken);

            editor.commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveTwitterAuthAccessTokenPreference(Context ctx, String TwitterAuthAccessToken) {
        try {
            // MY_PREFS_NAME - a static String variable like:
            //public static final String MY_PREFS_NAME = "MyPrefsFile";
            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putString("TwitterAuthAccessToken", TwitterAuthAccessToken);

            editor.commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static boolean loadIsTwitterAuthenticated(Context ctx) {
        boolean isUserLoggedIn = false;
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref" + loadUserIDPreference(ctx), Context.MODE_PRIVATE);
            isUserLoggedIn = prefs.getBoolean("TwitterAuthenticated", false);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return isUserLoggedIn;
    }

    public static void saveIsTwitterAuthenticatedPreference(Context ctx, boolean isUserLoggedIn) {
        try {
            // MY_PREFS_NAME - a static String variable like:
            //public static final String MY_PREFS_NAME = "MyPrefsFile";
            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putBoolean("TwitterAuthenticated", isUserLoggedIn);

            editor.commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveUserIDPreference(Context ctx, String userID) {

        try {

            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE).edit();
            editor.putString("UserID", userID);
            editor.commit();
//			SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
//			UserIDStr = prefs.getString("UserID", "");



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static String loadUserIDPreference(Context ctx) {
        String UserIDStr = "";
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
            UserIDStr = prefs.getString("UserID", AppGlobalConstants.DEFAULT_USER_ID);



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return UserIDStr;
    }
public static void saveSignupViaPreference(Context ctx, String signupVia) {

        try {

            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putString(ctx.getResources().getString(R.string.key_signup_via), signupVia);
            editor.commit();
//			SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
//			UserIDStr = prefs.getString("UserID", "");



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static String loadSignupViaPreference(Context ctx) {
        String signupVia = "";
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref" + loadUserIDPreference(ctx), Context.MODE_PRIVATE);
            signupVia = prefs.getString(ctx.getResources().getString(R.string.key_signup_via), ctx.getResources().getString(R.string.value_normal));



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return signupVia;
    }

 public static void saveCoverPicURLPreference(Context ctx, String CoverPicURL) {

        try {

            SharedPreferences.Editor editor = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE).edit();
            editor.putString("CoverPicURL", CoverPicURL);
            editor.commit();
//			SharedPreferences prefs = ctx.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
//			UserIDStr = prefs.getString("UserID", "");



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static String loadCoverPicURLPreference(Context ctx) {
        String CoverPicURL = "";
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("AppPref"+loadUserIDPreference(ctx), Context.MODE_PRIVATE);
            CoverPicURL = prefs.getString("CoverPicURL", "");



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return CoverPicURL;
    }



}
