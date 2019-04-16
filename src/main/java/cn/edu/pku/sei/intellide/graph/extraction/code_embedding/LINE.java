package cn.edu.pku.sei.intellide.graph.extraction.code_embedding;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.*;

public class LINE {

    GraphDatabaseService db;

    private static final int neg_table_size = (int) 1e8;
    private static final int sigmoid_table_size = 1000;
    private static final int SIGMOID_BOUND = 6;
    private static final double NEG_SAMPLING_POWER = 0.75;

    private int num_threads = 1;
    private int order = 1;
    private int dim = 200;
    private int num_negative = 5;
    private double init_rho = 0.025;
    private double rho;
    private int total_samples = 100;
    private int current_sample_count = 0;

    private List<EmbeddingRelation> edges = new ArrayList<>();
    Map<String, EmbeddingPoint> vertex = new HashMap<>();
    private long[] alias;
    private String[] neg_table;
    private double[] prob;
    private double[] sigmoid_table;
    private Random randomGenerator;

    public LINE(GraphDatabaseService db){
        this.db = db;
    }

    public void readData() {
        Graph graph = CsnExtractor.extract(db);
        graph.getVertexes().forEachRemaining(v1->{
            v1.getNeighbors(true,true).stream().forEach(v2->{
                double weight=v1.weightTo(v2,true,true);
                edges.add(new EmbeddingRelation(v1.getId(),v2.getId(),weight));
                EmbeddingPoint v1p;
                if (vertex.containsKey(v1.getId()))
                    v1p=vertex.get(v1.getId());
                else {
                    v1p = new EmbeddingPoint(0.0);
                    vertex.put(v1.getId(),v1p);
                }
                v1p.degree += weight;
            });
        });
        System.out.println("Vertex count: "+vertex.size());
        System.out.print("Edge count: "+edges.size());
    }

    private void initAliasTable() {
        alias = new long[edges.size()];
        prob = new double[edges.size()];
        double[] norm_prob = new double[edges.size()];
        int[] large_block = new int[edges.size()];
        int[] small_block = new int[edges.size()];
        double sum = 0;
        int cur_small_block, cur_large_block;
        int num_small_block = 0, num_large_block = 0;
        for (int k = 0; k != edges.size(); ++k)
            sum += edges.get(k).weight;
        for (int k = 0; k != edges.size(); ++k)
            norm_prob[k] = edges.get(k).weight * edges.size() / sum;
        for (int k = edges.size() - 1; k >= 0; k--) {
            if (norm_prob[k] < 1)
                small_block[num_small_block++] = k;
            else
                large_block[num_large_block++] = k;
        }
        while (num_small_block > 0 && num_large_block > 0) {
            cur_small_block = small_block[--num_small_block];
            cur_large_block = large_block[--num_large_block];
            prob[cur_small_block] = norm_prob[cur_small_block];
            alias[cur_small_block] = cur_large_block;
            norm_prob[cur_large_block] = norm_prob[cur_large_block];
            if (norm_prob[cur_large_block] < 1)
                small_block[num_small_block++] = cur_large_block;
            else
                large_block[num_large_block++] = cur_large_block;
        }
        while (num_large_block > 0)
            prob[large_block[--num_large_block]] = 1;
        while (num_small_block > 0)
            prob[small_block[--num_small_block]] = 1;
    }

    private void initVector() {
        for (String key : vertex.keySet()) {
            EmbeddingPoint v = vertex.get(key);
            v.emb_context = new double[dim];
            v.emb_vertex = new double[dim];
            for (int i = 0; i < dim; ++i)
                v.emb_vertex[i] = (Math.random() - 0.5) / dim;
        }
    }

    private void initNegTable() {
        double sum = 0, cur_sum = 0, por = 0;
        Iterator iter = vertex.entrySet().iterator();
        String vid=null;
        neg_table = new String[neg_table_size];
        for (String key : vertex.keySet()) {
            EmbeddingPoint v = vertex.get(key);
            sum += Math.pow(v.degree, NEG_SAMPLING_POWER);
        }
        for (int k = 0; k != neg_table_size; ++k) {
            if ((double) (k + 1) / neg_table_size > por) {
                Map.Entry entry = (Map.Entry) iter.next();
                EmbeddingPoint v = (EmbeddingPoint) entry.getValue();
                cur_sum += Math.pow(v.degree, NEG_SAMPLING_POWER);
                por = cur_sum / sum;
                vid = (String) entry.getKey();
            }
            neg_table[k] = vid;
        }
    }

    private void initSigmoidTable() {
        double x;
        sigmoid_table = new double[sigmoid_table_size + 1];
        for (int k = 0; k != sigmoid_table_size; ++k) {
            x = 2 * SIGMOID_BOUND * k / sigmoid_table_size - SIGMOID_BOUND;
            sigmoid_table[k] = 1 / (1 + Math.exp(-x));
        }
    }

    private double fastSigmoid(double x) {
        if (x > SIGMOID_BOUND) return 1;
        else if (x < -SIGMOID_BOUND) return 0;
        int k = (int) ((x + SIGMOID_BOUND) * sigmoid_table_size / SIGMOID_BOUND / 2);
        return sigmoid_table[k];
    }

    private long sampleAnEdge(double rand_value1, double rand_value2) {
        int k = (int) (edges.size() * rand_value1);
        return rand_value2 < prob[k] ? k : alias[k];
    }

    private void update(double[] vec_u, double[] vec_v, double[] vec_error, long label) {
        double x = 0, g;
        for (int c = 0; c != dim; ++c)
            x += vec_u[c] * vec_v[c];
        g = (label - fastSigmoid(x)) * rho;
        for (int c = 0; c != dim; ++c)
            vec_error[c] += g * vec_v[c];
        for (int c = 0; c != dim; ++c)
            vec_v[c] += g * vec_u[c];
    }

    private void trainLINE() {
        String u, v, target;
        long label;
        long count = 0, last_count = 0;
        int curedge;

        long seed = 1L;
        double[] vec_error = new double[dim];
        while (true) {
            if (count > total_samples / num_threads + 2) break;
            if (count - last_count > 10000) {
                current_sample_count += count - last_count;
                last_count = count;
                //System.out.println("Rho: "+rho+"Progress: "+(double)current_sample_count/(double)(total_samples+1)*100+'%');
                rho = init_rho * (1 - current_sample_count / (double) (total_samples + 1));
                if (rho < init_rho * 0.0001)
                    rho = init_rho * 0.0001;
            }
            curedge = (int) sampleAnEdge(randomGenerator.nextDouble(), randomGenerator.nextDouble());
            u = edges.get(curedge).src;
            v = edges.get(curedge).tgt;
            for (int c = 0; c != dim; ++c)
                vec_error[c] = 0;
            double[] emb = vertex.get(u).emb_vertex;

            for (int d = 0; d != num_negative + 1; ++d) {
                if (d == 0) {
                    target = v;
                    label = 1;
                } else {
                    seed = seed * 25214903917L + 11;
                    int idx = (int) ((seed >>> 16) % neg_table_size);
                    target = neg_table[idx];
                    label = 0;
                }
                if (order == 1)
                    update(emb, vertex.get(target).emb_vertex, vec_error, label);
                if (order == 2)
                    update(emb, vertex.get(target).emb_context, vec_error, label);
            }

            for (int c = 0; c != dim; ++c)
                emb[c] += vec_error[c];
            count++;
        }
    }

    public void run() {
        rho = init_rho;
        total_samples *= 1000000;
        initAliasTable();
        initVector();
        initNegTable();
        initSigmoidTable();
        randomGenerator = new Random();
        trainLINE();
    }

    class EmbeddingRelation {
        String src,tgt;
        double weight;

        public EmbeddingRelation(String src, String tgt, double weight) {
            this.src = src;
            this.tgt = tgt;
            this.weight = weight;
        }
    }

    class EmbeddingPoint {
        double degree;
        double[] emb_vertex, emb_context;

        EmbeddingPoint(double w) {
            degree = w;
            emb_vertex = emb_context = null;
        }
    }
}
