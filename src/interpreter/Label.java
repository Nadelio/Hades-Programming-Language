package src.interpreter;

public class Label {
    public String alias;
    public int address;

    public Label(String alias, int address) {
        this.alias = alias;
        this.address = address;
    }
}
