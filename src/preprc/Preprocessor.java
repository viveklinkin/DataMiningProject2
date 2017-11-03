package preprc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.FREE_MEM;

public class Preprocessor {

	static Pattern roiterP = Pattern
			.compile("<REUTERS[.\\s\\D\\d]*?>([\\s\\d\\D.]*?[^</REUTERS>])</REUTERS>");
	static Pattern topicP = Pattern
			.compile("<TOPICS><D>(([\\s\\d\\D]*)?)</D></TOPICS>");
	static Pattern idP = Pattern.compile("NEWID=\"([\\d]*)\"");
	static Pattern bodyP = Pattern
			.compile("<BODY[.\\s\\D\\d]*?>([\\s\\d\\D.]*?[^</BODY>])</BODY>");

	static String file = "/home/grad06/vaidy083/Documents/reuters";
	static String stopLL = "/home/grad06/vaidy083/Downloads/stoplist.txt";

	static String test = "<REUTERS TOPICS=\"YES\" LEWISSPLIT=\"TRAIN\" CGISPLIT=\"TRAINING-SET\" "
			+ "OLDID=\"5553\" NEWID=\"10\">\n\n\n\n<BODY>\n\n\nshgfsdyf\n</BODY><DATE>26-FEB-1987 15:18:06.67</DATE>\n</REUTERS>"
			+ "<REUTERS>vivekis</REUTERS><REUTERS asdfgsdfhgsd>jsd</REUTERS>";

	static String testID = "<><><asdgasduiyadsfsfg<REUTERS NEWID=\" 1234 \">\"\"";
	static Map<String, Integer> topicFrequency;
	static Set<String> stopList;

	public static void main(String[] args) {
		// System.out.print(removeNums(testID));
		// System.exit(0);
		File f = new File(file);
		List<List<String>> roiterData = new ArrayList<>();
		File[] roitersFiles = f.listFiles();
		topicFrequency = new HashMap<>();

		for (File currentFile : roitersFiles) {
			if (currentFile.getName().contains(".sgm")) {
				List<String> currentRoitersTags = new ArrayList<>();
				String fileContents = readFile(currentFile);
				Matcher matcher = roiterP.matcher(fileContents);
				while (matcher.find()) {
					currentRoitersTags.add(matcher.group());
				}
				roiterData.add(currentRoitersTags);
			}
		}

		List<Article> articleLists = new ArrayList<>();

		for (int i = 0; i < roiterData.size(); i++) {
			List<String> currentRoiters = roiterData.get(i);
			for (int j = 0; j < currentRoiters.size(); j++) {
				String currentTag = currentRoiters.get(j);
				String topic = getTopic(currentTag);
				if (topic != null) {
					int id = getId(currentTag);
					String body = getBody(currentTag);
					articleLists.add(new Article(i, id, topic, body));
					addFrequency(topic);
				}
			}
		}

		Set<String> frequentArticles = get20(topicFrequency);
		topicFrequency.clear();
		System.out.println(frequentArticles);
		Iterator<Article> iter = articleLists.iterator();
		while (iter.hasNext()) {
			if (!frequentArticles.contains(iter.next().topic)) {
				iter.remove();
			}
		}
		stopList = new HashSet<>();
		String sl = readFile(new File(stopLL));
		StringTokenizer st = new StringTokenizer(sl);
		while (st.hasMoreTokens()) {
			stopList.add(st.nextToken());
		}
		for (Article art : articleLists) {
			if (art.body != null) {
				art.body = removeNASCII(art.body);
				art.body = art.body.toLowerCase();
				art.body = art.body.replaceAll("[^a-z0-9]", " ");
				art.body = removeNums(art.body);
				art.body = removeStop(art.body);
				st = new StringTokenizer(art.body);
				while (st.hasMoreTokens()) {
					addFrequency(st.nextToken());
				}
			}
		}
		int cc = 0;
		for (Entry<String, Integer> currE : topicFrequency.entrySet()) {
			if (currE.getValue() >= 5) {
				cc++;
			}
		}
		System.out.println(cc);
		for (Article art : articleLists) {
			if (art.body != null)
				art.body = removeInfrequentWords(art.body);
		}
	}

	static String removeInfrequentWords(String s) {
		StringTokenizer st = new StringTokenizer(s);
		StringBuilder sb = new StringBuilder();
		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			if (topicFrequency.get(str) >= 5) {
				sb.append(" " + str);
			}
		}
		return sb.toString();
	}

	public static String readFile(File f) {
		StringBuilder res = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					res.append("\n" + line);
				}
			}

			br.close();

		} catch (IOException e) {
			System.err.println("Error reading file");
		}
		return res.toString();
	}

	static String getTopic(String tagString) {
		Matcher matcher = topicP.matcher(tagString);
		if (matcher.find()) {
			String s = matcher.group(1);
			if (!s.contains("<D>") && !s.isEmpty()) {
				return s;
			}
			return null;
		}
		return null;
	}

	static int getId(String s) {
		Matcher matcher = idP.matcher(s);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return -1;
	}

	static String getBody(String s) {
		Matcher matcher = bodyP.matcher(s);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	static void addFrequency(String s) {
		if (topicFrequency.get(s) == null) {
			topicFrequency.put(s, 1);
		} else {
			int fre = topicFrequency.get(s);
			fre++;
			topicFrequency.put(s, fre);
		}
	}

	static Set<String> get20(Map<String, Integer> s) {
		Set<String> output = new HashSet<>();
		for (int i = 0; i < 20; i++) {
			int max = 0;
			String val = "";
			for (Entry<String, Integer> entr : s.entrySet()) {
				if (entr.getValue() > max) {
					val = entr.getKey();
					max = entr.getValue();
				}
			}
			output.add(val);
			s.remove(val);
		}
		return output;
	}

	static String removeNASCII(String s) {
		if (s == null)
			return "";
		if (s.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		char[] ch = s.toCharArray();
		for (char currch : ch) {
			if ((int) currch < 128) {
				sb.append(currch);
			}
		}
		return sb.toString();
	}

	static String removeNums(String s) {
		if (s.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String myStr = st.nextToken();
			if (!myStr.matches("\\d+")) {
				sb.append(" " + myStr);
			}
		}
		return sb.toString();
	}

	static String removeStop(String s) {
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (!stopList.contains(token)) {
				sb.append(" " + token);
			}
		}
		return sb.toString();
	}
}
