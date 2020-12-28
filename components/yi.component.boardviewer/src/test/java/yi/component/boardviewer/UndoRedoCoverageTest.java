package yi.component.boardviewer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yi.component.boardviewer.editmodes.AbstractEditMode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * A meta-test that checks if a test suite has been written for a {@link AbstractEditMode }.
 *
 * It is vital for undo/redo system to ensure the integrity of each edit state, therefore each
 * edit mode must supply a rigorous set of tests to ensure the undo/redo mechanism works correctly
 * for it.
 *
 * The test suites should be placed within the unit test package under {@code test/unit/yi.component.board}
 */
public final class UndoRedoCoverageTest {

    @Test
    public void testAllEditModesHaveTestSuite() throws IOException, ClassNotFoundException {
        var classLoader = Thread.currentThread().getContextClassLoader();

        Assertions.assertNotNull(classLoader, "Class loader cannot be instantiated");

        String packagePath = "yi/component/board";
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        var directories = new HashSet<File>();

        while (resources.hasMoreElements()) {
            var resource = resources.nextElement();
            directories.add(new File(resource.getFile()));
        }

        var editModeClasses = new HashSet<Class<?>>();
        for (File dir : directories) {
            editModeClasses.addAll(findAbstractEditModeClasses(dir, packagePath.replace("/", ".")));
        }

        for (Class<?> editModeClass : editModeClasses) {
            String className = editModeClass.getSimpleName();
            String testClassName = className + "UndoRedoTest";

            int lastDot = editModeClass.getName().lastIndexOf(".");
            String packageName = editModeClass.getName().substring(0, lastDot);
            String testClassFullyQualifiedName = packageName + "." + testClassName;
            try {
                Class<?> abstractEditModeClass = Class.forName(testClassFullyQualifiedName);

                boolean hasAtLeastOneTest = false;
                for (Method method : abstractEditModeClass.getMethods()) {
                    if (method.isAnnotationPresent(Test.class)) {
                        hasAtLeastOneTest = true;
                        break;
                    }
                }

                if (!hasAtLeastOneTest) {
                    Assertions.fail(testClassFullyQualifiedName + " must have at least one test written. Use @org.junit.jupiter.api.Test to label a method as test. " +
                            "Don't try to cheat the system ;-)");
                }
            } catch (ClassNotFoundException e) {
                Assertions.fail("Cannot find test class for '" + className + "'. The test class should be at: " + testClassFullyQualifiedName);
            }
        }
    }

    private Set<Class<?>> findAbstractEditModeClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) {
            return Collections.emptySet();
        }

        File[] files = directory.listFiles();
        var results = new HashSet<Class<?>>();

        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                Set<Class<?>> subDirResults = findAbstractEditModeClasses(file, packageName + "." + dirName);
                results.addAll(subDirResults);
            } else if (file.getName().endsWith(".class")){
                String className = file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(packageName + "." + className);
                if (AbstractEditMode.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    results.add(clazz); // Should be fine if we checked for assignability.
                }
            }
        }

        return results;
    }

}
