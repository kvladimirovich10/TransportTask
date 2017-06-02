import java.util.LinkedList;

public class Main {

    static Integer[] u = new Integer[4];
    static Integer[] v = new Integer[5];
    static Cells[][] cells = new Cells[4][5];
    static Point pointMin = new Point(0, 0);
    static LinkedList<Cells> path = new LinkedList<Cells>();
    static LinkedList<Cells> cyclePath = new LinkedList<Cells>();

    public static void main(String[] args) {

        Integer[] need = new Integer[]{15, 24, 8, 14, 18};
        Integer[] stock = new Integer[]{25, 13, 21, 20};
        Integer[][] price = new Integer[][]{{4, 6, 7, 8, 7}, {9, 7, 9, 18, 20}, {8, 3, 4, 9, 12}, {6, 1, 13, 21, 6}};
        Direction down = new Direction("down", 2);

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new Cells(i, j, price[i][j], 0);
            }
        }

        System.out.println("Первый план перевозок  " + Optimum.northwest(cells, need, stock, path));

        int i = 0;
        while (!Optimum.checkForOpt(pointMin, cells, u, v, path)) {

            Optimum.newIteration(cells, u, v, cyclePath, path, price);
            System.out.println("i = " + i);


            Optimum.showVector(u);
            Optimum.showVector(v);
            System.out.println(" ");

            cyclePath.addFirst(cells[pointMin.i][pointMin.j]);
            Optimum.buildСyclePath(cells, cells[pointMin.i][pointMin.j], cyclePath, down);

            path.addLast(cyclePath.getFirst());
            Optimum.removeMid(cells, cyclePath);
            Optimum.reBuildPlan(cyclePath, cells, path);


            i++;
        }
        Optimum.plan(cells);
    }


}

class Optimum {

    static int northwest(Cells[][] cells, Integer[] need, Integer[] stock, LinkedList<Cells> path) { //в данном конкретном примере не наблюдается невырожденности, пока забьем

        int stocksum = 0;
        int needsum = 0;
        int k = 0, l = 0;
        int firsttransprice = 0;
        Integer[] newneed = need.clone();
        Integer[] newstock = stock.clone();


        for (int i : stock) stocksum += i;
        for (int i : need) needsum += i;

        System.out.println(stocksum);
        if (stocksum != needsum) {
            System.out.println("Условие баланса не выпонено" + stocksum + "," + needsum);
            return 0;
        }

        while ((k + l) < (newstock.length + newneed.length - 1)) {
            cells[k][l].value = Math.min(newstock[k], newneed[l]);
            cells[k][l].path = true;

            Cells pathPoint = new Cells(k, l);
            path.addLast(pathPoint);

            newstock[k] -= cells[k][l].value;
            newneed[l] -= cells[k][l].value;
            firsttransprice += cells[k][l].value * cells[k][l].price;
            if (newneed[l] == 0)
                l++;
            else
                k++;
        }
        plan(cells);
        return firsttransprice;
    }

    static void plan(Cells[][] cells) {
        int plan = 0;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].path)
                    plan += cells[i][j].price * cells[i][j].value;
            }
        }
        System.out.println("Plan = " + plan);
    }

    private static void potential(Cells[][] cells, Integer[] u, Integer[] v) {

        v[v.length - 1] = 0;

        for (int k = 0; k < 3; k++) {
            for (int i = cells.length - 1; i >= 0; i--)
                for (int j = cells[i].length - 1; j >= 0; j--) {

                    if (cells[i][j].path) {
                        if (v[j] != null) {
                            u[i] = cells[i][j].price - v[j];
                        } else if (u[i] != null) {
                            v[j] = cells[i][j].price - u[i];
                        } else if (v[j] == null & u[i] == null)
                            System.out.println("NULL");
                    }
                }
        }
        System.out.println(" ");

    }

    static boolean checkForOpt(Point pointMin, Cells[][] cells, Integer[] u, Integer[] v, LinkedList<Cells> path) {

        potential(cells, u, v);
        int min = 0;

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (!cells[i][j].path) {
                    cells[i][j].price -= (u[i] + v[j]);
                    if (cells[i][j].price < 0) { // рассмотреть случай с двумя одинаковыми числами
                        if (min > Math.min(min, cells[i][j].price)) {
                            min = cells[i][j].price;
                            pointMin.i = i;
                            pointMin.j = j;
                        }
                    }
                }
            }
        }
        cells[pointMin.i][pointMin.j].path = true;
        if (min == 0)
            return true;
        else
            return false;
    }

    static boolean buildСyclePath(Cells[][] cells, Cells cell, LinkedList<Cells> cycle, Direction from) {

        if (cycle.size() > 1 & cell == cycle.getFirst()) {
            return true;
        }

        Direction up = new Direction("up", 0);
        Direction right = new Direction("right", 1);
        Direction down = new Direction("down", 2);
        Direction left = new Direction("left", 3);

        for (int k = 0; k < 4; k++) {
            if (k == up.value & k != from.value) {
                for (int i = cell.i - 1; i >= 0; i--) {
                    if (cells[i][cell.j].path) {
                        cycle.add(cells[i][cell.j]);
                        if (buildСyclePath(cells, cells[i][cell.j], cycle, down))
                            return true;
                        break;
                    }
                }
            } else if (k == right.value & k != from.value) {
                for (int j = cell.j + 1; j != cells[0].length; j++) {
                    if (cells[cell.i][j].path) {
                        cycle.add(cells[cell.i][j]);
                        if (buildСyclePath(cells, cells[cell.i][j], cycle, left))
                            return true;
                        break;
                    }
                }
            } else if (k == down.value & k != from.value) {
                for (int i = cell.i + 1; i != cells.length; i++) {
                    if (cells[i][cell.j].path) {
                        cycle.add(cells[i][cell.j]);
                        if (buildСyclePath(cells, cells[i][cell.j], cycle, up))
                            return true;
                        break;
                    }
                }
            } else if (k == left.value & k != from.value) {
                for (int j = cell.j - 1; j >= 0; j--) {
                    if (cells[cell.i][j].path) {
                        cycle.add(cells[cell.i][j]);
                        if (buildСyclePath(cells, cells[cell.i][j], cycle, right))
                            return true;
                        break;
                    }
                }
            }
        }
        cycle.removeLast();

        return false;
    }

    private static int sign(Cells[][] cells, LinkedList<Cells> cyclePath) {
        int minOfMinus = 0;
        int div;
        for (int i = 0; i < cyclePath.size(); i++) {
            div = i / 2;
            if (2 * div == i)
                cyclePath.get(i).sign = true;
        }
        for (int k = 0; k < cyclePath.size(); k++) {
            System.out.println(cyclePath.get(k).i + " " + cyclePath.get(k).j + " " + cyclePath.get(k).sign);
        }
        System.out.println();
        return minOfMinus;
    }

    static void removeMid(Cells[][] cells, LinkedList<Cells> cyclePath) {
        for (int i = 2; i < cyclePath.size(); i++) {
            if (cyclePath.get(i - 2).i == cyclePath.get(i - 1).i & cyclePath.get(i).i == cyclePath.get(i - 1).i) {
                cyclePath.remove(i - 1);
            } else if (cyclePath.get(i - 2).j == cyclePath.get(i - 1).j & cyclePath.get(i).j == cyclePath.get(i - 1).j) {
                cyclePath.remove(i - 1);
            }
        }
        cyclePath.removeLast();
        sign(cells, cyclePath);


    }

    static void reBuildPlan(LinkedList<Cells> cyclePath, Cells[][] cells, LinkedList<Cells> path) {


        int delta;
        int lastMin = 0, min = cyclePath.get(1).value, count = 0;


        for (int i = 0; i < cyclePath.size(); i++) {
            if (!cyclePath.get(i).sign) {
                Optimum.showMatrixValue(cells);
                if (min >= cyclePath.get(i).value) {
                    count = i;
                }
            }


        }

        Cells minMinus = cyclePath.get(count);
        showMatrixValue(cells);
        delta = minMinus.value;
        showMatrixSign(cells);

        System.out.println("minVal on path of minuses = " + minMinus.value + ", " + minMinus.i + " " + minMinus.j);
        System.out.println();


        for (int i = 0; i < cyclePath.size(); i++) {
            if (cyclePath.get(i).i == minMinus.i & cyclePath.get(i).j == minMinus.j) {
                cells[cyclePath.get(0).i][cyclePath.get(0).i].path = true;
                cells[cyclePath.get(i).i][cyclePath.get(i).j].path = false;
            }
        }


        for (int i = 0; i < cyclePath.size(); i++) {
            if (cyclePath.get(i).sign)
                cells[cyclePath.get(i).i][cyclePath.get(i).j].value += delta;
            else
                cells[cyclePath.get(i).i][cyclePath.get(i).j].value -= delta;
        }

        showMatrixValue(cells);
        showMatrixPrice(cells);


        putFalse(cells);
    }

    static void newIteration(Cells[][] cells, Integer[] u, Integer[] v, LinkedList<Cells> cyclePath, LinkedList<Cells> path, Integer[][] price) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j].price = price[i][j];
                u[i] = null;
                v[j] = null;

            }
        }
        cyclePath.clear();
        path.clear();

        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j].path)
                    path.addLast(cells[i][j]);
            }
        }
    }

    static void showMatrixValue(Cells[][] matrix) {
        System.out.println("Value");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j].value + "  ");
            }
            System.out.println();
        }
        System.out.println();
    }

    static void showMatrixPrice(Cells[][] matrix) {
        System.out.println("Price");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j].price + "  ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    static void showMatrixSign(Cells[][] matrix) {
        System.out.println("Sign");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j].sign + "  ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    static void showMatrixSignPath(Cells[][] matrix) {
        System.out.println("Sign Path");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j].path + "  ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    static void showVector(Integer[] vector) {
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] + " ");
        }
        System.out.println();
    }

    static void putFalse(Cells[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j].sign = false;
            }
        }
    }

}

class Point {
    int i;
    int j;
    int value;

    Point(int i, int j, int value) {
        this.i = i;
        this.j = j;
        this.value = value;
    }

    Point(int i, int j) {
        this.i = i;
        this.j = j;
    }
}

class Cells {
    int i;
    int j;
    int price;
    int value;
    boolean path;
    boolean sign;

    Cells(int i, int j) {
        this.i = i;
        this.j = j;
    }

    Cells(int i, int j, int price, int value) {
        this.i = i;
        this.j = j;
        this.price = price;
        this.path = false;
        this.sign = false;
        this.value = value;
    }

    Cells(int i, int j, int price) {
        this.i = i;
        this.j = j;
        this.price = price;
        this.path = false;
        this.sign = false;
    }

}

class Direction {
    String name;
    int value;

    Direction(String name, int value) {
        this.name = name;
        this.value = value;
    }

}
