package nlp.lm.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * adding extra features to the data.
 * yufeng@cs.utexas.edu
 */
public class FeatureParser {
	
	public static String[] features = {"ing", "s"};

	public static void main(String[] args) throws IOException {
		System.out.println(args.length + " " + args[0]);
		if(args.length != 2) {
			System.out.println("java -cp bin/ nlp.lm.parser.FeatureParser src tgt");
			System.exit(0);
		}
		
		String src = args[0];
		String divider = "===========";
		String outLoc = args[1];
		PrintWriter writer;
		writer = new PrintWriter(outLoc, "UTF-8");
		
		File folder = new File(src);
		File[] listOfFiles = folder.listFiles();
		Arrays.sort(listOfFiles);

		for (int i = 0; i < listOfFiles.length; i++) {
			File posFile = listOfFiles[i];
			if (posFile.isFile()) {
				System.out.println("File " + posFile.getAbsolutePath());
				
				FileInputStream fis = new FileInputStream(posFile);

				// Construct BufferedReader from InputStreamReader
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line = null;
				boolean preEmpty = true;

				while ((line = br.readLine()) != null) {
					if (line.matches(".*@.*-.*/CD.*"))
						// @8k1011sx-a-14/CD
						continue;
					else if (line.trim().isEmpty())
						continue;
					else if (line.contains(divider)) {
						if (preEmpty)
							continue;
						else {
							preEmpty = true;
							// dump empty line here.
							writer.println();
						}
					} else {
						preEmpty = false;
						for (String str : Arrays.asList(line.split("\\s+"))) {
							if (str.contains("/")) {
								int idx = str.lastIndexOf('/');
								assert idx > 0;
								String part1 = str.substring(0, idx);
								String part2 = str.substring(idx+1, str.length());
								// dump the actual content.
								String content = part1;
								
								for(int j = 0; j < features.length; j++) {
									if(part1.endsWith(features[j]))
										content += " " + features[j];
								}
								
								//Caps?
								if(Character.isUpperCase(part1.charAt(0))) {
									content += " " + "caps";
								}
								
								content += (" " + part2);
								writer.println(content);
								
								if(str.equals("./.")) {
									writer.println();
									preEmpty = true;
								}
							}
						}

					}
				}
				br.close();
				if(!preEmpty)
					writer.println();
			}
		}
		writer.close();
	
	}
		/*
		http://grammar.about.com/od/words/a/comsuffixes.htm
		Noun Suffixes
		Suffix	Meaning	Example
		-acy	state or quality	privacy
		-al	act or process of	refusal
		-ance, -ence	state or quality of	maintenance, eminence
		-dom	place or state of being	freedom, kingdom
		-er, -or	one who	trainer, protector
		-ism	doctrine, belief	communism
		-ist	one who	chemist
		-ity, -ty	quality of	veracity
		-ment	condition of	argument
		-ness	state of being	heaviness
		-ship	position held	fellowship
		-sion, -tion	state of being	concession, transition

		Verb Suffixes
		-ate	become	eradicate
		-en	become	enlighten
		-ify, -fy	make or become	terrify
		-ize, -ise	become	civilize

		Adjective Suffixes
		-able, -ible	capable of being	edible, presentable
		-al	pertaining to	regional
		-esque	reminiscent of	picturesque
		-ful	notable for	fanciful
		-ic, -ical	pertaining to	musical, mythic
		-ious, -ous	characterized by	nutritious, portentous
		-ish	having the quality of	fiendish
		-ive	having the nature of	creative
		-less	without	endless
		-y	characterized by	sleazy
		*/
}
