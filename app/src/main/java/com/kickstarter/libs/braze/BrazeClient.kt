package com.kickstarter.libs.braze

import android.app.Application
import android.content.Context
import android.util.Log
import com.appboy.Appboy
import com.appboy.AppboyFirebaseMessagingService
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.configuration.AppboyConfig
import com.appboy.support.AppboyLogger
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.Secrets

/**
 * Braze SDK wrapper class.
 * @param context It needs the application context to be initialized,
 * @param build  the type of build will depetermine the IdSender from Firebase and the logs mode
 */
class BrazeClient(
        private val context: Context,
        private val build: Build) {

    fun init() {
        // Braze Push notification integration
        val appboyConfig = AppboyConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(true)
                .setFirebaseCloudMessagingSenderIdKey(getIdSender())
                //.setDefaultNotificationChannelName("General") --> TODO: Define notification channels for the new type of push notifications
                //.setDefaultNotificationChannelDescription("Braze related push")
                //.setPushDeepLinkBackStackActivityEnabled(true) ---> TODO: Define backstack behaviour
                //.setPushDeepLinkBackStackActivityClass(MainActivity.class)
                .setHandlePushDeepLinksAutomatically(false)
                .build()
        Appboy.configure(context, appboyConfig)

        if (this.build.isDebug || Build.isInternal()) {
            AppboyLogger.setLogLevel(Log.VERBOSE)
        }
    }

    private fun getIdSender(): String {
        var senderId = ""
        if (build.isRelease && Build.isExternal()) {
            senderId = Secrets.FirebaseSenderID.STAGING
        }
        if (build.isDebug || Build.isInternal()) {
            // senderId = Secrets.FirebaseSenderID.PRODUCTION  --> TODO: Add PRODUCTION SENDER ID
        }

        return senderId
    }

    /**
     * Register a Push token on Braze
     */
    fun registerPushMessages(context: Context, token: String){
        Appboy.getInstance(context).registerAppboyPushMessages(token)
    }

    // - Access to static methods without the braze client instance
    companion object {

        /**
         * In case the remote message is a braze message it will be handle by them.
         *
         * @return true if the message is a braze message and will be handled by them
         * @return false if the message is not from braze
         */
        fun handleRemoteMessage(context: Context, message: RemoteMessage): Boolean {
            return AppboyFirebaseMessagingService.handleBrazeRemoteMessage(context, message)
        }

        /**
         * Application Lifecycle events generated by Braze
         */
        fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks {
            return AppboyLifecycleCallbackListener(true, false)
        }
    }
}