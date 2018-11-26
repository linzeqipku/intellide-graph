package cn.edu.pku.sei.intellide.graph.extraction.qa;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linzeqi on 2017/5/9.
 */
class StackOverflowDumpExtractionUtils {

    public static void main(String[] args) {
        extract("jfreechart", "E:/stackoverflow",
                "E:/stackoverflow/jfreechart/Questions.xml",
                "E:/stackoverflow/jfreechart/Answers.xml",
                "E:/stackoverflow/jfreechart/Comments.xml",
                "E:/stackoverflow/jfreechart/Users.xml",
                "E:/stackoverflow/jfreechart/PostLinks.xml");

    }

    private static void extract(String projectName, String remoteQaPath, String qPath, String aPath, String cPath, String uPath, String plPath) {
        QuestionExtractor qExtractor = new QuestionExtractor(projectName);
        String postXmlPath = remoteQaPath + "/Posts.xml";
        qExtractor.extractQuestionXmlFile(postXmlPath, qPath);
        AnswerExtractor aExtractor = new AnswerExtractor(qExtractor.questionIdSet);
        aExtractor.extractAnswerXmlFile(postXmlPath, aPath);
        QaCommentExtractor cExtractor = new QaCommentExtractor(qExtractor.questionIdSet, aExtractor.answerIdSet);
        String commentXmlPath = remoteQaPath + "/Comments.xml";
        cExtractor.extractQaCommentXmlFile(commentXmlPath, cPath);

        Set<Integer> allUserIdSet = new HashSet<>();
        allUserIdSet.addAll(qExtractor.userIdSet);
        allUserIdSet.addAll(aExtractor.userIdSet);
        allUserIdSet.addAll(cExtractor.userIdSet);
        QaUserExtractor uExtractor = new QaUserExtractor(allUserIdSet);
        String userXmlPath = remoteQaPath + "/Users.xml";
        uExtractor.extractQuestionXmlFile(userXmlPath, uPath);

        PostLinkExtractor plExtractor = new PostLinkExtractor(qExtractor.questionIdSet);
        String postLinksXmlPath = remoteQaPath + "/PostLinks.xml";
        plExtractor.extractQuestionXmlFile(postLinksXmlPath, plPath);
    }

}

class QuestionExtractor {

    private static Pattern idRe = Pattern.compile("id=\"(\\d+)\"");
    private static Pattern userIdRe = Pattern.compile("owneruserid=\"(\\d+)\"");
    Set<Integer> questionIdSet = new HashSet<>();
    Set<Integer> userIdSet = new HashSet<>();
    private Pattern tagsRe = null;
    private String projectName = "";

    public QuestionExtractor(String projectName) {
        this.projectName = projectName.toLowerCase();
        tagsRe = Pattern.compile("tags=\"[^\"]*" + projectName);
    }

    public void extractQuestionXmlFile(String postXmlPath, String dstXmlPath) {

        BufferedReader br = null;
        FileWriter bw = null;
        try {
            //System.out.println(postXmlPath);
            br = new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(postXmlPath)))));
            bw = new FileWriter(dstXmlPath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String str = null;
        int i = 0;
        try {
            bw.write("<questions>\r\n");
            while ((str = br.readLine()) != null) {
                i++;
                boolean flag = tagsRe.matcher(str.toLowerCase()).find() && str.contains("PostTypeId=\"1\"");
                if (flag) {
                    bw.write(str + "\r\n");
                    Matcher matcher = idRe.matcher(str.toLowerCase());
                    if (!matcher.find())
                        continue;
                    int id = Integer.parseInt(matcher.group(1));
                    questionIdSet.add(id);

                    matcher = userIdRe.matcher(str.toLowerCase());
                    if (!matcher.find()) {
                        continue;
                    }
                    int userId = Integer.parseInt(matcher.group(1));
                    userIdSet.add(userId);
                }
            }
            bw.write("</questions>");
            br.close();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

class QaUserExtractor {
    private static final Pattern userIdRe = Pattern.compile("id=\"(\\d+)\"");

    private Set<Integer> userIdSet = null;

    public QaUserExtractor(Set<Integer> userIdSet) {
        this.userIdSet = userIdSet;
    }

    public void extractQuestionXmlFile(String postXmlPath, String dstXmlPath) {
        BufferedReader br = null;
        FileWriter bw = null;
        try {
            br = new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(postXmlPath)))));
            bw = new FileWriter(dstXmlPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = null;
        int i = 0;
        try {
            bw.write("<users>\r\n");
            while ((str = br.readLine()) != null) {
                Matcher matcher = userIdRe.matcher(str.toLowerCase());
                if (!matcher.find()) {
                    continue;
                }

                int userId = Integer.parseInt(matcher.group(1));
                if (userIdSet.contains(userId)) {
                    bw.write(str + "\r\n");
                    //System.out.printf("绗�%d琛�: 鐢ㄦ埛%d;\n", i, userId);
                }
                i++;
            }
            bw.write("</users>");
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class QaCommentExtractor {

    private static Pattern postIdRe = Pattern.compile("postid=\"(\\d+)\"");
    private static Pattern userIdRe = Pattern.compile("userid=\"(\\d+)\"");
    Set<Integer> userIdSet = new HashSet<>();
    private Set<Integer> questionIdSet = new HashSet<>();
    private Set<Integer> answerIdSet = new HashSet<>();

    public QaCommentExtractor(Set<Integer> questionIdSet, Set<Integer> answerIdSet) {
        this.questionIdSet = questionIdSet;
        this.answerIdSet = answerIdSet;
    }

    public void extractQaCommentXmlFile(String commentXmlPath, String dstXmlPath) {

        BufferedReader br = null;
        FileWriter bw = null;
        try {
            br = new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(commentXmlPath)))));
            bw = new FileWriter(dstXmlPath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String str = null;
        int i = 0;
        try {
            bw.write("<comments>\r\n");
            while ((str = br.readLine()) != null) {
                i++;
                Matcher matcher = postIdRe.matcher(str.toLowerCase());
                if (!matcher.find())
                    continue;
                int postId = Integer.parseInt(matcher.group(1));
                if (questionIdSet.contains(postId) || answerIdSet.contains(postId)) {
                    bw.write(str + "\r\n");

                    matcher = userIdRe.matcher(str.toLowerCase());
                    if (!matcher.find()) {
                        continue;
                    }
                    int userId = Integer.parseInt(matcher.group(1));
                    userIdSet.add(userId);
                }
            }
            bw.write("</comments>");
            br.close();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class AnswerExtractor {

    private static Pattern parentIdRe = Pattern.compile("parentid=\"(\\d+)\"");
    private static Pattern idRe = Pattern.compile("id=\"(\\d+)\"");
    private static Pattern userIdRe = Pattern.compile("owneruserid=\"(\\d+)\"");
    Set<Integer> answerIdSet = new HashSet<>();
    Set<Integer> userIdSet = new HashSet<>();
    private Set<Integer> questionIdSet = new HashSet<>();

    public AnswerExtractor(Set<Integer> questionIdSet) {
        this.questionIdSet = questionIdSet;
    }

    public void extractAnswerXmlFile(String postXmlPath, String dstXmlPath) {

        BufferedReader br = null;
        FileWriter bw = null;
        try {
            br = new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(postXmlPath)))));
            bw = new FileWriter(dstXmlPath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int i = 0;
        String str = null;
        try {
            bw.write("<answers>\r\n");
            while ((str = br.readLine()) != null) {
                i++;
                if (str.contains("PostTypeId=\"2\"")) {
                    Matcher matcher = parentIdRe.matcher(str.toLowerCase());
                    if (!matcher.find())
                        continue;
                    int parentId = Integer.parseInt(matcher.group(1));
                    if (questionIdSet.contains(parentId)) {
                        bw.write(str + "\r\n");
                        matcher = idRe.matcher(str.toLowerCase());
                        if (!matcher.find())
                            continue;
                        int id = Integer.parseInt(matcher.group(1));
                        answerIdSet.add(id);
                        //System.out.println("ç¬¬" + i + "è¡Œ: é—®é¢˜" + parentId + "çš„ç­”æ¡ˆ" + id + "ï¼›");

                        matcher = userIdRe.matcher(str.toLowerCase());
                        if (!matcher.find()) {
                            continue;
                        }
                        int userId = Integer.parseInt(matcher.group(1));
                        userIdSet.add(userId);
                        //System.out.println("ç¬¬" + i + "è¡Œ: ç­”æ¡ˆ" + id + "çš„ç”¨æˆ·" + userId + "ï¼›");
                    }
                }
            }
            bw.write("</answers>");
            br.close();
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class PostLinkExtractor {
    private static final Pattern postIdRe = Pattern.compile("postid=\"(\\d+)\"");
    private static final Pattern relatedPostIdRe = Pattern.compile("relatedpostid=\"(\\d+)\"");

    private Set<Integer> questionIdSet = null;

    public PostLinkExtractor(Set<Integer> questionIdSet) {
        this.questionIdSet = questionIdSet;
    }

    public void extractQuestionXmlFile(String postXmlPath, String dstXmlPath) {
        BufferedReader br = null;
        FileWriter bw = null;
        try {
            br = new BufferedReader(new BufferedReader(new InputStreamReader(new FileInputStream(new File(postXmlPath)))));
            bw = new FileWriter(dstXmlPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = null;
        int i = 0;
        try {
            bw.write("<PostLinks>\r\n");
            while ((str = br.readLine()) != null) {
                //涓€鏉uplicate璁板綍
                if (str.contains("LinkTypeId=\"3\"")) {
                    Matcher matcher = postIdRe.matcher(str.toLowerCase());
                    if (!matcher.find()) {
                        continue;
                    }
                    int postId = Integer.parseInt(matcher.group(1));
                    if (!questionIdSet.contains(postId)) {
                        continue;
                    }

                    matcher = relatedPostIdRe.matcher(str.toLowerCase());
                    if (!matcher.find()) {
                        continue;
                    }
                    int relatedPostId = Integer.parseInt(matcher.group(1));
                    if (!questionIdSet.contains(relatedPostId)) {
                        continue;
                    }

                    bw.write(str + "\r\n");
                    //System.out.printf("绗�%d琛�: Duplicate 鍏崇郴: postId=%d --> relatedPostId=%d;\n", i, postId, relatedPostId);
                }
                i++;
            }
            bw.write("</PostLinks>");
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}