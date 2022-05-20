#!/bin/sh

if [ $# -eq 0 ]
then
	echo "Usage: need image files"
	exit 1;
fi


for picture in $@
do
	read -p "Address to e-mail this image to? " yn
	


done
