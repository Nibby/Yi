package codes.nibby.yi.app.components;

import codes.nibby.yi.app.framework.AppWindow;
import codes.nibby.yi.app.framework.AppAccelerator;
import codes.nibby.yi.app.framework.AppText;
import codes.nibby.yi.app.framework.action.AppAction;
import codes.nibby.yi.app.framework.action.AppActionContext;
import codes.nibby.yi.app.framework.action.AppRadioAction;
import codes.nibby.yi.app.framework.action.AppSubMenuAction;
import codes.nibby.yi.app.settings.AppSettings;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.i18n.TextResource;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A series of window component layout strategies depending on the editor use case.
 */
public enum AppPerspective {

    // Note: The enum name itself is stored and loaded from general preference files, changing its value
    //       will break backwards compatibility.

    /**
     * A minimal layout that focuses on the game board.
     */
    COMPACT(AppText.MENUITEM_PERSPECTIVE_COMPACT, AppAccelerator.TOGGLE_PERSPECTIVE_COMPACT) {
        @Override
        public Parent getContent(AppWindow window) {
            var content = new BorderPane();

            var board = window.getBoardComponent();
            content.setCenter(board);
            content.setBottom(window.getFooterToolBar());

            return content;
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            return new Dimension2D(500, 600);
        }

        @Override
        protected Optional<AppAction> createAction(AppSubMenuAction submenu) {
            return Optional.of(AppPerspective.createPerspectiveAction(this, submenu, 0.001d));
        }
    },

    /**
     * Layout with extra editing and navigation tools.
     */
    EDIT(AppText.MENUITEM_PERSPECTIVE_REVIEW, AppAccelerator.TOGGLE_PERSPECTIVE_EDIT) {
        @Override
        public Parent getContent(AppWindow window) {
            var content = new BorderPane();

            var boardpane = new BorderPane();
            var board = window.getBoardComponent();
            boardpane.setCenter(board);
            boardpane.setBottom(window.getFooterToolBar());

            var tree = window.getTreeComponent();
            var commentArea = window.getCommentComponent();

            var sideSplit = new SplitPane(tree, commentArea);
            sideSplit.setOrientation(Orientation.VERTICAL);
            sideSplit.getDividers().get(0).setPosition(0.6d);

            var splitPane = new SplitPane(boardpane, sideSplit);
            content.setCenter(splitPane);

            SplitPane.setResizableWithParent(commentArea, false);
            SplitPane.setResizableWithParent(sideSplit, false);

            splitPane.getDividers().get(0).setPosition(0.7d);
            splitPane.getDividers().get(0).positionProperty().addListener(positionChanged -> {
                double position = splitPane.getDividerPositions()[0];
                // Prevent sidebar covering too much board content
                if (position < 0.7d) {
                    splitPane.setDividerPosition(0, 0.7d);
                }

                if (position > 0.85d) {
                    splitPane.setDividerPosition(0, 0.85d);
                }
            });

            return content;
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            return new Dimension2D(800, 650);
        }

        @Override
        protected Optional<AppAction> createAction(AppSubMenuAction submenu) {
            return Optional.of(AppPerspective.createPerspectiveAction(this, submenu, 0.002d));
        }
    },

    // Value created solely for the purpose of keeping the layout property non-null in EditorFrame.
    NONE(AppText.EMPTY, null) {
        @Override
        public Parent getContent(AppWindow window) {
            throw new UnsupportedOperationException("Should not be able to set perspective to NONE");
        }

        @Override
        public Dimension2D getMinimumWindowSize() {
            throw new UnsupportedOperationException("Should not be able to set perspective to NONE");
        }

        @Override
        protected Optional<AppAction> createAction(AppSubMenuAction submenu) {
            return Optional.empty();
        }
    }

    ;

    private static final ToggleGroup MENU_ACTION_TOGGLE_GROUP = new ToggleGroup();

    private final TextResource friendlyName;
    private final AppAccelerator acceleratorId;

    /**
     * Creates a new type of supported layout for {@link AppWindow}.
     *
     * @param friendlyName User friendly name of this layout
     */
    AppPerspective(TextResource friendlyName, AppAccelerator acceleratorId) {
        this.friendlyName = friendlyName;
        this.acceleratorId = acceleratorId;
    }

    /**
     *
     * @return User-friendly name for this layout.
     */
    public TextResource getFriendlyName() {
        return friendlyName;
    }

    /**
     *
     * @return The unique identifier for the accelerator for this layout.
     */
    public AppAccelerator getAcceleratorId() {
        return acceleratorId;
    }

    /**
     * Creates a container that has the window components arranged by this perspective.
     * The client can set the output container as its primary content.
     *
     * @param window The window to arrange.
     * @return The container with the layout defined by this perspective.
     */
    public abstract Parent getContent(AppWindow window);

    /**
     *
     * @return The default window dimensions when {@link AppWindow} is first launched
     *         with this layout selected.
     */
    public abstract Dimension2D getMinimumWindowSize();

    /**
     * Creates one selectable {@link AppAction} associated with this view perspective.
     * This is primarily used in the main menu to list available perspectives.
     * <p/>
     * If the provided submenu is not null, the perspective action must be added as its
     * child through {@link AppSubMenuAction#addChildAction(AppAction)}.
     * <p/>
     * If this perspective does not expose an action, return {@link Optional#empty()}.
     *
     * @param submenu Sub-menu that the perspective action belongs in.
     * @return An optional action that exposes this perspective to the main menu.
     */
    protected abstract Optional<AppAction> createAction(AppSubMenuAction submenu);

    /**
     * Each layout may display extra (or fewer) components which require a custom adequate
     * aspect ratio. This value will be used to adjust window size upon switching to the
     * layout so that its contents can fit properly within the window.
     *
     * @return The preferred width/height ratio for this layout.
     */
    public double getPreferredAspectRatio() {
        var startupSize = getMinimumWindowSize();
        return startupSize.getWidth() / startupSize.getHeight();
    }

    @Override
    public String toString() {
        return getFriendlyName().getLocalisedText();
    }

    /**
     *
     * @return The default content layout.
     */
    public static AppPerspective getDefaultValue() {
        return COMPACT;
    }

    /**
     *
     * @param serializedValue The content layout setting value serialized as a string.
     * @return The enum value equivalent of the serialized value, or {@link #getDefaultValue()}
     *         if the value is malformed or unsupported.
     */
    public static AppPerspective getValue(@Nullable String serializedValue) {
        try {
            return AppPerspective.valueOf(serializedValue);
        } catch (IllegalArgumentException e) {
            return getDefaultValue(); // Fail gracefully to default options.
        }
    }

    public static AppAction[] createActions() {
        var perspectiveSubmenu = new AppSubMenuAction(AppText.MENU_PERSPECTIVE);
        perspectiveSubmenu.setInMenuBar(AppMainMenuType.VIEW, 1.0d);

        var result = new HashSet<AppAction>();
        for (int i = 0; i < values().length; ++i) {
            values()[i].createAction(perspectiveSubmenu)
                       .ifPresent(result::add);
        }
        if (result.size() > 0) {
            result.add(perspectiveSubmenu);
        }

        return result.toArray(new AppAction[0]);
    }

    private static AppRadioAction createPerspectiveAction(
        AppPerspective thisPerspective,
        AppSubMenuAction submenu,
        double position
    ) {
        Consumer<AppActionContext> action = helper -> helper.getInvokerWindow().setPerspective(thisPerspective);
        var radioAction = new AppRadioAction(thisPerspective.getFriendlyName(), action) {
            @Override
            public void refreshState(AppActionContext context) {
                super.refreshState(context);
                var window = context.getInvokerWindow();
                setSelected(window.getPerspective() == thisPerspective);
            }
        };
        radioAction.setInMenuBar(AppMainMenuType.VIEW, position);
        radioAction.setSelected(AppSettings.general.getPerspective() == thisPerspective);
        radioAction.setMenuToggleGroup(MENU_ACTION_TOGGLE_GROUP);
        radioAction.setAccelerator(thisPerspective.getAcceleratorId());

        if (submenu != null) {
            submenu.addChildAction(radioAction);
        }

        return radioAction;
    }
}
