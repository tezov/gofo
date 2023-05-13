package com.tezov.gofo.activity;

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
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tezov.gofo.application.ApplicationSystem;
import com.tezov.lib_java_android.ui.layout.FrameLayout;

public class ActivityLauncher extends AppCompatActivity{
@Override
protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    FrameLayout view = FrameLayout.newMM(this);
    view.setBackgroundColor(0xFF00FF00);
    setContentView(view);
    ApplicationSystem application = (ApplicationSystem)getApplication();
    if(!application.isClosing(this)){
        application.startMainActivity(this, false, false);
    }
}

}