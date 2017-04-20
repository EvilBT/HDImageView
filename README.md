# HDImageView
[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[ ![Download](https://api.bintray.com/packages/sherlock/maven/hdimageview/images/download.svg) ](https://bintray.com/sherlock/maven/hdimageview/_latestVersion)

HD image view supporting pan and zoom, with very little memory usage and full featured image loading choices.

Requires Android SDK version 10 or higher.
## Demo
![Demo](https://raw.githubusercontent.com/EvilBT/HDImageView/master/gif/demo.gif)
## Getting started
**Step 1.** Add the dependency
```
dependencies {
    compile 'xyz.zpayh:hdimageview:1.0.1'
}
```
**Step 2.** Add the view to your layout XML
``` xml
<xyz.zpayh.hdimage.HDImageView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/image"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
**Step 3.** Set URI to HDImageView
``` java
    //...
    HDImageView mImageView = (HDImageView) findViewById(R.id.image);

    mImageView.setImageURI("res:///"+R.drawable.panorama);
    // Or 
    ImageSource imageSource = ImageSourceBuilder.newBuilder()
                .setUri(R.drawable.panorama)
                .build();
    mImageView.setImageSource(imageSource);
```
## Supported URIs

HDImageView supports images in a variety of locations.

HDImageView does **not** accept relative URIs. All URIs must be absolute and must include the scheme.

These are the URI schemes accepted:

| TYPE           | SCHEME                   | FETCH METHOD USED       |
|----------------|--------------------------|-------------------------|
|File on network | http://, https://        | HttpURLConnection       |
|File on device  | file://                  | FileInputStream         |
|Content provider|content://                |ContentResolver          |
|Asset in app    | asset://                 |AsseManager              |
|Resource in app | res:// as in res:///12345|Resources.openRawResource|

Like this:
``` java
mHDImageView.setImageURI("res:///"+R.drawable.panorama);
mHDImageView.setImageURI("asset://beauty.jpg");
mHDImageView.setImageURI("file:///sdcard/DCIM/IMG_001.JPG");
mHDImageView.setImageURI("http://7xi8d6.com1.z0.glb.clouddn.com/2017-04-16-17934400_1738549946443321_2924146161843437568_n.jpg");
//....
```
In the use of the process, please remember to add the appropriate permissions
## About
Powered by [Subsampling Scale Image View](https://github.com/davemorrissey/subsampling-scale-image-view)

Copyright 2016 David Morrissey, and licensed under the Apache License, Version 2.0. No attribution is necessary but it's very much appreciated. Star this project if you like it!