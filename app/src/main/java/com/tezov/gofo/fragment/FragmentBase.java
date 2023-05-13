package com.tezov.gofo.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.tezov.lib_java_android.ui.fragment.FragmentMenu;


import com.tezov.gofo.navigation.ToolbarContent;
import com.tezov.gofo.navigation.ToolbarHeaderBuilder;

import com.tezov.gofo.activity.ActivityMain;

public abstract class FragmentBase extends FragmentMenu{

private FragmentBase me(){
    return this;
}

@Override
protected State newState(){
    return super.newState();
}
@Override
public State getState(){
    return super.getState();
}
@Override
public State obtainState(){
    return super.obtainState();
}
@Override
public Param getParam(){
    return super.getParam();
}

protected <DATA> void setToolbarTittle(DATA data){
    getParam().setTitleData(data);
    ActivityMain activity = (ActivityMain)getActivity();
    ToolbarContent toolbarContent = activity.getToolbarContent();
    if(data == null){
        toolbarContent.setToolBarView(null);
    }
    else {
        ToolbarHeaderBuilder header = new ToolbarHeaderBuilder().setData(data);
        toolbarContent.setToolBarView(header.build(activity.getToolbar()));
    }
}

@Nullable
@Override
public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    return super.onCreateView(inflater, container, savedInstanceState);
}
@Override
public void onPrepare(boolean hasBeenReconstructed){
    super.onPrepare(hasBeenReconstructed);
    if(!hasState()){
        obtainParam();
    }
}

public abstract static class State extends FragmentMenu.State{
    @Override
    public Param getParam(){
        return (Param)super.getParam();
    }
}
public abstract static class Param extends FragmentMenu.Param{
    public Object titleData = null;
    public <DATA> DATA getTitleData(){
        return (DATA)titleData;
    }
    public <DATA> Param setTitleData(DATA data){
        this.titleData = data;
        return this;
    }
}

}
