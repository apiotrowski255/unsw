#!/usr/bin/perl

use warnings;

if (@ARGV == 0){
   exit;
}

for $argv (@ARGV){
   $url = "http://www.timetable.unsw.edu.au/current/" . $argv . ".html";
   %hash= (); 
   $reading = 0;
   $readcounter = 0;
   $IsT2 = 0;
   open F, "wget -q -O- $url|";
   while($line = <F>){
      if($line =~ m/>Lecture<\/a/g){
         $reading = 1;
         $readcounter = 7;
      }

      if($reading == 1){
         #print $line;
         $readcounter--;
      }

      if($readcounter == 5 && $reading == 1){
         $line =~ s/<.*?>//g;
         $line =~ s/^\s+//g;
         $line =~ s/\s+$//g;
         if($line eq T2){
            $IsT2 = "S2";
         } else {
            $IsT2 = "S1";
         }
      }

      if($readcounter == 0 && $reading == 1){
         $line =~ s/^\s+//g;
         $line =~ s/\s+$//g;
         $line =~ s/<.*?>//g;
         if($line){
            if(!defined $hash{$line}){
               print $argv. ":" . " " . $IsT2 . " " . $line, "\n";
            }
            $hash{$line} = 1;
         }  
         $reading = 0;
      }


   }

}
