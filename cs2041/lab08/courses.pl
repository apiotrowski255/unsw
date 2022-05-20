#!/usr/bin/perl

use warnings;

if (@ARGV <= 0 ) {
   exit;
} elsif (@ARGV > 1){
   exit;
}

$word = $ARGV[0];

$url = "http://www.timetable.unsw.edu.au/current/" . $word . "KENS.html";

open F, "wget -q -O- $url|" or die;
while ($line = <F>){
   if($line =~ m/>$word[\d]{4}</){
      $line =~ s/^\s+//g;
      $line =~ s/\s+$//g;
      $line =~ s/<.*?>//;
      $line =~ s/<.*?>//g;
      print $line, "\n";
   }
}
