package com.example.fbtwitterloginshare.fbtwitterloginshare.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.jivemap.app.R;
import com.jivemap.app.constants.AppGlobalConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.entity.BufferedHttpEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by adisoft2 on 17/8/15.
 */
public class CommonUtilities {

    private static final String class_name = "CommonUtilities";


    public static boolean isFacebookAppInstalled(Context ctx) {

        return isSpcifiedPackageInstalled(ctx, com.jivemap.app.constants.AppGlobalConstants.FACEBOOK_PKG_NAME);
    }


    public static boolean isTwitterAppInstalled(Context ctx) {
        return isSpcifiedPackageInstalled(ctx, com.jivemap.app.constants.AppGlobalConstants.TWITTER_PKG_NAME);
    }
    public static boolean isSpcifiedPackageInstalled(Context ctx, String package_name) {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = ctx.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(package_name))
                return true;
        }
        return false;
    }


    public static String copyFacebookProfilePicFromURLToFilePath(String fbUserID, Context ctx, String urlString, String filepath, String newFileName) {
        try
        {

            URL imageURL = new URL("https://graph.facebook.com/" + fbUserID + "/picture?type=large");
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

            File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();
            String filename=newFileName;
            Log.i("Local filename:", "" + filename);
            File file = new File(SDCardRoot,filename);
            if(file.createNewFile())
            {
                file.createNewFile();
            }
//Convert bitmap to byte array
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

//write the bytes in file
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
//
            filepath=file.getPath();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            filepath=null;
            e.printStackTrace();
        }
        Log.i("filepath:"," "+filepath) ;
        return filepath;
    }







    public static boolean isFacebookLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }


}
