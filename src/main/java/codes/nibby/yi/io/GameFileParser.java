package codes.nibby.yi.io;

import codes.nibby.yi.game.Game;

import java.io.File;
import java.io.IOException;

public class GameFileParser {

    public static Game parse(File file) throws IOException, GameParseException, UnsupportedFileTypeException {
        if (file == null)
            return null;

        if (file.getName().endsWith(".sgf"))
            return SgfFile.parse(file);

        throw new UnsupportedFileTypeException("Unrecognized file format: " + file.getName().toString());
    }

}
