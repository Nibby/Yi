package codes.nibby.yi.io;

import codes.nibby.yi.Yi;
import codes.nibby.yi.game.*;
import codes.nibby.yi.game.rules.GameRules;
import codes.nibby.yi.game.rules.IGameRules;
import codes.nibby.yi.game.rules.ProposalResult;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static codes.nibby.yi.io.SgfFile.Key.*;

/**
 * Parses a smart game format file into a Game object.
 * Ported code from WeiqiTool (old Yi codebase from 4 Nov 2016)
 *
 * @author Kevin Yang
 * Created on 24 October 2019
 * @see codes.nibby.yi.game.Game
 */
public class SgfFile {

    private static final String POINT_MAP = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @NotNull
    public static Game parse(@NotNull File resource) throws IOException, GameParseException {
        if (!resource.getName().endsWith(".sgf"))
            throw new GameParseException("File is not of .sgf file type!");

        BufferedReader reader = Files.newBufferedReader(resource.toPath());
        // Read raw data
        StringBuilder buffer = new StringBuilder();
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            buffer.append(nextLine).append("\n");
        }

        Game game;
        String data = buffer.toString();
//
//        // TODO Manual trimming OGS game data?
//        data = data.replace("-- chat --", "").replace("\n\n\n", "")
//                .replace("[Object object]", "");

        Stack<BranchData> dataStack = new Stack<>();
        buffer.delete(0, buffer.length());

        SgfFile.BranchData root = new SgfFile.BranchData();
        dataStack.push(root);

        char previous = '\0';
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);

            if (c == '(') {
                SgfFile.BranchData childSet = new SgfFile.BranchData();

                if (!dataStack.isEmpty())
                    dataStack.peek().addChild(childSet);
                else
                    root.addChild(childSet);

                dataStack.push(childSet);
            }

            if (dataStack.isEmpty() && c != '(')
                break;

            if (!dataStack.isEmpty() && dataStack.peek() != null && c == ';') {
                boolean readValue = false;
                char ch = data.charAt(i + 1);

                while (i + 1 < data.length() - 2) {
                    if (ch == '[')
                        readValue = true;

                    if (!readValue && (ch == ';' || ch == ')' || ch == '(')) {
                        break;
                    } else {
                        if (ch == ']' && previous != '\\')
                            readValue = false;
                    }

                    if (i + 1 > data.length() - 2)
                        break;
                    else {
                        if (ch != '\n' || readValue) {
                            buffer.append(ch);
                            previous = ch;
                        }
                        ++i;
                        ch = data.charAt(i + 1);
                    }

                }

                dataStack.peek().addData(buffer.toString());
                buffer.delete(0, buffer.length());
            }

            if (c == ')') {
                dataStack.pop();
                buffer.delete(0, buffer.length());
            }
        }

        if (root.getChildren().size() <= 0)
            throw new GameParseException("SGF record is missing root node!");

        // Parse header
        String sgfHeader = root.getChildren().get(0).getData().get(0);
        Map<SgfFile.Key, List<String>> headerProperties = splitDataTags(sgfHeader);

        if (headerProperties.get(GAME_MODE) != null
                && !headerProperties.get(GAME_MODE).get(0).equals("1")) {

            throw new GameParseException("GM type returned " +
                    headerProperties.get(GAME_MODE).get(0) + ", expected 1 for Go game records!");
        }

        // TODO: Board size may be rectangular?
        int boardWidth, boardHeight;
        IGameRules rules;
        try {
            String boardSizeString = headerProperties.get(BOARD_SIZE).get(0);
            boolean rectangular = boardSizeString.contains(":");
            if (!rectangular)
                boardWidth = boardHeight = Integer.parseInt(boardSizeString);
            else {
                String[] boardSizeData = boardSizeString.split(":");
                try {
                    boardWidth = Integer.parseInt(boardSizeData[0]);
                    boardHeight = Integer.parseInt(boardSizeData[1]);
                } catch (NumberFormatException e) {
                    throw new GameParseException("Malformed game board size: " + boardSizeString);
                }
            }
            rules = GameRules.getRuleset(headerProperties.get(RULESET).get(0), true);
        } catch (NumberFormatException e) {
            throw new GameParseException("Unrecognized board size: " + headerProperties.get(BOARD_SIZE).get(0) + "!");
        } catch (NullPointerException e) {
            throw new GameParseException("Unable to read root node header data.");
        }

        game = new Game(rules, boardWidth, boardHeight);

        // Parse moves
        GameNode rootNode = parseGameNode(sgfHeader, game, null);
        rootNode.setStoneData(new int[boardWidth * boardHeight]);
        Map<Integer, List<GameNode>> moveTree = parseMoveTree(root, true, 1, rootNode, game);
        // TODO implement later
//        if (moveTree.size() > 0 && moveTree.get(0).size() > 0)
//            sgf.getGameRecord().getGoban().update(moveTree.get(0).get(0), false);
//
//        // update liberty info on board position data
//        SgfFile.getGameRecord().getMoveTree().set(moveTree);
//
////             dumpNode(root, 0);

        return game;
    }

    public static Map<Integer, List<GameNode>> parseMoveTree(SgfFile.BranchData sgfData, boolean isRoot, int startTurn,
                                                             GameNode root, Game game) throws GameParseException {

        Map<Integer, List<GameNode>> result = new HashMap<>();
        result.put(0, new ArrayList<>());
        if (isRoot)
            result.get(0).add(root);

        // TODO file metadata support
//        String sgfVersion = sgf.getHeaderData(FILE_FORMAT);
//        if(sgfVersion == null)
//            throw new GameParseException("SGF file version is undefined!");
//        if(sgfVersion.trim().isEmpty())
//            throw new GameParseException("SGF file version is missing!");

        int turn = startTurn;
        List<String> data = (isRoot) ? sgfData.getChildren().get(0).getData() : sgfData.getData();
        GameNode previousMove = root;

        for (int i = isRoot ? 1 : 0; i < data.size(); i++) {
            GameNode node = parseGameNode(data.get(i), game, previousMove);

            if (isRoot && i == 1 || !isRoot && i == 0) {
                root.addChild(node, true);
            } else {
                previousMove.addChild(node);
            }
            // TODO file metadata support
//            // [tt] is a pass for earlier versions
//            if (!sgfVersion.equals("4") && node.getCurrentMove()[0] == 19 && node.getCurrentMove()[1] == 19) {
//                node.setCurrentMove(new int[] { -1, -1 });
//                node.setPass(true);
//            }

            result.putIfAbsent(turn, new ArrayList<>());
            result.get(turn).add(node);
            previousMove = node;
            ++turn;
        }

        for (SgfFile.BranchData childNode : (isRoot) ? sgfData.getChildren().get(0).getChildren() : sgfData.getChildren()) {
            Map<Integer, List<GameNode>> childTree = parseMoveTree(childNode, false, turn, previousMove, game);
            for (int move : childTree.keySet()) {
                result.putIfAbsent(move, new ArrayList<>());
                result.get(move).addAll(childTree.get(move));
            }
        }

        return result;
    }

    public static GameNode parseGameNode(String data, Game game, GameNode parentNode) throws GameParseException {
        Map<SgfFile.Key, List<String>> propertyData = splitDataTags(data);
        String position = null;
        int player = 0; // None (e.g. root)
        List<String> helperPositions = new ArrayList<>();
        List<Integer[]> helperStoneData = new ArrayList<>();
        List<String> clearPoints = new ArrayList<>();
        GameNode node = new GameNode(game, parentNode);

        // Addition stones
        if (propertyData.containsKey(ADD_BLACK)) {
            for (String helperPos : propertyData.get(ADD_BLACK))
                helperPositions.add("B:" + helperPos);

            player = Game.COLOR_NONE;
        }

        if (propertyData.containsKey(ADD_WHITE)) {
            for (String helperPos : propertyData.get(ADD_WHITE))
                helperPositions.add("W:" + helperPos);

            player = Game.COLOR_NONE;
        }

        if (propertyData.containsKey(CLEAR_POINT)) {
            clearPoints.addAll(propertyData.get(CLEAR_POINT));

            player = Game.COLOR_NONE;
        }

        // Standard play
        if (propertyData.containsKey(BLACK_MOVE))
            player = Game.COLOR_BLACK;
        else if (propertyData.containsKey(WHITE_MOVE))
            player = Game.COLOR_WHITE;

        // Parse marker stones
        if (helperPositions.size() > 0) {
            for (String pos : helperPositions) {
                String[] keyValue = pos.split(":");
                // TODO check if this assumption is 100% correct when I'm less tired
                int color = keyValue[0].equals("B") ? Game.COLOR_BLACK : Game.COLOR_WHITE;
                String helperPos = keyValue[1];

                int hx = POINT_MAP.indexOf(helperPos.charAt(0));
                int hy = POINT_MAP.indexOf(helperPos.charAt(1));

                helperStoneData.add(new Integer[]{hx, hy, color});
            }
        }

        if (player != Game.COLOR_NONE)
            position = propertyData.get(player == Game.COLOR_BLACK
                    ? BLACK_MOVE : WHITE_MOVE).get(0);
        else
            position = null;


        // Comments
        String comment;
        List<String> commentList = propertyData.get(COMMENT);
        comment = commentList != null ? commentList.get(0) : "";

        // Annotations/markers
        SgfFile.Key[] markerKeys = {MARKER_TRIANGLE,
                MARKER_SQUARE,
                MARKER_CIRCLE,
                MARKER_CROSS,
                MARKER_LABEL,
                MARKER_ARROWS,
                MARKER_DIM_STONE,};

        List<Markup> markups = new ArrayList<>();
        for (int markType = 0; markType < markerKeys.length; markType++) {
            List<String> markerDataSet = propertyData.get(markerKeys[markType]);
            if (markerDataSet != null && !markerDataSet.isEmpty()) {
                for (String markerData : markerDataSet) {
                    Markup markup = Markup.parseSgf(markType, markerData);
                    markups.add(markup);
                }
            }
        }

        // Parsing position
        int x, y;

        if (position != null && !position.isEmpty()) {
            try {
                x = POINT_MAP.indexOf(position.charAt(0));
                y = POINT_MAP.indexOf(position.charAt(1));
            } catch (Exception e) {
                throw new GameParseException("Malformed move #" + node.getMoveNumber() + ", co-ordinate '" + position + "'!");
            }
        } else {
            x = -1;
            y = -1;
        }

        node.setComments(comment);
        node.getMarkups().addAll(markups);
        node.setCurrentMove(new int[]{x, y});
        ProposalResult result = game.getRuleset().proposeMove(game, player, x, y);
        game.submitMove(result);

        // TODO implement later.
//        // Add helper stones
//        for(Integer[] stoneData : helperStoneData) {
//            node.addHelperStone(stoneData[0], stoneData[1], stoneData[2]);
//        }
//
//        // Clearing points
//        for(String clearPoint : clearPoints) {
//            x = POINT_MAP.indexOf(clearPoint.charAt(0));
//            y = POINT_MAP.indexOf(clearPoint.charAt(1));
//            node.addErasedPoint(x, y);
//        }
//
//        if(x == -1 && y == -1 && node.getHelperStones().size() == 0
//                && node.getErasedPoints().size() == 0)
//            node.setPass(true);

        return node;
    }

    public static Map<SgfFile.Key, List<String>> splitDataTags(String data) {
        Map<SgfFile.Key, List<String>> result = new HashMap<>();
        StringBuilder buffer = new StringBuilder();
        String key = "", lastKey = "", value = "";
        boolean readValue = false;
        boolean forwardKey = false;

        char previous = '\0';
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (!readValue) {
                if (ch == '[') {
                    if (previous == ']') {
                        //Continuous value following previous key
                        readValue = true;
                        forwardKey = true;
                    } else {
                        key = buffer.toString();

                        if (!forwardKey)
                            lastKey = key;

                        readValue = true;
                        buffer.delete(0, buffer.length());
                    }
                    previous = ch;
                    continue;
                } else if (forwardKey) {
                    forwardKey = false;
                }
            } else {
                if (ch == ']' && previous != '\\') {
                    //Reading value
                    value = buffer.toString();
                    value = value.replace("\\", "");
                    SgfFile.Key hKey = get(forwardKey ? lastKey : key);
                    result.putIfAbsent(hKey, new ArrayList<>());
                    result.get(hKey).add(value);
                    buffer.delete(0, buffer.length());
                    readValue = false;
                    previous = ch;
                    continue;
                }
            }

            buffer.append(ch);
            previous = ch;
        }

        return result;
    }

    public static void write(@NotNull Path path, @NotNull Game game) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path);
        writer.append("(;");

        // Determine board size string
        int boardWidth = game.getBoardWidth();
        int boardHeight = game.getBoardHeight();
        boolean square = boardWidth == boardHeight;
        String boardSize;
        if (square)
            boardSize = String.valueOf(boardWidth);
        else
            boardSize = boardWidth + ":" + boardHeight;

        // Move list data
        GameNode root = game.getGameTree();
        GameMetadata meta = game.getMetadata();
        writeTag(writer, APPLICATION, Yi.NAME);
        writeTag(writer, GAME_MODE, "1");
        writeTag(writer, BOARD_SIZE, boardSize);
        writeTag(writer, AUTHOR, meta.author);
        writeTag(writer, GAME_NAME, meta.gameName);
        writeTag(writer, EVENT, meta.eventName);
        writeTag(writer, ROUND, meta.round);
        writeTag(writer, RULESET, meta.ruleset);
        writeTag(writer, SOURCE, meta.source);
        writeTag(writer, DATE, meta.datePlayed);
        writeTag(writer, LOCATION, meta.location);
        writeTag(writer, COPYRIGHT, meta.copyright);

        writeTag(writer, HANDICAP, String.valueOf(meta.handicaps));
        writeTag(writer, KOMI, meta.komi);
        writeTag(writer, MAIN_TIME, meta.mainTime);
        writeTag(writer, OVERTIME, meta.overtime);

        writeTag(writer, BLACK_NAME, meta.playerBlackName);
        writeTag(writer, BLACK_RANK, meta.playerBlackRank);
        writeTag(writer, BLACK_TEAM, meta.playerBlackTeam);
        writeTag(writer, WHITE_NAME, meta.playerWhiteName);
        writeTag(writer, WHITE_RANK, meta.playerWhiteRank);
        writeTag(writer, WHITE_TEAM, meta.playerWhiteTeam);

        writeTag(writer, RESULT, meta.result);

        // Write initial setup stones, markers in the root node
        writeNodeMarkups(writer, game, root);
        writeNodeHelperStones(writer, game, root);
        writeNodeComments(writer, game, root);

        // Write standard branch
        writeMoveBranch(writer, game, root);
        writer.append(")");
        writer.flush();
        writer.close();

        game.setModified(false);
    }

    private static void writeMoveBranch(BufferedWriter writer, Game game, GameNode node) throws IOException {
        writeNodeData(writer, game, node);
        writer.append("\n");

        if (node.getChildren().size() > 1) {
            for (GameNode variation : node.getChildren()) {
                writer.append("(");
                writeMoveBranch(writer, game, variation);
                writer.append(")\n");
            }
        } else if (node.getChildren().size() == 1) {
            writeMoveBranch(writer, game, node.getChildren().get(0));
        }
    }

    private static void writeNodeData(BufferedWriter writer, Game game, GameNode node) throws IOException {
        // Skip root node
        if (node.getMoveNumber() == 0)
            return;

        int[] move = node.getCurrentMove();
        int x = move[0];
        int y = move[1];
        int player = node.getColor();
        writer.append(";");

        // Write node position
        if (x != -1 && y != -1 && player != Game.COLOR_NONE) {
            writeTag(writer,
                    player == Game.COLOR_BLACK
                            ? SgfFile.Key.BLACK_MOVE : SgfFile.Key.WHITE_MOVE,
                    toSGFPoint(x, y));
        } else {
            if (node.getMoveNumber() > 0) {
                //TODO check erasure node

                //TODO what is this condition for?
                if (node.getClearPoints().size() == 0) {
                    // Pass (sgf4 format, no tt)
                    // TODO Is it okay to assume this condition?
                    // By this I mean, if not black then it has to be white.
                    writeTag(writer, player == Game.COLOR_BLACK
                            ? SgfFile.Key.BLACK_MOVE
                            : SgfFile.Key.WHITE_MOVE, "");
                }
            }
        }

        writeNodeHelperStones(writer, game, node);
        writeNodeMarkups(writer, game, node);
        writeNodeComments(writer, game, node);
        writeNodeClearPoints(writer, game, node);
    }

    private static void writeNodeComments(@NotNull BufferedWriter writer, @NotNull Game game, @NotNull GameNode node) throws IOException {
        if (node.getComments() != null && !node.getComments().trim().isEmpty()) {
            String comment = node.getComments().replace("]", "\\]");
            writeTag(writer, SgfFile.Key.COMMENT, comment);
        }
    }

    private static void writeNodeHelperStones(@NotNull BufferedWriter writer, @NotNull Game game, @NotNull GameNode node) throws IOException {
        int width = game.getBoardWidth();
        for (Integer helper : node.getHelperStonesBlack()) {
            int hx = helper % width;
            int hy = helper / width;
            writeTag(writer, ADD_BLACK, toSGFPoint(hx, hy));
        }
        for (Integer helper : node.getHelperStonesWhite()) {
            int hx = helper % width;
            int hy = helper / width;
            writeTag(writer, ADD_WHITE, toSGFPoint(hx, hy));
        }
    }

    private static void writeNodeClearPoints(BufferedWriter writer, Game game, GameNode node) throws IOException {
        int width = game.getBoardWidth();
        for (Integer point : node.getClearPoints()) {
            int px = point % width;
            int py = point / width;
            writeTag(writer, SgfFile.Key.CLEAR_POINT, toSGFPoint(px, py));
        }
    }

    private static void writeNodeMarkups(BufferedWriter writer, Game game, GameNode node) throws IOException {
        for (Markup markup : node.getMarkups()) {
            MarkupType type = markup.getType();
            SgfFile.Key annotationKey = null;
            int x1 = markup.getX1(); // Start X
            int y1 = markup.getY1(); // Start Y
            int x2 = markup.getX2(); // End X (for lines / arrows)
            int y2 = markup.getY2(); // End Y (for lines / arrows)
            String data = toSGFPoint(x1, y1);
            switch (type) {
                case TRIANGLE:
                    annotationKey = MARKER_TRIANGLE;
                    break;
                case CIRCLE:
                    annotationKey = MARKER_CIRCLE;
                    break;
                case SQUARE:
                    annotationKey = MARKER_SQUARE;
                    break;
                case CROSS:
                    annotationKey = MARKER_CROSS;
                    break;
                case LABEL:
                    annotationKey = MARKER_LABEL;
                    data += ":" + markup.getArguments().split(":")[1];
                    break;
                // TODO implement lines and arrows
            }

            if (annotationKey != null) {
                writeTag(writer, annotationKey, data);
            }
        }
    }

    private static void writeTag(@NotNull BufferedWriter writer, @NotNull SgfFile.Key key, @NotNull String value)
            throws IOException {
        writeTag(writer, key, new String[]{value});
    }

    private static void writeTag(@NotNull BufferedWriter writer, @NotNull SgfFile.Key key, @NotNull String[] values)
            throws IOException {

        writer.write(key.asString());
        for (int i = 0; i < values.length; i++) {
            writer.write("[");
            writer.write(values[i]);
            writer.write("]");
        }
    }

    public static String toSGFPoint(int x, int y) {
        if (x < 0 || y < 0)
            return "-";
        return Character.toString(POINT_MAP.charAt(x)) + Character.toString(POINT_MAP.charAt(y));
    }

    public static void dumpNode(SgfFile.BranchData set, int depth) {
        StringBuilder indents = new StringBuilder();
        for (int i = 0; i < depth; i++)
            indents.append("\t");
        System.out.println(indents + "Node [depth:" + depth + "]: \n" + indents + set.getData());
        for (SgfFile.BranchData child : set.getChildren()) {
            dumpNode(child, depth + 1);
        }
    }

    public enum Key {
        ADD_BLACK("AB", "Add black", null),
        ADD_WHITE("AW", "Add white", null),
        ANNOTATIONS("AN", "Annotations", null),
        APPLICATION("AP", "Application", Yi.TITLE),
        BLACK_RANK("BR", "Black rank", "?"),
        WHITE_RANK("WR", "White rank", "?"),
        BLACK_MOVE("B", "Black move", null),
        WHITE_MOVE("W", "White move", null),
        BLACK_TEAM("BT", "Black team", null),
        WHITE_TEAM("WT", "White team", null),
        FILE_FORMAT("FF", "File format", "4"),
        DATE("DT", "Date played", null),
        COMMENT("C", "Comment", null),
        COPYRIGHT("CP", "Copyright", null),
        EVENT("EV", "Event", null),
        GAME_MODE("GM", "Game type", "1"),
        GAME_NAME("GN", "Game name", null),
        HANDICAP("HA", "Handicap", "0"),
        KOMI("KM", "Komi", null),
        OVERTIME("OT", "Overtime", null),
        BLACK_NAME("PB", "Black name", "Black"),
        WHITE_NAME("PW", "White name", "White"),
        RESULT("RE", "Result", null),
        ROUND("RO", "Round", null),
        RULESET("RU", "Ruleset", null),
        SOURCE("SO", "Source", null),
        BOARD_SIZE("SZ", "Board size", "19"),
        MAIN_TIME("TM", "Main time", null),
        AUTHOR("US", "Author", null),
        LOCATION("PC", "Played at", null),

        CLEAR_POINT("AE", "", null),

        MARKER_ARROWS("AR", "", null),
        MARKER_LABEL("LB", "", null),
        MARKER_CIRCLE("CR", "", null),
        MARKER_DIM_STONE("DD", "", null),
        MARKER_CROSS("XX", "", null),
        MARKER_SQUARE("SQ", "", null),
        MARKER_TRIANGLE("TR", "", null);

        String key;
        String name;
        String defaultValue;

        Key(String key, String name, String defaultValue) {
            this.name = name;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public static Key get(String tag) {
            for (Key key : Key.values()) {
                if (key.key.equals(tag))
                    return key;
            }
            return null;
        }

        public String asString() {
            return key;
        }

        public String getName() {
            return name;
        }
    }

    public static class BranchData {

        private List<BranchData> children = new ArrayList<>();
        private List<String> data = new ArrayList<>();

        public List<String> getData() {
            return data;
        }

        public void addData(String data) {
            this.data.add(data);
        }

        public void addChild(BranchData moveNode) {
            this.children.add(moveNode);
        }

        public List<BranchData> getChildren() {
            return children;
        }
    }
}
