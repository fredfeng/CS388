
tagger=``

if [ "$1" = "crf" ]; then 
    echo Running CRF---------------------- 
    tagger=cc.mallet.fst.SimpleTagger
else
    echo Running HMM---------------------- 
    tagger=cc.mallet.fst.HMMSimpleTagger
fi;

java -ea -cp bin/:lib/mallet-deps.jar $tagger --train true --model-file model_file --training-proportion 1.0 --test lab $2 $3


echo ----Summary------------------------
echo Using model: $1
echo training set: $2
echo testing set: $3

#-------------------------------------------------------
#java -cp "mallet/class:mallet/lib/mallet-deps.jar" 
#cc.mallet.fst.HMMSimpleTagger
#--train true --model-file model_file
#--training-proportion 1.0
#--test lab $train_file $test_file
#Just make sure to set training-proportion to 1.0 and put two files at the end.
#---------------------------------------------------------
