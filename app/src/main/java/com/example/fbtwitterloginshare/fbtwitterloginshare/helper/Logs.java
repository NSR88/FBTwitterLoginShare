package com.example.fbtwitterloginshare.fbtwitterloginshare.helper;

import android.util.Log;

import com.jivemap.app.constants.AppGlobalConstants;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;


public class Logs {

	public static String class_name = "Logs";
	public static String tag = "Logs";
	public static String mErrorTag = "JiveError";

	public static void setLog(String class_name, String function_name,
			String tag, String msg) {
		try {

			Log.d(tag, "In class " + class_name + ", method " + function_name
					+ "\n  " + msg);
			if (tag.equalsIgnoreCase("request") || tag.equalsIgnoreCase("response")) {


				appendLog(tag+":    "+msg);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			setLogException(class_name, "setLog", e);
		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
			// setLogException(class_name, "setLog", mErrorTag, e);
		}
	}

	public static void setLogException(String class_name, String function_name,
			Exception mException) {

		try {
			mException.printStackTrace();
			 if(AppGlobalConstants.isDebugMode) {


				 Log.e(tag, "In class " + class_name + ", method " + function_name);

				 // if (mException != null)
				 // Log.e(tag, mException.toString());

				 appendCatchLog("In class " + class_name + ", method " + function_name + "\n " + Log.getStackTraceString(mException));

				 Log.e(mErrorTag, Log.getStackTraceString(mException));

				 if (AppGlobalConstants.isDebugMode) {
					 String exceptionName = mException.getClass().getName();

					 SocketTimeoutException so = new SocketTimeoutException();
					 ConnectTimeoutException co = new ConnectTimeoutException();

					 if (exceptionName.equalsIgnoreCase(so.getClass().getName())
							 || exceptionName.equalsIgnoreCase(co.getClass()
							 .getName())) {
						 exceptionName = "Network Operation timed out";
					 }

					 if ((mException.getMessage() != null)
							 && !mException.getMessage().trim().equalsIgnoreCase("")) {
						 exceptionName += ", \n " + mException.getMessage();
					 }

					 Toaster.popShortToast(App.getContext(), exceptionName);
				 }
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			setLogException(class_name, "setLogException", e);
		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
			Log.e(mErrorTag, Log.getStackTraceString(e));
		}
	}


public static void setLogError(String class_name, String function_name,
			Error mError) {

		try {
			mError.printStackTrace();

			Log.e(tag, "In class " + class_name + ", method " + function_name);

			// if (mException != null)
			// Log.e(tag, mException.toString());

			 appendCatchLog("In class " + class_name + ", method " + function_name+"\n "+Log.getStackTraceString(mError));

			Log.e(mErrorTag, Log.getStackTraceString(mError));

			if (AppGlobalConstants.isDebugMode) {
				String exceptionName = mError.getClass().getName();

				SocketTimeoutException so = new SocketTimeoutException();
				ConnectTimeoutException co = new ConnectTimeoutException();

				if (exceptionName.equalsIgnoreCase(so.getClass().getName())
						|| exceptionName.equalsIgnoreCase(co.getClass()
								.getName())) {
					exceptionName = "Network Operation timed out";
				}

				if ((mError.getMessage() != null)
						&& !mError.getMessage().trim().equalsIgnoreCase("")) {
					exceptionName += ", \n " + mError.getMessage();
				}

				Toaster.popShortToast(App.getContext(), exceptionName);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			setLogException(class_name, "setLogException", e);
		} catch (OutOfMemoryError e) {
			// TODO Auto-generated catch block
			Log.e(mErrorTag, Log.getStackTraceString(e));
		}
	}

	public static void appendCatchLog(String text) {
		try {

//			File cacheFile = CommonUtilities.getCacheDir(App.getContext());
			File cacheFile = CommonUtilities.createAppFolderInSDCardInNotExists();
			File logFile = new File(cacheFile.getAbsolutePath()
					+ File.separator + "JiveCatchlog.txt");
			if (!logFile.exists()) {

				logFile.createNewFile();

			}

			// BufferedWriter for performance, true to set append to file
			// flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));

			buf.append(new Date().toString() + "-" + text);
			buf.newLine();
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logs.setLogException(class_name, "appendCatchLog(), main", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.setLogException(class_name, "appendCatchLog(), main", e);
		}
	}

	public static void appendLog(String text) {
		try {

			File cacheFile = CommonUtilities.getCacheDir(App.getContext());

			File logFile = new File(cacheFile.getAbsolutePath()
					+ File.separator + "Jivelog.txt");
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Logs.setLogException(class_name, "appendLog", e);
				}
			}
			try {
				// BufferedWriter for performance, true to set append to
				// file flag
				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
						true));

				buf.append(new Date().toString() + "-" + text);
				buf.newLine();
				buf.newLine();
				buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logs.setLogException(class_name, "appendLog()", e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.setLogException(class_name, "appendLog(), main", e);
		}
	}

}