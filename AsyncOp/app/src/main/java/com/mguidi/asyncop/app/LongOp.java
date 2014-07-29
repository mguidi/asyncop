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

package com.mguidi.asyncop.app;

import android.content.Context;
import android.os.Bundle;

import com.mguidi.asyncop.AsyncOp;

/**
 * Created by marco on 21/07/14.
 */
public class LongOp extends AsyncOp {

    @Override
    public Bundle execute(Context context, Bundle args) {
        System.out.println("wait");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("done");

        Bundle result = new Bundle(1);
        result.putString("result", "hello");

        return result;
    }

}
