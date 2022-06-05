## Prerequisites

You must own the original game in order to be able to install and run this port.

This article will teach you how to repack the original game into a package readable by this project.

## Repack the game

First, unzip the game, and navigate to `$GAME/resources/app`. You will see the following files and directories:

```
data, node_modules, tyrano, index.html, main.js, package.json, tyrano.icns, tyrano.ico
```

The following files and directories are needed by this project. Zip them into a file named `app.zip`:

```
data, tyrano, index.html
```

Then, we need to create a patch in order to make the game compatible with Android WebViews. Use a text editor to open `index.html`, and find the following line:

```html
</head>
```

Then, add the following line **before** that line:

```html
<script type="text/javascript" src="./tyrano_player.js"></script>
```

Finally, zip the modified `index.html` along with [this file](./tyrano_player.js) (\*) into `patch.zip`. The game package is now ready.

## Build and install

You should have Android SDK installed. NDK is not necessary.

Run the following command to build and install a debug version of this project:

```sh
./gradlew installDebug
```

When the app finishes installing, run the app once to let Android OS create its data directory, and then push the game package into the device:

```sh
adb push app.zip /sdcard/Android/data/com.tyranobuilder.teachingfeeling/files/app.zip
adb push patch.zip /sdcard/Android/data/com.tyranobuilder.teachingfeeling/files/patch.zip
```

Now the game should be able to run normally (albeit a little slow on average devices, about which there is nothing we can do).

## Credits

This project is based on [TyranoPlayer standalone version for Android](https://tyranobuilder.com/exporting-for-android-devices/). The file marked with (\*) comes unmodified from the above link. Credits goes to its original authors.

This project is in no way associated with the original game (Teaching Feeling).
