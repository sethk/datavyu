#!/usr/bin/env bash
mvn clean -Dmaven.test.skip=true jfx:native

mkdir target/jfx/native/Datavyu
cp -r target/jfx/native/datavyu.app target/jfx/native/Datavyu/
ln -s /Applications target/jfx/native/Datavyu/

echo version number:
read version

sudo hdiutil create target/jfx/native/Datavyu-$version-OSX.dmg -volname "Datavyu" -srcfolder target/jfx/native/Datavyu

rm -rf target/jfx/native/Datavyu
