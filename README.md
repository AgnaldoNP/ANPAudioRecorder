# ANP Audio Recorder

## Introduction
*ANP Audio Recorder* is a library that helps a developer to provide a easy way to to user record an audio and play it to get a feedback about the record.

![Screenshot](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/screenshot01.png?raw=true)

## Install

**Step 1**. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
**Step 2.** Add the dependency
```
dependencies {
  implementation 'com.github.AgnaldoNP:ANPAudioRecorder:1.0'
}
```
[![](https://jitpack.io/v/AgnaldoNP/ANPAudioRecorder.svg)](https://jitpack.io/#AgnaldoNP/ANPAudioRecorder)

**Step 3.** Add permissions
```xml
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Usage
Sample of usage
```xml
<pereira.agnaldo.audiorecorder.AudioRecorderView
    android:id="@+id/recordView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp" />
```

### Options
| Property         | Value / Type |
|------------------|--------------|
| recordIcon       | drawable     |
| playIcon         | drawable     |
| pauseIcon        | drawable     |
| stopIcon         | drawable     |
| deleteIcon       | drawable     |
| background       | drawable     |
| baseColor        | color        |
| recordIconTint   | color        |
| playIconTint     | color        |
| pauseIconTint    | color        |
| stopIconTint     | color        |
| deleteIconTint   | color        |
| recordWaveTint   | color        |
| playProgressTint | color        |
| backgroundTint   | color        |
| timeTint         | color        |
| totalTimeTint    | color        |
| currentTimeTint  | color        |

These configs also can be changes at runtime by calling their set methods

### Default style
![Screenshot](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/screenshot01.png?raw=true)
![Screenshot](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/screenshot02.png?raw=true)

![Screenshot](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/screenshot03.png?raw=true)
![Screenshot](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/screenshot04.png?raw=true)



### Other Examples
![GIF](https://github.com/AgnaldoNP/ANPAudioRecorder/blob/master/screenshots/sample.gif?raw=true)
```xml
<pereira.agnaldo.audiorecorder.AudioRecorderView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    android:minHeight="@dimen/anp_ar_min_height"
    app:baseColor="#FF0000" />

<pereira.agnaldo.audiorecorder.AudioRecorderView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="10dp"
    android:minHeight="@dimen/anp_ar_min_height"
    app:background="@drawable/custom_bg_layout_1"
    app:baseColor="#FF0000"
    app:currentTimeTint="#F8A007"
    app:playProgressTint="#FF0"
    app:recordIconTint="#0000FF"
    app:recordWaveTint="#00FF00"
    app:totalTimeTint="#FF00FB" />
```

## Listeners
### Kotlin
```kotlin
recordView.setOnStartRecording {
    ...
}

recordView.setOnFinishRecord { file ->
    ...
}

recordView.setOnPlay {
    ...
}

recordView.setOnPause {
    ...
}

recordView.setOnResume {
    ...
}

recordView.setOnFinishPlay {
    ...
}

recordView.setOnDelete {
    ...
}
```

### Java
```java
recordView.setOnStartRecording(new AudioRecorderView.OnStartRecordingListener() {
    @Override
    public void onStartRecording() {
        ...
    }
});
// or with java 8+
recordView.setOnStartRecording(() -> {
    ...
});


recordView.setOnFinishRecord(new AudioRecorderView.OnFinishRecordListener() {
    @Override
    public void onFinishRecordListener(@NotNull File file) {
        ...
    }
});
// or with java 8+
recordView.setOnFinishRecord(file -> {
    ...
});


recordView.setOnPlay(new AudioRecorderView.OnPlayListener() {
    @Override
    public void onPlay() {
        ...
    }
});
// or with java 8+
recordView.setOnPlay(() -> {
    ...
});


recordView.setOnPause(new AudioRecorderView.OnPauseListener() {
    @Override
    public void onPause() {
        ...
    }
});
// or with java 8+
recordView.setOnPause(() -> {
    ...
});


recordView.setOnResume(new AudioRecorderView.OnResumeListener() {
    @Override
    public void onResume() {
        ...
    }
});
// or with java 8+
recordView.setOnResume(() -> {
    ...
});


recordView.setOnFinishPlay(new AudioRecorderView.OnFinishPlayListener() {
    @Override
    public void onFinishPlayListener() {
        ...
    }
});
// or with java 8+
recordView.setOnFinishPlay() -> {
    ...
});


recordView.setOnDelete(new AudioRecorderView.OnDeleteListener() {
    @Override
    public void onDelete() {
        ...
    }
});
// or with java 8+
recordView.setOnDelete() -> {
    ...
});

```


## Contributions and Support

This project made use of [NaraeAudioRecorder](https://github.com/WindSekirun/NaraeAudioRecorder) by [WindSekirun](https://github.com/WindSekirun) to enable record audio functionality.

Contributions are welcome. Create a new pull request in order to submit your fixes and they shall be merged after moderation. In case of any issues, bugs or any suggestions, either create a new issue or post comments in already active relevant issues

## Please consider supporting me
Bitcoin URI: bitcoin:BC1Q4RT2KNSX28CA4H5YA08VF0SXMG3JPHKS6GWDXV?label=Consider%20support%20me

Bitcoin Address: bc1q4rt2knsx28ca4h5ya08vf0sxmg3jphks6gwdxv
