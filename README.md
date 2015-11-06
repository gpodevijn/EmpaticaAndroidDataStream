# EmpaticaAndroidDataStream

This Android application allows you to communicate with the Empatica E3 wristband device.

It allows you to monitor in real-time (via Bluetooth) the physiological acitivy (heart rate, electrodermal activity, body temperature) of someone wearing the device.

The app allows you to:
- record a baseline of the physiological activity (you can configure the duration of the baseline)
- record the physiological activity during an experiment (the experiment can be divided into multiple sessions, which can be of different duractions)
- tag specific moments of the experiment
- save the data in text files

In order to be able to build and run the application, you will need to add a file 'privatekey.xml' in 'app/src/main/res/values/' with the following content:

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="empatica_key">YOUR_PRIVATE_KEY</string>
</resources>
