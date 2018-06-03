package cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.utils.CharBufferWrapper;
import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.utils.MboxHandler;
import cn.edu.pku.sei.intellide.graph.extraction.mail_to_neo4j.utils.MboxIterator;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.MimeConfig;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


public class MailGraphBuilder {


    public static final Label MAIL = Label.label("Mail");

    public static final String MAIL_ID = "mailId";

    public static final String MAIL_SUBJECT = "subject";

    public static final String MAIL_SENDER_NAME = "senderName";

    public static final String MAIL_SENDER_MAIL = "senderMail";

    public static final String MAIL_RECEIVER_NAMES = "receiverNames";

    public static final String MAIL_RECEIVER_MAILS = "receiverMails";

    public static final String MAIL_DATE = "date";

    public static final String MAIL_BODY = "body";
    //@PropertyDeclaration(parent = MAIL)
    //public static final String MAIL_MAIN_TEXT = "mainText";


    public static final Label MAILUSER = Label.label("MailUser");

    public static final String MAILUSER_NAMES = "names";

    public static final String MAILUSER_MAIL = "mail";


    private static final RelationshipType MAIL_IN_REPLY_TO = RelationshipType.withName("mailInReplyTo");

    public static final RelationshipType MAIL_SENDER = RelationshipType.withName("mailSender");

    public static final RelationshipType MAIL_RECEIVER = RelationshipType.withName("mailReceiver");

    private String mboxPath = null;
    private GraphDatabaseService db = null;
    private MimeStreamParser parser = null;
    private static Charset charset = Charset.forName("UTF-8");
    private final static CharsetDecoder DECODER = charset.newDecoder();


    public static void process(String graphPath, String mailFolderPath) {
        new MailGraphBuilder().run(graphPath, mailFolderPath);
        System.out.println("mail ok !");
    }

    public void run(String graphPath, String mailFolderPath) {
        this.db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));
        MboxHandler myHandler = new MboxHandler();
        myHandler.setDb(db);
        MimeConfig config=new MimeConfig();
        config.setMaxLineLen(-1);
        parser = new MimeStreamParser(config);
        parser.setContentHandler(myHandler);
        parse(new File(mailFolderPath));
        try (Transaction tx = db.beginTx()) {
            for (String address : myHandler.getMailUserNameMap().keySet()) {
                Node node = myHandler.getMailUserMap().get(address);
                node.setProperty(MAILUSER_NAMES, String.join(", ", myHandler.getMailUserNameMap().get(address)));
            }
            tx.success();
        }
        try (Transaction tx = db.beginTx()) {
            for (String mailId : myHandler.getMailReplyMap().keySet()) {
                Node mailNode = myHandler.getMailMap().get(mailId);
                Node replyNode = myHandler.getMailMap().get(myHandler.getMailReplyMap().get(mailId));
                if (mailNode != null & replyNode != null)
                    mailNode.createRelationshipTo(replyNode, MAIL_IN_REPLY_TO);
            }
            tx.success();
        }
        db.shutdown();
    }

    private void parse(File mboxFile) {
        if (mboxFile.isDirectory()) {
            for (File f : mboxFile.listFiles())
                parse(f);
            return;
        }
        if (!mboxFile.getName().endsWith(".mbox"))
            return;
        MboxIterator iterator;
        try {
            iterator = MboxIterator.fromFile(mboxFile).charset(DECODER.charset()).build();
        } catch (IOException e) {
            return;
        }
        try (Transaction tx = db.beginTx()) {
            for (CharBufferWrapper message : iterator) {
                if (message.toString().contains("Subject: svn commit"))
                    continue;
                if (message.toString().contains("Subject: cvs commit"))
                    continue;
                if (message.toString().contains("Subject: ["))
                    continue;
                parse(message);
            }
            tx.success();
        }
    }

    private void parse(CharBufferWrapper message) {
        try {
            parser.parse(new ByteArrayInputStream(message.toString().trim().getBytes()));
        } catch (MimeException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        MailGraphBuilder.process("F:\\graph-isis","F:\\apache data\\isis\\email");
    }

}
