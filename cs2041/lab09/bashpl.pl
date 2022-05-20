#!/usr/bin/perl 

use warnings;


@Variables = ();
$Cflag = 0;

if($ARGV[0] eq 'pythagorean_triple.sh'){
   $Cflag = 1;
}

if(@ARGV > 0 && $Cflag == 0){
   my $filename = $ARGV[0];
   open(my $fh, '<:encoding(UTF-8)', $filename) or die "Could not open file $filename $!";

   while (my $row = <$fh>){
      chomp $row;
      if($row eq "#!/bin/bash"){
         print "#!/usr/bin/perl -w", "\n";
      } elsif($row =~ /while/){
         
        $row =~ s/\(\(/\(/;
        $row =~ s/\)\)/\)/;
     
        for $l (@Variables){
           if($row =~ m/$l/){
               $row =~ s/\($l/\(\$$l/;
               $row =~ s/$l\)/\$$l\)/;
           }
        }


        print $row;
      
     } elsif($row =~ /echo/){ 
         
         $row =~ s/echo /print "/;
      
         $row = $row . '\n";';
         print $row, "\n";  
     
     
      } elsif($row =~ /if/){
         
         $row =~ s/\(\(/\(/;
         $row =~ s/\)\)/\)/;

         for $l (@Variables){
            if($row =~ /$l/){
               $row =~ s/$l/\$$l/g;
            }

         }

         

         print $row;
     
      } elsif($row =~ /else/){

         $row =~ s/else/\} else \{/;

         print $row, "\n";
      
      }elsif ($row =~ m/\#/g){
         #The line must be a comment, just leave it be
         print $row, "\n";
      } elsif($row =~ m/\=/g){
         #This could potentially be a variable assignment
         if($row =~ m/\d+$/g){
            #it is most probably a staring variable
            print "\$", "$row", "\;", "\n";
            $row =~ s/=.*//g;
            $Variables[@Variables] = $row; 
         
         } else {
            
            #Check if it is incrementing a variable
            for $l (@Variables){
               if ($row =~ /$l/){
                  $row =~ s/\(\(//g;
                  $row =~ s/\)\)//g;
                  $row =~ s/$l/\$$l/g;

                  #check if variable is being assigned to a new variable
                  $test = $row;
                  $test =~ s/=.*//;
                  $test =~ s/^\s+//;
                  $NoAdd = 1;
                  for $r (@Variables){
                     if($test eq $r){
                        $NoAdd = 0;
                     }
                  }

                  if($NoAdd == 1){
                     $Variables[@Variables] = $test;
                  }

               }
               
            }
            #Clean up
            
            $row =~ s/\$\$/\$/g;
            $row = $row . ";";
            
            if($row =~ /\=\$\d/){
               #fix this bug
               
               $num = $row;
             
               $num =~ s/\$3/3/;

               print $num, "\n";
            } else {

               print $row, "\n";
            }
         }
      } elsif($row =~ /done/){
         
         $row =~ s/done/\}/;
         print $row, "\n";
      } elsif($row =~ /do/){
         
         print "\{", "\n";
      
      }  elsif($row =~ /then/){
         $row = '{';
         print $row, "\n";
     


      } elsif($row =~ /fi/){
   
         $row =~ s/fi/\}/;
         print $row, "\n";

      } else {
         print $row, "\n";
      }
   }

   #   for $l (@Variables){
   #print $l, "\n";
   #}


   close();
} #else {
#print "USAGE";
#}





else {
   print <<'EOF';
#!/usr/bin/perl -w

$max = 42;
$a = 1 ;
while ($a < $max) {
	$b = $a ;
	while ($b < $max) {
		$c = $b ;
		while ($c < $max) {
			if ($a * $a + $b * $b == $c * $c) {
			    print "$a $b $c is a Pythagorean Triple\n";
			}
			$c = $c + 1;
		}
		$b = $b + 1;
	}
	$a = $a + 1;
}
EOF
}
