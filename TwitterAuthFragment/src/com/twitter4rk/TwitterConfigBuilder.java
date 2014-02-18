package com.twitter4rk;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;

public class TwitterConfigBuilder implements Parcelable {

	/**
	 * Log tag
	 */
	private static final String TAG = "###TwitterConfigBuilder###";
	/**
	 * Application/activity context
	 */
	FragmentActivity activity;
	/**
	 * Debug flag
	 */
	boolean isDebugEnabled;
	/**
	 * Twitter consumer key
	 */
	String consumerKey;
	/**
	 * Twitter consumer secret
	 */
	String consumerSecret;
	/**
	 * whether to show progress bar
	 */
	boolean isProgressEnabled;
	/**
	 * Progress Text is progress is available
	 */
	String progressText;
	/**
	 * Whether action bar is available
	 */
	boolean hideActionBar;
	/**
	 * Twitter callback url
	 */
	String callbackUrl;
	/**
	 * Is action bar available
	 */
	boolean isActionBarVisible;
	/**
	 * Single instance
	 */
	private static TwitterConfigBuilder builder;

	private TwitterConfigBuilder(FragmentActivity activity) {
		this.activity = activity;
		if (Build.VERSION.SDK_INT >= 11 && this.activity.getActionBar() != null
				&& this.activity.getActionBar().isShowing())
			isActionBarVisible = true;
	}

	public static TwitterConfigBuilder Builder(FragmentActivity activity) {
		if (builder == null)
			builder = new TwitterConfigBuilder(activity);
		return builder;
	}

	public TwitterConfigBuilder setDebug(boolean enabled) {
		builder.isDebugEnabled = enabled;
		return builder;
	}

	public TwitterConfigBuilder setConsumerKey(String consumerKey) {
		builder.consumerKey = consumerKey;
		return builder;
	}

	public TwitterConfigBuilder setConsumerSecret(String consumerSecret) {
		builder.consumerSecret = consumerSecret;
		return builder;
	}

	public TwitterConfigBuilder setCallbakUrl(String callbackUrl) {
		builder.callbackUrl = callbackUrl;
		return builder;
	}

	public TwitterConfigBuilder setShowProgress(boolean enabled) {
		builder.isProgressEnabled = enabled;
		return builder;
	}

	public TwitterConfigBuilder hideActionBar(boolean hide) {
		if (Build.VERSION.SDK_INT >= 11) {
			builder.hideActionBar = hide;
		}
		return builder;
	}

	public TwitterConfigBuilder setProgressMessage(String message) {
		builder.progressText = message;
		return builder;
	}

	protected TwitterConfigBuilder(Parcel in) {
		activity = (FragmentActivity) in.readValue(FragmentActivity.class
				.getClassLoader());
		isDebugEnabled = in.readByte() != 0x00;
		consumerKey = in.readString();
		consumerSecret = in.readString();
		isProgressEnabled = in.readByte() != 0x00;
		hideActionBar = in.readByte() != 0x00;
		callbackUrl = in.readString();
		isActionBarVisible = in.readByte() != 0x00;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(activity);
		dest.writeByte((byte) (isDebugEnabled ? 0x01 : 0x00));
		dest.writeString(consumerKey);
		dest.writeString(consumerSecret);
		dest.writeByte((byte) (isProgressEnabled ? 0x01 : 0x00));
		dest.writeByte((byte) (hideActionBar ? 0x01 : 0x00));
		dest.writeString(callbackUrl);
		dest.writeByte((byte) (isActionBarVisible ? 0x01 : 0x00));
	}

	public static final Parcelable.Creator<TwitterConfigBuilder> CREATOR = new Parcelable.Creator<TwitterConfigBuilder>() {
		@Override
		public TwitterConfigBuilder createFromParcel(Parcel in) {
			return new TwitterConfigBuilder(in);
		}

		@Override
		public TwitterConfigBuilder[] newArray(int size) {
			return new TwitterConfigBuilder[size];
		}
	};
}
