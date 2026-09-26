# Finder audio and silent mode

Configure **Finder sound settings** from Home on each target PDA. Grant Do Not Disturb policy access if the PDA uses priority DND, then allow alarms in each active DND mode. Run the five-second speaker test in normal, silent, vibrate, and the store's usual DND modes. The test uses maximum alarm volume; Stop, leaving the screen, or the timeout releases playback and restores the previous alarm volume/mute state.

## Implemented behavior

- Urovo, Zebra and generic Android adapters share the Android alarm-audio implementation. It uses `USAGE_ALARM`, unmutes and maximizes `STREAM_ALARM`, and requests the built-in speaker. It does not change the ringer mode or global DND settings.
- DND All and Alarms-only allow playback. Priority mode requires readable policy and an alarm exception; on API 30+ the consolidated policy is checked. Total silence, unknown policy, or missing access to inspect priority DND fail closed.
- Muted/zero alarm volume, denied audio focus, player failure, or an observed output route away from the speaker result in `FAILED`. Every 500 ms during playback, the app rechecks DND, volume, player state and reported output route. Loss of audio focus stops the alarm.
- `RINGING` means playback passed software checks. It is not proof of physical audibility; a broken speaker, OEM behavior, or an unreported route cannot be verified by these APIs. Perform the local sound test on actual hardware.
- Failure remains `FAILED`; service cleanup no longer emits a subsequent `STOPPED` event for a failed request. The latest audio failure reason is shown in local finder sound settings. The existing backend event contract is unchanged.
- A persisted volume/mute snapshot supports recovery at the next application start after process death. Restoration blocked by device policy is retained for a later retry. The app cannot execute cleanup while force-stopped. Concurrent sound tests and finder alarms cannot overwrite each other's snapshots.

## Platform limits

Android 15+ apps targeting API 35 cannot switch off global DND using the old notification-policy APIs. This app requires the target device's active modes to allow alarms instead of claiming to bypass that restriction. See [Android 15 DND changes](https://developer.android.com/about/versions/15/behavior-changes-15) and [NotificationManager](https://developer.android.com/reference/android/app/NotificationManager).

No proprietary Urovo/Zebra policy override is implemented without a verified SDK/device configuration. A device-management policy that forbids alarm audio must be adjusted by its administrator.

## Device acceptance checks

| Scenario | Expected result |
|---|---|
| Silent or vibrate, DND off | Speaker test plays; ringer mode remains unchanged |
| Alarm volume zero or muted, DND off | Alarm becomes audible; previous state restored after Stop |
| DND alarms-only | Playback allowed |
| Priority DND with alarm exception and policy access | Playback allowed |
| Priority DND without alarm exception/access | Explicit local failure; remote request reports FAILED |
| DND total silence | Explicit local failure; no false RINGING |
| DND starts blocking during playback | Playback stopped and FAILED reported |
| Wired/Bluetooth headset attached | Built-in speaker requested; observed routing elsewhere fails |
| Another app takes audio focus | Finder stops and reports failure |
| Stop, timeout, or leave local test screen | Player and focus released; volume/mute restored |
| Process dies during alarm | Snapshot restored on next app launch |
| Physical speaker damaged or restricted by OEM policy | Local listening test required; software checks alone are insufficient |

Live FCM delivery and these cases on actual Urovo/Zebra models remain deployment acceptance work.
