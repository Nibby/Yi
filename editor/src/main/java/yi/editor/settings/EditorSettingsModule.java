package yi.editor.settings;

/**
 * Represents all settings for one component in the program. Upon startup, all modules are loaded once.
 * If settings are changed, call {@link #save()} to persist them in file storage.<br />
 * <br />
 * Modules should be self-contained, in the sense that the values in one module must not depend on the
 * values in another. The only exception to this rule is {@link EditorGeneralSettings}, which can be considered
 * a global settings module that others may depend on. As a result, it is always loaded first, so it is
 * safe to access the values in GeneralSettings during {@link #load()}. However, the same cannot be
 * guaranteed for other modules.
 */
abstract class EditorSettingsModule {

    /**
     * Loads all the data associated with this module. If this module is not {@link EditorGeneralSettings}, values
     * from {@link EditorGeneralSettings} can be accessed through {@link EditorSettings#general}. It is not safe to access
     * the values from any other settings module, as they might not be initialized.
     */
    public abstract void load();

    /**
     * Saves all the data associated with this module.
     */
    public abstract void save();

}
