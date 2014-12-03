TwitterAuthFragment - Android
===================

Twitter auth api with single method call using android fragment. 
You can download a jar file **[here](https://github.com/rohitiskul/TwitterAuthFragment/raw/master/libs/twitter4rk_v1.1.jar)**


Usage 
===================

``` java
// Create builder for the auth fragment
TwitterConfigBuilder builder = TwitterConfigBuilder.Builder(this)
				.hideActionBar(true)
				.setShowProgress(true)
				.setProgressMessage(LOADING_MESSAGE)
				.setCallbakUrl(CALLBACK_URL)
				.setConsumerKey(CONSUMER_KEY)
				.setConsumerSecret(CONSUMER_SECRET);
// Start authentication				
TwitterAuthFragment.startTwitterAuth(builder, new TwitterAuthFragment.TwitterAuthListener() {
	
		@Override
		public void onSuccess(String oauthToken, String oauthVerifier) {
			// Here do whatever your like with oauthToken and oauthVerifier
		}

		@Override
		public void onFailure(Exception e) {
			// Failure 
		}
	});
```

License
===================

    Copyright 2014 Rohit Kulkarni
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
     
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
