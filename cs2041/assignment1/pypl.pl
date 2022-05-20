#!/usr/bin/perl

use warnings;

$USAGE_ERROR = "Usage: ./pypl.pl <python file>";

if(@ARGV == 0){
   print $USAGE_ERROR,"\n";
   exit;
}

if(@ARGV > 1){
   print $USAGE_ERROR, "\n";
   exit;
}

#At this point we should strictly have only one argument, We will not be checking whether this file is a python file (As it is assumed)
#Technically we can, by checking whether the file ends with .py, perhaps to be implemented later


#Now we try to open the file and then read it line by line
open (my $fh, '<:encoding(UTF-8)', $ARGV[0]) or die "Could not open file $ARGV[0] $!";

#The first time we scan through, we will just be gathering data about the variables used
@Variables = ();
@Arrays = ();
@Hashs = ();
while (my $row = <$fh>){
   #See if the row has the assign symbol
   if($row =~ m/\=/){
      if($row =~ m/\=\=/){
         #The == symbol is not what we are looking for
      } elsif($row =~ m/\<\=/){

      } elsif($row =~ m/\>\=/){

      } elsif($row =~ m/\+\=/){

      } elsif($row =~ m/\-\=/){

      } elsif($row =~ m/\*\=/){

      } elsif($row =~ m/\\\=/){

      }
   }

}
