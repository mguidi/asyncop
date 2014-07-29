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

package com.mguidi.asyncop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by marco on 21/07/14.
 */
public class AsyncOpManager {

    private static AsyncOpManager sInstance;

    public static synchronized AsyncOpManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AsyncOpManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private static String getAsyncOpId(String idHelper, int idRequest) {
        return idHelper + "-" + idRequest;
    }

    private ExecutorService mExecutor;
    private HashMap<String, Class<? extends AsyncOp>> mAsyncOp;
    private HashMap<String, AsyncOp> mAsyncOpRunning;

    private AsyncOpManager(Context context) {
        mExecutor = Executors.newFixedThreadPool(5);
        mAsyncOp = new HashMap<String, Class<? extends AsyncOp>>();
        mAsyncOpRunning = new HashMap<String, AsyncOp>();

        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_ASYNCOP));
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_ASYNCOP_FINISH));
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_PING));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_ASYNCOP)) {
                if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                    Log.d(Constants.LOG_TAG, "action: asyncop");
                }

                String opAction = intent.getStringExtra(Constants.ARGS_ACTION);
                Class<? extends AsyncOp> asyncOpClass = mAsyncOp.get(opAction);
                if (asyncOpClass != null) {
                    try {
                        AsyncOp asyncOp = asyncOpClass.newInstance();

                        String idHelper = intent.getStringExtra(Constants.ARGS_ID_HELPER);
                        int idRequest = intent.getIntExtra(Constants.ARGS_ID_REQUEST, -1);
                        Bundle args = intent.getBundleExtra(Constants.ARGS_ARGS);

                        asyncOp.init(context.getApplicationContext(), idHelper, idRequest, args);

                        mAsyncOpRunning.put(getAsyncOpId(idHelper, idRequest), asyncOp);
                        mExecutor.execute(asyncOp);

                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                } else {
                    if (Log.isLoggable(Constants.LOG_TAG, Log.WARN)) {
                        Log.w(Constants.LOG_TAG, "no asyncop mapped for this action");
                    }
                }
            } else if (action.equals(Constants.ACTION_ASYNCOP_FINISH)) {
                if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                    Log.d(Constants.LOG_TAG, "action: asyncop finish");
                }

                String idHelper = intent.getStringExtra(Constants.ARGS_ID_HELPER);
                int idRequest = intent.getIntExtra(Constants.ARGS_ID_REQUEST, -1);

                mAsyncOpRunning.remove(getAsyncOpId(idHelper, idRequest));

            } else if (action.equals(Constants.ACTION_PING)) {
                if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                    Log.d(Constants.LOG_TAG, "action: ping");
                }

                String idHelper = intent.getStringExtra(Constants.ARGS_ID_HELPER);
                ArrayList<Integer> pendingRequests = intent.getIntegerArrayListExtra(Constants.ARGS_PENDING_REQUESTS);

                for (int idRequest : pendingRequests) {
                    if (mAsyncOpRunning.containsKey(getAsyncOpId(idHelper, idRequest))) {
                        AsyncOp asyncOp = mAsyncOpRunning.get(getAsyncOpId(idHelper, idRequest));
                        if (asyncOp.isDone()) {
                            asyncOp.dispatchResult();
                        }

                    } else {
                        Intent failIntent = new Intent(idHelper);
                        failIntent.putExtra(Constants.ARGS_IS_FAIL, true);
                        failIntent.putExtra(Constants.ARGS_ID_REQUEST, idRequest);

                        LocalBroadcastManager.getInstance(context).sendBroadcast(failIntent);
                    }
                }
            }
        }
    };

    /**
     * Map an action to an async operation class
     *
     * @param action  action
     * @param asyncOp async operation class
     */
    public final void mapOp(String action, Class<? extends AsyncOp> asyncOp) {
        mAsyncOp.put(action, asyncOp);
    }
}
