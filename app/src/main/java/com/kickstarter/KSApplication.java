package com.kickstarter;

import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.PushNotifications;
import com.kickstarter.libs.SegmentTrackingClient;
import com.kickstarter.libs.braze.RemotePushClientType;
import com.kickstarter.libs.utils.ApplicationLifecycleUtil;
import com.kickstarter.libs.utils.Secrets;

import org.joda.time.DateTime;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import timber.log.Timber;

public class KSApplication extends MultiDexApplication implements IKSApplicationComponent {
  private ApplicationComponent component;
  @Inject protected CookieManager cookieManager;
  @Inject protected PushNotifications pushNotifications;
  @Inject protected RemotePushClientType remotePushClientType;
  @Inject protected SegmentTrackingClient segmentTrackingClient;

  @Override
  @CallSuper
  public void onCreate() {
    super.onCreate();

    this.component = getComponent();
    component().inject(this);

    if (!isInUnitTests()) {
      initApplication();
    }
  }

  public ApplicationComponent getComponent() {
    final ApplicationComponent component =  DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(this))
            .build();
    return component;
  }

  private void initApplication() {
    MultiDex.install(this);

    // Only log for internal builds
    if (BuildConfig.FLAVOR.equals("internal")) {
      Timber.plant(new Timber.DebugTree());
    }

    if (FirebaseApp.getApps(getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(getApplicationContext());
      FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
      FirebaseAnalytics.getInstance(getApplicationContext()).setAnalyticsCollectionEnabled(true);
    }

    setVisitorCookie();
    this.pushNotifications.initialize();

    final ApplicationLifecycleUtil appUtil = new ApplicationLifecycleUtil(this);
    registerActivityLifecycleCallbacks(appUtil);
    registerComponentCallbacks(appUtil);

    // - Initialize Segment SDK
    if (this.segmentTrackingClient != null) {
      this.segmentTrackingClient.initialize();
    }

    // - Register lifecycle callback for Braze
    this.remotePushClientType.registerActivityLifecycleCallbacks(this);
  }

  public ApplicationComponent component() {
    return this.component;
  }

  /**
   * Method override in tha child class for testings purposes
   */
  public boolean isInUnitTests() {
    return false;
  }

  private void setVisitorCookie() {
    final String deviceId = FirebaseInstanceId.getInstance().getId();
    final String uniqueIdentifier = TextUtils.isEmpty(deviceId) ? UUID.randomUUID().toString() : deviceId;
    final HttpCookie cookie = new HttpCookie("vis", uniqueIdentifier);
    cookie.setMaxAge(DateTime.now().plusYears(100).getMillis());
    cookie.setSecure(true);
    final URI webUri = URI.create(Secrets.WebEndpoint.PRODUCTION);
    final URI apiUri = URI.create(ApiEndpoint.PRODUCTION.url());
    this.cookieManager.getCookieStore().add(webUri, cookie);
    this.cookieManager.getCookieStore().add(apiUri, cookie);
    CookieHandler.setDefault(this.cookieManager);
  }
}
