#! /usr/local/bin/bash
for i in `ls *.vc`
do
   echo $i:
   java VC.vc $i
   java VC.vc -u ${i}uu ${i}u
   diff ${i}u ${i}uu
done
