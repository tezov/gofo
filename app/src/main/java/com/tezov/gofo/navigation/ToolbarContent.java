package com.tezov.gofo.navigation;

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
import static com.tezov.lib_java.type.defEnum.Event.ON_CLICK_SHORT;

import android.view.View;
import android.view.ViewGroup;

import com.tezov.lib_java.type.ref.WR;
import com.tezov.lib_java_android.ui.activity.ActivityToolbar;
import com.tezov.lib_java_android.ui.layout.LinearLayout;
import com.tezov.lib_java_android.ui.toolbar.Toolbar;
import com.tezov.lib_java_android.ui.toolbar.ToolbarCollapsible;
import com.tezov.lib_java_android.wrapperAnonymous.ViewOnClickListenerW;

public class ToolbarContent{
private WR<Toolbar> toolbarWR;
private WR<ToolbarCollapsible> toolbarCollapsibleWR;

public ToolbarContent(ActivityToolbar activity){
/*#-debug-> DebugTrack.start().create(this).end(); <-debug-#*/
    this.toolbarWR = WR.newInstance(activity.getToolbar());
}

public void clear(){
    toolbarWR.get().setContentView(null);
    toolbarCollapsibleWR.get().setContentView(null);
}

public void setToolBarView(View view){
    if((view != null) && !view.hasOnClickListeners()){
        view.setOnClickListener(new ViewOnClickListenerW(){
            @Override
            public void onClicked(View v){
                toolbarWR.get().post(ON_CLICK_SHORT, v);
            }
        });
    }
    toolbarWR.get().setContentView(view);
}

private LinearLayout getAppBarLayout(){
    return (LinearLayout)toolbarCollapsibleWR.get().getContentView();
}
public int collapsibleViewCount(){
    return getAppBarLayout().getChildCount();
}
public boolean hasCollapsibleView(View view){
    return getAppBarLayout().hasView(view);
}
public View findCollapsibleViewById(int id){
    return getAppBarLayout().findViewById(id);
}
public void putCollapsibleView(View view){
    getAppBarLayout().putView(view);
}
public void removeCollapsibleView(int id){
    getAppBarLayout().removeViewWithId(id);
}

@Override
protected void finalize() throws Throwable{
/*#-debug-> DebugTrack.start().destroy(this).end(); <-debug-#*/
    super.finalize();
}

}
