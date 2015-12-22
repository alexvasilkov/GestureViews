GestureViews
============

[![Maven Central](https://img.shields.io/maven-central/v/com.alexvasilkov/gesture-views.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/gesture-views)

ImageView and FrameLayout with gestures control and position animation.

Main goal of this library is to make images viewing process as smooth as possible and to make it
easier for developers to integrate it into their apps. 

#### Features ####

- Gestures support: pan, zoom, quick scale, fling, double tap, rotation.
- [Gestures listener](https://github.com/alexvasilkov/GestureViews/wiki/Usage#listeners): down, single tap, double tap, long press.
- Lots of [settings] (https://github.com/alexvasilkov/GestureViews/wiki/Settings).
- [Seamless integration](https://github.com/alexvasilkov/GestureViews/wiki/Usage#viewpager) with ViewPager (panning smoothly turns into ViewPager flipping and vise versa).
- State animation (animating position, zoom, rotation).
- [View position animation](https://github.com/alexvasilkov/GestureViews/wiki/Basic-animations) ("opening" animation). Useful to animate into full image view mode.
- [Advanced animation](https://github.com/alexvasilkov/GestureViews/wiki/Advanced-animations) from RecyclerView (or ListView) into ViewPager which keeps track of views positions.
- [Image cropping](https://github.com/alexvasilkov/GestureViews/wiki/Image-cropping) (supports rotation).
- Supports both ImageView and FrameLayout out of the box, but also supports [custom views](https://github.com/alexvasilkov/GestureViews/wiki/Custom-views).

#### Sample app ####

[![Get it on Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](http://play.google.com/store/apps/details?id=com.alexvasilkov.gestures.sample)

#### Demo video ####

[YouTube](http://www.youtube.com/watch?v=5N5G_vgqZbI)

[![Demo video](https://github.com/alexvasilkov/GestureViews/raw/master/sample/art/demo.gif)](http://www.youtube.com/watch?v=5N5G_vgqZbI)  

#### Usage ####

Add dependency to your `build.gradle` file:

    compile 'com.alexvasilkov:gesture-views:2.0.3'

Note: min SDK version for library is 9, but it was tested mainly on 15+.

[Usage wiki](https://github.com/alexvasilkov/GestureViews/wiki/Usage)

[Javadoc](https://oss.sonatype.org/service/local/repositories/releases/archive/com/alexvasilkov/gesture-views/2.0.3/gesture-views-2.0.3-javadoc.jar/!/index.html)

[Sample app sources](https://github.com/alexvasilkov/GestureViews/tree/master/sample)

#### License ####

    Copyright 2014 Alex Vasilkov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
