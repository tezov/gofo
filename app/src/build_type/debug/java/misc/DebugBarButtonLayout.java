package misc;

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
import com.tezov.lib_java.type.primitive.ObjectTo;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.tezov.lib_java_android.application.Application;
import com.tezov.lib_java_android.application.AppDisplay;
import com.tezov.lib_java_android.toolbox.SingletonHolder;
import com.tezov.lib_java.application.AppConfig;
import com.tezov.lib_java.debug.DebugException;
import com.tezov.lib_java.debug.DebugTrack;
import com.tezov.lib_java_android.debug.debugBar.DebugBarLogLayout;
import com.tezov.lib_java.type.defEnum.EnumBase;
import com.tezov.lib_java_android.ui.component.plain.Switch;
import com.tezov.lib_java_android.util.UtilsView;
import com.tezov.gofo.R;

import static com.tezov.lib_java_android.application.AppConfigKey.DEBUG_LOG_ON_DEVICE;

import com.tezov.gofo.room.data.ItemFilter;
import com.tezov.gofo.room.database.Database;

public class DebugBarButtonLayout extends LinearLayout{

public DebugBarButtonLayout(Context context){
    super(context);
    init(context, null, -1, -1);
}
public DebugBarButtonLayout(Context context, @Nullable AttributeSet attrs){
    super(context, attrs);
    init(context, attrs, -1, -1);
}
public DebugBarButtonLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr, -1);
}
public DebugBarButtonLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs, defStyleAttr, defStyleRes);
}

private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
/*#-debug-> DebugTrack.start().create(this).end(); <-debug-#*/
}
private DebugBarButtonLayout me(){
    return this;
}

@Override
public void onViewAdded(View child){
    super.onViewAdded(child);
    ViewGroup.LayoutParams param = child.getLayoutParams();
    if(param instanceof LinearLayout.LayoutParams){
        LinearLayout.LayoutParams paramLinear = (LinearLayout.LayoutParams)param;
        paramLinear.gravity = Gravity.CENTER_VERTICAL;
        int padding = AppDisplay.convertDpToPx(2);
        paramLinear.setMargins(padding, padding, padding, padding);
    }
    int padding = AppDisplay.convertDpToPx(12);
    child.setPadding(padding, padding, padding, padding);
    float textSize = 20;
    if(child instanceof AppCompatButton){
        ((AppCompatButton)child).setTextSize(textSize);
    } else if(child instanceof LinearLayout){
        TextView textView = UtilsView.findFirst(TextView.class, child, UtilsView.Direction.DOWN);
        textView.setTextSize(textSize);
    }
    child.setLayoutParams(param);
    child.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v){
            menuOnItemSelected(v);
        }
    });
    updateActionView(child);
    if(!AppConfig.getBoolean(DEBUG_LOG_ON_DEVICE.getId()) && child.getId() == R.id.mn_toggle_log_visibility){
        child.setVisibility(GONE);
    }
}
private <C extends Class<V>, V extends View> V find(C type, View v){
    return UtilsView.findFirst(type, v, UtilsView.Direction.DOWN);
}

protected void updateActionView(View v){
    switch(v.getId()){
        case R.id.mn_config_trackclass:{
            Switch sw = find(Switch.class, v);
            sw.setEntry(new Switch.Entry(){
                boolean ENABLE_RECORD_mem = DebugTrack.ENABLE_RECORD;
                @Override
                public boolean get(){
                    return DebugTrack.ENABLE_LOG;
                }
                @Override
                public void set(boolean b){
                    DebugTrack.ENABLE_LOG = b;
                    if(b){
                        DebugTrack.ENABLE_RECORD = true;
                    }
                    else{
                        DebugTrack.ENABLE_RECORD = ENABLE_RECORD_mem;
                    }
                }
            });
        }
        break;
        case R.id.mn_config_toggle_exception:{
            Switch sw = find(Switch.class, v);
            sw.setEntry(new Switch.Entry(){
                @Override
                public boolean get(){
                    return DebugException.VERBOSE;
                }

                @Override
                public void set(boolean b){
                    DebugException.VERBOSE = b;
                }
            });
        }
        break;
    }
}

public boolean menuOnItemSelected(View v){
    switch(v.getId()){
        case R.id.mn_log_filter:{
            Database.Companion.getFilters().toDebugLog(ItemFilter.Type.FEED);
            return true;
        }
        case R.id.mn_log_photo:{
            Database.Companion.getPhotos().toDebugLog(25L);
            return true;
        }

        case R.id.mn_toggle_log_visibility:{
            DebugBarLogLayout debugLogView = UtilsView.findFirst(R.id.debug_log, me().getRootView(), UtilsView.Direction.DOWN);
            int visibility = debugLogView.getVisibility();
            if(visibility == VISIBLE){
                debugLogView.setVisibility(GONE);
            } else {
                debugLogView.setVisibility(VISIBLE);
            }
            return true;
        }
        case R.id.mn_enum_base:{
            EnumBase.toDebugLog();
            return true;
        }
        case R.id.mn_singleton_memory:{
            SingletonHolder.toDebugLog();
            return true;
        }
        case R.id.mn_navigation_stack:{
            Application.navigationHelper().toDebugLogStack();
            return true;
        }
        case R.id.mn_debug_track_created:{
            DebugTrack.toDebugLogCreated();
            return true;
        }
        case R.id.mn_debug_track_memory:{
            DebugTrack.toDebugLogInMemory();
            return true;
        }
        case R.id.mn_debug_track_memory_size:{
            DebugTrack.toDebugLogMemorySize();
            return true;
        }
        case R.id.mn_debug_track_memory_list:{
            DebugTrack.toDebugLogMemoryList();
            return true;
        }

        case R.id.mn_config_trackclass:
        case R.id.mn_config_toggle_exception:{
            Switch sw = find(Switch.class, v);
            sw.toggle();
            return true;
        }
        default:
            return false;
    }
}

@Override
protected void finalize() throws Throwable{
/*#-debug-> DebugTrack.start().destroy(this).end(); <-debug-#*/
    super.finalize();
}

}
