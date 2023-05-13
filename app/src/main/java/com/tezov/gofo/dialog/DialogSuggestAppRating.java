package com.tezov.gofo.dialog;

import com.tezov.lib_java.type.primitive.IntTo;
import com.tezov.lib_java.toolbox.CompareType;
import com.tezov.lib_java.debug.DebugLog;
import java.util.Set;
import com.tezov.lib_java_android.database.sqlLite.filter.chunk.ChunkCommand;
import java.util.List;
import com.tezov.lib_java_android.database.sqlLite.filter.dbFilterOrder;
import androidx.fragment.app.Fragment;
import com.tezov.lib_java.type.unit.UnitByte;
import com.tezov.lib_java.util.UtilsString;
import com.tezov.lib_java.toolbox.Clock;
import java.util.LinkedList;
import com.tezov.lib_java.debug.DebugException;
import com.tezov.lib_java.debug.DebugTrack;
import com.tezov.lib_java.type.primitive.ObjectTo;
import static com.tezov.gofo.application.SharePreferenceKey.SP_SUGGEST_APP_RATING_ALREADY_DONE_BOOL;

import com.tezov.lib_java.async.notifier.observer.state.ObserverStateE;
import com.tezov.lib_java.async.notifier.task.TaskValue;
import com.tezov.lib_java.toolbox.Compare;
import com.tezov.lib_java_android.application.SharedPreferences;
import com.tezov.lib_java_android.playStore.PlayStore;
import com.tezov.lib_java_android.ui.dialog.modal.DialogModalWebview;
import com.tezov.lib_java_android.ui.navigation.Navigate;
import com.tezov.gofo.R;
import com.tezov.gofo.application.Application;

public class DialogSuggestAppRating extends DialogModalWebview{

public static TaskValue<DialogSuggestAppRating>.Observable open(){
    State state = new State();
    Param param = state.obtainParam();
    param.setConfirmButtonText(R.string.btn_ok);
    param.setCancelButtonText(R.string.btn_maybe_later);
    param.setCheckBoxText(R.string.chk_already_done);
    param.setChecked(isAlreadyDone());
    param.setRawFileId(R.raw.suggest_app_rating);
    return Navigate.To(DialogSuggestAppRating.class, state);
}

public static boolean isAlreadyDone(){
    return isAlreadyDone(Application.sharedPreferences());
}
private static void setDoNotShowValue(boolean flag){
    SharedPreferences sp = Application.sharedPreferences();
    sp.put(SP_SUGGEST_APP_RATING_ALREADY_DONE_BOOL, flag);
}
private static boolean isAlreadyDone(SharedPreferences sp){
    return Compare.isTrue(sp.getBoolean(SP_SUGGEST_APP_RATING_ALREADY_DONE_BOOL));
}

private static Class<DialogSuggestAppRating> myClass(){
    return DialogSuggestAppRating.class;
}

@Override
protected void onConfirm(){
    close();
    PlayStore.requestRate().observe(new ObserverStateE(this){
        @Override
        public void onComplete(){
            postConfirm();
        }
        @Override
        public void onException(Throwable e){
            postException(e);
        }
    });
}
@Override
protected void onCheckBoxChange(boolean flag){
    setDoNotShowValue(flag);
}



}