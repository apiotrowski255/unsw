#!/usr/bin/perl

use warnings;
$words = 0;

while ($line = <STDIN>){
	chomp $line;
	@words = split /[^a-zA-Z]/, $line;
	#$words += @words;
   foreach $str (@words){
      if($str){
         $words += 1;
      }
   }
}

print $words, " words", "\n";
