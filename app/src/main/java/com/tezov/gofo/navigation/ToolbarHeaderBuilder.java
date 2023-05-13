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
import static android.view.View.GONE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tezov.lib_java_android.application.AppContext;
import com.tezov.gofo.R;

public class ToolbarHeaderBuilder{
private String name = null;

private ToolbarHeaderBuilder setData(String data){
    this.name = data;
    return this;
}
public <DATA> ToolbarHeaderBuilder setData(DATA data){
    if(data instanceof String){
        return setData((String)data);
    }
    if(data instanceof Integer){
        return setData(AppContext.getResources().getString((Integer)data));
    }
/*#-debug-> DebugException.start().unknown("type", DebugTrack.getFullSimpleName(data)).end(); <-debug-#*/
    return null;
}
public View build(ViewGroup toolbar){
    View view = LayoutInflater.from(toolbar.getContext()).inflate(R.layout.toolbar_content, toolbar, false);
    TextView lblName = view.findViewById(R.id.lbl_name);
    if(name != null){
        lblName.setText(name);
    } else {
        lblName.setVisibility(GONE);
    }
    return view;
}

}
