/*
 * Copyright 2014 Marco Guidi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mguidi.asyncop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by marco on 22/07/14.
 */
public class AsyncOpHelper {

    public static final String SAVED_ID_HELPER = "com.github.mguidi.asyncop:id_helper";
    public static final String SAVED_ID_REQUEST = "com.github.mguidi.asyncop:id_request";
    public static final String SAVED_PENDING_REQUESTS = "com.github.mguidi.asyncop:pending_requests";
    public static final String SAVED_MAP_PENDING_REQUESTS_ACTION = "com.github.mguidi.asyncop:map_pending_requests_action";

    private Context mContext;
    private String mIdHelper;
    private AsyncOpCallback mAsyncOpCallback;
    private int mNextIdRequest;
    private ArrayList<Integer> mPendingRequests;
    private Bundle mMapPendingRequestsAction;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Integer idRequest = intent.getIntExtra(Constants.ARGS_ID_REQUEST, -1);

            Intent doneIntent = new Intent(Constants.ACTION_ASYNCOP_FINISH);
            doneIntent.putExtra(Constants.ARGS_ID_HELPER, mIdHelper);
            doneIntent.putExtra(Constants.ARGS_ID_REQUEST, idRequest);

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(doneIntent);

            if (mPendingRequests.contains(idRequest)) {
                String action = mMapPendingRequestsAction.getString(String.valueOf(idRequest));

                mPendingRequests.remove(idRequest);
                mMapPendingRequestsAction.remove(String.valueOf(idRequest));

                boolean isFail = intent.getBooleanExtra(Constants.ARGS_IS_FAIL, false);
                if (isFail) {
                    if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                        Log.d(Constants.LOG_TAG, "asyncop: fail");
                    }

                    mAsyncOpCallback.onAsyncOpFail(idRequest, action);

                } else {
                    if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                        Log.d(Constants.LOG_TAG, "asyncop: finish");
                    }

                    Bundle args = intent.getBundleExtra(Constants.ARGS_ARGS);
                    Bundle result = intent.getBundleExtra(Constants.ARGS_RESULTS);

                    mAsyncOpCallback.onAsyncOpFinish(idRequest, action, args, result);
                }

            } else {
                if (Log.isLoggable(Constants.LOG_TAG, Log.WARN)) {
                    Log.w(Constants.LOG_TAG, "no pending request for the idRequest received");
                }
            }
        }
    };

    /**
     * @param context
     * @param savedInstanceState
     * @param callback
     */
    public AsyncOpHelper(Context context, Bundle savedInstanceState, AsyncOpCallback callback) {
        mContext = context.getApplicationContext();
        mAsyncOpCallback = callback;

        if (savedInstanceState == null) {
            mIdHelper = UUID.randomUUID().toString();
            mNextIdRequest = 0;
            mPendingRequests = new ArrayList<Integer>();
            mMapPendingRequestsAction = new Bundle();

        } else {
            mIdHelper = savedInstanceState.getString(SAVED_ID_HELPER);
            mNextIdRequest = savedInstanceState.getInt(SAVED_ID_REQUEST);
            mPendingRequests = savedInstanceState.getIntegerArrayList(SAVED_PENDING_REQUESTS);
            mMapPendingRequestsAction = savedInstanceState.getBundle(SAVED_MAP_PENDING_REQUESTS_ACTION);
        }
    }

    /**
     * @param idRequest id of the request
     * @return return true if the request was still pending
     */
    public boolean cancel(Integer idRequest) {
        mMapPendingRequestsAction.remove(String.valueOf(idRequest));
        return mPendingRequests.remove(idRequest);
    }

    /**
     * @param idRequest id of the request
     * @return return true if the request is still pending
     */
    public boolean isLoading(Integer idRequest) {
        return mPendingRequests.contains(idRequest);
    }

    /**
     * @param action action
     * @param args   arguments
     * @return id of the request
     */
    public int execute(String action, Bundle args) {
        int idRequest = mNextIdRequest++;
        mPendingRequests.add(idRequest);
        mMapPendingRequestsAction.putString(String.valueOf(idRequest), action);

        Intent intent = new Intent(Constants.ACTION_ASYNCOP);
        intent.putExtra(Constants.ARGS_ACTION, action);
        intent.putExtra(Constants.ARGS_ID_HELPER, mIdHelper);
        intent.putExtra(Constants.ARGS_ID_REQUEST, idRequest);
        intent.putExtra(Constants.ARGS_ARGS, args);

        if (!LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)) {
            if (Log.isLoggable(Constants.LOG_TAG, Log.ERROR)) {
                Log.e(Constants.LOG_TAG, "asyncop manager not initialize");
            }
            throw new RuntimeException("asyncop manager not initialize");
        }

        return idRequest;
    }

    /**
     *
     */
    public void onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, new IntentFilter(mIdHelper));

        if (mPendingRequests.size() > 0) {
            Intent intent = new Intent(Constants.ACTION_PING);
            intent.putExtra(Constants.ARGS_ID_HELPER, mIdHelper);
            intent.putExtra(Constants.ARGS_PENDING_REQUESTS, mPendingRequests);

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    /**
     *
     */
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    /**
     * @param outState state
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_ID_HELPER, mIdHelper);
        outState.putInt(SAVED_ID_REQUEST, mNextIdRequest);
        outState.putIntegerArrayList(SAVED_PENDING_REQUESTS, mPendingRequests);
        outState.putBundle(SAVED_MAP_PENDING_REQUESTS_ACTION, mMapPendingRequestsAction);
    }
}
