# Urovo ScanWedge scanner

The adapter receives decoded barcodes through the shared `BarcodeReceiver` and sends
software start/stop commands using the Urovo ScanWedge Intent API. No vendor SDK is bundled.

Configure an active ScanWedge profile for `com.company.pda`, enable scanner input and
broadcast/intent output, and disable keyboard output. In the app choose **UROVO**, press
**Use selected scanner defaults** and save; adjust the action/key to match custom profiles.
Defaults: action `android.intent.ACTION_DECODE_DATA`, payload `barcode_string`.

The app's **Trigger configured scanner** button sends:

```text
Action: com.ubx.datawedge.api.ACTION
Extra:  SOFT_SCAN_TRIGGER (String)
Value:  START_SCANNING
```

`stop()` sends `STOP_SCANNING` for an active session, then disables callbacks and
unregisters the receiver even if sending the stop command throws. Starting the receiver
does not start hardware decoding. Trigger calls before start or after stop do nothing.
Barcode results continue through the same receiver/repository/ViewModel path.

The Urovo sample identifies ScanWedge `V2.1.19_20230220` as introducing this Intent API
and targets Android 10 or newer with OS releases from 2023-02-20. Verify API support
on the target firmware; older devices can still use hardware-trigger broadcast output
when configured, but may ignore software trigger commands. The adapter does not probe
API availability, create profiles, or receive command acknowledgements.

Validate hardware/software scans, leaving the screen during decoding, repeated sessions
and custom result profiles on a real Urovo PDA. JVM tests verify outgoing commands,
session guards and receiver cleanup; they do not establish firmware compatibility.

References: [Urovo ScanWedge sample](https://github.com/urovosamples/ScanWedgeAPIsSample),
[command constants](https://github.com/urovosamples/ScanWedgeAPIsSample/blob/main/src/main/java/android/device/scanner/configuration/IntentKeys.java),
[sample and version requirements](https://github.com/urovosamples/ScanWedgeAPIsSample/blob/main/src/main/java/com/ubx/scanwedge/intentapi/MainActivity.java).
