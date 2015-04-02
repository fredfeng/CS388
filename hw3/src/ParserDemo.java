
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
   *
   */
	public static void main(String[] args) {
		commandOption.processOptions(args);
		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		Selection sel = Selection.PROB;
		op.setOptions("-goodPCFG", "-evals", "tsv");
		String initLoc = "/home/yufeng/courses/CS388/hw3/wsj/init/init.mrg";
		String buffLoc = "/home/yufeng/courses/CS388/hw3/wsj/init.msg";
		String trainLoc = "/home/yufeng/courses/CS388/hw3/wsj/0103.mrg";
		String testLoc = "/home/yufeng/courses/CS388/hw3/wsj/20.mrg";
		if (args.length > 0) {
			// from command line.
			String s = args[0];
			sel = Selection.valueOf(s);
		} else {

		}

		System.out.println("Selection function: ===============" + sel);
		Treebank initBank = makeTreebank(initLoc, op, null);
		Treebank trainBank = makeTreebank(trainLoc, op, null);
		Treebank testBank = makeTreebank(testLoc, op, null);

		int iteration = 20;
		// try length of the tree.
		LinkedList<Tree> goldTrees = new LinkedList<Tree>();
		for (Tree goldTree : trainBank) {
			goldTrees.add(goldTree);
		} // for tree iterator

		LinkedList<Tree> initTrees = new LinkedList<Tree>();
		for (Tree initTree : initBank) {
			initTrees.add(initTree);
		} // for tree iterator

		parser = LexicalizedParser.trainFromTreebank(initBank, null, op);
		while (iteration > 0) {
		    List<Tree> selList;
		    if(sel == Selection.RAN) {
		    	selList = pickNRandom(goldTrees, 60);
		    } else if(sel == Selection.LEN) {
		    	selList = pickLength(goldTrees, 60);
		    } else if(sel == Selection.PROB) {
		    	selList = pickProb(goldTrees, 60);
		    } else {
		    	assert sel == Selection.TREE;
		    	selList = pickEntropy(goldTrees, 60);
		    }
			// int i = 1;
			// for (Tree t : entropy) {
			// LexicalizedParserQuery lpq = parser.lexicalizedParserQuery();
			// double entro1;
			// lpq.parse(t.yieldWords());
			// List<ScoredObject<Tree>> trees1 = lpq.getKBestPCFGParses(10);
			// double sum = 0.0;
			// for (ScoredObject<Tree> so : trees1) {
			// sum += Math.exp(so.score());
			// }
			// Set<Double> set = new HashSet<Double>();
			// for (ScoredObject<Tree> so : trees1) {
			// double val = Math.exp(so.score()) / sum;
			// set.add(val);
			// }
			// entro1 = entropy(set, t.getLeaves().size());
			// i++;
			// }
			goldTrees.removeAll(selList);
			StringBuffer sb = new StringBuffer();
			initTrees.addAll(selList);
			for (Tree init : initTrees) {
				sb.append(init.pennString());
			}
			for (Tree ran : selList) {
				sb.append(ran.pennString());
			}
			// dump to file.
			try (PrintStream out = new PrintStream(
					new FileOutputStream(buffLoc))) {
				out.print(sb.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Treebank buffBank = makeTreebank(buffLoc, op, null);
			parser = LexicalizedParser.trainFromTreebank(buffBank, null, op);
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
	
	public static List<Tree> pickNRandom(List<Tree> lst, int n) {
	    List<Tree> copy = new LinkedList<Tree>(lst);
	    Collections.shuffle(copy);
	    return copy.subList(0, n);
	}
	
	public static List<Tree> pickLength(List<Tree> lst, int n) {
	    List<Tree> copy = new LinkedList<Tree>(lst);
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				return o2.getLeaves().size() - o1.getLeaves().size();
			}
		};
	    Collections.sort(copy, cmp);
	    return copy.subList(0, n);
	}
	
	/*Probability of the parse tree.*/
	public static List<Tree> pickProb(List<Tree> lst, int n) {
	    List<Tree> copy = new LinkedList<Tree>(lst);
	    HashMap<Tree, Double> map = new HashMap<Tree, Double>();
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				double s1, s2;
				if (map.containsKey(o1)) {
					s1 = map.get(o1);
				} else {
					Tree t1 = parser.apply(o1.yieldWords());
					s1 = t1.score();
					map.put(o1, s1);
				}

				if (map.containsKey(o2)) {
					s2 = map.get(o2);
				} else {
					Tree t2 = parser.apply(o2.yieldWords());
					s2 = t2.score();
					map.put(o2, s2);
				}
				double exp1 = Math.exp(s1);
				double exp2 = Math.exp(s2);
				int len1 = o1.getLeaves().size() - 1;
				int len2 = o2.getLeaves().size() - 1;
				double norm1, norm2;
				if (len1 == 0) {
					norm1 = exp1;
				} else {
					norm1 = Math.pow(exp1, (1.0 / len1));
				}
				if (len2 == 0) {
					norm2 = exp2;
				} else {
					norm2 = Math.pow(exp2, (1.0 / len2));
				}
				int diff = (norm1 - norm2) > 0 ? 1 : -1;
				return diff;
			}
		};
	    Collections.sort(copy, cmp);
	    return copy.subList(0, n);
	}
	
	public static List<Tree> pickEntropy(List<Tree> lst, int n) {
		List<Tree> copy = new LinkedList<Tree>(lst);
		Map<Tree, Double> cache = new HashMap<Tree, Double>();
		Comparator<Tree> cmp = new Comparator<Tree>() {
			@Override
			public int compare(Tree o1, Tree o2) {
				assert o1 != null;
				assert o2 != null;
				LexicalizedParserQuery lpq = parser.lexicalizedParserQuery();
				double entro1, entro2;
				if (cache.containsKey(o1)) {
					entro1 = cache.get(o1);
				} else {
					lpq.parse(o1.yieldWords());
					List<ScoredObject<Tree>> trees1 = lpq
							.getKBestPCFGParses(10);
					double sum = 0.0;
					for (ScoredObject<Tree> so : trees1) {
						sum += Math.exp(so.score());
					}
					Set<Double> set = new HashSet<Double>();
					for (ScoredObject<Tree> so : trees1) {
						double val = Math.exp(so.score()) / sum;
						set.add(val);
					}
					entro1 = entropy(set, o1.getLeaves().size());
					cache.put(o1, entro1);
				}
				if (cache.containsKey(o2)) {
					entro2 = cache.get(o2);
				} else {
					lpq.parse(o2.yieldWords());
					List<ScoredObject<Tree>> trees2 = lpq
							.getKBestPCFGParses(10);
					double sum = 0.0;
					for (ScoredObject<Tree> so : trees2) {
						sum += Math.exp(so.score());
					}
					Set<Double> set = new HashSet<Double>();
					for (ScoredObject<Tree> so : trees2) {
						double val = Math.exp(so.score()) / sum;
						set.add(val);
					}
					entro2 = entropy(set, o2.getLeaves().size());
					cache.put(o2, entro2);
				}
				int diff = (entro2 - entro1) > 0 ? 1 : -1;
				return diff;
			}
		};
		Collections.sort(copy, cmp);
		return copy.subList(0, n);
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
