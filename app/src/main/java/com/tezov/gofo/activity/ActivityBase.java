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
import com.tezov.lib_java_android.ui.activity.ActivityToolbar;


public abstract class ActivityBase extends ActivityToolbar{

@Override
public State getState(){
    return super.getState();
}
@Override
protected State newState(){
    return super.newState();
}
@Override
public State obtainState(){
    return super.obtainState();
}

@Override
public boolean hasState(){
    return super.hasState();
}

public static class State extends ActivityToolbar.State{

}

}
