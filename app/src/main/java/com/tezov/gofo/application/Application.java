package com.tezov.gofo.application;

import com.tezov.lib_java.type.primitive.IntTo;
import com.tezov.lib_java.toolbox.CompareType;
import com.tezov.lib_java.debug.DebugLog;
import java.util.Set;
import com.tezov.lib_java_android.database.sqlLite.filter.chunk.ChunkCommand;
import com.tezov.lib_java_android.database.sqlLite.filter.dbFilterOrder;
import androidx.fragment.app.Fragment;
import com.tezov.lib_java.type.unit.UnitByte;
import com.tezov.lib_java.util.UtilsString;
import com.tezov.lib_java.toolbox.Clock;
import java.util.LinkedList;
import com.tezov.lib_java.debug.DebugException;
import com.tezov.lib_java.debug.DebugTrack;
import com.tezov.lib_java.type.primitive.ObjectTo;
import static com.tezov.gofo.navigation.NavigationHelper.DestinationKey.FRAGMENT_FEED;
import static com.tezov.lib_java_android.application.SharePreferenceKey.makeKey;
import static com.tezov.gofo.application.SharePreferenceKey.SP_NAVIGATION_LAST_DESTINATION_STRING;
import static com.tezov.gofo.application.SharePreferenceKey.SP_OWNED_NO_ADS_INT;

import android.content.Context;
import android.content.Intent;

import com.tezov.lib_java.toolbox.Compare;
import com.tezov.lib_java_android.application.AppConfig;
import com.tezov.lib_java_android.application.AppInfo;
import com.tezov.lib_java_android.application.ApplicationSystem;
import com.tezov.lib_java_android.application.ConnectivityManager;
import com.tezov.lib_java_android.application.SharedPreferences;
import com.tezov.lib_java_android.ui.misc.TransitionManagerAnimation;
import com.tezov.gofo.R;
import com.tezov.gofo.navigation.NavigationHelper;

import java.util.List;

public class Application extends com.tezov.lib_java_android.application.Application{
public final static String SKU_NO_ADS = com.tezov.lib_java_android.application.AppContext.getResources().getString(R.string.billing_sku_no_ads);

private static Class<Application> myClass(){
    return Application.class;
}

public static void onMainActivityStart(ApplicationSystem app, Intent source, boolean isRestarted){
    com.tezov.lib_java_android.application.Application.onMainActivityStart(app, source, isRestarted);
    if(!isRestarted){
        Context context = app.getApplicationContext();
        if(state == null){
            state = new State();
        }
        state.onMainActivityStart(app, source);
        transitionManager = new TransitionManagerAnimation();
        navigationHelper = new NavigationHelper();
        sharedPreferences(AppConfig.newSharedPreferencesEncrypted());
        if(AppInfo.isFirstLaunch()){
            setDefaultSharePreference();
        }
        connectivityManager = new ConnectivityManager();
    }
    Application_K.onMainActivityStart(app, source, isRestarted);
}
public static void onApplicationPause(ApplicationSystem app){
    Application_K.onApplicationPause(app);
    com.tezov.lib_java_android.application.Application.onApplicationPause(app);
}
public static void onApplicationClose(ApplicationSystem app){
    Application_K.onApplicationClose(app);
    if(connectivityManager != null){
        connectivityManager.unregisterReceiver(true);
        connectivityManager = null;
    }
    sharedPreferences = null;
    navigationHelper = null;
    transitionManager = null;
    if(state != null){
        state.onApplicationClose(app);
    }
    com.tezov.lib_java_android.application.Application.onApplicationClose(app);
}

private static void setDefaultSharePreference(){
    SharedPreferences sp = sharedPreferences();
    sp.put(SP_NAVIGATION_LAST_DESTINATION_STRING, FRAGMENT_FEED.name());
}

public static boolean isOwnedNoAds(){
    SharedPreferences sp = Application.sharedPreferences();
    return Compare.isTrue(sp.getBoolean(makeKey(SP_OWNED_NO_ADS_INT, getState().sessionUid().toHexString())));
}
public static void setOwnedNoAds(boolean flag){
    SharedPreferences sp = Application.sharedPreferences();
    List<String> previous = sp.findKeyStartWith(SP_OWNED_NO_ADS_INT);
    if(previous != null){
        for(String key: previous){
            sp.remove(key);
        }
    }
    sp.put(makeKey(SP_OWNED_NO_ADS_INT, getState().sessionUid().toHexString()), flag);
}

}
