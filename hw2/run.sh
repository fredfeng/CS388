echo Basic section========================================
echo Running 1st experiment-HMM atis----------------------
time ./atis.sh hmm posMallet/atis/atis.txt

echo Running 2nd experiment-HMM plus atis----------------
time ./atis.sh hmm posMalletFea/atis/atis.txt

echo Running 3rd experiment-CRF atis----------------------
time ./atis.sh crf posMallet/atis/atis.txt

echo Running 4th experiment-CRF plus atis-------------------
time ./atis.sh crf posMalletFea/atis/atis.txt

echo Running 5th experiment-HMM wsj00--01-------------------
time ./wsj.sh hmm posMallet/wsj/wsj00.txt posMallet/wsj/wsj01.txt

echo Running 6th experiment-CRF wsj00--01-------------------
time ./wsj.sh crf posMallet/wsj/wsj00.txt posMallet/wsj/wsj01.txt

echo Running 7th experiment-HMM plus wsj00--01-------------------
time ./wsj.sh hmm posMalletFea/wsj/wsj00.txt posMalletFea/wsj/wsj01.txt

echo Running 8th experiment-CRF plus wsj00--01-------------------
time ./wsj.sh crf posMalletFea/wsj/wsj00.txt posMalletFea/wsj/wsj01.txt

echo Extension section 1========================================
echo Running 9th experiment-HMM plus wsj234-567-------------------
time ./wsj.sh hmm posMalletFea/wsj/wsj2_3_4.txt posMalletFea/wsj/wsj5_6_7.txt

echo Running 10th experiment-CRF plus wsj234-567-------------------
time ./wsj.sh crf posMalletFea/wsj/wsj2_3_4.txt posMalletFea/wsj/wsj5_6_7.txt

echo Whether HMM can beat CRF by increasing the training set and test on 24.
echo Running 11th experiment-HMM plus wsj0_9_24-------------------
time ./wsj.sh hmm posMalletFea/wsj/wsj0_9.txt posMalletFea/wsj/wsj24.txt

echo Running 12th experiment-CRF plus wsj234_24-------------------
time ./wsj.sh crf posMalletFea/wsj/wsj2_3_4.txt posMalletFea/wsj/wsj24.txt

echo What if we test it on other benchmark
echo Running 13th experiment-HMM plus wsj0_9_brown-------------------
time ./wsj.sh hmm posMalletFea/wsj/wsj0_9.txt posMalletFea/brown/brown_ca.txt

echo Running 14th experiment-CRF plus wsj234_brown-------------------
time ./wsj.sh crf posMalletFea/wsj/wsj2_3_4.txt posMalletFea/brown/brown_ca.txt


