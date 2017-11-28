

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.Map.Entry;


public class Preprocessor {

    private static Pattern roiterP = Pattern
            .compile("<REUTERS[.\\s\\D\\d]*?>([\\s\\d\\D.]*?[^</REUTERS>])</REUTERS>");
    private static Pattern topicP = Pattern
            .compile("<TOPICS><D>(([\\s\\d\\D]*)?)</D></TOPICS>");
    private static Pattern idP = Pattern.compile("NEWID=\"([\\d]*)\"");
    private static Pattern bodyP = Pattern
            .compile("<BODY[.\\s\\D\\d]*?>([\\s\\d\\D.]*?[^</BODY>])</BODY>");

    private static final String file = "/home/vivek/NetBeansProjects/JavaApplication5/reuters/";
    private static final String stopLL = "/home/vivek/NetBeansProjects/JavaApplication5/reuters/stoplist.txt";
    private static final String outputPath = "/home/vivek/NetBeansProjects/JavaApplication5/output/";

    private static Map<String, Integer> topicFrequency;
    private static Map<String, Integer> stringIdMap;
    private static int currentAllocId = 0;
    private static Set<String> stopList;

    public static void main(String[] args) {
        final File f = new File(file);
        final File[] roitersFiles = f.listFiles();
        final List<List<String>> roiterData = new ArrayList<>();
        topicFrequency = new HashMap<>();

        for (final File currentFile : roitersFiles) {
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

        final List<Article> articleLists = new ArrayList<>();

        for (int i = 0; i < roiterData.size(); i++) {
            final List<String> currentRoiters = roiterData.get(i);
            for (int j = 0; j < currentRoiters.size(); j++) {
                final String currentTag = currentRoiters.get(j);
                final String topic = getTopic(currentTag);
                if (topic != null) {
                    int id = getId(currentTag);
                    String body = getBody(currentTag);
                    articleLists.add(new Article(i, id, topic, body));
                    addFrequency(topic);
                }
            }
        }

        final Set<String> frequentArticles = get20(topicFrequency);
        topicFrequency.clear();

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
                art.body = getStemmed(art.body);
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
            if (art.body != null) {
                art.body = removeInfrequentWords(art.body);
            }
        }

        iter = articleLists.iterator();

        while (iter.hasNext()) {
            Article currArt = iter.next();
            if (currArt.body == null || currArt.body.isEmpty()) {
                iter.remove();
            }
        }

        //List<int[]> ijv = new ArrayList<>();
        List<String> ijvFileContent1 = new ArrayList<>();
        List<String> ijvFileContent2 = new ArrayList<>();
        List<String> ijvFileContent3 = new ArrayList<>();
        List<String> stringIdFile = new ArrayList<>();

        stringIdMap = new HashMap<>();
        for (Article currentArticle : articleLists) {
            int i = currentArticle.id;
            Map<String, Integer> words = new HashMap<>();
            st = new StringTokenizer(currentArticle.body);
            while (st.hasMoreTokens()) {
                String currentWord = st.nextToken();
                if (words.containsKey(currentWord)) {
                    words.put(currentWord, words.get(currentWord) + 1);
                } else {
                    words.put(currentWord, 1);
                }
            }
            double sum1 = 0, sum2 = 0, sum3 = 0;
            for (Entry<String, Integer> currentEntry : words.entrySet()) {
                int x = currentEntry.getValue();
                sum1 += x * x;
                double y = Math.sqrt(x) + 1;
                sum2 += y * y;
                double z = Math.log(x) / Math.log(2) + 1;
                sum3 += z * z;

            }
            sum1 = Math.sqrt(sum1);
            sum2 = Math.sqrt(sum2);
            sum3 = Math.sqrt(sum3);

            for (Entry<String, Integer> currentEntry : words.entrySet()) {
                String prefix = i + "," + getStringId(currentEntry.getKey()) + ",";
                double x = currentEntry.getValue();
                ijvFileContent1.add(prefix + x / sum1);
                ijvFileContent2.add(prefix + (Math.sqrt(x) + 1) / sum2);
                ijvFileContent3.add(prefix + ((Math.log(x) / Math.log(2)) + 1) / sum3);
            }
        }

        writeFile(outputPath + "ijvFile1", ijvFileContent1);
        writeFile(outputPath + "ijvFile2", ijvFileContent2);
        writeFile(outputPath + "ijvFile3", ijvFileContent3);
        for (Article a : articleLists) {
            stringIdFile.add(a.id + "," + a.topic);
        }
        writeFile(outputPath + "reuters.class", stringIdFile);

    }

    static void writeFile(String filePath, List<String> data) {
        File f = new File(filePath);
        BufferedWriter bw;

        try {
            bw = new BufferedWriter(new FileWriter(f));
            for (String x : data) {
                bw.write(x + "\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Error creating file");
        }

    }

    static int getStringId(String str) {
        if (!stringIdMap.containsKey(str)) {
            stringIdMap.put(str, currentAllocId);
            currentAllocId++;
        }
        return stringIdMap.get(str);
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
        if (s == null) {
            return "";
        }
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

    static String getStemmed(String s) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            String currentString = st.nextToken();
            Stemmer stem = new Stemmer();
            for (char c : currentString.toCharArray()) {
                stem.add(c);
            }
            stem.stem();
            sb.append(" " + stem.toString());
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

    static class Article {

        int docid;
        int id;
        String topic, body;

        Article(int docid, int id, String topic, String body) {
            this.docid = docid;
            this.id = id;
            this.topic = topic;
            this.body = body;
        }
    }
}
