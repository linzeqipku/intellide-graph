package cn.edu.pku.sei.intellide.graph.qa.nl_query.mapping;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;

public class AtomFactory {

    public static Atom fromJson(JSONObject object){
        Atom atom = null;
        try {
            int order = object.getInt("order");
            String type = object.getString("type");
            if (order == 1){
                atom = new UnaryAtom();
            }
            else if (order == 2){
                BinaryAtom bi = new BinaryAtom();
                Pair<String, String> p = Schema.relations.get(type);
                bi.setLeftAtomType(p.getLeft());
                bi.setRightAtomType(p.getRight());
                atom = bi;
            }
            else if (order == 3){
                atom = new OperationAtom();
            }
            atom.setOrder(order);
            atom.setType(type);
            atom.setName(object.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return atom;
    }

    public static Atom createOp(String type){
        Atom atom = new OperationAtom();
        atom.setOrder(3);
        atom.setType(type);
        atom.setName(type);
        return atom;
    }

    public static Atom createBinary(String type){
        BinaryAtom atom = new BinaryAtom();
        atom.setOrder(2);
        atom.setType(type);
        atom.setName(type);
        Pair<String, String> p = Schema.relations.get(type);
        atom.setLeftAtomType(p.getLeft());
        atom.setRightAtomType(p.getRight());
        return atom;
    }
}
