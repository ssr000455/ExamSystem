package ui;

import config.ConfigManager;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CodeEditorPanel extends JPanel {
    public RSyntaxTextArea textArea;
    private AutoCompletion ac;
    private CompletionProvider provider;
    private ConfigManager config;
    
    public CodeEditorPanel(int rows, int cols) {
        setLayout(new BorderLayout());
        config = ConfigManager.getInstance();
        
        textArea = new RSyntaxTextArea(rows, cols);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(config.isCodeFolding());
        textArea.setAutoIndentEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setCloseCurlyBraces(true);
        textArea.setMarkOccurrences(true);
        textArea.setTabSize(4);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        applyEditorMode();
        
        if (config.isAutoComplete()) {
            provider = createCompletionProvider();
            ac = new AutoCompletion(provider);
            ac.setAutoCompleteEnabled(true);
            ac.setAutoActivationEnabled(true);
            ac.setAutoActivationDelay(200);
            ac.install(textArea);
        }
        
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setFoldIndicatorEnabled(config.isCodeFolding());
        add(sp, BorderLayout.CENTER);
        
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "importFile");
        textArea.getActionMap().put("importFile", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { importFile(); }
        });
    }
    
    private void applyEditorMode() {
        ConfigManager.EditorMode mode = config.getEditorMode();
        if (mode == ConfigManager.EditorMode.VIM) {
            bindVimKeys();
        } else if (mode == ConfigManager.EditorMode.NANO) {
            textArea.setCodeFoldingEnabled(false);
            textArea.setBracketMatchingEnabled(false);
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            bindNanoKeys();
        } else {
            textArea.setCodeFoldingEnabled(config.isCodeFolding());
            textArea.setBracketMatchingEnabled(true);
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        }
        // 强制刷新背景以适配当前主题
        textArea.setBackground(UIManager.getColor("TextArea.background"));
        textArea.setForeground(UIManager.getColor("TextArea.foreground"));
        textArea.setCaretColor(UIManager.getColor("TextArea.caretForeground"));
    }
    
    private void bindVimKeys() {
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();
        im.put(KeyStroke.getKeyStroke('h'), "vim-left");
        am.put("vim-left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { textArea.setCaretPosition(Math.max(0, textArea.getCaretPosition() - 1)); }
        });
        im.put(KeyStroke.getKeyStroke('l'), "vim-right");
        am.put("vim-right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { textArea.setCaretPosition(Math.min(textArea.getDocument().getLength(), textArea.getCaretPosition() + 1)); }
        });
        im.put(KeyStroke.getKeyStroke('k'), "vim-up");
        am.put("vim-up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int pos = textArea.getCaretPosition();
                    int line = textArea.getLineOfOffset(pos);
                    if (line > 0) textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
                } catch (Exception ex) {}
            }
        });
        im.put(KeyStroke.getKeyStroke('j'), "vim-down");
        am.put("vim-down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int pos = textArea.getCaretPosition();
                    int line = textArea.getLineOfOffset(pos);
                    if (line < textArea.getLineCount() - 1) textArea.setCaretPosition(textArea.getLineStartOffset(line + 1));
                } catch (Exception ex) {}
            }
        });
    }
    
    private void bindNanoKeys() {
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "nano-save");
        am.put("nano-save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { firePropertyChange("nanoSave", false, true); }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "nano-exit");
        am.put("nano-exit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { firePropertyChange("nanoExit", false, true); }
        });
    }
    
    private CompletionProvider createCompletionProvider() {
        DefaultCompletionProvider p = new DefaultCompletionProvider();
        for (String kw : new String[]{"abstract","assert","boolean","break","byte","case","catch","char","class",
            "const","continue","default","do","double","else","enum","extends","final","finally","float",
            "for","if","implements","import","instanceof","int","interface","long","native","new","package",
            "private","protected","public","return","short","static","strictfp","super","switch",
            "synchronized","this","throw","throws","transient","try","void","volatile","while","true","false","null"})
            p.addCompletion(new BasicCompletion(p, kw));
        return p;
    }
    
    public void importFile() {
        JFileChooser fc = new JFileChooser(config.getStoragePath());
        fc.setFileFilter(new FileNameExtensionFilter("Java/Text Files", "java", "txt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                setText(new String(java.nio.file.Files.readAllBytes(fc.getSelectedFile().toPath())));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取失败: " + ex.getMessage());
            }
        }
    }
    
    public void setText(String s) { textArea.setText(s); textArea.setCaretPosition(0); }
    public String getText() { return textArea.getText(); }
    public void setEnabled(boolean e) { textArea.setEnabled(e); textArea.setEditable(e); }
    
    public void refreshSettings() {
        textArea.setCodeFoldingEnabled(config.isCodeFolding());
        if (ac != null) {
            ac.setAutoCompleteEnabled(config.isAutoComplete());
            ac.setAutoActivationEnabled(config.isAutoComplete());
        } else if (config.isAutoComplete()) {
            provider = createCompletionProvider();
            ac = new AutoCompletion(provider);
            ac.install(textArea);
        }
        applyEditorMode();
    }
}
