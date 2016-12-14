#!/bin/sh

hot_folder=/tmp/hot_folder

if [ ! -d $hot_folder ]; then
  mkdir $hot_folder
fi

path="$hot_folder/input.csv"
echo "VALUE" > $path
seq 1 1 11 >> $path
#seq 1 1 111111111 >> $path
