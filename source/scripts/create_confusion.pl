#! /usr/bin/perl

use strict;

my $total_langs = 14;

my $input = shift;

$input =~ /.*\/(...)\.(.*\.)?pred/;
my $lang = $1;

my $conf = `cut -f 1 -d " " $input | sort | uniq -c`;

my %conf_total = ();

my @conf_mat = split("\n", $conf);
foreach (@conf_mat) {
	/(\d+)\s(\d+)/;
	$conf_total{$2} = $1;
}

print "$lang\t";
for (my $i=1; $i<=$total_langs; $i++) {
	print int($conf_total{$i}), "\t";
}
print "\n";
