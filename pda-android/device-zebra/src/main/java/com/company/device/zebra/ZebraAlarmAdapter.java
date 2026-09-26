package com.company.device.zebra;

import android.content.Context;
import com.company.device.android.AndroidAlarmAdapter;

/** Uses public Android audio APIs on this device family. */
public class ZebraAlarmAdapter extends AndroidAlarmAdapter {
  public ZebraAlarmAdapter(Context context) {
    super(context);
  }
}
