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

import android.os.Bundle;

/**
 * Created by marco on 22/07/14.
 */
public interface AsyncOpCallback {

    /**
     * @param idRequest id of the request
     * @param action    action
     * @param args      arguments
     * @param result    result
     */
    public void onAsyncOpFinish(int idRequest, String action, Bundle args, Bundle result);

    /**
     * @param idRequest id of the request
     * @param action    action
     */
    public void onAsyncOpFail(int idRequest, String action);

}
