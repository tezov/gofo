package fragment;

import com.tezov.gofo.BuildConfig;
import com.tezov.lib_java.type.primitive.IntTo;
import com.tezov.lib_java.type.unit.UnitByte;
import com.tezov.lib_java.toolbox.CompareType;
import com.tezov.lib_java.util.UtilsString;
import com.tezov.lib_java_android.database.sqlLite.filter.dbFilterOrder;

import androidx.fragment.app.Fragment;

import com.tezov.lib_java.debug.DebugException;
import com.tezov.lib_java.debug.DebugTrack;
import com.tezov.lib_java.type.primitive.ObjectTo;
import com.tezov.lib_java_android.ui.component.plain.ButtonMultiIconMaterial;
import com.tezov.lib_java.toolbox.Clock;
import com.tezov.lib_java_android.database.sqlLite.filter.chunk.ChunkCommand;

import java.util.LinkedList;
import java.util.Set;

import com.tezov.lib_java_android.application.ConnectivityHotSpotManager;
import com.tezov.lib_java_android.application.ConnectivityManager;
import com.tezov.lib_java_android.application.AppDisplay;
import com.tezov.lib_java_android.application.PackageSignature;
import com.tezov.lib_java_android.playStore.PlayStore;
import com.tezov.lib_java.async.Handler;


import java.util.List;

import androidx.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tezov.gofo.application.Application;
import com.tezov.gofo.fragment.FragmentBase;
import com.tezov.gofo.navigation.ToolbarContent;
import com.tezov.gofo.navigation.ToolbarHeaderBuilder;
import com.tezov.lib_java_android.application.AppContext;
import com.tezov.lib_java.async.notifier.observer.event.ObserverEvent;
import com.tezov.lib_java.debug.DebugLog;
import com.tezov.lib_java.debug.DebugString;
import com.tezov.lib_java.type.defEnum.Event;
import com.tezov.lib_java_android.ui.fragment.FragmentMenu;
import com.tezov.lib_java_android.ui.toolbar.Toolbar;

public abstract class FragmentBase_bt extends FragmentBase{

@Override
public void onPrepare(boolean hasBeenReconstructed){
    super.onPrepare(hasBeenReconstructed);
    if(!BuildConfig.DEBUG_ONLY && !PackageSignature.matchesFingerPrint()){
        AppDisplay.disableTouchScreen(this);
    }
}

}
