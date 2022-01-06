GestureViews
============

[![Maven][mvn-img]][mvn-url]
[![Build][build-img]][build-url]

ImageView and FrameLayout with gestures control and position animation.

Main goal of this library is to make images viewing process as smooth as possible and to make it
easier for developers to integrate it into their apps. 

#### Features ####

- Gestures support: pan, zoom, quick scale, fling, double tap, rotation.
- [Seamless integration](https://github.com/alexvasilkov/GestureViews/wiki/Usage#viewpager) with ViewPager (panning smoothly turns into ViewPager flipping and vise versa).
- [View position animation](https://github.com/alexvasilkov/GestureViews/wiki/Basic-animations) ("opening" animation). Useful to animate into full image view mode.
- [Advanced animation](https://github.com/alexvasilkov/GestureViews/wiki/Advanced-animations) from RecyclerView (or ListView) into ViewPager.
- Exit full image mode by scroll and scale gestures.
- Rounded images with animations support. 
- [Image cropping](https://github.com/alexvasilkov/GestureViews/wiki/Image-cropping) (supports rotation).
- [Lots of settings](https://github.com/alexvasilkov/GestureViews/wiki/Settings).
- [Gestures listener](https://github.com/alexvasilkov/GestureViews/wiki/Usage#listeners): down (touch), up (touch), single tap, double tap, long press.
- Custom state animation (animating position, zoom, rotation).
- Supports both ImageView and FrameLayout out of the box, also supports [custom views](https://github.com/alexvasilkov/GestureViews/wiki/Custom-views).

#### Sample app ####

<a href="http://play.google.com/store/apps/details?id=com.alexvasilkov.gestures.sample">
  <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge-border.png" height="64" />
</a>

#### Demo video ####

[YouTube](https://youtu.be/KDJj08qN7n4)

[![Demo video](https://github.com/alexvasilkov/GestureViews/raw/master/sample/art/demo.gif)](https://youtu.be/KDJj08qN7n4)  

#### Usage ####

Add dependency to your `build.gradle` file:

    implementation 'com.alexvasilkov:gesture-views:2.8.3'

[Usage wiki](https://github.com/alexvasilkov/GestureViews/wiki/Usage)

[Javadoc][javadoc-url]

[Sample app sources](https://github.com/alexvasilkov/GestureViews/tree/master/sample)

#### License ####

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[mvn-url]: https://maven-badges.herokuapp.com/maven-central/com.alexvasilkov/gesture-views
[mvn-img]: https://img.shields.io/maven-central/v/com.alexvasilkov/gesture-views.svg?style=flat-square

[build-url]: https://actions-badge.atrox.dev/alexvasilkov/GestureViews/goto?ref=master
[build-img]: https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Falexvasilkov%2FGestureViews%2Fbadge%3Fref%3Dmaster&style=flat-square

[javadoc-url]: http://javadoc.io/doc/com.alexvasilkov/gesture-views
