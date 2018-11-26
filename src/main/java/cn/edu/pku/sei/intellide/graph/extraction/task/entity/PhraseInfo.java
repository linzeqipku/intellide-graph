package cn.edu.pku.sei.intellide.graph.extraction.task.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.edu.pku.sei.intellide.graph.extraction.task.parser.Proof;
import cn.edu.pku.sei.intellide.graph.extraction.task.parser.ProofType;

public class PhraseInfo implements Serializable {
    private static final long	serialVersionUID	= -8383713376186053397L;

    public static final int		PHRASE_TYPE_DEFAULT	= 0;
    public static final int		PHRASE_TYPE_VP		= 1;
    public static final int		PHRASE_TYPE_NP		= 2;

    public static final int	PROOF_SCORE_DEFAULT	= -1;

    private int					id;
    private int					phraseType			= PHRASE_TYPE_DEFAULT;

    /**
     * The parent ({@code}SentenceInfo) of the phrase.
     */
    private int					parentId			= -1;

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

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Proof> getProofs() {
        return proofs;
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

}
