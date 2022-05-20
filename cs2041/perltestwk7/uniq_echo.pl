#!/usr/bin/perl

use warnings;

$size = @ARGV;
$i = 0;
@uniq_array = {};



while($i < $size){

   #$data{$ARGV[$i]} += 1;
   if(@uniq_array == 0){
      $uniq_array[0] = $ARGV[0];
   } else {
      if (grep( /^$ARGV[$i]/, @uniq_array) ){

      } else {
         $uniq_array[@uniq_array] = $ARGV[$i];
      }
   }

   $i++;
}

shift @uniq_array for 0;

print join(" ", @uniq_array);
print "\n";
