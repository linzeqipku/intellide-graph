package cn.edu.pku.sei.intellide.graph.extraction.qa;

import cn.edu.pku.sei.intellide.graph.extraction.KnowledgeExtractor;
import cn.edu.pku.sei.intellide.graph.extraction.qa.entity.AnswerInfo;
import cn.edu.pku.sei.intellide.graph.extraction.qa.entity.QaCommentInfo;
import cn.edu.pku.sei.intellide.graph.extraction.qa.entity.QaUserInfo;
import cn.edu.pku.sei.intellide.graph.extraction.qa.entity.QuestionInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从StackOverflow的XML文件数据中生成图数据
 *
 * @author Zeqi Lin
 */

public class StackOverflowExtractor extends KnowledgeExtractor {

    public static final Label QUESTION = Label.label("StackOverflowQuestion");
    public static final String QUESTION_ID = "questionId";
    public static final String QUESTION_CREATION_DATE = "creationDate";
    public static final String QUESTION_SCORE = "score";
    public static final String QUESTION_VIEW_COUNT = "viewCount";
    public static final String QUESTION_BODY = "body";
    public static final String QUESTION_OWNER_USER_ID = "ownerUserId";
    public static final String QUESTION_TITLE = "title";
    public static final String QUESTION_TAGS = "tags";

    public static final Label ANSWER = Label.label("StackOverflowAnswer");
    public static final String ANSWER_ACCEPTED = "accepted";
    public static final String ANSWER_ID = "answerId";
    public static final String ANSWER_PARENT_QUESTION_ID = "parentQuestionId";
    public static final String ANSWER_CREATION_DATE = "creationDate";
    public static final String ANSWER_SCORE = "score";
    public static final String ANSWER_BODY = "body";
    public static final String ANSWER_OWNER_USER_ID = "ownerUserId";

    public static final Label COMMENT = Label.label("StackOverflowComment");
    public static final String COMMENT_ID = "commentId";
    public static final String COMMENT_PARENT_ID = "parentId";
    public static final String COMMENT_SCORE = "score";
    public static final String COMMENT_TEXT = "text";
    public static final String COMMENT_CREATION_DATE = "creationDate";
    public static final String COMMENT_USER_ID = "userId";

    public static final Label USER = Label.label("StackOverflowUser");
    public static final String USER_ID = "user_id";
    public static final String USER_REPUTATION = "reputation";
    public static final String USER_CREATION_DATE = "creationDate";
    public static final String USER_DISPLAY_NAME = "displayName";
    public static final String USER_LAST_ACCESS_dATE = "lastAccessDate";
    public static final String USER_VIEWS = "views";
    public static final String USER_UP_VOTES = "upVotes";
    public static final String USER_DOWN_VOTES = "downVotes";

    public static final RelationshipType HAVE_ANSWER = RelationshipType.withName("haveSoAnswer");
    private static final RelationshipType HAVE_COMMENT = RelationshipType.withName("haveSoComment");
    private static final RelationshipType AUTHOR = RelationshipType.withName("soAuthor");
    private static final RelationshipType DUPLICATE = RelationshipType.withName("soDuplicate");

    private Map<Integer, QuestionInfo> questionMap = new HashMap<>();
    private Map<Integer, AnswerInfo> answerMap = new HashMap<>();
    private Map<Integer, QaCommentInfo> commentMap = new HashMap<>();
    private Map<Integer, QaUserInfo> userMap = new HashMap<>();
    private List<Pair<Integer, Integer>> duplicateLinkList = new ArrayList<>();

    private String questionXmlPath = null;
    private String answerXmlPath = null;
    private String commentXmlPath = null;
    private String userXmlPath = null;
    private String postLinkXmlPath = null;

    @Override
    public void extraction() {

        GraphDatabaseService db = this.getDb();

        this.questionXmlPath = this.getDataDir() + "/Questions.xml";
        this.answerXmlPath = this.getDataDir() + "/Answers.xml";
        this.commentXmlPath = this.getDataDir() + "/Comments.xml";
        this.userXmlPath = this.getDataDir() + "/Users.xml";
        this.postLinkXmlPath = this.getDataDir() + "/PostLinks.xml";

        SAXParser qParser = null;
        SAXParser aParser = null;
        SAXParser cParser = null;
        SAXParser uParser = null;
        SAXParser plParser = null;

        try {
            qParser = SAXParserFactory.newInstance().newSAXParser();
            aParser = SAXParserFactory.newInstance().newSAXParser();
            cParser = SAXParserFactory.newInstance().newSAXParser();
            uParser = SAXParserFactory.newInstance().newSAXParser();
            plParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        try (Transaction tx = db.beginTx()) {

            //生成图数据结点，并记录在questionMap, answerMap和commentMap中.

            QuestionHandler qHandler = new QuestionHandler(db, questionMap);
            AnswerHandler aHandler = new AnswerHandler(db, answerMap);
            QaCommentHandler cHandler = new QaCommentHandler(db, commentMap);
            QaUserHandler uHandler = new QaUserHandler(db, userMap);
            PostLinkHandler plHandler = new PostLinkHandler(duplicateLinkList);
            try {
                qParser.parse(new File(questionXmlPath), qHandler);
                aParser.parse(new File(answerXmlPath), aHandler);
                cParser.parse(new File(commentXmlPath), cHandler);
                uParser.parse(new File(userXmlPath), uHandler);
                plParser.parse(new File(postLinkXmlPath), plHandler);
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            tx.success();
        }

        try (Transaction tx = db.beginTx()) {

            //建立QA结点之间的关联关系

            for (AnswerInfo answerInfo : answerMap.values()) {
                Node answerNode = answerInfo.getNode();
                QuestionInfo questionInfo = questionMap.get(answerInfo.getParentQuestionId());
                if (questionInfo != null)
                    questionInfo.getNode().createRelationshipTo(answerNode, HAVE_ANSWER);
            }
            for (QaCommentInfo commentInfo : commentMap.values()) {
                Node commentNode = commentInfo.getNode();
                QuestionInfo questionInfo = questionMap.get(commentInfo.getParentId());
                if (questionInfo != null)
                    questionInfo.getNode().createRelationshipTo(commentNode, HAVE_COMMENT);
                AnswerInfo answerInfo = answerMap.get(commentInfo.getParentId());
                if (answerInfo != null)
                    answerInfo.getNode().createRelationshipTo(commentNode, HAVE_COMMENT);
            }

            //建立Question/Answer/Comment到User结点之间的关联关系
            for (QuestionInfo questionInfo : questionMap.values()) {
                int userId = questionInfo.getOwnerUserId();
                QaUserInfo userInfo = userMap.get(userId);
                if (userInfo != null) {
                    userInfo.getNode().createRelationshipTo(questionInfo.getNode(), AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Question(%d)\n",userInfo.getUserId(),questionInfo.getQuestionId());
                }
            }

            for (AnswerInfo answerInfo : answerMap.values()) {
                int userId = answerInfo.getOwnerUserId();
                QaUserInfo userInfo = userMap.get(userId);
                if (userInfo != null) {
                    userInfo.getNode().createRelationshipTo(answerInfo.getNode(), AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Answer(%d)\n",userInfo.getUserId(),answerInfo.getAnswerId());
                }
            }

            for (QaCommentInfo commentInfo : commentMap.values()) {
                int userId = commentInfo.getUserId();
                QaUserInfo userInfo = userMap.get(userId);
                if (userInfo != null) {
                    userInfo.getNode().createRelationshipTo(commentInfo.getNode(), AUTHOR);
//					System.out.printf("Create Author relationship from User(%d) --> Comment(%d)\n",userInfo.getUserId(),commentInfo.getCommentId());
                }
            }

            //建立Question之间的Duplicate关系, postId -[:DUPLICATE]-> relatedPostId
            for (Pair<Integer, Integer> dupLink : duplicateLinkList) {
                int postId = dupLink.getLeft();
                int relatedPostId = dupLink.getRight();

                QuestionInfo post = questionMap.get(postId);
                QuestionInfo relatedPost = questionMap.get(relatedPostId);
                if (post != null && relatedPost != null) {
                    post.getNode().createRelationshipTo(relatedPost.getNode(), DUPLICATE);
//					System.out.printf("Create Duplicate relationship from Question(%d) --> Question(%d)\n",post.getQuestionId(),relatedPost.getQuestionId());
                }
            }

            tx.success();
        }

        try (Transaction tx = db.beginTx()) {

            //标记Answer结点是否是被接受的结点

            for (QuestionInfo questionInfo : questionMap.values()) {
                int acceptedAnswerId = questionInfo.getAcceptedAnswerId();
                AnswerInfo answerInfo = answerMap.get(acceptedAnswerId);
                if (answerInfo != null)
                    answerInfo.setAccepted(true);
            }
            tx.success();
        }

    }

}

class QuestionHandler extends DefaultHandler {

    private GraphDatabaseService db = null;
    private Map<Integer, QuestionInfo> questionMap = null;

    public QuestionHandler(GraphDatabaseService db, Map<Integer, QuestionInfo> questionMap) {
        super();
        this.db = db;
        this.questionMap = questionMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!qName.equals("row"))
            return;
        int id = Integer.parseInt(attributes.getValue("Id"));
        String creationDate = attributes.getValue("CreationDate");
        int score = Integer.parseInt(attributes.getValue("Score"));
        int viewCount = Integer.parseInt(attributes.getValue("ViewCount"));
        String body = attributes.getValue("Body");
        String ownerUserIdString = attributes.getValue("OwnerUserId");
        if (ownerUserIdString == null)
            ownerUserIdString = "-1";
        int ownerUserId = Integer.parseInt(ownerUserIdString);
        String title = attributes.getValue("Title");
        String tags = attributes.getValue("Tags");
        String acceptedAnswerIdString = attributes.getValue("AcceptedAnswerId");
        if (acceptedAnswerIdString == null)
            acceptedAnswerIdString = "-1";
        int acceptedAnswerId = Integer.parseInt(acceptedAnswerIdString);

        Node node = db.createNode();
        QuestionInfo questionInfo = new QuestionInfo(node, id, creationDate, score, viewCount, body, ownerUserId, title, tags, acceptedAnswerId);
        questionMap.put(id, questionInfo);
    }

}

class AnswerHandler extends DefaultHandler {

    private GraphDatabaseService db = null;
    private Map<Integer, AnswerInfo> answerMap = null;

    public AnswerHandler(GraphDatabaseService db, Map<Integer, AnswerInfo> answerMap) {
        super();
        this.db = db;
        this.answerMap = answerMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!qName.equals("row"))
            return;
        int id = Integer.parseInt(attributes.getValue("Id"));
        int parentId = Integer.parseInt(attributes.getValue("ParentId"));
        String creationDate = attributes.getValue("CreationDate");
        int score = Integer.parseInt(attributes.getValue("Score"));
        String body = attributes.getValue("Body");
        String ownerUserIdString = attributes.getValue("OwnerUserId");
        if (ownerUserIdString == null)
            ownerUserIdString = "-1";
        int ownerUserId = Integer.parseInt(ownerUserIdString);

        Node node = db.createNode();
        AnswerInfo answerInfo = new AnswerInfo(node, id, parentId, creationDate, score, body, ownerUserId);
        answerMap.put(id, answerInfo);
    }

}

class QaCommentHandler extends DefaultHandler {

    private GraphDatabaseService db = null;
    private Map<Integer, QaCommentInfo> commentMap = null;

    public QaCommentHandler(GraphDatabaseService db, Map<Integer, QaCommentInfo> commentMap) {
        super();
        this.db = db;
        this.commentMap = commentMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!qName.equals("row"))
            return;
        int id = Integer.parseInt(attributes.getValue("Id"));
        int postId = Integer.parseInt(attributes.getValue("PostId"));
        int score = Integer.parseInt(attributes.getValue("Score"));
        String text = attributes.getValue("Text");
        String creationDate = attributes.getValue("CreationDate");
        String userIdString = attributes.getValue("UserId");
        if (userIdString == null)
            userIdString = "-1";
        int userId = Integer.parseInt(userIdString);

        Node node = db.createNode();
        QaCommentInfo commentInfo = new QaCommentInfo(node, id, postId, score, text, creationDate, userId);
        commentMap.put(id, commentInfo);
    }

}


class QaUserHandler extends DefaultHandler {
    private GraphDatabaseService db = null;
    private Map<Integer, QaUserInfo> userMap = null;

    public QaUserHandler(GraphDatabaseService db, Map<Integer, QaUserInfo> userMap) {
        super();
        this.db = db;
        this.userMap = userMap;
    }

    /*
     * 对于以"<row" 开头的记录，即User记录，创建User 结点，并将(userId,schema)放置userMap；
     * 对于其它记录，忽略该记录。
     *
     * 一条User记录样例如下：
     * 	 <row Id="-1" Reputation="1" CreationDate="2015-10-26T21:36:24.767" DisplayName="Comunidad"
     *        LastAccessDate="2015-10-26T21:36:24.767" WebsiteUrl="" Location="en la granja de servidores"
     *        AboutMe="about me" Views="0" UpVotes="0" DownVotes="106" Age="16" AccountId="-1" />
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!qName.equals("row"))
            return;

        int id = Integer.parseInt(attributes.getValue("Id"));
        int reputation = Integer.parseInt(attributes.getValue("Reputation"));
        String creationDate = attributes.getValue("CreationDate");
        String displayName = attributes.getValue("DisplayName");
        String lastAccessDate = attributes.getValue("LastAccessDate");
        int views = Integer.parseInt(attributes.getValue("Views"));
        int upVotes = Integer.parseInt(attributes.getValue("UpVotes"));
        int downVotes = Integer.parseInt(attributes.getValue("DownVotes"));

        Node node = db.createNode();
        QaUserInfo userInfo = new QaUserInfo(node, id, reputation, creationDate, displayName, lastAccessDate, views, upVotes, downVotes);
        userMap.put(id, userInfo);
    }
}

class PostLinkHandler extends DefaultHandler {
    private List<Pair<Integer, Integer>> dupLinkList = null;

    public PostLinkHandler(List<Pair<Integer, Integer>> dupLinkList) {
        super();
        this.dupLinkList = dupLinkList;
    }

    /*
     * 对于以"<row" 开头的记录，即PostLink记录，将(postId,relatedPostId)放置dupLinkList；
     * 对于其它记录，忽略该记录。
     *
     * 一条PostLink样例如下：
     * 		<row Id="45" CreationDate="2015-11-23T20:08:24.160" PostId="68" RelatedPostId="8" LinkTypeId="3" />
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!qName.equals("row"))
            return;

        int postId = Integer.parseInt(attributes.getValue("PostId"));
        int relatedPostId = Integer.parseInt(attributes.getValue("RelatedPostId"));

        dupLinkList.add(Pair.of(postId, relatedPostId));
//		System.out.printf("Duplicate from %d-->%d.\n",postId,relatedPostId);
    }


}