package cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity;

import cn.edu.pku.sei.intellide.graph.qa.nl_query.NlpInterface.entity.TokenMapping.NLPMapping;

import java.util.ArrayList;
import java.util.List;

public class NLPToken {
    public String text;
    public NLPMapping mapping;
    public List<NLPMapping> mappingList = new ArrayList<>();
    public String POS;
    public String NE;
    public boolean nomapping = false;
    public long offset = -1;
    public double offsetVal = -1;
    public double roffset = -1;

    public NLPToken(String text) {
        this.text = text;
    }

    public NLPToken(String text, String POS, String NE) {
        this.text = text;
        this.POS = POS;
        this.NE = NE;
    }

    public NLPToken copy() {
        NLPToken token = new NLPToken(text, POS, NE);
        token.offset = offset;
        token.offsetVal = offsetVal;
        token.roffset = roffset;
        token.nomapping = nomapping;
        token.mapping = mapping;
        return token;
    }
}
