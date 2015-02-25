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
		String src = "/home/yufeng/courses/CS388/hw1/pos/atis/atis3.pos";
		src = "/home/yufeng/courses/CS388/hw1/pos/wsj/00/wsj_0001.pos";
		String divider = "===========";
		String outLoc = "/home/yufeng/courses/CS388/hw1/train.txt";
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
