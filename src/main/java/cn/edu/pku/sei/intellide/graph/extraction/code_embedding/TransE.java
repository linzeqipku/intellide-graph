package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

class TransE {

    private boolean L1_flag = true;
    private int n = 200;
    private int method = 0;
    private double rate = 0.001;
    private double margin = 1;

    private int relation_num;
    private int entity_num;
    private Map<String, Integer> relation2id, entity2id;
    private Map<Integer, String> id2entity, id2relation;
    private Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    private Map<Integer, Double> left_num, right_num;
    private Set<Triple<Integer, Integer, Integer>> ok;
    private double res;
    private List<Integer> fb_h, fb_l, fb_r;

    private double[][] relation_vec, entity_vec;
    private double[][] relation_tmp, entity_tmp;

    public void prepare(List<String> entities, List<String> relations, List<Triple<String, String, String>> triples) {
        ok = new HashSet<>();

        entity2id = new HashMap<>();
        id2entity = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            entity2id.put(entities.get(i), i);
            id2entity.put(i, entities.get(i));
        }
        entity_num = entities.size();
        relation2id = new HashMap<>();
        id2relation = new HashMap<>();
        for (int i = 0; i < relations.size(); i++) {
            relation2id.put(relations.get(i), i);
            id2relation.put(i, relations.get(i));
        }
        relation_num = relations.size();

        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        fb_h = new ArrayList<>();
        fb_r = new ArrayList<>();
        fb_l = new ArrayList<>();
        for (Triple<String, String, String> triple : triples) {
            if (!left_entity.containsKey(relation2id.get(triple.getRight())))
                left_entity.put(relation2id.get(triple.getRight()), new HashMap<>());
            if (!left_entity.get(relation2id.get(triple.getRight())).containsKey(entity2id.get(triple.getLeft())))
                left_entity.get(relation2id.get(triple.getRight())).put(entity2id.get(triple.getLeft()), 0);
            left_entity.get(relation2id.get(triple.getRight())).put(entity2id.get(triple.getLeft()),
                    left_entity.get(relation2id.get(triple.getRight())).get(entity2id.get(triple.getLeft())) + 1);

            if (!right_entity.containsKey(relation2id.get(triple.getRight())))
                right_entity.put(relation2id.get(triple.getRight()), new HashMap<>());
            if (!right_entity.get(relation2id.get(triple.getRight())).containsKey(entity2id.get(triple.getMiddle())))
                right_entity.get(relation2id.get(triple.getRight())).put(entity2id.get(triple.getMiddle()), 0);
            right_entity.get(relation2id.get(triple.getRight())).put(entity2id.get(triple.getMiddle()),
                    right_entity.get(relation2id.get(triple.getRight())).get(entity2id.get(triple.getMiddle())) + 1);
            add(entity2id.get(triple.getLeft()), entity2id.get(triple.getMiddle()), relation2id.get(triple.getRight()));
        }
        left_num = new HashMap<>();
        right_num = new HashMap<>();
        for (int i = 0; i < relation_num; i++) {
            double sum1 = 0, sum2 = 0;
            for (Entry<Integer, Integer> it : left_entity.get(i).entrySet()) {
                sum1++;
                sum2 += it.getValue();
            }
            left_num.put(i, sum2 / sum1);
        }
        for (int i = 0; i < relation_num; i++) {
            double sum1 = 0, sum2 = 0;
            for (Entry<Integer, Integer> it : right_entity.get(i).entrySet()) {
                sum1++;
                sum2 += it.getValue();
            }
            right_num.put(i, sum2 / sum1);
        }
        System.out.println("relation_num=" + relation_num);
        System.out.println("entity_num=" + entity_num);
    }

    public void run() {
        relation_vec = new double[relation_num][n];
        entity_vec = new double[entity_num][n];
        relation_tmp = new double[relation_num][n];
        entity_tmp = new double[entity_num][n];
        for (int i = 0; i < relation_num; i++) {
            for (int ii = 0; ii < n; ii++)
                relation_vec[i][ii] = randn(0, 1.0 / n, -6.0 / Math.sqrt(n), 6.0 / Math.sqrt(n));
            norm(relation_vec[i]);
        }
        for (int i = 0; i < entity_num; i++) {
            for (int ii = 0; ii < n; ii++)
                entity_vec[i][ii] = randn(0, 1.0 / n, -6.0 / Math.sqrt(n), 6.0 / Math.sqrt(n));
            norm(entity_vec[i]);
        }
        bfgs();
    }

    public Map<String, double[]> getEntityVecMap() {
        Map<String, double[]> r = new HashMap<>();
        for (int i = 0; i < entity_num; i++) {
            String name = id2entity.get(i);
            double[] vec = entity_vec[i];
            r.put(name, vec);
        }
        return r;
    }

    private void bfgs() {
        System.out.println("BFGS:");
        res = 0;
        int nbatches = 100;
        int nepoch = 1000;
        int batchsize = fb_h.size() / nbatches;
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;
            for (int batch = 0; batch < nbatches; batch++) {
                relation_tmp = relation_vec.clone();
                entity_tmp = entity_vec.clone();
                for (int k = 0; k < batchsize; k++) {
                    int i = rand_max(fb_h.size());
                    int j = rand_max(entity_num);
                    double pr = 1000.0 * right_num.get(fb_r.get(i))
                            / (right_num.get(fb_r.get(i)) + left_num.get(fb_r.get(i)));
                    if (method == 0)
                        pr = 500;
                    if (rand_max(1000) < pr) {
                        while (ok.contains(new ImmutableTriple<>(fb_h.get(i), fb_r.get(i), j)))
                            j = rand_max(entity_num);
                        train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), fb_h.get(i), j, fb_r.get(i));
                    } else {
                        while (ok.contains(new ImmutableTriple<>(j, fb_r.get(i), fb_l.get(i))))
                            j = rand_max(entity_num);
                        train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    }
                    norm(relation_tmp[fb_r.get(i)]);
                    norm(entity_tmp[fb_h.get(i)]);
                    norm(entity_tmp[fb_l.get(i)]);
                    norm(entity_tmp[j]);
                }
                relation_vec = relation_tmp;
                entity_vec = entity_tmp;
            }
            System.out.println("epoch:" + epoch + ' ' + res);
        }
    }

    private double calc_sum(int e1, int e2, int rel) {
        double sum = 0;
        if (L1_flag)
            for (int ii = 0; ii < n; ii++)
                sum += Math.abs(entity_vec[e2][ii] - entity_vec[e1][ii] - relation_vec[rel][ii]);
        else
            for (int ii = 0; ii < n; ii++)
                sum += Math.pow(entity_vec[e2][ii] - entity_vec[e1][ii] - relation_vec[rel][ii], 2);
        return sum;
    }

    private void gradient(int e1_a, int e2_a, int rel_a, int e1_b, int e2_b, int rel_b) {
        for (int ii = 0; ii < n; ii++) {
            double x = 2.0 * (entity_vec[e2_a][ii] - entity_vec[e1_a][ii] - relation_vec[rel_a][ii]);
            if (L1_flag)
                if (x > 0)
                    x = 1;
                else
                    x = -1;
            relation_tmp[rel_a][ii] -= -1.0 * rate * x;
            entity_tmp[e1_a][ii] -= -1.0 * rate * x;
            entity_tmp[e2_a][ii] += -1.0 * rate * x;
            x = 2.0 * (entity_vec[e2_b][ii] - entity_vec[e1_b][ii] - relation_vec[rel_b][ii]);
            if (L1_flag)
                if (x > 0)
                    x = 1;
                else
                    x = -1;
            relation_tmp[rel_b][ii] -= rate * x;
            entity_tmp[e1_b][ii] -= rate * x;
            entity_tmp[e2_b][ii] += rate * x;
        }
    }

    private void train_kb(int e1_a, int e2_a, int rel_a, int e1_b, int e2_b, int rel_b) {
        double sum1 = calc_sum(e1_a, e2_a, rel_a);
        double sum2 = calc_sum(e1_b, e2_b, rel_b);
        if (sum1 + margin > sum2) {
            res += margin + sum1 - sum2;
            gradient(e1_a, e2_a, rel_a, e1_b, e2_b, rel_b);
        }
    }

    private static double rand(double min, double max) {
        return min + (max - min) * Math.random();
    }

    private static double normal(double x, double miu, double sigma) {
        return 1.0 / Math.sqrt(2.0 * Math.PI) / sigma * Math.exp(-1.0 * (x - miu) * (x - miu) / (2.0 * sigma * sigma));
    }

    private static double randn(double miu, double sigma, double min, double max) {
        double x, y, dScope;
        do {
            x = rand(min, max);
            y = normal(x, miu, sigma);
            dScope = rand(0.0, normal(miu, miu, sigma));
        } while (dScope > y);
        return x;
    }

    private static double vec_len(double[] a) {
        double res = 0;
        for (double anA : a) res += anA * anA;
        res = Math.sqrt(res);
        return res;
    }

    private static void norm(double[] a) {
        double x = vec_len(a);
        if (x > 1)
            for (int ii = 0; ii < a.length; ii++)
                a[ii] /= x;
    }

    private static int rand_max(int x) {
        return (int) (Math.random() * x);
    }

    private void add(int e1, int e2, int r) {
        fb_h.add(e1);
        fb_r.add(r);
        fb_l.add(e2);
        Triple<Integer, Integer, Integer> triple = new ImmutableTriple<>(e1, r, e2);
        ok.add(triple);
    }

}