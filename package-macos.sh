#!/usr/bin/env bash
mvn clean -Dmaven.test.skip=true jfx:native

ln -s /Applications target/jfx/native/

echo version number:
read version

sudo hdiutil create target/jfx/native/Datavyu-$version-OSX.dmg -volname "Datavyu" -srcfolder target/jfx/native/
