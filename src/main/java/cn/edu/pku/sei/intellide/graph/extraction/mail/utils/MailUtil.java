package cn.edu.pku.sei.intellide.graph.extraction.mail.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class MailUtil {

    private static final String MAIL_REGEX = "[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+";
    private static final Pattern MAIL_PATTERN = Pattern.compile(MAIL_REGEX);

    private static final String SIGNATURE_LINE = "--";
    private static final String SIGNATURE_LINE2 = "__";

    private static final String REF_LINE = "wrote:";
    private static final String REF_TAG = ">";

    /*
     * 从包含用户邮件地址和用户名称的字符串中，提取出用户的邮件地址和用户名称。
     * 比如：
     * 		输入： "Scott Ganyo <scott.ganyo@eTapestry.com>"
     *  	输出： <"Scott Ganyo","scott.ganyo@eTapestry.com">  (用户名称和用户邮件地址的值对)
     * 使用场景： Mbox归档中，From和To字段为包含用户邮件地址和用户名称的字符串。
     */
    public static Pair<String, String> extractMailNameAndAddress(String mailAddressWithName) {
        if (mailAddressWithName == null) {
            return null;
        }

        mailAddressWithName = mailAddressWithName.replaceAll("<", "");
        mailAddressWithName = mailAddressWithName.replaceAll(">", "");

        Matcher matcher = MAIL_PATTERN.matcher(mailAddressWithName);
        if (!matcher.find()) {
            System.err.println("Warning: Fail to match an email! Content is:" + mailAddressWithName);
            return null;
        }

        String mail = matcher.group();
        //remove mail
        String name = mailAddressWithName.replaceAll(mail, "");

        //replace possible " or '
        name = name.replaceAll("\"", "");
        name = name.replaceAll("'", "");

        name = name.trim();

        //remove possible beginning '(' and ending ')'
        if (name.startsWith("(") && name.endsWith(")")) {
            name = name.substring(1, name.length() - 1);
        }

        name = name.trim();

        if (name.isEmpty()) {
            name = mail;
        }
        return Pair.of(name, mail);
    }

    /*
     * 从包含多个用户邮件地址和用户名称的字符串中，不同用户以","分割，提取出所有用户的邮件地址和用户名称。
     *
     * 比如：
     * 		输入: "java-user@lucene.apache.org" <java-user@lucene.apache.org>, Ahmet Arslan <iorixxx@yahoo.com>
     * 		输出：[<"java-user@lucene.apache.org","java-user@lucene.apache.org">,
     *           <"Ahmet Arslan","iorixxx@yahoo.com">]
     *
     */
    public static List<Pair<String, String>> extractMultiMailNameAndAddress(String multiMailAddressWithName) {
        if (multiMailAddressWithName == null) {
            return Collections.emptyList();
        }

        List<Pair<String, String>> userMailList = new ArrayList<>();
        List<String> mailAddressWithNameList = new ArrayList<>();

        int beginIndex = 0;
        int endIndex = 0;
        boolean dotInQuotation = false;
        int len = multiMailAddressWithName.length();
        while (endIndex < len) {
            char ch = multiMailAddressWithName.charAt(endIndex);
            if (ch == '"') {
                dotInQuotation = !dotInQuotation;
            } else if (ch == ',') {
                if (!dotInQuotation) {//引号外的逗号，用户切分处
                    String oneMailInfo = multiMailAddressWithName.substring(beginIndex, endIndex);
                    mailAddressWithNameList.add(oneMailInfo);
                    beginIndex = endIndex + 1;
                }
            }
            endIndex++;
        }

        if (beginIndex < len) {
            String oneMailInfo = multiMailAddressWithName.substring(beginIndex);
            mailAddressWithNameList.add(oneMailInfo);
        }

        for (String mailAddressWithName : mailAddressWithNameList) {
            Pair<String, String> mailPair = extractMailNameAndAddress(mailAddressWithName);
            if (mailPair != null) {
                userMailList.add(mailPair);
            }
        }

        return userMailList;
    }

    public static String extractMainText(String body) {
        BufferedReader in = new BufferedReader(new StringReader(body));
        ArrayList<String> lineList = new ArrayList<>();
        try {
            String line;
            while ((line = in.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<MailSegment> segmentList = new ArrayList<>();
        MailSegment seg;
        ArrayList<String> sentenceList = new ArrayList<>();
        boolean start = true;
        for (String line : lineList) {
            // 忽略段落开始的空行
            if (start && line.trim().isEmpty()) continue;
            // 一个段落开始
            if (start) {
                sentenceList = new ArrayList<>();
                start = false;
            }
            // 将一行话加入段落
            if (!line.trim().isEmpty()) {
                sentenceList.add(line);
                continue;
            }
            seg = new MailSegment(sentenceList);
            segmentList.add(seg);
            start = true;
            start = true;
        }
        filterReference(segmentList);
        filterSignature(segmentList);
        String r = "";
        for (MailSegment segment : segmentList)
            r += segment.getText() + "\r\n";
        return r;
    }

    private static void filterSignature(List<MailSegment> segments) {
        List<MailSegment> signatures = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            MailSegment segment = segments.get(i);
            List<String> sentences = segment.getSentences();
            if (isSignatureStart(sentences.get(0))) {
                signatures.add(segment);
                for (; i < segments.size(); i++) {
                    signatures.add(segments.get(i));
                }
                break;
            } else if ((segments.size() - i <= 2) && containsSignatureTag(segments.get(i))) {
                signatures.add(segment);
                for (; i < segments.size(); i++) {
                    signatures.add(segments.get(i));
                }
            }
        }
        segments.removeAll(signatures);
    }

    private static void filterReference(List<MailSegment> segments) {
        List<MailSegment> references = new ArrayList<>();
        for (MailSegment segment : segments) {
            if (isReference(segment)) references.add(segment);

        }
        segments.removeAll(references);
    }

    private static boolean isSignatureStart(String str) {
        return !(str == null || str.length() == 0) && (str.trim().toLowerCase().startsWith(SIGNATURE_LINE) || str.trim().startsWith(SIGNATURE_LINE2));
    }

    private static boolean containsSignatureTag(MailSegment segment) {
        List<String> sentences = segment.getSentences();
        for (String sentence : sentences) {
            if (sentence.trim().startsWith(SIGNATURE_LINE) || sentence.startsWith(SIGNATURE_LINE2)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReference(MailSegment segment) {
        boolean isRef = false;
        int totalLine;
        int arrowLine = 0;

        List<String> sentences = segment.getSentences();
        totalLine = sentences.size();

        for (int i = 0; i < totalLine; i++) {
            String line = sentences.get(i);
            if (i == 0 && line.toLowerCase().trim().endsWith(REF_LINE)) {
                isRef = true;
                break;
            }
            if (line.toLowerCase().trim().startsWith(REF_TAG)) {
                arrowLine++;
            }
        }

        if (arrowLine >= totalLine * 0.5) {
            isRef = true;
        }
        return isRef;
    }

}

class MailSegment {
    private boolean isCode;
    private List<String> sentences;

    public MailSegment(List<String> sentences) {
        this.sentences = sentences;
        this.isCode = false;
    }

    public Iterator<String> sentenceIterator() {
        return sentences.iterator();
    }

    public boolean isCode() {
        return isCode;
    }

    public void setCode(boolean code) {
        isCode = code;
    }

    public int getSentenceNumber() {
        return sentences.size();
    }

    public ImmutableList<String> getSentences() {
        return ImmutableList.copyOf(sentences);
    }

    public String getText() {
        return sentences.stream().collect(Collectors.joining("\n"));
    }

    public String getText(int start, int end) {
        return sentences.stream().skip(start).limit(end - start).collect(Collectors.joining("\n"));
    }
}