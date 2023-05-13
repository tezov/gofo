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
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.tezov.lib_java_android.application.AppDisplay;
import com.tezov.lib_java_android.ui.activity.ActivityToolbar;
import com.tezov.lib_java_android.ui.component.plain.WebViewHtmlResource;
import com.tezov.lib_java_android.ui.toolbar.Toolbar;
import com.tezov.lib_java_android.ui.toolbar.ToolbarBottom;
import com.tezov.gofo.R;
import com.tezov.gofo.navigation.ToolbarContent;
import com.tezov.gofo.navigation.ToolbarHeaderBuilder;


public class ActivityPrivacyPolicy extends ActivityToolbar{
private ToolbarContent toolbarContent = null;

@Override
protected int getLayoutId(){
    return R.layout.tpl_activity_tbc_tbba_overlap;
}

public ToolbarContent getToolbarContent(){
    return toolbarContent;
}

@Override
protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    AppDisplay.setOrientationUser(true);
    toolbarContent = new ToolbarContent(this);
    WebViewHtmlResource webView = new WebViewHtmlResource(this);
    webView.setRawFileId(R.raw.privacy_policy);
    webView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    FrameLayout frame = findViewById(R.id.container_fragment);
    frame.addView(webView);
    webView.loadData();
}
@Override
public void onOpen(boolean hasBeenReconstructed, boolean hasBeenRestarted){
    super.onOpen(hasBeenReconstructed, hasBeenRestarted);
    setToolbarTittle(R.string.activity_privacy_policy_title);
}
protected <DATA> void setToolbarTittle(DATA data){
    ToolbarContent toolbarContent = getToolbarContent();
    if(data == null){
        toolbarContent.setToolBarView(null);
    } else {
        ToolbarHeaderBuilder header = new ToolbarHeaderBuilder().setData(data);
        toolbarContent.setToolBarView(header.build(getToolbar()));
    }
}
@Override
protected boolean onCreateMenu(){
    Toolbar toolbar = getToolbar();
    toolbar.setVisibility(View.VISIBLE);
    ToolbarBottom toolbarBottom = getToolbarBottom();
    toolbarBottom.setVisibility(View.GONE);
    return true;
}

}
