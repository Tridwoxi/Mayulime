package infra.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

final class InputHandler {

    sealed interface Result permits AcceptedMapCode, RejectedMapCode {}

    record AcceptedMapCode(String mapCode) implements Result {}

    record RejectedMapCode(String message) implements Result {}

    Result chooseMapCode(final Window owner) {
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("Pathery MapCode", "*.mapcode"));
        final File chosen = chooser.showOpenDialog(owner);
        if (chosen == null) {
            return new RejectedMapCode("File picker cancelled.");
        }
        return this.loadMapCodeFile(chosen);
    }

    Result loadPastedMapCode() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            return new RejectedMapCode("Clipboard does not contain text.");
        }
        final String mapCode = clipboard.getString();
        if (mapCode == null || mapCode.isBlank()) {
            return new RejectedMapCode("Clipboard text is empty.");
        }
        return new AcceptedMapCode(mapCode);
    }

    boolean copyMapCode(final String mapCode) {
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(mapCode);
        return Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    Result loadDroppedFiles(final List<File> files) {
        if (files.size() != 1) {
            return new RejectedMapCode("Drop exactly one .mapcode file.");
        }
        return this.loadMapCodeFile(files.get(0));
    }

    private Result loadMapCodeFile(final File file) {
        if (!UiMath.isSupportedMapFile(file)) {
            return new RejectedMapCode("Unsupported file type. Expected .mapcode.");
        }

        final String mapCode;
        try {
            mapCode = Files.readString(file.toPath());
        } catch (IOException _) {
            return new RejectedMapCode("Can't read that file.");
        }
        return new AcceptedMapCode(mapCode);
    }
}
