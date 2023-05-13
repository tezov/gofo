package application;

import com.tezov.lib_java.debug.DebugLog;
import com.tezov.lib_java.debug.DebugTrack;
import com.tezov.lib_java.debug.DebugException;
import com.tezov.lib_java.type.primitive.ObjectTo;
import com.tezov.lib_java.type.primitive.IntTo;
import com.tezov.lib_java.type.unit.UnitByte;
import com.tezov.lib_java.toolbox.CompareType;
import com.tezov.lib_java.toolbox.Clock;
import com.tezov.lib_java.util.UtilsString;
import com.tezov.lib_java_android.database.sqlLite.filter.dbFilterOrder;
import com.tezov.lib_java_android.database.sqlLite.filter.chunk.ChunkCommand;
import androidx.fragment.app.Fragment;
import com.tezov.lib_java.debug.DebugException;

import android.content.Intent;

import com.tezov.lib_java_android.file.StorageMedia;
import com.tezov.lib_java_android.ui.component.plain.EditTextWithIcon;
import com.tezov.lib_java_android.ui.dialog.dialogPicker.picker.base.DialogButtonView;
import com.tezov.gofo.application.Application;
import com.tezov.lib_java_android.application.ApplicationSystem;
import com.tezov.lib_java.debug.DebugLog;
import com.tezov.lib_java.debug.DebugTrack;

import com.tezov.lib_java.type.primitive.IntTo;
import com.tezov.lib_java.toolbox.CompareType;
import com.tezov.lib_java.type.primitive.ObjectTo;
import com.tezov.lib_java.util.UtilsString;
import com.tezov.lib_java.toolbox.Clock;
import com.tezov.lib_java_android.database.sqlLite.filter.dbFilterOrder;
import com.tezov.lib_java_android.database.sqlLite.filter.chunk.ChunkCommand;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import com.tezov.lib_java.type.unit.UnitByte;



public class Application_bt extends Application{

public static void onMainActivityStart(ApplicationSystem app, Intent source, boolean isRestarted){
    Application.onMainActivityStart(app, source, isRestarted);
}
public static void onApplicationPause(ApplicationSystem app){
    Application.onApplicationPause(app);
}
public static void onApplicationClose(ApplicationSystem app){
    Application.onApplicationClose(app);
}


}
