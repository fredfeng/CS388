#java -cp bin/:lib/stanford-parser.jar edu.utexas.nlp.ActiveLearner --trainBank wsj/00/wsj_0001.mrg --candidateBank wsj/00/wsj_0002.mrg --testBank wsj/20/wsj_2095.mrg --nextTrainBank wsj/20/wsj_2001.mrg --nextCandidatePool pool --selectionFunction random

echo "------------RAN-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -wordsPerIter 2500 -selectionFunction RAN -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/bat1.msg
echo "------------LEN-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -wordsPerIter 2500 -selectionFunction LEN -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/bat2.msg
echo "------------PROB-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -wordsPerIter 2500 -selectionFunction PROB -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/bat3.msg
echo "------------TREE-------------------------"
java -mx4500m -cp bin/:lib/stanford-parser.jar:lib/commons-cli-1.2.jar edu.utexas.nlp.ActiveLearner -wordsPerIter 2500 -selectionFunction TREE -initBank wsj/init/init.mrg -trainBank wsj/0103.mrg -testBank wsj/20.mrg -candidateBank wsj/bat4.msg
