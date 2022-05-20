#!/usr/bin/perl

use warnings;

$Dflag = 0;

for $arg (@ARGV){
   if($arg eq "-d"){
      $Dflag = 1;
   }
}


if (@ARGV == 0){
   exit;
}

for $argv (@ARGV){
   $url = "http://www.timetable.unsw.edu.au/current/" . $argv . ".html";
   %hash= ();
   #%hash2=();
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
            #print "1", "\n";
            if(!defined $hash{$line}){
               if($Dflag == 0){
                  print $argv. ":" . " " . $IsT2 . " " . $line, "\n";
               } elsif($Dflag == 1){
                  #print $IsT2 . " " . $argv . " " . $line, "\n";
                  $line =~ s/\(.*?\)//g;
                  @times = split /, /, $line;
                  for $time (@times){
                     @all_nums = $time =~ /(\d+)/g;
                     $time =~ s/\d+//g;
                     $time =~ s/\://g;
                     $time =~ s/\-//g;
                     $time =~ s/\s*$//g;
                     $num1 = $all_nums[0];
                     $num1 =~ s/^0//g;
                     $num2 = $all_nums[2];
                     while($num1 < $num2){
                        my $text = $IsT2 . " " . $argv . " " . $time . " " . $num1;
                        if(!defined $hash{$text}){
                           print $text, "\n";
                           $hash{$text} = 1;
                        }
                        $num1 = $num1 + 1;
                     }

                  }
                  
               }
            }
            $hash{$line} = 1;
         }  
         $reading = 0;
      }


   }

}
