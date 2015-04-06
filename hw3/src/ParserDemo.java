
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.EvaluateTreebank;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.ScoredObject;
import edu.stanford.nlp.util.Timing;
import edu.utexas.nlp.util.CommandOption;

class ParserDemo {
	
	private static CommandOption commandOption = new CommandOption();

	private static LexicalizedParser parser;
	
	private static int K = 10;
	
	private static int wordsPerIter = 1500;
	
	private static int iteration = 20;
	
	public static enum Selection {
		RAN, LEN, PROB, TREE
	}

  /**
   * The main method demonstrates the easiest way to load a parser.
   * Simply call loadModel and specify the path of a serialized grammar
   * model, which can be a file, a resource on the classpath, or even a URL.
   * For example, this demonstrates loading a grammar from the models jar
   * file, which you therefore need to include on the classpath for ParserDemo
   * to work.
   *
   * Usage: {@code java ParserDemo [[model] textFile]}
   * e.g.: java ParserDemo edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz data/chinese-onesent-utf8.txt
 * @throws ParseException 
   *
   */
	public static void main(String[] args) throws ParseException {
		commandOption.processOptions(args);

		CommandLineParser cmdParser = new GnuParser();
		// create Options object
		org.apache.commons.cli.Options opts = new org.apache.commons.cli.Options();
		opts.addOption("trainBank", true, "train bank");
		opts.addOption("initBank", true, "init bank");
		opts.addOption("candidateBank", true, "candidate bank");
		opts.addOption("testBank", true, "candidate bank");
		opts.addOption("nextTrainBank", true, "nextTrain bank");
		opts.addOption("nextCandidatePool", true, "next candidate bank");
		opts.addOption("selectionFunction", true, "selection function");
		opts.addOption("K", true, "Top k parsers");
		opts.addOption("iteration", true, "Number of iterations");
		opts.addOption("wordsPerIter", true, "Words per iteration");
		CommandLine cmd = cmdParser.parse(opts, args);

		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		Selection sel = Selection.TREE;
		op.setOptions("-goodPCFG", "-evals", "tsv");

		String initLoc = "/home/yufeng/courses/CS388/hw3/wsj/init/init.mrg";
		String buffLoc = "/home/yufeng/courses/CS388/hw3/wsj/init.msg";
		String trainLoc = "/home/yufeng/courses/CS388/hw3/wsj/0103.mrg";
		String testLoc = "/home/yufeng/courses/CS388/hw3/wsj/20.mrg";
		
		if (cmd.hasOption("initBank"))
			initLoc = cmd.getOptionValue("initBank");
		if (cmd.hasOption("trainBank"))
			trainLoc = cmd.getOptionValue("trainBank");
		if (cmd.hasOption("testBank"))
			testLoc = cmd.getOptionValue("testBank");
		if (cmd.hasOption("candidateBank"))
			buffLoc = cmd.getOptionValue("candidateBank");
		
		if (cmd.hasOption("selectionFunction"))
			sel = Selection.valueOf(cmd.getOptionValue("selectionFunction"));
		if (cmd.hasOption("iteration"))
			iteration = Integer.parseInt(cmd.getOptionValue("iteration"));
		if (cmd.hasOption("wordsPerIter"))
			wordsPerIter = Integer.parseInt(cmd.getOptionValue("wordsPerIter"));
		if (cmd.hasOption("K"))
			K = Integer.parseInt(cmd.getOptionValue("K"));
		System.out.println("init bank: ------------" + initLoc);
		System.out.println("buff bank: ------------" + buffLoc);
		System.out.println("train bank: ------------" + trainLoc);
		System.out.println("test bank: ------------" + testLoc);
		System.out.println("selectionFunction: ----------------------" + sel);
		System.out.println("iteration: ------------------------------"
				+ iteration);
		System.out.println("wordsPerIter: ---------------------------"
				+ wordsPerIter);
		System.out.println("K value: --------------------------------" + K);

		Treebank initBank = makeTreebank(initLoc, op, null);
		Treebank trainBank = makeTreebank(trainLoc, op, null);
		Treebank testBank = makeTreebank(testLoc, op, null);

		// try length of the tree.
		LinkedList<Tree> goldTrees = new LinkedList<Tree>();
		for (Tree goldTree : trainBank) {
			goldTrees.add(goldTree);
		} // for tree iterator

		LinkedList<Tree> initTrees = new LinkedList<Tree>();
		for (Tree initTree : initBank) {
			initTrees.add(initTree);
		} // for tree iterator

		parser = LexicalizedParser.trainFromTreebank(initBank, op);

		if (sel == Selection.RAN) {
			sortRandom(goldTrees);
		} else if (sel == Selection.LEN) {
			sortLength(goldTrees);
		} else if (sel == Selection.PROB) {
			sortProb(goldTrees);
		} else {
			assert sel == Selection.TREE;
			sortEntropy(goldTrees);
		}

		while (iteration > 0) {
			List<Tree> selList = topN(goldTrees, wordsPerIter);
			List<Tree> copy = new LinkedList<Tree>(selList);
			goldTrees.removeAll(copy);
			StringBuffer sb = new StringBuffer();
			initTrees.addAll(copy);
			for (Tree init : initTrees) {
				sb.append(init.pennString());
			}
			// for (Tree ran : copy) {
			// sb.append(ran.pennString());
			// }
			// dump to file.
			try (PrintStream out = new PrintStream(
					new FileOutputStream(buffLoc))) {
				out.print(sb.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Treebank buffBank = makeTreebank(buffLoc, op, null);
			System.out.println("Current train bank:" + buffBank.size());
			System.out.println("Remain train bank:" + goldTrees.size());

			parser = LexicalizedParser.trainFromTreebank(buffBank, op);
			EvaluateTreebank evaluator = new EvaluateTreebank(parser);
			System.out.println("Begin iteration:-----------" + iteration);
			evaluator.testOnTreebank(testBank);
			System.out.println("End iteration:-----------" + iteration);
			iteration--;
		}

	}
	
	public static List<? extends HasWord> getInputSentence(Tree t) {
        ArrayList<? extends HasWord> s = t.taggedYield();
        for (HasWord word : s) {
          String tag = ((HasTag) word).tag();
          tag = tag.split("-")[0];
          ((HasTag) word).setTag(tag);
        }
        return Sentence.toCoreLabelList(s);
	}

  /**
   * demoDP demonstrates turning a file into tokens and then parse
   * trees.  Note that the trees are printed by calling pennPrint on
   * the Tree object.  It is also possible to pass a PrintWriter to
   * pennPrint if you want to capture the output.
   * This code will work with any supported language.
   */
  public static void demoDP(LexicalizedParser lp, String filename) {
    // This option shows loading, sentence-segmenting and tokenizing
    // a file using DocumentPreprocessor.
    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
    GrammaticalStructureFactory gsf = null;
    if (tlp.supportsGrammaticalStructures()) {
      gsf = tlp.grammaticalStructureFactory();
    }
    // You could also create a tokenizer here (as below) and pass it
    // to DocumentPreprocessor
    for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
      Tree parse = lp.apply(sentence);
      parse.pennPrint();
      System.out.println();

      if (gsf != null) {
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
        System.out.println();
      }
    }
  }

  /**
   * demoAPI demonstrates other ways of calling the parser with
   * already tokenized text, or in some cases, raw text that needs to
   * be tokenized as a single sentence.  Output is handled with a
   * TreePrint object.  Note that the options used when creating the
   * TreePrint can determine what results to print out.  Once again,
   * one can capture the output by passing a PrintWriter to
   * TreePrint.printTree. This code is for English.
   */
  public static void demoAPI(LexicalizedParser lp) {
    // This option shows parsing a list of correctly tokenized words
    String[] sent = { "This", "is", "an", "easy", "sentence", "." };
    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
    Tree parse = lp.apply(rawWords);
    parse.pennPrint();
    System.out.println();

    // This option shows loading and using an explicit tokenizer
    String sent2 = "This is another sentence.";
    TokenizerFactory<CoreLabel> tokenizerFactory =
        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    Tokenizer<CoreLabel> tok =
        tokenizerFactory.getTokenizer(new StringReader(sent2));
    List<CoreLabel> rawWords2 = tok.tokenize();
    parse = lp.apply(rawWords2);

    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
    System.out.println(tdl);
    System.out.println();

    // You can also use a TreePrint object to print trees and dependencies
    TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
    tp.printTree(parse);
  }

	private static Treebank makeTreebank(String treebankPath, Options op,
			FileFilter filt) {
		System.err.println("Training a parser from treebank dir: "
				+ treebankPath);
		Treebank trainTreebank = op.tlpParams.diskTreebank();
		System.err.print("Reading trees...");
		if (filt == null) {
			trainTreebank.loadPath(treebankPath);
		} else {
			trainTreebank.loadPath(treebankPath, filt);
		}

		Timing.tick("done [read " + trainTreebank.size() + " trees].");
		return trainTreebank;
	}
	
	public static void sortRandom(List<Tree> lst) {
	    Collections.shuffle(lst);
	}
	
	public static List<Tree> topN(List<Tree> lst, int n) {
		int cnt = 0;
		int sum = 0;
		for (Tree t : lst) {
			sum += t.yield().size();
			if (sum > n)
				break;

			cnt++;
		}
		return lst.subList(0, cnt);
	}
	
	public static void sortLength(List<Tree> lst) {
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				return o2.getLeaves().size() - o1.getLeaves().size();
			}
		};
	    Collections.sort(lst, cmp);
	}
	
	/*Probability of the parse tree.*/
	public static void sortProb(List<Tree> lst) {
		HashMap<Tree, Double> cache = new HashMap<Tree, Double>();
		for (Tree t : lst) {
			double s1, norm;
			Tree t1 = parser.apply(t.yieldWords());
			s1 = t1.score();
			double exp = Math.exp(s1);
			int len = t.yield().size() - 1;
			if (len == 0) {
				norm = exp;
			} else {
				norm = Math.pow(exp, (1.0 / len));
			}
			assert !Double.isNaN(norm);
			cache.put(t, norm);
		}
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				assert cache.containsKey(o1);
				assert cache.containsKey(o2);
				double norm1 = cache.get(o1);
				double norm2 = cache.get(o2);
				int diff = (norm1 - norm2) > 0 ? 1 : -1;
				if (norm1 == norm2)
					diff = 0;
				return diff;
			}
		};
		Collections.sort(lst, cmp);
	}
	
	public static void sortEntropy(List<Tree> lst) {
		Map<Tree, Double> cache = new HashMap<Tree, Double>();
		LexicalizedParserQuery lpq = parser.lexicalizedParserQuery();

		for (Tree t : lst) {
			double entrophy;
			lpq.parse(t.yieldWords());

			List<ScoredObject<Tree>> trees1 = lpq.getKBestPCFGParses(K);
			double sum = 0.0;
			for (ScoredObject<Tree> so : trees1) {
				sum += Math.exp(so.score());
			}
			Set<Double> set = new HashSet<Double>();
			for (ScoredObject<Tree> so : trees1) {
				double val = Math.exp(so.score()) / sum;
				set.add(val);
			}
			entrophy = entropy(set, t.getLeaves().size());
			cache.put(t, entrophy);
		}
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				assert cache.containsKey(o1);
				assert cache.containsKey(o2);
				double entro1 = cache.get(o1);
				double entro2 = cache.get(o2);
				assert !Double.isNaN(entro1) : o1.yieldWords();
				assert !Double.isNaN(entro2) : o2.yieldWords();
				int diff = (entro2 - entro1) > 0 ? 1 : -1;
				if (entro2 == entro1)
					diff = 0;
				return diff;
			}
		};
		Collections.sort(lst, cmp);
	}
	
	public static double logBase2(double x) {
		return Math.log(x)/Math.log(2);
	}
	
	public static double entropy(Set<Double> set, int len) {
		double sum = 0.0;
		for (double dd : set) {
			double log2 = logBase2(dd);
			double val = log2 * dd * (-1);
			sum += val;
		}
		return (sum / len);
	}

}
