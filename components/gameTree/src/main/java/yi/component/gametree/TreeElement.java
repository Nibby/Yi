package yi.component.gametree;

import java.util.Optional;

public interface TreeElement {

    Optional<TreeElement> getParent();

    int getLogicalX();

    int getLogicalY();

}
