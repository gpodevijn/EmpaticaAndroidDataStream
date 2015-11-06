# EmpaticaDataStream

This Android application allows you to communicate with the Empatica E3 wristband device. It uses the Android Empatica API version 1.4.

It allows you to monitor in real-time (via Bluetooth) the physiological activity (heart rate, electrodermal activity, body temperature) of someone wearing the device.

The app allows you to:
- record a baseline of the physiological activity (you can configure the duration of the baseline)
- record the physiological activity during an experiment (the experiment can be divided into multiple sessions, which can be of different duractions)
- tag specific moments of the experiment
- save the data in text files

In order to be able to build and run the application, you will need to add a file 'privatekey.xml' in 'app/src/main/res/values/' with the following content:

<pre>
<code>
&lt;?xml version="1.0" encoding="utf-8"?&gt;
&lt;resources&gt;
    &lt;string name="empatica_key">YOUR_PRIVATE_KEY&lt;/string&gt;
&lt;/resources&gt;
</code>
</pre>

**Please take into account that possible bugs are present in this software. I do not take any responsability in case of problems (e.g., data loss)**


# Screenshots
<img src="https://github.com/gpodevijn/EmpaticaAndroidDataStream/blob/master/screnshots/Screenshot_20151106-100541.png" width="250">
<img src="https://github.com/gpodevijn/EmpaticaAndroidDataStream/blob/master/screnshots/Screenshot_20151106-100553.png" width="250">
<img src="https://github.com/gpodevijn/EmpaticaAndroidDataStream/blob/master/screnshots/Screenshot_20151106-100612.png" width="250">
<img src="https://github.com/gpodevijn/EmpaticaAndroidDataStream/blob/master/screnshots/Screenshot_20151106-100618.png" width="250">
<img src="https://github.com/gpodevijn/EmpaticaAndroidDataStream/blob/master/screnshots/Screenshot_20151106-100634.png" width="250">


