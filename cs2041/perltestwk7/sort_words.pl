#!/usr/bin/perl

use warnings;

foreach $line ( <STDIN> ) {
   chomp( $line );
   #Now sort the words in the line
   @line = split / /, $line;
      
   @line = sort @line;
   foreach (@line){
      print "$_ ";
   }
   print "\n";
}
