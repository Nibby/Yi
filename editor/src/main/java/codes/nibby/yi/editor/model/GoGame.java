package codes.nibby.yi.editor.model;

public class GoGame {

    private int gridsHorizontal;
    private int gridsVertical;

    public GoGame(int gridsHorizontal, int gridsVertical) {
        this.gridsHorizontal = gridsHorizontal;
        this.gridsVertical = gridsVertical;
    }

    public int getGridsHorizontal() {
        return gridsHorizontal;
    }

    public int getGridsVertical() {
        return gridsVertical;
    }
}
