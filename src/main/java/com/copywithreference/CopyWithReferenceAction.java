package com.copywithreference;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;

/**
 * Copies selected code (or current line) along with the project-relative file path
 * and line numbers, formatted for LLM tools like Claude Code and ChatGPT.
 *
 * <p>Output format:
 * <pre>
 * &#64;src/main/UserService.java:42-58
 * ```java
 * public User findById(Long id) {
 *     return repository.findById(id).orElseThrow(...);
 * }
 * ```
 * </pre>
 */
public class CopyWithReferenceAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Project project = event.getData(CommonDataKeys.PROJECT);
        boolean visible = editor != null && project != null;
        event.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        VirtualFile virtualFile = event.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);

        SelectionModel selectionModel = editor.getSelectionModel();
        Document document = editor.getDocument();

        String selectedText;
        int startLine;
        int endLine;

        if (selectionModel.hasSelection()) {
            // Use the selected text and its line range
            int selectionStart = selectionModel.getSelectionStart();
            int selectionEnd = selectionModel.getSelectionEnd();
            selectedText = selectionModel.getSelectedText();
            startLine = document.getLineNumber(selectionStart) + 1; // 0-based → 1-based
            endLine = document.getLineNumber(selectionEnd) + 1;
        } else {
            // No selection: copy the current line
            CaretModel caretModel = editor.getCaretModel();
            int logicalLine = caretModel.getLogicalPosition().line;
            int lineStartOffset = document.getLineStartOffset(logicalLine);
            int lineEndOffset = document.getLineEndOffset(logicalLine);
            selectedText = document.getText().substring(lineStartOffset, lineEndOffset);
            startLine = logicalLine + 1;
            endLine = startLine;
        }

        if (selectedText == null || selectedText.isEmpty()) {
            return;
        }

        // Build project-relative file path
        String relativePath = getRelativePath(project, virtualFile);

        // Build the formatted output
        String output = buildOutput(relativePath, virtualFile, selectedText, startLine, endLine);

        // Copy to system clipboard
        CopyPasteManager.getInstance().setContents(new StringSelection(output));
    }

    /**
     * Returns the project-relative path of the given file.
     * Falls back to the file name if the project base path is unavailable.
     */
    @NotNull
    private static String getRelativePath(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String projectBasePath = project.getBasePath();
        String filePath = virtualFile.getPath();

        if (projectBasePath != null && filePath.startsWith(projectBasePath)) {
            // Remove project base path + trailing separator
            String relative = filePath.substring(projectBasePath.length());
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative;
        }

        // Fallback: use the full path or just the file name
        return virtualFile.getName();
    }

    /**
     * Builds the clipboard content in Claude Code / ChatGPT friendly format.
     */
    @NotNull
    private static String buildOutput(
            @NotNull String relativePath,
            @NotNull VirtualFile virtualFile,
            @NotNull String selectedText,
            int startLine,
            int endLine
    ) {
        StringBuilder sb = new StringBuilder();

        // File reference line: @path:startLine-endLine
        sb.append("@").append(relativePath);
        if (startLine == endLine) {
            sb.append(":").append(startLine);
        } else {
            sb.append(":").append(startLine).append("-").append(endLine);
        }
        sb.append("\n");

        // Language tag from file extension
        String extension = virtualFile.getExtension();
        String langTag = (extension != null && !extension.isEmpty()) ? extension : "";
        // Normalize common extensions to language identifiers
        langTag = normalizeLangTag(langTag);

        // Code fence
        sb.append("```").append(langTag).append("\n");
        sb.append(selectedText);
        if (!selectedText.endsWith("\n")) {
            sb.append("\n");
        }
        sb.append("```");

        return sb.toString();
    }

    /**
     * Maps common file extensions to markdown language tags.
     */
    @NotNull
    private static String normalizeLangTag(@NotNull String extension) {
        return switch (extension) {
            case "java" -> "java";
            case "kt", "kts" -> "kotlin";
            case "py" -> "python";
            case "js" -> "javascript";
            case "jsx" -> "jsx";
            case "ts" -> "typescript";
            case "tsx" -> "tsx";
            case "go" -> "go";
            case "rs" -> "rust";
            case "swift" -> "swift";
            case "rb" -> "ruby";
            case "php" -> "php";
            case "c", "h" -> "c";
            case "cpp", "cc", "cxx", "hpp" -> "cpp";
            case "cs" -> "csharp";
            case "sql" -> "sql";
            case "sh", "bash", "zsh" -> "bash";
            case "yml", "yaml" -> "yaml";
            case "json" -> "json";
            case "xml" -> "xml";
            case "html", "htm" -> "html";
            case "css" -> "css";
            case "scss", "sass" -> "scss";
            case "md", "mdx" -> "markdown";
            case "txt" -> "";
            default -> extension;
        };
    }
}
