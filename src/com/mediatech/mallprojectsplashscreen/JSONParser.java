package com.mediatech.mallprojectsplashscreen;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	private static final String LOGTAG = "Cinemall Splash Screen";
	
	private static JSONObject mJSONObject;
	private static String mJSONArrayName = "All_Messages";
	private ArrayList<Message> mResultArrayList = new ArrayList<Message>();
	
	public JSONParser() {}
	
	public ArrayList<Message> getJSONFromUrl(String url) {
		try {
			DefaultHttpClient mHttpClient = new DefaultHttpClient();
			HttpPost mHttpPost = new HttpPost(url);
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
			Integer mResponseStatus = mHttpResponse.getStatusLine().getStatusCode();
			if (mResponseStatus == 200) {
				HttpEntity mHttpEntity = mHttpResponse.getEntity();
				String mData = EntityUtils.toString(mHttpEntity);
				
				mJSONObject = new JSONObject(mData);
				JSONArray mJSONArray = mJSONObject.getJSONArray(mJSONArrayName);
				
				for (int i = 0; i < mJSONArray.length(); i++) {
					Message mMessage = new Message();
					JSONObject mJSONMessageData = mJSONArray.getJSONObject(i);
					
					mMessage.setId(mJSONMessageData.getString("id"));
					mMessage.setTitle(mJSONMessageData.getString("title"));
					mMessage.setContent(mJSONMessageData.getString("content"));
					mMessage.setPublishedDate(mJSONMessageData.getString("published_date"));
					mMessage.setStartDate(mJSONMessageData.getString("start_date"));
					mMessage.setEndDate(mJSONMessageData.getString("end_date"));
					mMessage.setActive(mJSONMessageData.getString("active"));
					
					mResultArrayList.add(mMessage);
				}
			} else {
				Log.i(LOGTAG, "Failed to retrieve data; server response was: " + mResponseStatus.toString());
				mResultArrayList = null;
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return mResultArrayList;
	}
	
	
	
}

