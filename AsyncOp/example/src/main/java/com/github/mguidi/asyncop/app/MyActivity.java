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

package com.github.mguidi.asyncop.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mguidi.asyncop.AsyncOpCallback;
import com.github.mguidi.asyncop.AsyncOpHelper;

/**
 * Created by marco on 21/07/14.
 */
public class MyActivity extends ActionBarActivity implements AsyncOpCallback, View.OnClickListener {

    private AsyncOpHelper mOpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mOpHelper = new AsyncOpHelper(this, savedInstanceState, this);

        findViewById(R.id.btnHello).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOpHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mOpHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOpHelper.onPause();
    }


    @Override
    public void onAsyncOpFinish(int idRequest, String action, Bundle args, Bundle result) {
        if (action.equals(Constants.ACTION_LONGOP)) {
            if (result.containsKey(LongOp.RES_RESULT)) {
                TextView txtResult = (TextView) findViewById(R.id.txtResult);
                txtResult.setText(result.getString(LongOp.RES_RESULT));
            }
        }
    }

    @Override
    public void onAsyncOpFail(int idRequest, String action) {
        if (action.equals(Constants.ACTION_LONGOP)) {
            TextView txtResult = (TextView) findViewById(R.id.txtResult);
            txtResult.setText("fail");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnHello) {
            Bundle args = new Bundle();
            args.putLong(LongOp.ARGS_TIMEOUT, 2000L);

            mOpHelper.execute(Constants.ACTION_LONGOP, args);
        }
    }

}
