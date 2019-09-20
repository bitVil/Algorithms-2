/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.Topological;

public class WordNet {
    private ST<String, Bag<Integer>> nounSet;
    private ST<Integer, String> synsetsTable;
    // private Digraph wordNetGraph;
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        // Defines a symbol table of Strings and Bag<Integer>,
        // adds the int at start of each line to the bag of
        // each String on that line.
        if (synsets == null || hypernyms == null) throw new IllegalArgumentException();
        int numSynsets = 0;
        nounSet = new ST<String, Bag<Integer>>();
        synsetsTable = new ST<Integer, String>();
        In in = new In(synsets);
        while (!in.isEmpty()) {
            String[] line = in.readLine().split(",");
            synsetsTable.put(numSynsets, line[1]);
            line = line[1].split(" ");
            for (String noun : line) {
                Bag<Integer> idBag = nounSet.get(noun);
                if (idBag != null) idBag.add(numSynsets);
                else {
                    idBag = new Bag<Integer>();
                    idBag.add(numSynsets);
                    nounSet.put(noun, idBag);
                }
            }
            numSynsets++;
        }

        // Reads input lines one by one, making edges
        // from first entry in line to each of the others.
        in = new In(hypernyms);
        Digraph wordNetGraph = new Digraph(numSynsets);
        while (!in.isEmpty()) {
            String[] line = in.readLine().split(",");
            int vertex = Integer.parseInt(line[0]);
            for (int i = 1; i < line.length; i++) {
                wordNetGraph.addEdge(vertex, Integer.parseInt(line[i]));
            }
        }

        // Checks if wordNetGraph is a rooted DAG.
        Topological sortedG = new Topological(wordNetGraph);
        if (!sortedG.hasOrder()) throw new IllegalArgumentException();
        int numRoots = 0;
        for (int i = 0; i < numSynsets; i++) {
            if (wordNetGraph.outdegree(i) == 0) numRoots++;
            if (numRoots > 1) throw new IllegalArgumentException();
        }
        sap = new SAP(wordNetGraph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return this.nounSet.keys();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException();
        return this.nounSet.contains(word);
    }

    // distance between nounA and nounB
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null) throw new IllegalArgumentException();
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();
        if (nounA.equals(nounB)) return 0;
        Bag<Integer> a = nounSet.get(nounA);
        Bag<Integer> b = nounSet.get(nounB);
        return sap.length(a, b);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (nounA == null || nounB == null) throw new IllegalArgumentException();
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();
        Bag<Integer> a = nounSet.get(nounA);
        Bag<Integer> b = nounSet.get(nounB);
        int ancestor = sap.ancestor(a, b);
        return this.synsetsTable.get(ancestor);
    }

    public static void main(String[] args) {
        WordNet net = new WordNet(args[0], args[1]);
        System.out.println("nounSet: ");
        for (String s : net.nounSet.keys()) {
            System.out.print(s + ": ");
            for (int n : net.nounSet.get(s)) System.out.print(n + " ");
            System.out.println();
        }
        System.out.println("synsetsTable: ");
        for (int s : net.synsetsTable.keys()) {
            System.out.println(s + ": " + net.synsetsTable.get(s));
        }
    }
}
