package com.tweet4rk;
/**
 * Copyright 2014 Rohit Kulkarni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * */


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Twitter Fragment for managing twitter authentication
 * 
 * @author Rohit Kulkarni
 * 
 */
public final class TwitterAuthFragment extends Fragment {

	/**
	 * Fragment TAG used while adding this fragment
	 */
	public static final String TWITTER_AUTH_FRAGMENT_TAG = "TWITTER-AUTH-FRAGMENT";
	/**
	 * Callback url for requesting desired data from twitter4j API
	 */
	private static final String CALLBACK_URL_KEY = "callback_url_key";
	/**
	 * Consumer key can be obtained from twitter app
	 */
	private static final String CONSUMER_KEY_KEY = "consumer_key_key";
	/**
	 * Consumer key can be obtained from twitter app
	 */
	private static final String CONSUMER_SECRET_KEY = "consumer_secret_key";
	/**
	 * Debug flag
	 */
	private boolean mIsDebug;
	/**
	 * Authenticated or not
	 */
	private boolean mIsAuthenticated;
	/**
	 * Web view to manage twitter authentication
	 */
	private WebView mTwitterWebView;
	/**
	 * Twitter Listener to listen to twitter auth callbacks
	 */
	private TwitterAuthListener mListener;
	/**
	 * Single instance of this fragment
	 */
	private static TwitterAuthFragment mFragment;

	/**
	 * Method to start twitter authentication
	 * 
	 * @param activity
	 *            Activity responsible for showing this fragment
	 * @param consumerKey
	 *            Consumer key can be obtained from twitter app
	 * @param consumerSecret
	 *            Consumer secret can be obtained from twitter app
	 * @param callbackUrl
	 *            url set in Twitter app settings, Pass <b>NULL</b> if no
	 *            callback url
	 * @param listener
	 *            to listen to auth callbacks
	 */
	public static void startTwitterAuth(final FragmentActivity activity,
			final String consumerKey, final String consumerSecret,
			final String callbackUrl, final TwitterAuthListener listener) {
		if (activity == null)
			return;
		final FragmentTransaction transaction = activity
				.getSupportFragmentManager().beginTransaction();
		mFragment = new TwitterAuthFragment();
		mFragment.setTwitterAuthListener(listener);
		final Bundle args = new Bundle();
		args.putString(CALLBACK_URL_KEY, callbackUrl);
		args.putString(CONSUMER_KEY_KEY, consumerKey);
		args.putString(CONSUMER_SECRET_KEY, consumerSecret);
		mFragment.setArguments(args);
		transaction.add(android.R.id.content, mFragment,
				TWITTER_AUTH_FRAGMENT_TAG);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	/**
	 * Set listener to listen to Auth callbacks
	 * 
	 * @param listener
	 *            TwitterAuthListener
	 */
	public void setTwitterAuthListener(final TwitterAuthListener listener) {
		mListener = listener;
	}

	public void enableDebug() {
		mIsDebug = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTwitterWebView = new WebView(getActivity());
		final Bundle args = getArguments();
		if (args == null) {
			// This wont be case because startTwitterAuth() handles args for
			// fragment
			if (mListener != null)
				mListener.onFailure(new IllegalArgumentException(
						"No arguments passed to fragment"));
			removeMe();
			return mTwitterWebView;
		}
		final String callbackURL = args.getString(CALLBACK_URL_KEY);
		final String consumerKey = args.getString(CONSUMER_KEY_KEY);
		final String consumerSecretKey = args.getString(CONSUMER_SECRET_KEY);

		final ConfigurationBuilder cb = new ConfigurationBuilder();
		if (mIsDebug)
			cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecretKey);
		final Handler handler = new Handler();
		mTwitterWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				final Uri uri = Uri.parse(url);
				if (url.contains(callbackURL)) {
					String oauthToken = uri.getQueryParameter("oauth_token");
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					if (mListener != null)
						mListener.onSuccess(oauthToken, oauthVerifier);
					mIsAuthenticated = true;
					removeMe();
					return true;
				} else if (uri.getQueryParameter("oauth_token") != null
						&& uri.getQueryParameter("oauth_verifier") != null) {
					String oauthToken = uri.getQueryParameter("oauth_token");
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					if (mListener != null)
						mListener.onSuccess(oauthToken, oauthVerifier);
					mIsAuthenticated = true;
					removeMe();
					return true;
				} else {
					if (mListener != null)
						mListener
								.onFailure(new Exception(
										"Couldn't find the callback URL or oath parameters in response"));
					removeMe();
					return false;
				}
			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final TwitterFactory twitterFactory = new TwitterFactory(cb
							.build());
					final Twitter twitter = twitterFactory.getInstance();
					RequestToken requestToken = null;
					if (callbackURL == null)
						requestToken = twitter.getOAuthRequestToken();
					else
						requestToken = twitter
								.getOAuthRequestToken(callbackURL);
					final RequestToken finalRequestToken = requestToken;
					handler.post(new Runnable() {

						@Override
						public void run() {
							final String url = finalRequestToken
									.getAuthorizationURL();
							mTwitterWebView.loadUrl(url);
						}
					});
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
		}).start();

		return mTwitterWebView;
	}

	/**
	 * Self destruction
	 */
	public void removeMe() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				FragmentActivity activity = getActivity();
				if (activity == null)
					return;
				activity.onBackPressed();
			}
		}, 200);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mListener != null && !mIsAuthenticated)
			mListener.onFailure(new Exception("User cancelled"));
	}

	public interface TwitterAuthListener {
		public void onSuccess(String oauthToken, String oauthVerifier);

		public void onFailure(Exception e);
	}
}
