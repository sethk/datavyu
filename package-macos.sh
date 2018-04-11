#! /bin/#!/usr/bin/env bash
mvn clean -Dmaven.test.skip=true jfx:native

cp -R target/jfx/native/ target/jfx/native/Datavyu

ln -s /Applications target/jfx/native/Datavyu/

read version

sudo hdiutil create ~/Desktop/Datavyu-X.X.X-PLATFORM.dmg -volname "Datavyu" -srcfolder target/jfx/native/Datavyu/
