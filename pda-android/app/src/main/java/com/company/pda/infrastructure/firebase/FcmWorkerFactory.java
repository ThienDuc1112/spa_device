package com.company.pda.infrastructure.firebase;

import android.content.Context;
import androidx.work.*;

/** Existing WorkSpecs survive the package reorganization; the Room event queue stays intact. */
public class FcmWorkerFactory extends WorkerFactory {
  public ListenableWorker createWorker(Context context, String name, WorkerParameters params) {
    return name.equals("com.company.pda.data.SyncWorker") ? new SyncWorker(context, params) : null;
  }
}
