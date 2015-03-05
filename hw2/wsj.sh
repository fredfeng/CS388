start_time=`date +%s`
#seed=`shuf -i 1000-10000 -n 1`
#java -ea -cp bin/:lib/mallet-deps.jar cc.mallet.fst.HMMSimpleTagger --random-seed $seed --train true --model-file model_file --training-proportion 0.8 --test lab test.txt
#java -ea -cp bin/:lib/mallet-deps.jar cc.mallet.fst.SimpleTagger --train true --model-file model_file --training-proportion 0.8 --test lab test.txt

tagger=``

if [ "$1" = "crf" ]; then 
    echo Running CRF---------------------- 
    tagger=cc.mallet.fst.SimpleTagger
else
    echo Running HMM---------------------- 
    tagger=cc.mallet.fst.HMMSimpleTagger
fi;

for i in `seq 101 110`;
do
    echo Begin trial-----------: $i
    #seed=`shuf -i 1000-10000 -n 1`
    echo random seed: $i
    java -ea -cp bin/:lib/mallet-deps.jar $tagger --random-seed $i --train true --model-file model_file --training-proportion 0.8 --test lab $2
    echo End trial-----------: $i
done    
end_time=`date +%s`
echo execution time was `expr $end_time - $start_time` s.
