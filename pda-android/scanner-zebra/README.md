# Zebra DataWedge scanner

ZEBRA receives broadcasts through the shared BarcodeReceiver; no EMDK dependency
or runtime library is required. ZebraScannerConfig() supplies the app default
DataWedge action and payload key. Custom settings remain supported.

Configure a DataWedge profile associated with com.company.pda (all activities):

1. Enable Barcode input and the desired decoders.
2. Enable Intent output, action `com.company.pda.SCAN`, delivery **Broadcast Intent**.
3. Leave Intent category empty or use `android.intent.category.DEFAULT`.
4. Disable Keystroke output to avoid duplicate input.
5. In the app, select **ZEBRA**, choose **Use selected scanner defaults**, and save.

The payload is `com.symbol.datawedge.data_string`; symbology comes from
`com.symbol.datawedge.label_type`. DataWedge handles the hardware trigger.
The software trigger sends DataWedge START_SCANNING; stopping the foreground session
sends STOP_SCANNING and unregisters the receiver. DataWedge must have an enabled,
active profile; the app does not create profiles or confirm command results.

After upgrading from EMDK, enable/configure DataWedge as above. Previously empty
Zebra action/payload preferences use defaults; nonempty custom values are preserved.

Validate repeated hardware/software scans, navigation, rotation, background/foreground,
custom actions and release builds on a Zebra device. Local tests cover command delivery
and receiver lifecycle, not scanner hardware.

References: [Intent output](https://techdocs.zebra.com/datawedge/14-3/guide/output/intent/),
[Soft Scan Trigger](https://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/).