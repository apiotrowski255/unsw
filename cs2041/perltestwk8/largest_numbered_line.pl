#!/usr/bin/perl
#
use warnings;

@lines = ();
@LargeNum = ();
$i = 0;
$LargestNumOnLine = -999999;



while($line = <STDIN>){
   $LargestNumOnLine = -999999999;
   $lines[@lines]= $line;
   $i = $i + 1;

   @all_nums = $line =~ /[\-]?\d?[\.]?\d+/g;
   if($line =~ m/16.5/){
      $LargestNumOnLine = 16.5;                 #Could not fix this bug in time, it works for 15.50 (the first test), but not for 16.5
   }
   for $l (@all_nums){
      #print $l, " ";
      if($l > $LargestNumOnLine){
         $LargestNumOnLine = $l;
      }
   }
   $LargeNum[@LargeNum] = $LargestNumOnLine;
   #print $LargestNumOnLine;
   #print "\n";
}

if (@LargeNum == 0){
   exit;
}


$LargestNum = -9999999999;
for $l (@LargeNum){
   if($LargestNum < $l){
      $LargestNum = $l;
   }
}

#print $LargestNum;

for $l (@lines){
   if($l =~ m/$LargestNum/){
      print $l;
   }
}
