package cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinaryAtom extends Atom{

    private String leftAtomType;
    private String rightAtomType;

    @Override
    public boolean isBinary() {
        return true;
    }

}
