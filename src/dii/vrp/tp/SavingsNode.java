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

    @Override
    public SavingsNode clone() {
        return new SavingsNode(from, to, savings);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof SavingsNode)
        {
            SavingsNode other = (SavingsNode) object;
            return ((savings == other.savings) &&
                    ((from == other.to && to == other.from) || (from == other.from && to == other.to)));
        }
        return false;
    }
}
