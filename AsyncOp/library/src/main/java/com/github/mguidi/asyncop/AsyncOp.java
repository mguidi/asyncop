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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by marco on 22/07/14.
 */
public abstract class AsyncOp implements Runnable {

    private Context mContext;
    private String mIdHelper;
    private int mIdRequest;
    private Bundle mArgs;
    private Bundle mResult;
    private boolean mIsDone;

    protected final void init(Context context, String idHelper, int idRequest, Bundle args) {
        mContext = context;
        mIdHelper = idHelper;
        mIdRequest = idRequest;
        mArgs = args;
        mIsDone = false;
    }

    protected final boolean isDone() {
        return mIsDone;
    }

    protected final void dispatchResult() {
        Intent intent = new Intent(mIdHelper);
        intent.putExtra(Constants.ARGS_ID_REQUEST, mIdRequest);
        intent.putExtra(Constants.ARGS_ARGS, mArgs);
        intent.putExtra(Constants.ARGS_RESULTS, mResult);

        if (!LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)) {
            if (Log.isLoggable(Constants.LOG_TAG, Log.DEBUG)) {
                Log.d(Constants.LOG_TAG, "dispatch result missed");
            }
        }
    }

    @Override
    public final void run() {
        mResult = execute(mContext, mArgs);
        mIsDone = true;
        dispatchResult();
    }

    /**
     * @param context application context
     * @param args    arguments
     * @return result
     */
    public abstract Bundle execute(Context context, Bundle args);

}
