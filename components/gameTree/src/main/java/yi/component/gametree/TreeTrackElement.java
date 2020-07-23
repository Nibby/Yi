package yi.component.gametree;

import java.util.Optional;

public class TreeTrackElement implements TreeElement {

    @Override
    public Optional<TreeElement> getParent() {
        return Optional.empty();
    }

    @Override
    public int getLogicalX() {
        return 0;
    }

    @Override
    public int getLogicalY() {
        return 0;
    }
}
