package cn.edu.pku.sei.intellide.graph.extraction.task.parser;

import java.util.HashMap;
import java.util.Map;

public enum ProofType {
    INIT_EXTRACTION_VP("init_extraction_vp"), // 抽取vp
    INIT_EXTRACTION_NP("init_extraction_np"), // 抽取np

    FORM_VP_NP("form_vp_np"), // VP(VB+NP)
    FORM_VP_NP_PP("form_vp_np_pp"), // VP(VB+NP+PP)
    FORM_VP_PP("form_vp_pp"), // VP(VB+[dt/prp]+PP)

    FEATURE_NP_NN("feature_np_nn"), // ...NP(NN.*)
    FEATURE_NP_NN_PP("feature_np_nn_pp"), // ...NP(NN.*+PP)
    FEATURE_NP_PRP("feature_np_prp"), // ...NP(PRP)
    FEATURE_NP_DT("feature_np_dt"), // ...NP(DT)
    FEATURE_NP_UNKNOWN("feature_np_unknown"), // ...NP(unknown)

    CONTEXT_IMMEDIATE("context_immediate"), // 紧邻上下文
    CONTEXT_NEARBY("context_nearby"), // 附近上下文
    CONTEXT_PRECEDING("context_preceding"), // 句中出现

    FAIL_HAVE_VERB("fail_have_verb"), // VP的VB.*中有have
    FAIL_QA_VERB("fail_qa_verb"), // 涉及问答的verb
    FAIL_STOP_VERB("fail_stop_verb"), // 不可能的verb
    FAIL_UNLIKE_VERB("fail_unlike_verb"), // 不太可能的verb

    FAIL_QA_NOUN("fail_qa_noun"), // 不可能的noun
    FAIL_STOP_NOUN("fail_stop_noun"), // 不可能的noun
    FAIL_UNLIKE_NOUN("fail_unlike_noun"), // 不太可能的noun

    FAIL_CODE_ELEMENT("fail_code_element"), // 标注出来的CODE#

    FAIL_STOP_PHRASES("fail_stop_phrases"), // 不可能的短语

    FAIL_BE_VERB_ROOT("fail_be_verb_root"), // VP紧邻儿子是be动词
    FAIL_BE_VERB_THOROUGHLY("fail_be_verb_thoroughly"), // VP的子孙中有be动词
    FAIL_MODAL_VERB_ROOT("fail_modal_verb_root"), // VP紧邻儿子是情态动词
    FAIL_MODAL_VERB_THOROUGHLY("fail_modal_verb_thoroughly"), // VP子孙有情态动词
    FAIL_NEGATION_ROOT("fail_negation_root"), // VP紧邻副词中有否定词
    FAIL_NEGATION_THOROUGHLY("fail_negation_thoroughly"), // 整句中有否定词
    FAIL_PERSONAL_PRONOUN("fail_personal_pronoun"), // 包含人称代词，I you me us 等

    ILLEGAL_VP_PHRASE("illegal_vp"), // 不是vp的VP
    ILLEGAL_NP_PHRASE("illegal_np"), // 不是np的NP

    DEFAULT("default"), //

    PASS_VP_NP_FORM("pass_VP_NP_form"), // 短语符合名词形式 ：VP+NP(NN.*)
    PASS_VP_NP_PP_FORM("pass_VP_NP_PP_form"), // 短语符合名词形式 ：VP+NP(NN.*)
    PASS_VP_PP_FORM("pass_VP_PP_form"), // 短语符合不及物动词的名词宾语形式：VP+PP(NP(NN.*))
    PASS_VP_PRP_PP_FORM("pass_VP_PRP_PP_form"), // 短语符合代词规则形式：VP(VB+NP(PRP)+PP(NP(NN)))
    PASS_VP_DT_PP_FORM("pass_VP_DT_PP_form"); // 短语符合定冠词代词规则形式：VP(VB+NP(DT)+PP(NP(NN)))

    private String name; // 定义自定义的变量

    private ProofType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private final static Map<String, ProofType> PROOF_MAP = new HashMap<String, ProofType>();
    static {
        for (ProofType v : values()) {
            PROOF_MAP.put(v.getName(), v);
        }
    }

    public static ProofType getProofTypeOf(String name) {
        ProofType type = PROOF_MAP.get(name);

        if (type == null)
            type = DEFAULT;

        return type;
    }

    public static void main(String[] args) {
        for (ProofType v : values()) {
            System.out.println(v);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}