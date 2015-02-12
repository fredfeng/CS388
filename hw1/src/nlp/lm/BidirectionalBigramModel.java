package nlp.lm;

import java.io.*;
import java.util.*;

/**
 * @author Ray Mooney A simple Bidrection bigram language model that uses simple
 *         fixed-weight interpolation with a unigram model for smoothing.
 */

public class BidirectionalBigramModel {

	/** Interpolation weight for normal bigram model */
	public double lambda3 = 0.5;

	/** Interpolation weight for normal backwardBigram model */
	public double lambda4 = 0.5;

	/**
	 * Initialize model with empty hashmaps with initial unigram entries for
	 * setence start (<S>), sentence end (</S>) and unknown tokens
	 */
	public BidirectionalBigramModel() {
		bigramModel = new BigramModel();
		backBigramModel = new BackwardBigramModel();
	}

	/**
	 * Train the model on a List of sentences represented as Lists of String
	 * tokens
	 */
	public void train(List<List<String>> sentences) {
		// train two models separately.
		bigramModel.train(sentences);
		backBigramModel.train(sentences);
	}

	/**
	 * Like test1 but excludes predicting end-of-sentence when computing
	 * perplexity
	 */
	public void test2(List<List<String>> sentences) {
		double totalLogProb = 0;
		double totalNumTokens = 0;
		for (List<String> sentence : sentences) {
			totalNumTokens += sentence.size();
			double sentenceLogProb = sentenceLogProb2(sentence);
			// System.out.println(sentenceLogProb + " : " + sentence);
			totalLogProb += sentenceLogProb;
		}
		double perplexity = Math.exp(-totalLogProb / totalNumTokens);
		System.out.println("Word Perplexity = " + perplexity);
	}

	/**
	 * Like sentenceLogProb but excludes predicting end-of-sentence when
	 * computing prob
	 */
	public double sentenceLogProb2(List<String> sentence) {
		double[] forward = this.bigramModel.sentenceTokenProbs(sentence);
		List<String> senRev = new ArrayList<String>(sentence);
		Collections.reverse(senRev);
		double[] backward = this.backBigramModel.sentenceTokenProbs(senRev);
		double sentenceLogProb = 0;

		for (int i = 0; i < sentence.size(); i++) {
			double d1 = forward[i] * lambda3;
			double d2 = backward[sentence.size() - i - 1] * lambda4;
			double logProb = Math.log(d1 + d2);
			sentenceLogProb += logProb;
		}

		return sentenceLogProb;
	}

	public static int wordCount(List<List<String>> sentences) {
		int wordCount = 0;
		for (List<String> sentence : sentences) {
			wordCount += sentence.size();
		}
		return wordCount;
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
		BidirectionalBigramModel model = new BidirectionalBigramModel();
		System.out.println("Training...");
		model.train(trainSentences);
		
		// Test on training data using test and test2
		model.test2(trainSentences);
		System.out.println("Testing...");
		// Test on test data using test and test2
		model.test2(testSentences);
	}

	BigramModel bigramModel;
	BackwardBigramModel backBigramModel;

}
