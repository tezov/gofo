package com.tezov.gofo.application;

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
import static com.tezov.gofo.navigation.NavigationHelper.DestinationKey.PRIVACY_POLICY;

import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.tezov.lib_java.async.notifier.task.TaskState;
import com.tezov.lib_java_android.application.AppContext;
import com.tezov.lib_java_android.file.UriW;
import com.tezov.lib_java_android.ui.navigation.Navigate;
import com.tezov.lib_java_android.ui.navigation.NavigationOption;
import com.tezov.lib_java_android.util.UtilsIntent;
import com.tezov.lib_java_android.wrapperAnonymous.ViewOnClickListenerW;
import com.tezov.gofo.R;

public final class AppInfo extends com.tezov.lib_java_android.application.AppInfo{

public static void privacyPolicySetOnClickListener(View view, Boolean keepInStack){
    TextView lblPrivacyPolicy = view.findViewById(R.id.lbl_privacy_policy);
    lblPrivacyPolicy.setOnClickListener(new ViewOnClickListenerW(){
        @Override
        public void onClicked(View v){
            NavigationOption option = null;
            if(keepInStack != null){
                option = new NavigationOption().setKeepInStack_NavTo(keepInStack);
            }
            Navigate.To(PRIVACY_POLICY, option);
        }
    });
}
public static void contactSetOnClickListener(View view){
    TextView lblContact = view.findViewById(R.id.lbl_contact);
    lblContact.setOnClickListener(new ViewOnClickListenerW(){
        @Override
        public void onClicked(View v){
            String target = AppContext.getResources().getString(R.string.app_email);
            String subject = "Contact from " + AppContext.getResources().getString(R.string.app_name);
            subject += "_" + AppContext.getResources().getString(R.string.application_version) + "/" + Build.VERSION.SDK_INT;
            UtilsIntent.emailTo(target, subject, null);
        }
    });
}

public static TaskState.Observable open(UriW uri){
    if(uri == null){
        return TaskState.Exception("uri out is null");
    } else {
        return uri.open();
    }
}

public static TaskState.Observable openLink(String link){
    if(link == null){
        return TaskState.Exception("link is null");
    } else {
        return UtilsIntent.openLink(link);
    }
}

}
