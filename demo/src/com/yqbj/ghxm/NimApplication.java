package com.yqbj.ghxm;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.mixpush.NIMPushClient;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.netease.yqbj.avchatkit.AVChatKit;
import com.netease.yqbj.avchatkit.config.AVChatOptions;
import com.netease.yqbj.avchatkit.model.ITeamDataProvider;
import com.netease.yqbj.avchatkit.model.IUserInfoProvider;
import com.netease.yqbj.rtskit.RTSKit;
import com.netease.yqbj.rtskit.api.config.RTSOptions;
import com.netease.yqbj.uikit.api.NimUIKit;
import com.netease.yqbj.uikit.api.UIKitOptions;
import com.netease.yqbj.uikit.business.contact.core.query.PinYin;
import com.netease.yqbj.uikit.business.team.helper.TeamHelper;
import com.netease.yqbj.uikit.business.uinfo.UserInfoHelper;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tinker.entry.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.yqbj.ghxm.chatroom.ChatRoomSessionHelper;
import com.yqbj.ghxm.common.util.LogHelper;
import com.yqbj.ghxm.common.util.crash.AppCrashHandler;
import com.yqbj.ghxm.config.Constants;
import com.yqbj.ghxm.config.preference.Preferences;
import com.yqbj.ghxm.config.preference.UserPreferences;
import com.yqbj.ghxm.contact.ContactHelper;
import com.yqbj.ghxm.main.SplashActivity;
import com.yqbj.ghxm.main.activity.WelcomeActivity;
import com.yqbj.ghxm.mixpush.DemoMixPushMessageHandler;
import com.yqbj.ghxm.mixpush.DemoPushContentProvider;
import com.yqbj.ghxm.redpacket.NIMRedPacketClient;
import com.yqbj.ghxm.requestutils.RequestInterceptor;
import com.yqbj.ghxm.rts.RTSHelper;
import com.yqbj.ghxm.session.NimDemoLocationProvider;
import com.yqbj.ghxm.session.SessionHelper;
import com.yqbj.ghxm.utils.EventBusUtils;
import com.yqbj.ghxm.utils.cookieUtil.PersistentCookieStore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class NimApplication extends Application {

    public static String APP_ID;                   //微信APPid
    public static String SECRET;     //微信Secret
    public static IWXAPI api;

    public static String ALIPAY_APPID;             //支付宝APPID  测试环境 2019032063603734  正式环境 2019060365466232
    public static String ALIPAY_PID;               //支付宝APPID  测试环境 2088331211860731  正式环境 2088531221113007

    private static NimApplication instance;
    public static OkHttpClient okHttpClient;
    private ApplicationLike tinkerApplicationLike;

    public  OkHttpClient getOkHttpClinetInstance(){
        if (okHttpClient == null){
            synchronized (OkHttpClient.class){
                if (okHttpClient == null){
                    okHttpClient = new OkHttpClient.Builder().cookieJar(new CookiesManager(this)).readTimeout(300, TimeUnit.SECONDS).writeTimeout(300,TimeUnit.SECONDS).build();
                }
            }
        }
        return okHttpClient;
    }

    private class CookiesManager implements CookieJar {

        private PersistentCookieStore cookieStore;
        public CookiesManager(Context context){
            cookieStore= new PersistentCookieStore(context);
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            if (cookies != null && cookies.size() > 0) {
                for (Cookie item : cookies) {
                    cookieStore.add(url, item);
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies;
        }
    }



    public static synchronized NimApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    /**
     * 注意：每个进程都会创建自己的Application 然后调用onCreate() 方法，
     * 如果用户有自己的逻辑需要写在Application#onCreate()（还有Application的其他方法）中，一定要注意判断进程，不能把业务逻辑写在core进程，
     * 理论上，core进程的Application#onCreate()（还有Application的其他方法）只能做与im sdk 相关的工作
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        disableAPIDialog();
        initTinker();
        if (isApkInDebug()){
            ALIPAY_APPID = "2019032063603734";
            ALIPAY_PID = "2088331211860731";
            APP_ID = "wx9767fd8beab57eb6";
            SECRET = "35a32ee1a893b236fa87f10292823246";
        }else {
            ALIPAY_APPID = "2019060365466232";
            ALIPAY_PID = "2088531221113007";
            APP_ID = "wxf8fd85aa6f55069a";
            SECRET = "b4d630ad645b132f262b609d6421a4a5";
        }

        ALIPAY_APPID = "2019060365466232";
        ALIPAY_PID = "2088531221113007";
        APP_ID = "wxf8fd85aa6f55069a";
        SECRET = "b4d630ad645b132f262b609d6421a4a5";

        initUMeng();
        initWeiXin();
        initOkgo();
        //EventBus工具类
        EventBusUtils.init();
        // 内存泄漏检测
        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
        }

        DemoCache.setContext(this);
        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        NIMClient.init(this, getLoginInfo(), NimSDKOptionConfig.getSDKOptions(this));

        // crash handler
        AppCrashHandler.getInstance(this);

//        Snake.init(this);

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {

            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(new DemoMixPushMessageHandler());

            // 初始化红包模块，在初始化UIKit模块之前执行
            NIMRedPacketClient.init(this);
            // init pinyin
            PinYin.init(this);
            PinYin.validate();
            // 初始化UIKit模块
            initUIKit();
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
            //关闭撤回消息提醒
//            NIMClient.toggleRevokeMessageNotification(false);
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true);
            // 初始化音视频模块
            initAVChatKit();
            // 初始化rts模块
            initRTSKit();
        }

    }

    private void initTinker() {
        if (BuildConfig.TINKER_ENABLE) {
            // 我们可以从这里获得Tinker加载过程的信息
            tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();

            // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
            TinkerPatch.init(tinkerApplicationLike)
                    .reflectPatchLibrary()
                    .setPatchRollbackOnScreenOff(true)
                    .setPatchRestartOnSrceenOff(true);

            // 每隔1个小时去访问后台时候有更新,通过handler实现轮训的效果
            new FetchPatchHandler().fetchPatchWithInterval(1);
        }
    }

    private void initUMeng() {

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "");

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        /**
         * 设置组件化的Log开关
         * 参数: boolean 默认为false，如需查看LOG设置为true
         */
        UMConfigure.setLogEnabled(false);
    }

    private void initOkgo() {
        //----------------------------------------------------------------------------------------//

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("okgo");
        if (!Constants.DEBUG) {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BASIC);        //log打印级别，决定了log显示的详细程度
        } else {
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);
        }
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志

        builder.addInterceptor(new RequestInterceptor());

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS * 3, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS * 3, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS * 3, TimeUnit.MILLISECONDS);   //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));              //使用数据库保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        //方法二：自定义信任规则，校验服务端证书
//        HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));

        if (isApkInDebug()){
            //Debug版本不需要证书认证
            builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
//            try {
//                HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("wulewan.cer"));
//                builder.sslSocketFactory(sslParams3.sSLSocketFactory, sslParams3.trustManager);
//                //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//                builder.hostnameVerifier(new SafeHostnameVerifier());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }else {
            try {
                HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("wulewan.cer"));
                builder.sslSocketFactory(sslParams3.sSLSocketFactory, sslParams3.trustManager);
                //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
                builder.hostnameVerifier(new SafeHostnameVerifier());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 其他统一的配置
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.FIRST_CACHE_THEN_REQUEST)//全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
    }
    private class SafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                for (X509Certificate certificate : chain) {
                    certificate.checkValidity(); //检查证书是否过期，签名是否通过等
                }
            } catch (Exception e) {
                throw new CertificateException(e);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    /**
     * 认证规则
     */
    private class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
            return hostname.equals("api.weixin.qq.com") || hostname.equals("gate.wulewan.cn") || hostname.equals("im.wulewan.cn");
            // "https://im.wulewan.cn/"
//            String host = BASE_URL.substring("https://".length(), BASE_URL.length()-1);
//            return hostname.equals(host);
//            return true;
        }
    }

    private void initWeiXin() {
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);
    }

    private LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    private void initUIKit() {
        // 初始化
        NimUIKit.init(this, buildUIKitOptions());

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());

        // IM 会话窗口的定制初始化。
        SessionHelper.init();

        // 聊天室聊天窗口的定制初始化。
        ChatRoomSessionHelper.init();

        // 通讯录列表定制初始化
        ContactHelper.init();

        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());

//        NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }

    private UIKitOptions buildUIKitOptions() {
        UIKitOptions options = new UIKitOptions();
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app";
        return options;
    }

    private void initAVChatKit() {
        AVChatOptions avChatOptions = new AVChatOptions() {
            @Override
            public void logout(Context context) {
//                MainActivity.logout(context, true);
                SplashActivity.logout(context, true);
            }
        };
        avChatOptions.entranceActivity = WelcomeActivity.class;
        avChatOptions.notificationIconRes = R.drawable.ic_logo;
        AVChatKit.init(avChatOptions);

        // 初始化日志系统
        LogHelper.init();
        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NimUIKit.getUserInfoProvider().getUserInfo(account);
            }

            @Override
            public String getUserDisplayName(String account) {
                return UserInfoHelper.getUserDisplayName(account);
            }
        });
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
            @Override
            public String getDisplayNameWithoutMe(String teamId, String account) {
                return TeamHelper.getDisplayNameWithoutMe(teamId, account);
            }

            @Override
            public String getTeamMemberDisplayName(String teamId, String account) {
                return TeamHelper.getTeamMemberDisplayName(teamId, account);
            }
        });
    }

    private void initRTSKit() {
        RTSOptions rtsOptions = new RTSOptions() {
            @Override
            public void logout(Context context) {
//                MainActivity.logout(context, true);
                SplashActivity.logout(context, true);
            }
        };
        RTSKit.init(rtsOptions);
        RTSHelper.init();
    }

    //判断当前应用是否是debug状态
    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = instance.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 反射 禁止弹窗
     */
    private void disableAPIDialog(){
        if (Build.VERSION.SDK_INT < 28)return;
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
