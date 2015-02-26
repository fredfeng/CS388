package nlp.lm.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

public class MalletParser {

	public static void main(String[] args) throws IOException {
		System.out.println(args.length + " " + args[0]);
		if(args.length != 2) {
			System.out.println("java -cp bin/ nlp.lm.parser.MalletParser src tgt");
			System.exit(0);
		}
			
		String src = args[0];
		String divider = "===========";
		String outLoc = args[1];
		File in = new File(src);
		FileInputStream fis = new FileInputStream(in);

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		boolean preEmpty = true;
		PrintWriter writer;
		writer = new PrintWriter(outLoc, "UTF-8");
		
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
						String[] words = str.trim().split("/");
						assert words.length == 2 : line;
						// dump the actual content.
						writer.println(words[0] + " " + words[1]);
					}
				}

			}
		}
		br.close();
		writer.close();
	}

}
