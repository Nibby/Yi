package codes.nibby.yi.app.framework;

import java.util.*;
import java.util.stream.Collectors;

public final class ResourcePath {

    // These fields must be at the top of the class since static variables are
    // initialized in sequential order.
    private static final String[] ROOT_FOLDER_PATH = { "codes", "nibby", "yi", "app" };
    private static final String FOLDER_DELIMITER = "/";
    private static final String PACKAGE_DELIMITER = ".";

    public static final ResourcePath SOUNDS = new ResourcePath("sounds");
    public static final ResourcePath ICONS = new ResourcePath("icons");
    public static final ResourcePath FONTS = new ResourcePath("fonts");
    public static final ResourcePath BOARD = new ResourcePath("board");
    public static final ResourcePath BOARD_THEMES = BOARD.resolve("themes");
    public static final ResourcePath I18N = new ResourcePath("i18n");
    public static final ResourcePath SKINS = new ResourcePath("skins");

    private final List<String> pathSegments;
    private String folderPath = null;
    private String filePath = null;
    private String packagePath = null;

    public ResourcePath(String ... resourceFolderSegments) {
        List<String> segments = new ArrayList<>();
        segments.addAll(Arrays.asList(ROOT_FOLDER_PATH));
        segments.addAll(Arrays.asList(resourceFolderSegments));

        pathSegments = Collections.unmodifiableList(segments);
    }

    private ResourcePath(List<String> segments) {
        pathSegments = Collections.unmodifiableList(segments);
    }

    public ResourcePath resolve(String childSegmentName) {
        List<String> newSegments = new ArrayList<>(pathSegments);
        newSegments.add(childSegmentName);
        return new ResourcePath(newSegments);
    }

    public String getFolderPath() {
        synchronized (this) {
            if (folderPath == null) {
                folderPath = getFolderOrFilePath(false);
            }
            return folderPath;
        }
    }

    public String getFilePath() {
        synchronized (this) {
            if (filePath == null) {
                filePath = getFolderOrFilePath(true);
            }
            return filePath;
        }
    }

    private String getFolderOrFilePath(boolean lastSegmentIsFile) {
        var result = new StringBuilder();
        result.append(FOLDER_DELIMITER);

        for (int i = 0; i < pathSegments.size(); i++) {
            String segment = pathSegments.get(i);
            result.append(segment);
            if (i != pathSegments.size() - 1 || !lastSegmentIsFile) {
                result.append(FOLDER_DELIMITER);
            }
        }

        return result.toString();
    }

    public String getPackagePath() {
        synchronized (this) {
            if (packagePath == null) {
                packagePath = String.join(PACKAGE_DELIMITER, pathSegments);
            }
            return packagePath;
        }
    }

    @Override
    public int hashCode() {
        return pathSegments.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourcePath)) {
            return false;
        }

        var other = (ResourcePath) obj;
        return other.pathSegments.equals(this.pathSegments);
    }

    @Override
    public String toString() {
        return pathSegments.stream().collect(Collectors.joining(", ", "[", "]"));
    }
}
