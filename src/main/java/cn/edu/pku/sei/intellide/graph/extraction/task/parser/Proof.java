package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Proof implements Serializable {
    private static final long					serialVersionUID		= 2493710153498582714L;

    public static final String					PROOF_SEPARATOR			= "|";
    public static final String					PROOF_PREFIX			= "<PROOF>";
    public static final String					TAG_SEPARATOR			= "\t";
    public static final String					TAG_END_MARKER			= "</>";
    public static final String					PROOF_TYPE_TAG			= "<Type>";
    public static final String					PROOF_EVIDENCE_TAG		= "<Evidence>";
    public static final String					PROOF_EVIDENCE_TREE_TAG	= "<Evidence_Tree>";

    public static final int						MIN						= -100;
    public static final int						NEGA_10					= -10;
    public static final int						NEGA_5					= -5;
    public static final int						MID						= 0;
    public static final int						POSI_5					= 5;
    public static final int						POSI_10					= 10;
    public static final int						MAX						= 100;

    private ProofType							type;
    private String								evidenceTree;
    private String								evidence;

    // 每一条评分证据的得分表，如需修改请在下面。
    private static HashMap<ProofType, Integer>	proofScoreMap;

    static {
        proofScoreMap = new HashMap<ProofType, Integer>();

        proofScoreMap.put(ProofType.INIT_EXTRACTION_NP, POSI_5);
        proofScoreMap.put(ProofType.INIT_EXTRACTION_VP, MID);
        proofScoreMap.put(ProofType.FORM_VP_NP, POSI_5);
        proofScoreMap.put(ProofType.FORM_VP_NP_PP, POSI_5);
        proofScoreMap.put(ProofType.FORM_VP_PP, POSI_5);
        proofScoreMap.put(ProofType.FEATURE_NP_NN, MID);
        proofScoreMap.put(ProofType.FEATURE_NP_NN_PP, MID);
        proofScoreMap.put(ProofType.FEATURE_NP_PRP, MID);
        proofScoreMap.put(ProofType.FEATURE_NP_DT, MID);
        proofScoreMap.put(ProofType.FEATURE_NP_UNKNOWN, NEGA_5);

        proofScoreMap.put(ProofType.CONTEXT_IMMEDIATE, POSI_10);
        proofScoreMap.put(ProofType.CONTEXT_NEARBY, POSI_10);
        proofScoreMap.put(ProofType.CONTEXT_PRECEDING, POSI_5);

        proofScoreMap.put(ProofType.FAIL_HAVE_VERB, MIN);
        proofScoreMap.put(ProofType.FAIL_QA_VERB, MIN);
        proofScoreMap.put(ProofType.FAIL_STOP_VERB, MIN);
        proofScoreMap.put(ProofType.FAIL_UNLIKE_VERB, NEGA_10);

        proofScoreMap.put(ProofType.FAIL_QA_NOUN, MIN);
        proofScoreMap.put(ProofType.FAIL_STOP_NOUN, MIN);
        proofScoreMap.put(ProofType.FAIL_UNLIKE_NOUN, NEGA_10);
        proofScoreMap.put(ProofType.FAIL_CODE_ELEMENT, NEGA_10);

        proofScoreMap.put(ProofType.FAIL_STOP_PHRASES, MIN);

        proofScoreMap.put(ProofType.FAIL_BE_VERB_ROOT, MIN);
        proofScoreMap.put(ProofType.FAIL_BE_VERB_THOROUGHLY, NEGA_10);
        proofScoreMap.put(ProofType.FAIL_MODAL_VERB_ROOT, MIN);
        proofScoreMap.put(ProofType.FAIL_MODAL_VERB_THOROUGHLY, NEGA_10);
        proofScoreMap.put(ProofType.FAIL_NEGATION_ROOT, MIN);
        proofScoreMap.put(ProofType.FAIL_NEGATION_THOROUGHLY, NEGA_10);
        proofScoreMap.put(ProofType.FAIL_PERSONAL_PRONOUN, NEGA_10);

        proofScoreMap.put(ProofType.ILLEGAL_NP_PHRASE, MIN);
        proofScoreMap.put(ProofType.ILLEGAL_VP_PHRASE, MIN);
        proofScoreMap.put(ProofType.DEFAULT, NEGA_5);

        proofScoreMap.put(ProofType.PASS_VP_DT_PP_FORM, POSI_5);
        proofScoreMap.put(ProofType.PASS_VP_NP_FORM, POSI_5);
        proofScoreMap.put(ProofType.PASS_VP_NP_PP_FORM, POSI_5);
        proofScoreMap.put(ProofType.PASS_VP_PP_FORM, POSI_5);
        proofScoreMap.put(ProofType.PASS_VP_PRP_PP_FORM, POSI_5);

    }

    public Proof() {
        super();
    }

    public Proof(ProofType type) {
        this.type = type;
    }

    /**
     * A string representation of the proof
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(PROOF_PREFIX);
        sb.append(PROOF_TYPE_TAG + type + TAG_END_MARKER);
        if (evidence != null && !"".equals(evidence))
            sb.append(TAG_SEPARATOR + PROOF_EVIDENCE_TAG + evidence + TAG_END_MARKER);
        if (evidenceTree != null)
            sb.append(TAG_SEPARATOR + PROOF_EVIDENCE_TREE_TAG + evidenceTree + TAG_END_MARKER);
        return sb.toString();
    }

    /** build a proof from its string representation **/
    public static Proof of(String proofString) {
        if (StringUtils.isBlank(proofString) || !proofString.contains(PROOF_PREFIX)
                || !proofString.contains(PROOF_TYPE_TAG))
            return null;

        Proof proof = new Proof();

        int beginIndex, endIndex;

        beginIndex = proofString.indexOf(PROOF_TYPE_TAG) + PROOF_TYPE_TAG.length();
        endIndex = proofString.indexOf(TAG_END_MARKER, beginIndex);
        String typeString = proofString.substring(beginIndex, endIndex);
        proof.setType(ProofType.getProofTypeOf(typeString));

        if (proofString.contains(PROOF_EVIDENCE_TAG)) {
            beginIndex = proofString.indexOf(PROOF_EVIDENCE_TAG) + PROOF_EVIDENCE_TAG.length();
            endIndex = proofString.indexOf(TAG_END_MARKER, beginIndex);
            String evidenceString = proofString.substring(beginIndex, endIndex);
            proof.setEvidence(evidenceString);
        }

        if (proofString.contains(PROOF_EVIDENCE_TREE_TAG)) {
            beginIndex = proofString.indexOf(PROOF_EVIDENCE_TREE_TAG) + PROOF_EVIDENCE_TREE_TAG.length();
            endIndex = proofString.indexOf(TAG_END_MARKER, beginIndex);
            String evidenceTree = proofString.substring(beginIndex, endIndex);
            proof.setEvidenceTree(evidenceTree);
        }

        return proof;
    }

    /** Concatenate multiple proofs and build a string representation **/
    public static String concatenateProofs(List<Proof> proofs) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < proofs.size(); i++) {
            if (i > 0)
                str.append(PROOF_SEPARATOR);
            str.append(proofs.get(i).toString());

        }
        return str.toString();
    }

    public int getScore() {
        return proofScoreMap.get(type);
    }

    public ProofType getType() {
        return type;
    }

    public void setType(ProofType proofType) {
        this.type = proofType;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public void setEvidenceTree(String evidenceTree) {
        this.evidenceTree = evidenceTree;
    }

}

