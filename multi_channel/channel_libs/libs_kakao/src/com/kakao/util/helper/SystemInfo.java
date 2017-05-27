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
package com.kakao.util.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
//import com.kakao.util.BuildConfig;

import java.io.IOException;
import java.util.Locale;

public class SystemInfo {
    private static final int OS_VERSION = Build.VERSION.SDK_INT;
    private static final String DEVICE_MODEL = Build.MODEL.replaceAll("\\s", "-").toUpperCase();
    private static final String LANGUAGE = Locale.getDefault().getLanguage().toLowerCase();
    private static final String COUNTRY_CODE = Locale.getDefault().getCountry().toUpperCase();
    private static String KA_HEADER;

    public static void initialize(final Context context) {
        if(KA_HEADER == null) {
            KA_HEADER = new StringBuilder().append(CommonProtocol.KA_SDK_KEY).append("1.1.20").append(" ")
                    .append(CommonProtocol.KA_OS_KEY).append(CommonProtocol.OS_ANDROID).append("-").append(OS_VERSION).append(" ")
                    .append(CommonProtocol.KA_LANG_KEY).append(LANGUAGE).append("-").append(COUNTRY_CODE).append(" ")
                    .append(CommonProtocol.KA_KEY_HASH).append(Utility.getKeyHash(context)).append(" ")
                    .append(CommonProtocol.KA_DEVICE_KEY).append(DEVICE_MODEL).append(" ")
                    .append(CommonProtocol.KA_ANDROID_PKG).append(Utility.getAppPackageName(context)).append(" ")
                    .append(CommonProtocol.KA_GAME_SDK).append("2.3.5")
                    .toString();
        }

        putAdidToKAHeader(context);
    }

    private static void putAdidToKAHeader(final Context context) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    String limitAdTrackingEnabled;
                    if (adInfo.isLimitAdTrackingEnabled()) {
                        limitAdTrackingEnabled = "true";
                    } else {
                        limitAdTrackingEnabled = "false";
                    }

                    String kaAdid = "adid/" + adInfo.getId() + " dnt/" + limitAdTrackingEnabled;
                    KA_HEADER = KA_HEADER + " " + kaAdid;
                } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    public static String getKAHeader() {
        return KA_HEADER;
    }
}
