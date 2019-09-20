/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private boolean inverted = false;
    private int height;
    private int width;
    private double[][] energy;
    private int[][] color;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException();
        picture = new Picture(picture);
        height = picture.height();
        width = picture.width();
        energy = new double[width][height];
        color = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                color[i][j] = picture.getRGB(i, j);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                energy[i][j] = calcEnergy(i, j);
            }
        }

    }

    // current picture
    public Picture picture() {
        if (inverted) invertPic();
        Picture newPic = new Picture(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newPic.setRGB(i, j, color[i][j]);
            }
        }
        return newPic;
    }

    // width of current picture
    public int width() {
        if (inverted) return height;
        return width;
    }

    // height of current picture
    public int height() {
        if (inverted) return width;
        return height;
    }

    // Energy of pixel at column x and row y for
    // original orientation.
    public double energy(int x, int y) {
        if (inverted) {
            int temp = x;
            x = y;
            y = temp;
        }
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException();
        }
        return energy[x][y];
    }

    //  Calc energy of pixel at column x and row y for
    // current orientation
    private double calcEnergy(int x, int y) {
        if (x == 0 || x == width - 1 || y == 0 || y == height - 1) return 1000;
        int cLeft = color[x - 1][y];
        int cRight = color[x + 1][y];
        int cUp = color[x][y - 1];
        int cDown = color[x][y + 1];
        double yGrad = gradByColor(cUp, cDown);
        double xGrad = gradByColor(cLeft, cRight);
        return Math.sqrt(xGrad + yGrad);
    }


    // takes integer representations of 2 pixels RGB values
    // and computes their gradient.
    private static double gradByColor(int pixel1, int pixel2) {
        int grad = 0;
        // get rad difference and add to grad.
        int r1 = ((256 * 256 * 256 + pixel1) / 65536);
        int r2 = ((256 * 256 * 256 + pixel2) / 65536);
        grad += Math.pow(r1 - r2, 2);
        // get green difference.
        int g1 = (((256 * 256 * 256 + pixel1) / 256) % 256);
        int g2 = (((256 * 256 * 256 + pixel2) / 256) % 256);
        grad += Math.pow(g1 - g2, 2);
        // get blue difference.
        int b1 = ((256 * 256 * 256 + pixel1) % 256);
        int b2 = ((256 * 256 * 256 + pixel2) % 256);
        grad += Math.pow(b1 - b2, 2);
        return grad;
    }

    // sequence of indices for seam of current orientation going
    // from top to bottom.
    private int[] findSeam() {
        // Set initial cost and edgeTo for nodes.
        double[][] cost = new double[width][height];
        int[][] edgeTo = new int[width][height];
        for (int i = 0; i < width; i++) {
            cost[i][0] = 1000;
            edgeTo[i][0] = -1;
        }
        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                cost[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        // relax edges of all vertices in row major order.
        for (int j = 0; j < height - 1; j++) {
            for (int i = 0; i < width; i++) {
                if (i > 0 && cost[i][j] + energy[i - 1][j + 1] < cost[i - 1][j + 1]) {
                    cost[i - 1][j + 1] = cost[i][j] + energy[i - 1][j + 1];
                    edgeTo[i - 1][j + 1] = i;
                }
                if (cost[i][j] + energy[i][j + 1] < cost[i][j + 1]) {
                    cost[i][j + 1] = cost[i][j] + energy[i][j + 1];
                    edgeTo[i][j + 1] = i;
                }
                if (i < width - 1 && cost[i][j] + energy[i + 1][j + 1] < cost[i + 1][j + 1]) {
                    cost[i + 1][j + 1] = cost[i][j] + energy[i + 1][j + 1];
                    edgeTo[i + 1][j + 1] = i;
                }
            }
        }

        // find node on bottom row with lowest cost.
        int min = 0;
        for (int i = 1; i < width; i++) {
            if (cost[i][height - 1] < cost[min][height - 1]) min = i;
        }

        // put xcoords of nodes on shortest path in order of
        // increasing row.
        int[] shortestPath = new int[height];
        shortestPath[height - 1] = min;
        for (int j = height - 2; j >= 0; j--) {
            shortestPath[j] = edgeTo[shortestPath[j + 1]][j + 1];
        }
        return shortestPath;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        if (inverted) invertPic();
        return findSeam();
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (inverted) return findSeam();
        invertPic();
        return findSeam();
    }

    private void invertPic() {
        // Transpose color matrix.
        int[][] b = new int[color[0].length][color.length];
        for (int j = 0; j < color[0].length; j++) {
            for (int i = 0; i < color.length; i++) {
                b[j][i] = color[i][j];
            }
        }
        color = b;

        double[][] c = new double[energy[0].length][energy.length];
        for (int j = 0; j < energy[0].length; j++) {
            for (int i = 0; i < energy.length; i++) {
                c[j][i] = energy[i][j];
            }
        }
        energy = c;

        inverted = !inverted;
        int temp = width;
        width = height;
        height = temp;
    }

    // Checks if a seam is valid.
    private boolean validSeam(int[] seam, char seamOr) {
        int dist;
        if (seamOr == 'H') {
            dist = height();
            if (seam.length != width()) return false;
            if (seam[0] < 0 || seam[0] >= dist) return false;
            for (int i = 1; i < seam.length; i++) {
                if (seam[i] < 0 || seam[i] >= dist) return false;
                if (Math.abs(seam[i] - seam[i - 1]) > 1) return false;
            }
        }
        else {
            dist = width();
            if (seam.length != height()) return false;
            if (seam[0] < 0 || seam[0] >= dist) return false;
            for (int i = 1; i < seam.length; i++) {
                if (seam[i] < 0 || seam[i] >= dist) return false;
                if (Math.abs(seam[i] - seam[i - 1]) > 1) return false;
            }
        }
        return true;
    }

    // remove vertical seam from current picture
    private void removeSeam(int[] seam) {
        if (seam == null || width <= 1) throw new IllegalArgumentException();
        int[][] newColor = new int[width - 1][height];
        double[][] newEnergy = new double[width - 1][height];

        // update/plug holes in color matrix.
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < seam[j]; i++) newColor[i][j] = color[i][j];
            for (int i = seam[j]; i < width - 1; i++) newColor[i][j] = color[i + 1][j];
        }
        width--;
        color = newColor;

        // update/plug holes in energy matrix.
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) newEnergy[i][j] = calcEnergy(i, j);
        }
        energy = newEnergy;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (!validSeam(seam, 'V')) throw new IllegalArgumentException();
        if (inverted) invertPic();
        removeSeam(seam);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (!validSeam(seam, 'H')) throw new IllegalArgumentException();
        if (!inverted) invertPic();
        removeSeam(seam);
    }

    //  unit testing (optional)
    public static void main(String[] args) {

        /*
        int left = 16763699;
        int right = 16764415;
        int up = 16751001;
        int down = 16777113;

        double yGrad = gradByColor(up, down);
        double xGrad = gradByColor(left, right);
        System.out.print(Math.sqrt(xGrad + yGrad) + "\n");
        */

        /*
        int[][] a = { { 1, 2, 3 }, { 4, 5, 6 } };
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) System.out.print(a[i][j] + "  ");
            System.out.println();
        }
        System.out.println();
        a = invert(a);
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) System.out.print(a[i][j] + "  ");
            System.out.println();
        }

        Picture picture = new Picture(args[0]);
        SeamCarver sc = new SeamCarver(picture);
        System.out
                .printf("Height = %d; Width = %d; Inverted = " + sc.inverted, sc.height, sc.width);
        System.out.println();
        for (int row = 0; row < sc.height(); row++) {
            for (int col = 0; col < sc.width(); col++) {
                StdOut.printf("%9.0f ", sc.energy(col, row));
            }
            StdOut.println();
        }

        System.out.println();
        sc.invertPic();
        System.out
                .printf("Height = %d; Width = %d; Inverted = " + sc.inverted, sc.height, sc.width);
        System.out.println();
        for (int row = 0; row < sc.height(); row++) {
            for (int col = 0; col < sc.width(); col++) {
                StdOut.printf("%9.0f ", sc.energy(col, row));
            }
            StdOut.println();
        }
        System.out.println();
        sc.invertPic();
        System.out
                .printf("Height = %d; Width = %d; Inverted = " + sc.inverted, sc.height, sc.width);
        System.out.println();
        for (int row = 0; row < sc.height(); row++) {
            for (int col = 0; col < sc.width(); col++) {
                StdOut.printf("%9.0f ", sc.energy(col, row));
            }
            StdOut.println();
        }

         */
    }
}
