package codes.nibby.yi.io;

public abstract class AbstractGameFile implements IGameFile {

    protected boolean edited = false;

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
