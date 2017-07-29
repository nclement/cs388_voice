#! /bin/bash

i=0
for testF in `ls output/*`; do
	j=0
	for trainF in `ls output/*`; do
		#if [ $j -ge $i ]; then
			java -cp ../bigram_java/bin/ nlp.lm.BigramModel $testF $trainF
		#fi
		let "j+=1"
		printf "\t"
	done
	echo
	let "i+=1"
done
