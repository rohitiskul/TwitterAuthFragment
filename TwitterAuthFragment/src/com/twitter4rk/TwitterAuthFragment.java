package com.twitter4rk;

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
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
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
	 * Builder key
	 */
	private static final String BUILDER_KEY = "builder_key";
	/**
	 * Authenticated or not
	 */
	private boolean mIsAuthenticated;
	/**
	 * Web view to manage twitter authentication
	 */
	private WebView mTwitterWebView;
	/**
	 * Web view loading dialog
	 */
	private ProgressDialog mProgressDialog;
	/**
	 * Twitter fragment config builder
	 */
	private TwitterConfigBuilder mBuilder;
	/**
	 * Twitter Listener to listen to twitter auth callbacks
	 */
	private TwitterAuthListener mListener;
	/**
	 * Single instance of this fragment
	 */
	private static TwitterAuthFragment mFragment = new TwitterAuthFragment();

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
	public static void startTwitterAuth(final TwitterConfigBuilder builder,
			final TwitterAuthListener listener) {
		if (builder == null)
			throw new NullPointerException("Builder cannot be null");
		if (builder.activity == null)
			throw new NullPointerException("Activity cannot be null");

		final FragmentTransaction transaction = builder.activity
				.getSupportFragmentManager().beginTransaction();
		mFragment.setTwitterAuthListener(listener);
		final Bundle args = new Bundle();
		args.putParcelable(BUILDER_KEY, builder);
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTwitterWebView = new WebView(getActivity());
		final Bundle args = getArguments();
		if (args == null) {
			// This wont be case because startTwitterAuth() handles args for
			// fragment
			throw new IllegalArgumentException(
					"No arguments passed to fragment, Please use startTwitterAuth(...) method for showing this fragment");
		}
		// Get builder from args
		mBuilder = args.getParcelable(BUILDER_KEY);
		// Hide action bar
		if (mBuilder.hideActionBar)
			mBuilder.activity.getActionBar().hide();
		// Init progress dialog
		mProgressDialog = new ProgressDialog(mBuilder.activity);
		mProgressDialog
				.setMessage(mBuilder.progressText == null ? "Loading ..."
						: mBuilder.progressText);
		if (mBuilder.isProgressEnabled)
			mProgressDialog.show();

		// Init ConfigurationBuilder twitter4j
		final ConfigurationBuilder cb = new ConfigurationBuilder();
		if (mBuilder.isDebugEnabled)
			cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(mBuilder.consumerKey);
		cb.setOAuthConsumerSecret(mBuilder.consumerSecret);

		// Web view client to handler url loading
		mTwitterWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// Get url first
				final Uri uri = Uri.parse(url);
				// Check if we need to see for callback URL
				if (mBuilder.callbackUrl != null
						&& url.contains(mBuilder.callbackUrl)) {
					// Get req info
					String oauthToken = uri.getQueryParameter("oauth_token");
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					if (mListener != null)
						mListener.onSuccess(oauthToken, oauthVerifier);
					if (mBuilder.isActionBarVisible && mBuilder.hideActionBar
							&& getActivity() != null)
						getActivity().getActionBar().show();
					mIsAuthenticated = true;
					removeMe();
					return true;
					// If no callback URL then check for info directly
				} else if (uri.getQueryParameter("oauth_token") != null
						&& uri.getQueryParameter("oauth_verifier") != null) {
					// Get req info
					String oauthToken = uri.getQueryParameter("oauth_token");
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					if (mListener != null)
						mListener.onSuccess(oauthToken, oauthVerifier);
					if (mBuilder.isActionBarVisible && mBuilder.hideActionBar
							&& getActivity() != null)
						getActivity().getActionBar().show();
					mIsAuthenticated = true;
					removeMe();
					return true;
					// If nothing then its failure
				} else {
					// Notify user
					if (mListener != null)
						mListener
								.onFailure(new Exception(
										"Couldn't find the callback URL or oath parameters in response"));
					if (mBuilder.isActionBarVisible && mBuilder.hideActionBar
							&& getActivity() != null)
						getActivity().getActionBar().show();
					removeMe();
					return false;
				}
			}
		});
		// Web Crome client to handler progress dialog visibility
		mTwitterWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
				}
			}
		});
		final Handler handler = new Handler();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final TwitterFactory twitterFactory = new TwitterFactory(cb
							.build());
					final Twitter twitter = twitterFactory.getInstance();
					RequestToken requestToken = null;
					if (mBuilder.callbackUrl == null)
						requestToken = twitter.getOAuthRequestToken();
					else
						requestToken = twitter
								.getOAuthRequestToken(mBuilder.callbackUrl);
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
				if (mBuilder.activity == null)
					return;
				mBuilder.activity.onBackPressed();
			}
		}, 200);
	}

	@Override
	public void onDestroy() {
		if (mListener != null && !mIsAuthenticated)
			mListener.onFailure(new Exception("User cancelled"));
		if (mBuilder.isActionBarVisible && mBuilder.hideActionBar
				&& getActivity() != null)
			getActivity().getActionBar().show();
		super.onDestroy();
	}

	public interface TwitterAuthListener {
		public void onSuccess(String oauthToken, String oauthVerifier);

		public void onFailure(Exception e);
	}
}
