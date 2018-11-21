package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu.Separator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;

public class PhraseInfo implements Serializable {
    private static final long	serialVersionUID	= -8383713376186053397L;
    public static final String	TABLE_NAME			= "phrases";

    public static final int		PHRASE_TYPE_DEFAULT	= 0;
    public static final int		PHRASE_TYPE_VP		= 1;
    public static final int		PHRASE_TYPE_NP		= 2;

    public static final int	PROOF_SCORE_DEFAULT	= -1;
    // public static final String PATH_OFFSET = "Offset";

    private int					id;
    private int					phraseType			= PHRASE_TYPE_DEFAULT;

    /**
     * The parent ({@code}SentenceInfo) of the phrase.
     */
    private int					parentId			= -1;

    /**
     * The offset/index of this phrase in parent sentence
     */
    private int					offsetInSentence	= -1;
    /**
     * (Thread|Post|Comment)@{id}\Content@{id}\Paragraph@{id}\Sentence@{id}
     */
    private String				sourcePath;

    private String				text;
    private String				syntaxTree;
    private List<Proof>			proofs;

    /** only for db io **/
    private String				proofString;
    private int					proofScore			= PROOF_SCORE_DEFAULT;

    private int					taskId;

    public PhraseInfo() {
        super();
        proofs = new ArrayList<Proof>();
    }

//    public String getSourcePath() {
//        if (StringUtils.isBlank(sourcePath)) {
//            if (parentId <= 0)
//                return sourcePath;
//
//            String path = DocumentParser.getSentenceSourcePath(parentId);
//            if (StringUtils.isBlank(path))
//                sourcePath = null;
//            else
//                sourcePath = path;
//        }
//        return sourcePath;
//    }

    public void addProof(Proof proof) {
        if (proofs == null)
            proofs = new ArrayList<Proof>();
        proofs.add(proof);
    }

    public boolean hasProof(ProofType type) {
        if (type == null)
            return false;
        for (Proof proof : proofs) {
            if (type.equals(proof.getType()))
                return true;
        }
        return false;
    }

    public String printProofs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < proofs.size(); i++) {
            sb.append(proofs.get(i).toString());
            if (i < proofs.size() - 1)
                sb.append(System.getProperty("line.separator"));
        }
        sb.insert(0, "[ProofScore] " + getProofScore() + System.getProperty("line.separator"));
        return sb.toString();
    }

    public String getProofString() {
        if (StringUtils.isBlank(proofString))
            proofString = Proof.concatenateProofs(proofs);
        return proofString;
    }

    public int getProofScore() {
        if (proofScore == PROOF_SCORE_DEFAULT) {
            if (proofs == null || proofs.size() <= 0)
                return proofScore;

            proofScore = 0;
            for (Proof proof : proofs) {
                proofScore += proof.getScore();
            }
        }
        return proofScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPhraseType() {
        return phraseType;
    }

    public void setPhraseType(int phraseType) {
        this.phraseType = phraseType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSyntaxTree() {
        return syntaxTree;
    }

    public void setSyntaxTree(String syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getOffsetInSentence() {
        return offsetInSentence;
    }

    public void setOffsetInSentence(int offsetInSentence) {
        this.offsetInSentence = offsetInSentence;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<Proof> getProofs() {
        return proofs;
    }

    public void setProofs(List<Proof> proofs) {
        this.proofs = proofs;
    }

    public void setProofScore(int proofScore) {
        this.proofScore = proofScore;
    }

    public void setProofString(String proofString) {
        this.proofString = proofString;
    }

    public static void main(String[] args) {
        PhraseInfo p = new PhraseInfo();
        p.setId(35);
        p.setParentId(1126800);
//        System.out.println(p.getSourcePath());
//        for (String string : p.getSourcePath().split(DocumentParser.PATH_SEPARATOR)) {
//            System.out.println(string);
//        }
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
}
