package dii.vrp.tp;

public class SavingsNode {
    private int from;
    private int to;
    private double savings;

    public SavingsNode(int c1, int c2, double savings) {
        this.from = c1;
        this.to = c2;
        this.savings = savings;
    }

    public int getFrom() {
        return this.from;
    }

    public int getTo() {
        return this.to;
    }

    public double getSavings() {
        return this.savings;
    }
}
