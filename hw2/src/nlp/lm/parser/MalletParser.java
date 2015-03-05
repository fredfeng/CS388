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
								writer.println(part1 + " " + part2);
								
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

}
