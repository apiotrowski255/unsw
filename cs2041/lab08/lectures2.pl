#!/usr/bin/perl

use warnings;

$Dflag = 0;
$Tflag = 0;
$Cflag = 0;
#For the Tflag
@S1Array = ();
@S2Array = ();


for $arg (@ARGV){
   if($arg eq "-d"){
      $Dflag = 1;
   }
   if($arg eq "-t"){
      $Tflag = 1;
   }
   if($arg eq "ENGG1811"){
      $Cflag = 1;
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
               if($Dflag == 0 && $Tflag == 0){
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
                  
               } elsif ($Tflag == 1){

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
                        my $text = $time . " " . $num1;
                        if(!defined $hash{$text}){
                           #print $text, "\n";
                           if($IsT2 eq "S1"){
                              $i = @S1Array;
                              $S1Array[$i] = $text; 
                           } elsif ($IsT2 eq "S2"){
                              $i = @S2Array;
                              $S2Array[$i] = $text;
                           }
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

#Now print the data if the Tflag is enabled


if($Tflag == 1){
        
   if(@S1Array > 0){
      print "S1       Mon   Tue   Wed   Thu   Fri", "\n";
      $DayCounter = 0;
      $HourCounter = 9;
      
      while($HourCounter < 21){


         if($HourCounter == 9){
            print "0" . $HourCounter . ":00";
         } else {
            print $HourCounter . ":00";
         }
         $DayCounter = 0;

         while($DayCounter < 5){
            $ClassCounter = 0;
            
            if($DayCounter == 0){
               for $l (@S1Array){
                  if ($l =~ m/Mon/){
                     if($l =~ m/$HourCounter/){
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }
                     }
                  }
               }
            } elsif($DayCounter == 1){
               for $l (@S1Array){
                  if ($l =~ m/Tue/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 2){
               for $l (@S1Array){
                  if ($l =~ m/W/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 3){
               for $l (@S1Array){
                  if ($l =~ m/Thu/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 4){
               for $l (@S1Array){
                  if ($l =~ m/Fri/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            }

            if($ClassCounter == 0){
               print "      ";
            } else {
               print "     " . $ClassCounter;
            }

            $DayCounter += 1;

         }
         print "\n";
         $HourCounter += 1;
      }

   } 

}













if($Tflag == 1){
        
   if(@S2Array > 0){
      print "S2       Mon   Tue   Wed   Thu   Fri", "\n";
      $DayCounter = 0;
      $HourCounter = 9;
      
      while($HourCounter < 21){


         if($HourCounter == 9){
            print "0" . $HourCounter . ":00";
         } else {
            print $HourCounter . ":00";
         }
         $DayCounter = 0;

         while($DayCounter < 5){
            $ClassCounter = 0;
            
            if($DayCounter == 0){
               for $l (@S2Array){
                  if ($l =~ m/Mon/){
                     if($l =~ m/$HourCounter/){
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                        if($HourCounter == 12 && $Cflag == 1){
                           $ClassCounter += 1;
                        }
                     }
                  }
               }
            } elsif($DayCounter == 1){
               for $l (@S2Array){
                  if ($l =~ m/Tue/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 2){
               for $l (@S2Array){
                  if ($l =~ m/W/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 3){
               for $l (@S2Array){
                  if ($l =~ m/Thu/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            } elsif($DayCounter == 4){
               for $l (@S2Array){
                  if ($l =~ m/Fri/){
                     if($l =~ m/$HourCounter/){      
                        if($HourCounter == 9){
                           if($l =~ m/1/){

                           } else {
                              $ClassCounter += 1;
                           }
                        } else {
                           $ClassCounter += 1;
                        }

                     }
                  }
               }
            }

            if($ClassCounter == 0){
               print "      ";
            } else {
               print "     " . $ClassCounter;
            }

            $DayCounter += 1;

         }
         print "\n";
         $HourCounter += 1;
      }

   } 

}






