package nlp.lm;

import java.io.*;
import java.util.*;

/**
 * @author Ray Mooney A simple Bidrection bigram language model that uses simple
 *         fixed-weight interpolation with a unigram model for smoothing.
 */

public class BidirectionalBigramModel {

	/** Total count of tokens in training data */
	public double tokenCount = 0;

	/** Interpolation weight for unigram model */
	public double lambda1 = 0.1;

	/** Interpolation weight for bigram model */
	public double lambda2 = 0.9;

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

	/** Return bigram string as two tokens separated by a newline */
	public String bigram(String prevToken, String token) {
		return prevToken + "\n" + token;
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
		String prevToken = "<S>";

		double sentenceLogProb = 0;
		for (String token : sentence) {
			DoubleValue unigramVal = bigramModel.unigramMap.get(token);
			if (unigramVal == null) {
				token = "<UNK>";
				unigramVal = bigramModel.unigramMap.get(token);
			}
			token2UnigramVal.put(token, unigramVal);
			String bigram = bigram(prevToken, token);
			// DoubleValue bigramVal = bigramMap.get(bigram);
			DoubleValue bigramVal = bigramModel.bigramMap.get(bigram);
			token2BigramVal.put(token, bigramVal);
			prevToken = token;

		}
		
		String nextToken = "</S>";
		List<String> backSentence = new ArrayList<String>(sentence);
		Collections.reverse(backSentence);
		for (String token : backSentence) {
			DoubleValue unigramVal = backBigramModel.unigramMap.get(token);
			if (unigramVal == null) {
				token = "<UNK>";
				unigramVal = backBigramModel.unigramMap.get(token);
			}
			token2BackUnigramVal.put(token, unigramVal);

			String bigram = bigram(nextToken, token);
			// DoubleValue bigramVal = bigramMap.get(bigram);
			DoubleValue bigramVal = backBigramModel.bigramMap.get(bigram);
			token2BackBigramVal.put(token, bigramVal);
			nextToken = token;
		}
		
		for (String token : sentence) {

			DoubleValue unigramVal1 = token2UnigramVal.get(token);
			if (unigramVal1 == null) {
				token = "<UNK>";
				unigramVal1 = token2UnigramVal.get(token);
			}
			DoubleValue unigramVal2 = token2BackUnigramVal.get(token);
			if (unigramVal2 == null) {
				token = "<UNK>";
				unigramVal2 = token2UnigramVal.get(token);
			}
			DoubleValue bigramVal1 = token2BigramVal.get(token);
			DoubleValue bigramVal2 = token2BackUnigramVal.get(token);
			
			double logProb = Math.log(interpolatedProb(unigramVal1,
					unigramVal2, bigramVal1, bigramVal2));
			assert logProb > -2000 : token;
			
			sentenceLogProb += logProb;
			assert !token.equals("agreed-upon") : sentence;

		}
		return sentenceLogProb;
	}

	HashMap<String, DoubleValue> token2UnigramVal = new HashMap<String, DoubleValue>();
	HashMap<String, DoubleValue> token2BigramVal = new HashMap<String, DoubleValue>();

	HashMap<String, DoubleValue> token2BackUnigramVal = new HashMap<String, DoubleValue>();
	HashMap<String, DoubleValue> token2BackBigramVal = new HashMap<String, DoubleValue>();

	/** Interpolate bigram prob using bigram and unigram model predictions */
	public double interpolatedProb(DoubleValue unigramVal1,
			DoubleValue unigramVal2, DoubleValue bigramVal1,
			DoubleValue bigramVal2) {
		double bigramProb = 0;
		// In bigram unknown then its prob is zero
		if (bigramVal1 != null)
			bigramProb = bigramVal1.getValue() * lambda3;
		if (bigramVal2 != null)
			bigramProb += bigramVal2.getValue() * lambda4;
		
		double unigramProb = 0;
		
		if(unigramVal1 != null)
			unigramProb = unigramVal1.getValue() * lambda3;
		if(unigramVal2 != null)
			unigramProb += unigramVal2.getValue() * lambda4;

		// Linearly combine weighted unigram and bigram probs
		return lambda1
				* unigramProb + lambda2 * bigramProb;
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
