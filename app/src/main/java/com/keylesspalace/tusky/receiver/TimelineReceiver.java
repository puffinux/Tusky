package com.keylesspalace.tusky.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

import com.keylesspalace.tusky.fragment.TimelineFragment;
import com.keylesspalace.tusky.interfaces.AdapterItemRemover;

public class TimelineReceiver extends BroadcastReceiver {
    public static final class Types {
        public static final String UNFOLLOW_ACCOUNT = "UNFOLLOW_ACCOUNT";
        public static final String BLOCK_ACCOUNT = "BLOCK_ACCOUNT";
        public static final String MUTE_ACCOUNT = "MUTE_ACCOUNT";
    }

    AdapterItemRemover adapter;

    public TimelineReceiver(AdapterItemRemover adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        String id = intent.getStringExtra("id");
        adapter.removeAllByAccountId(id);
    }

    public static IntentFilter getFilter(@Nullable TimelineFragment.Kind kind) {
        IntentFilter intentFilter = new IntentFilter();
        if (kind == TimelineFragment.Kind.HOME) {
            intentFilter.addAction(Types.UNFOLLOW_ACCOUNT);
        }
        intentFilter.addAction(Types.BLOCK_ACCOUNT);
        intentFilter.addAction(Types.MUTE_ACCOUNT);

        return intentFilter;
    }
}
