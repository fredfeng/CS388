package nlp.lm;

import java.io.*;
import java.util.*;

/** 
 * @author Yu Feng
 * Backward bigram language model that uses simple fixed-weight interpolation
 * with a unigram model for smoothing.
 * Modified based on Ray Mooney's code
*/

public class BackwardBigramModel extends BigramModel{

	/** Overide its super class by reverse the order of the sentence. */
	public void trainSentence(List<String> sentence) {
		List<String> senRev = new ArrayList<String>(sentence);
		Collections.reverse(senRev);
		super.trainSentence(senRev);
	}

	/**
	 * Do the same thing for sentenceLogProb2
	 */
	public double sentenceLogProb2(List<String> sentence) {
		List<String> senRev = new ArrayList<String>(sentence);
		Collections.reverse(senRev);
		return super.sentenceLogProb2(senRev);
	}

	/**
	 * Train and test a bigram model. Command format:
	 * "nlp.lm.BigramModel [DIR]* [TestFrac]" where DIR is the name of a file or
	 * directory whose LDC POS Tagged files should be used for input data; and
	 * TestFrac is the fraction of the sentences in this data that should be
	 * used for testing, the rest for training. 0 < TestFrac < 1 Uses the last
	 * fraction of the data for testing and the first part for training.
	 */
	public static void main(String[] args) throws IOException {
		// All but last arg is a file/directory of LDC tagged input data
		File[] files = new File[args.length - 1];
		for (int i = 0; i < files.length; i++)
			files[i] = new File(args[i]);
		// Last arg is the TestFrac
		double testFraction = Double.valueOf(args[args.length - 1]);
		// Get list of sentences from the LDC POS tagged input files
		List<List<String>> sentences = POSTaggedFile.convertToTokenLists(files);
		int numSentences = sentences.size();
		// Compute number of test sentences based on TestFrac
		int numTest = (int) Math.round(numSentences * testFraction);
		// Take test sentences from end of data
		List<List<String>> testSentences = sentences.subList(numSentences
				- numTest, numSentences);
		// Take training sentences from start of data
		List<List<String>> trainSentences = sentences.subList(0, numSentences
				- numTest);
		System.out.println("# Train Sentences = " + trainSentences.size()
				+ " (# words = " + wordCount(trainSentences)
				+ ") \n# Test Sentences = " + testSentences.size()
				+ " (# words = " + wordCount(testSentences) + ")");
		// Create a bigram model and train it.
		BackwardBigramModel model = new BackwardBigramModel();
		System.out.println("Training...");
		model.train(trainSentences);
		// Test on training data using test and test2
		model.test2(trainSentences);
		System.out.println("Testing...");
		// Test on test data using test and test2
		model.test2(testSentences);
	}

}
