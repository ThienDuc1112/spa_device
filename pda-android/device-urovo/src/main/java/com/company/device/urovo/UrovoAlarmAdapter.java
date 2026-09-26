package com.company.device.urovo;

import android.content.Context;
import com.company.device.android.AndroidAlarmAdapter;

/** Uses public Android audio APIs on this device family. */
public class UrovoAlarmAdapter extends AndroidAlarmAdapter {
  public UrovoAlarmAdapter(Context context) {
    super(context);
  }
}
