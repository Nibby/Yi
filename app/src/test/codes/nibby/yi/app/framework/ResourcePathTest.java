package codes.nibby.yi.app.framework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ResourcePathTest {

    private static final String EXPECTED_ROOT_FOLDER_PATH = "/codes/nibby/yi/app/";
    private static final String EXPECTED_ROOT_PACKAGE_PATH = "codes.nibby.yi.app";

    @Test
    public void testRootDirectory() {
        var path = new ResourcePath();
        Assertions.assertEquals(EXPECTED_ROOT_PACKAGE_PATH, path.getPackagePath());
        Assertions.assertEquals(EXPECTED_ROOT_FOLDER_PATH, path.getFolderPath());
    }

    @Test
    public void testResolve() {
        var path = new ResourcePath();
        var testSegPath = path.resolve("testSegment");

        Assertions.assertEquals(EXPECTED_ROOT_PACKAGE_PATH + ".testSegment", testSegPath.getPackagePath());
        Assertions.assertEquals(EXPECTED_ROOT_FOLDER_PATH + "testSegment/", testSegPath.getFolderPath());
        Assertions.assertEquals(EXPECTED_ROOT_FOLDER_PATH + "testSegment", testSegPath.getFilePath());
    }

    @Test
    public void testEquals() {

    }

    @Test
    public void testHashCode() {

    }

}
