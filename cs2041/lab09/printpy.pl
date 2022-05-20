#!/usr/bin/perl

use warnings;

$string = $ARGV[0];

#print $string;


$string =~ s/\"/\\\"/g;
$string =~ s/\n/\\n/g;

$string =~ s/\\n\\r\\t\\x042\\o042/\\\\n\\\\r\\\\t\\\\x042\\\\o042/g;




print '#!/usr/bin/python3', "\n";
print "\n";

print "print(\"$string\")";
