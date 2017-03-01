package com.tianyou.sdk.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.tianyou.sdk.activity.ExitActivity;
import com.tianyou.sdk.activity.FloatMenu;
import com.tianyou.sdk.activity.LoginActivity;
import com.tianyou.sdk.activity.PayActivity;
import com.tianyou.sdk.base.FloatControl;
import com.tianyou.sdk.bean.ServerInfo;
import com.tianyou.sdk.bean.ServerInfo.ResultBean.CustominfoBean;
import com.tianyou.sdk.holder.ConfigHolder;
import com.tianyou.sdk.holder.SPHandler;
import com.tianyou.sdk.holder.URLHolder;
import com.tianyou.sdk.utils.AppUtils;
import com.tianyou.sdk.utils.HttpUtils;
import com.tianyou.sdk.utils.HttpUtils.HttpCallback;
import com.tianyou.sdk.utils.HttpUtils.HttpsCallback;
import com.tianyou.sdk.utils.LogUtils;
import com.tianyou.sdk.utils.ToastUtils;
import com.tianyou.sdk.utils.XMLParser;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;
import com.umeng.analytics.MobclickAgent.UMAnalyticsConfig;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class Tianyouxi {

	public static Activity mActivity;
	public static TianyouCallback mTianyouCallback;
	
	// 初始化接口
	@SuppressWarnings("unchecked")
	public static void applicationInit(Context context, String gameId, String gameToken, boolean isLandscape) {
		LogUtils.d("appliction初始化");
		ConfigHolder.isLandscape = isLandscape;
		InputStream is;
		try {
			is = context.getAssets().open("tianyou_config.xml");
		} catch (IOException e) {
			e.printStackTrace();
			is = null;
		}
		if (is == null) LogUtils.e("没有找到配置文件");
		Map<String, Object> mm = XMLParser.paraserXML(is);
		ConfigHolder.channelId = (String) ((Map<String, Object>) mm.get("infos")).get("channel_id");
		ConfigHolder.isOverseas = "1".equals((String) ((Map<String, Object>) mm.get("infos")).get("is_overseas"));
		ConfigHolder.isOpenLog = "1".equals((String) ((Map<String, Object>) mm.get("infos")).get("log_switch"));
		ConfigHolder.gameId = gameId;
		ConfigHolder.gameToken = gameToken;

		// 友盟统计
		MobclickAgent.setScenarioType(context, EScenarioType.E_UM_NORMAL);// 设置为普通统计场景类型
		String umAppKey = AppUtils.getMetaDataValue(context, "UMENG_APPKEY");
		String umChannel = AppUtils.getMetaDataValue(context, "UMENG_CHANNEL");
		UMAnalyticsConfig umAnalyticsConfig = new UMAnalyticsConfig(context, umAppKey, umChannel);
		MobclickAgent.startWithConfigure(umAnalyticsConfig);

		// 友盟推送
		PushAgent mPushAgent = PushAgent.getInstance(context);
		mPushAgent.setDebugMode(false);
		mPushAgent.register(new IUmengRegisterCallback() {
			@Override
			public void onSuccess(String deviceToken) { }

			@Override
			public void onFailure(String s, String s1) { }
		});
		
		if (ConfigHolder.isOverseas) {
			//facebook初始化
			FacebookSdk.sdkInitialize(context);
			AppEventsLogger.activateApp((Application)context);
		}

	}
	
	// Activity初始化接口
	public static void activityInit(Activity activity, TianyouCallback callback) {
		mActivity = activity;
		mTianyouCallback = callback;
		createFloatMenu();
		getPayWay();
		getServiceInfo();
		showLoginWay();
	}
	
	// 显示隐藏登录方式
	private static void showLoginWay() {
		Map<String,String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("usertoken", ConfigHolder.gameToken);
		HttpUtils.post(mActivity, URLHolder.URL_LOGIN_WAY, map, new HttpsCallback() {
			@Override
			public void onSuccess(String response) {
				SPHandler.putString(mActivity, SPHandler.SP_LOGIN_WAY, response);
			}
		});
	}

	// 支付方式控制
	private static void getPayWay(){
		Map<String,String> map = new HashMap<String, String>();
    	map.put("appID", ConfigHolder.gameId);
		map.put("usertoken", ConfigHolder.gameToken);
		map.put("language", ConfigHolder.gameToken);
		HttpUtils.post(mActivity, URLHolder.URL_PAY_WAY_CONTROL, map, new HttpCallback() {
			@Override
			public void onSuccess(String response) {
				SPHandler.putString(mActivity, SPHandler.SP_PAY_WAY, response);
			}
			
			@Override
			public void onFailed() { }
		});
	}
	
	// 创建悬浮球接口
	private static void createFloatMenu() {
		final Map<String, String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("usertoken", ConfigHolder.gameToken);
		map.put("language", AppUtils.getLanguageSort(mActivity));
		HttpUtils.post(mActivity, URLHolder.URL_FLOAT_CONTROL, map, new HttpUtils.HttpsCallback() {
			@Override
			public void onSuccess(String response) {
				FloatControl control = new Gson().fromJson(response, FloatControl.class);
				if (control.getResult().getCode() == 200) {
					SPHandler.putString(mActivity, SPHandler.SP_FLOAT_CONTROL, response);
					if (control.getResult().getStatus() != 0) {
						new FloatMenu(mActivity).createLogoPopupWindow();
					}
				} else {
					ToastUtils.show(mActivity, control.getResult().getMsg());
				}
			}
		});
	}
	
	// 获取客户服务信息
	private static void getServiceInfo() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("usertoken", ConfigHolder.gameToken);
		HttpUtils.post(mActivity, URLHolder.URL_SERVER_IMG, map, new HttpsCallback() {
			@Override
			public void onSuccess(String response) {
				ServerInfo serverInfo = new Gson().fromJson(response, ServerInfo.class);
    			if (serverInfo.getResult().getCode() == 200) {
    				CustominfoBean customInfo = serverInfo.getResult().getCustominfo();
    				SPHandler.putString(mActivity, SPHandler.SP_URL_PHONE, customInfo.getCall().getImgurl());
    				SPHandler.putString(mActivity, SPHandler.SP_URL_QQ, customInfo.getQq().getImgurl());
    				SPHandler.putString(mActivity, SPHandler.SP_URL_WX, customInfo.getWx().getImgurl());
    				SPHandler.putString(mActivity, SPHandler.SP_TEXT_PHONE, customInfo.getCall().getValue());
    				SPHandler.putString(mActivity, SPHandler.SP_TEXT_QQ, customInfo.getQq().getValue());
    				SPHandler.putString(mActivity, SPHandler.SP_TEXT_WX, customInfo.getWx().getValue());
    			}
			}
		});
	}

	// 登陆接口
	public static void login(String gameName) {
		ConfigHolder.gameName = gameName;
		if (ConfigHolder.userIsLogin) {
			ToastUtils.show(mActivity, "用户已登录");
		} else {
			mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
		}
	}

	// 支付接口
	public static void pay(String payInfo) {
		if (ConfigHolder.userIsLogin) {
			Intent intent = new Intent(mActivity, PayActivity.class);
			intent.putExtra("payInfo", payInfo);
			mActivity.startActivity(intent);
		} else {
			ToastUtils.show(mActivity, "请先登录！");
		}
	}
	
	// 支付接口
	public static void pay(String payInfo, int money, String productDesc) {
		if (ConfigHolder.userIsLogin) {
			Intent intent = new Intent(mActivity, PayActivity.class);
			intent.putExtra("payInfo", payInfo);
			intent.putExtra("money", money);
			intent.putExtra("productDesc", productDesc);
			mActivity.startActivity(intent);
		} else {
			ToastUtils.show(mActivity, "请先登录！");
		}
	}

	// 进入游戏
	public static void enterGame(String jsonData) {
		if (!ConfigHolder.userIsLogin) {
			ToastUtils.show(mActivity, "请先登录！");
			return;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("userID", ConfigHolder.userId);
		map.put("channel", ConfigHolder.channelId);
		try {
			JSONObject roleInfo;
			roleInfo = new JSONObject(jsonData);
			map.put("roleID", roleInfo.getString("roleId"));
			map.put("roleLevel", roleInfo.getString("roleLevel"));
			map.put("serverID", roleInfo.getString("serverId"));
			map.put("serverName", roleInfo.getString("serverName"));
			map.put("vipLevel", roleInfo.getString("vipLevel"));
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtils.show(mActivity, "角色信息解析错误");
		}
		HttpUtils.post(mActivity, URLHolder.URL_ENTER_GAME, map, new HttpsCallback() {
			@Override
			public void onSuccess(String response) { }
		});
	}

	// 创建角色
	public static void createRole(String jsonData) {
		if (!ConfigHolder.userIsLogin) {
			ToastUtils.show(mActivity, "请先登录！");
			return;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("userID", ConfigHolder.userId);
		map.put("channel", ConfigHolder.channelId);
		try {
			JSONObject roleInfo;
			roleInfo = new JSONObject(jsonData);
			map.put("roleID", roleInfo.getString("roleId"));
			map.put("roleName", roleInfo.getString("roleName"));
			map.put("serverID", roleInfo.getString("serverId"));
			map.put("serverName", roleInfo.getString("serverName"));
			map.put("profession", roleInfo.getString("profession"));
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtils.show(mActivity, "角色信息解析错误");
		}
		HttpUtils.post(mActivity, URLHolder.URL_CREATE_ROLE, map, new HttpsCallback() {
			@Override
			public void onSuccess(String response) { }
		});
	}
	
	// 更新角色信息
	public static void updateRoleInfo(String jsonData) {
		if (!ConfigHolder.userIsLogin) {
			ToastUtils.show(mActivity, "请先登录！");
			return;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("appID", ConfigHolder.gameId);
		map.put("userID", ConfigHolder.userId);
		map.put("channel", ConfigHolder.channelId);
		try {
			JSONObject roleInfo;
			roleInfo = new JSONObject(jsonData);
			map.put("roleId", roleInfo.getString("roleId"));
			map.put("roleLevel", roleInfo.getString("roleLevel"));
			map.put("roleName", roleInfo.getString("roleName"));
			map.put("serverId", roleInfo.getString("serverId"));
			map.put("serverName", roleInfo.getString("serverName"));
			map.put("vipLevel", roleInfo.getString("vipLevel"));
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtils.show(mActivity, "角色信息解析错误");
		}
		HttpUtils.post(mActivity, URLHolder.URL_UPDATE_ROLE_INFO, map, new HttpsCallback() {
			@Override
			public void onSuccess(String response) { }
		});
	}

	// 退出游戏接口
	public static void exitGame() {
		mActivity.startActivity(new Intent(mActivity, ExitActivity.class));
	}
}
