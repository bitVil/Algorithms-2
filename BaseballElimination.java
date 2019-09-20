/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    private int[] w;
    private int[] l;
    private int[] r;
    private int[][] g;
    private String[] numToName;
    private ST<String, Integer> nameToNum;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In file = new In(filename);
        int numTeams = file.readInt();
        w = new int[numTeams];
        l = new int[numTeams];
        r = new int[numTeams];
        g = new int[numTeams][numTeams];
        numToName = new String[numTeams];
        nameToNum = new ST<String, Integer>();

        // Read each line of input file.
        for (int i = 0; i < numTeams; i++) {
            String name = file.readString();
            numToName[i] = name;
            nameToNum.put(name, i);
            w[i] = file.readInt();
            l[i] = file.readInt();
            r[i] = file.readInt();
            for (int j = 0; j < numTeams; j++) g[i][j] = file.readInt();
        }

    }

    // number of teams
    public int numberOfTeams() {
        return w.length;
    }

    // all teams
    public Iterable<String> teams() {
        return nameToNum.keys();
    }

    // number of wins for given team
    public int wins(String team) {
        if (nameToNum.contains(team)) {
            return w[nameToNum.get(team)];
        }
        else throw new IllegalArgumentException();
    }

    // number of losses for given team
    public int losses(String team) {
        if (nameToNum.contains(team)) {
            return l[nameToNum.get(team)];
        }
        else throw new IllegalArgumentException();
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (nameToNum.contains(team)) {
            return r[nameToNum.get(team)];
        }
        else throw new IllegalArgumentException();
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (numberOfTeams() == 1) return 0;
        if (nameToNum.contains(team1) && nameToNum.contains(team2)) {
            int num1 = nameToNum.get(team1);
            int num2 = nameToNum.get(team2);
            return g[num1][num2];
        }
        else throw new IllegalArgumentException();
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (numberOfTeams() == 1) return false;
        if (nameToNum.contains(team)) {
            int teamNum = nameToNum.get(team);
            if (playSeason(teamNum) == null) return false;
            else return true;
        }
        else throw new IllegalArgumentException();
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (numberOfTeams() == 1) return null;
        if (nameToNum.contains(team)) {
            int teamNum = nameToNum.get(team);
            Bag<String> cert = playSeason(teamNum);
            if (cert == null) return null;
            else return cert;
        }
        else throw new IllegalArgumentException();
    }


    private Bag<String> playSeason(int teamNum) {
        // Check for simple elimination
        int numTeams = numberOfTeams();
        for (int i = 0; i < numTeams; i++) {
            if (i != teamNum && w[teamNum] + r[teamNum] < w[i]) {
                Bag<String> cert = new Bag<String>();
                cert.add(numToName[i]);
                return cert;
            }
        }

        double inf = Double.POSITIVE_INFINITY;
        int n = 1 + numberOfTeams() + bin(numTeams - 1, 2);
        FlowNetwork season = new FlowNetwork(n);
        int totalGames = 0;

        // Connect source to game node.
        int count = 1;
        for (int i = 0; i < numTeams; i++) {
            if (i != teamNum) {
                for (int j = i + 1; j < numTeams; j++) {
                    if (j != teamNum) {
                        totalGames += g[i][j];
                        FlowEdge fromSrc = new FlowEdge(numTeams, numTeams + count, g[i][j]);
                        FlowEdge toTeami = new FlowEdge(numTeams + count, i, inf);
                        FlowEdge toTeamj = new FlowEdge(numTeams + count, j, inf);
                        season.addEdge(fromSrc);
                        season.addEdge(toTeami);
                        season.addEdge(toTeamj);
                        count++;
                    }
                }
            }
        }


        // Connect team nodes to sink.
        for (int i = 0; i < numTeams; i++) {
            if (i != teamNum) {
                FlowEdge toSink = new FlowEdge(i, teamNum, w[teamNum] + r[teamNum] - w[i]);
                season.addEdge(toSink);
            }
        }

        // Compute the maxflow for season, find cert of elimination.
        FordFulkerson seasonFlow = new FordFulkerson(season, numTeams, teamNum);
        if (seasonFlow.value() < totalGames) {
            Bag<String> cert = new Bag<String>();
            for (int i = 0; i < numTeams; i++) {
                if (i != teamNum && seasonFlow.inCut(i)) {
                    cert.add((numToName[i]));
                }
            }
            return cert;
        }

        // The team is not eliminated, return null.
        return null;
    }

    private int bin(int n, int k) {
        // Base Cases
        if (k == 0 || k == n)
            return 1;

        // Recursive call
        return bin(n - 1, k - 1) + bin(n - 1, k);
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
