#!/usr/bin/perl

use warnings;

$num = $ARGV[0];
$numOfLines = 0;
@array = {};

while ($line = <STDIN>){
   $isequal = 0;

   $line = lc($line);
   $line =~ s/\s+/ /g;
   
   #left trim
   $line =~ s/^\s+//g;

   #right trim
   $line =~ s/\s+$//g;

   #check if $line is in array
   if(@array == 0){
      $array[0] = $line;
   } else {
      $i = 0;
      $size = @array;
      while($i < $size){
         if($array[$i] eq $line){
            $isequal = 1;
         }
         $i++;
      }

      if ($isequal == 0){
         $array[$size] = $line;
      }
   }


   $numOfLines++;
   if (@array > $num){
      last;
   }
}

shift @array for 0;
#foreach (@array){
#   print "$_", "\n";
#}
#print $numOfLines;

$size = @array;
if($size >= $num){
   print $size, " distinct lines seen after ", $numOfLines, " lines read.", "\n";
} else {
   print "End of input reached after ", $numOfLines, " lines read - ", $num, " different lines not seen.", "\n";
}























