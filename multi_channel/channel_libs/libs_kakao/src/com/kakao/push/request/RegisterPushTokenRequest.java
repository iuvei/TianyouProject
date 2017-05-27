/**
 * Copyright 2014 Daum Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
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
 */
package com.kakao.push.request;

import com.kakao.network.ServerProtocol;
import com.kakao.auth.network.request.ApiRequest;
import com.kakao.push.StringSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leoshin, created at 15. 8. 10..
 */
public class RegisterPushTokenRequest extends ApiRequest {
    private final String pushToken;
    private final String deviceId;

    public RegisterPushTokenRequest(String pushToken, String deviceId) {
        this.pushToken = pushToken;
        this.deviceId = deviceId;
    }

    @Override
    public String getMethod() {
        return POST;
    }

    @Override
    public String getUrl() {
        return createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_REGISTER_PATH);
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(StringSet.device_id, deviceId);
        params.put(StringSet.push_type, StringSet.gcm);
        params.put(StringSet.push_token, pushToken);
        return params;
    }
}
