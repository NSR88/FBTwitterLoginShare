package com.example.fbtwitterloginshare.fbtwitterloginshare.helper;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

import com.jivemap.app.R;


/**
 * Created by adisoft2 on 18/8/15.
 */
public class Toaster {

    public static void popShortToast(Context ctx, String toast_msg) {
        try {
            if(!toast_msg.trim().equalsIgnoreCase("")) {
                Toast toast = Toast.makeText(ctx, toast_msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void popShorterToast(Context ctx, String toast_msg) {
        try {
            if(!toast_msg.trim().equalsIgnoreCase("")) {
                final Toast toast = Toast.makeText(ctx, toast_msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void popLongToast(Context ctx, String toast_msg) {
        try {
            if(!toast_msg.trim().equalsIgnoreCase("")) {
                Toast toast = Toast.makeText(ctx, toast_msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void popLongerToast(Context ctx, String toast_msg) {
        try {
            if(!toast_msg.trim().equalsIgnoreCase("")) {
                Toast toast = Toast.makeText(ctx, toast_msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                for(int i = 1; i<=50;i++) {
                    toast.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void popTimedToast(Context ctx, String toast_msg, int duration_in_millis) {
        try {
            if(!toast_msg.trim().equalsIgnoreCase("")) {
                Toast toast = Toast.makeText(ctx, toast_msg, duration_in_millis);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void popInternetUnavailableToast(Context ctx) {
        try {
            String toast_msg = ctx.getResources().getString(R.string.toast_internet_unavailable);
            if(!toast_msg.trim().equalsIgnoreCase(""))
                popShortToast(ctx, toast_msg);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void popLowNetworkConnectionToast(Context ctx) {
        try {
            String toast_msg = ctx.getResources().getString(R.string.toast_seems_network_slow);
            if(!toast_msg.trim().equalsIgnoreCase(""))
                popShortToast(ctx, toast_msg);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
}
