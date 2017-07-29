#! /usr/bin/perl

use strict;

my $verbose = 0;

my $input = shift;
my $output = shift;
my $C = shift;

die "usage run_cv <INPUT.train> <OUTPUT> [C]" if !$input || !$output;

system("rm $output") if -f $output;

$C=1 if !$C;

open INF, $input or die "Could not open input file $input: $!\n";

# Create a unique temporary file
my $count = 0;
my $temptrain = "$input.temp$count.train";
while(-f $temptrain) {
	$count++;
	$temptrain = "$input.temp$count.train";
}
print "Temporary file is: $temptrain\n";

# Create a temporary file to train a single instance
my $tempmodel = "$input.temp$count.model";
my $temptest = "$input.temp$count.test";
my $temppred = "$input.temp$count.pred";

while(<INF>) {
	chomp;
	/^(\d+).*# (\S+)/;
	my $test_line = $_;
	my $exp_lang = $1;
	my $user = $2;

	system("echo \"$test_line\" > $temptest");
	print "echo \"[test_line]\" > $temptest\n" if $verbose;

	open(TEMP, ">$temptrain");
	open(INF2, $input);
	while(<INF2>) {
		/^(\d+).*# (\S+)/;
		if ($2 ne $user) {
			print TEMP $_;
		}
	}
	close(INF2);
	close(TEMP);

	my $run_script = "./run_classifier_single.sh $temptrain $tempmodel $temptest $temppred $C";
	print "$run_script\n";
	print "Running...";
	if ($verbose) {
		system ("$run_script");
	}
	else {
		system ("$run_script > /dev/null");
	}
	print "\n";
	system ("printf \"$exp_lang \" >> $output");
	system ("cat $temppred >> $output");

	#sleep(10);
	if($verbose) {
		print "OUTPUT IS:\n";
		system ("cat $output");
	}
}

system("rm $temptrain $tempmodel $temptest $temppred");
