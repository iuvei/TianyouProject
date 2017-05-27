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
package com.kakao.kakaotalk.request;

import com.kakao.auth.network.request.ApiRequest;
import com.kakao.kakaotalk.StringSet;
import com.kakao.network.ServerProtocol;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leoshin, created at 15. 7. 29..
 */
public class SendMemoRequest extends ApiRequest {
    @Override
    public String getMethod() {
        return POST;
    }

    @Override
    public String getUrl() {
        return ApiRequest.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.TALK_MEMO_SEND);
    }

    @Override
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(StringSet.template_id, templateId);

        if (args != null && args.length() > 0) {
            params.put(StringSet.args, args.toString());
        }
        return params;
    }

    private final String templateId;
    private final JSONObject args;

    public SendMemoRequest(String templateId, Map<String, String> args) {
        this.templateId = templateId;
        this.args = args != null ? new JSONObject(args) : null;
    }
}
