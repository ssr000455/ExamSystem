package ui;

import model.*;
import data.*;
import compiler.*;
import progress.*;
import config.ConfigManager;
import i18n.I18nManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.*;

public class MainFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    
    private List<Plan> plans;
    private Plan currentPlan;
    private PlanLevel currentPlanLevel;
    private Section currentSection;
    private Question currentQuestion;
    private int currentQuestionIndex = 0;
    
    public CodeEditorPanel editorPanel;
    private ResultPanel resultPanel;
    private JTextPane questionPanel;
    private JLabel titleLabel;
    private JLabel progressLabel;
    private JLabel scoreLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    private JButton submitBtn;
    private JButton runBtn;
    private JComboBox<String> questionSelector;
    
    private DefaultComboBoxModel<String> questionListModel;
    private Map<String, Question> questionMap = new HashMap<>();
    private Map<String, Integer> questionScores = new HashMap<>();
    private Map<String, String> savedCodes = new HashMap<>();
    
    private WelcomePanel welcomePanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private static final String WELCOME_CARD = "welcome";
    private static final String MAIN_CARD = "main";
    
    private Point dragStartPoint;
    private I18nManager i18n;
    
    private javax.swing.Timer autoSaveTimer;
    private long sessionStartTime;
    
    private JPanel questionTypePanel;
    private CardLayout questionCardLayout;
    private FillBlankPanel fillBlankPanel;
    private MatchPanel matchPanel;
    private DebugPanel debugPanel;
    private CompletePanel completePanel;
    
    public MainFrame() throws Exception {
        i18n = I18nManager.getInstance();
        setTitle(i18n.get("app.title"));
        setSize(1200, 720);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        ConfigManager.getInstance().applyTheme();
        enableWindowDrag();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    i18n.get("confirm.exit.message"),
                    i18n.get("confirm.exit.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    saveCurrentProgress();
                    long sessionSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000;
                    ProgressManager.addSessionTime(sessionSeconds);
                    if (autoSaveTimer != null) autoSaveTimer.stop();
                    dispose();
                    System.exit(0);
                }
            }
        });
        
        CompletableFuture<List<Plan>> future = CompletableFuture.supplyAsync(() -> FastQuestionLoader.loadStructureOnly());
        
        createMenuBar();
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        welcomePanel = new WelcomePanel();
        mainContentPanel.add(welcomePanel, WELCOME_CARD);
        JPanel mainPanel = createMainPanel();
        mainContentPanel.add(mainPanel, MAIN_CARD);
        setContentPane(mainContentPanel);
        
        plans = future.get();
        
        for (Plan p : plans) {
            for (PlanLevel level : p.difficultyLevels) {
                if (level.difficulty == DifficultyLevel.BEGINNER) {
                    level.unlocked = true;
                }
            }
        }
        
        ProgressManager.ProgressData progress = ProgressManager.load();
        sessionStartTime = progress.lastSessionStart;
        if (!ProgressManager.hasProgress() || progress.currentCard.equals(WELCOME_CARD)) {
            cardLayout.show(mainContentPanel, WELCOME_CARD);
        } else {
            cardLayout.show(mainContentPanel, MAIN_CARD);
            loadAndRestoreProgress(progress);
        }
        
        setupKeyboardShortcuts();
        startAutoSaveTimer();
    }
    
    private void setupKeyboardShortcuts() {
        JRootPane rootPane = getRootPane();
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "saveProgress");
        rootPane.getActionMap().put("saveProgress", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveCurrentProgress();
                JOptionPane.showMessageDialog(MainFrame.this, i18n.get("dialog.save"), i18n.get("settings.restartTitle"), JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "runCheck");
        rootPane.getActionMap().put("runCheck", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (runBtn.isEnabled()) checkCodeAsync();
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), "prevQuestion");
        rootPane.getActionMap().put("prevQuestion", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (prevBtn.isEnabled()) navigateQuestion(-1);
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), "nextQuestion");
        rootPane.getActionMap().put("nextQuestion", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (nextBtn.isEnabled()) navigateQuestion(1);
            }
        });
        
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToPlan");
        rootPane.getActionMap().put("backToPlan", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveCurrentProgress();
                showSectionSelector();
            }
        });
    }
    
    private void startAutoSaveTimer() {
        autoSaveTimer = TimerManager.createTimer(60000, () -> {
            if (currentSection != null && currentQuestion != null) {
                saveCurrentProgress();
                LOGGER.fine("自动保存进度");
            }
        });
        autoSaveTimer.start();
    }
    
    public void refreshUI() {
        i18n.refresh();
        setTitle(i18n.get("app.title"));
        createMenuBar();
        welcomePanel.rebuild();
        if (currentSection != null) {
            titleLabel.setText(currentPlan.name + " - " + currentSection.name);
        } else {
            titleLabel.setText(i18n.get("app.title"));
        }
        updateButtonTexts();
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    public void fullRefresh() {
        i18n.refresh();
        setTitle(i18n.get("app.title"));
        createMenuBar();
        welcomePanel.rebuild();
        if (currentSection != null) {
            titleLabel.setText(currentPlan.name + " - " + currentSection.name);
            displayQuestion(currentQuestion);
        } else {
            titleLabel.setText(i18n.get("app.title"));
        }
        updateButtonTexts();
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }
    
    private void updateButtonTexts() {
        if (prevBtn != null) prevBtn.setText(i18n.get("question.prev"));
        if (nextBtn != null) nextBtn.setText(i18n.get("question.next"));
        if (runBtn != null) runBtn.setText(i18n.get("question.check"));
        if (submitBtn != null) submitBtn.setText(i18n.get("question.submit"));
    }
    
    private void enableWindowDrag() {
        MouseAdapter adapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStartPoint = e.getPoint(); }
            public void mouseDragged(MouseEvent e) {
                Point current = e.getLocationOnScreen();
                setLocation(current.x - dragStartPoint.x, current.y - dragStartPoint.y);
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(i18n.get("menu.file"));
        JMenuItem homeItem = new JMenuItem(i18n.get("menu.home"));
        homeItem.addActionListener(e -> {
            saveCurrentProgress();
            cardLayout.show(mainContentPanel, WELCOME_CARD);
            ProgressManager.saveCardState(WELCOME_CARD, null, null, null, 0);
        });
        JMenuItem planItem = new JMenuItem(i18n.get("menu.selectPlan"));
        planItem.addActionListener(e -> { cardLayout.show(mainContentPanel, MAIN_CARD); showPlanSelectorDialog(); });
        JMenuItem importItem = new JMenuItem(i18n.get("menu.importPlan"));
        importItem.addActionListener(e -> importPlan());
        JMenuItem saveItem = new JMenuItem(i18n.get("menu.saveProgress"));
        saveItem.addActionListener(e -> {
            saveCurrentProgress();
            JOptionPane.showMessageDialog(this, i18n.get("dialog.save"), i18n.get("settings.restartTitle"), JOptionPane.INFORMATION_MESSAGE);
        });
        JMenuItem exitItem = new JMenuItem(i18n.get("menu.exit"));
        exitItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, i18n.get("confirm.exit.message"), i18n.get("confirm.exit.title"), JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveCurrentProgress();
                long sessionSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000;
                ProgressManager.addSessionTime(sessionSeconds);
                if (autoSaveTimer != null) autoSaveTimer.stop();
                dispose();
                System.exit(0);
            }
        });
        fileMenu.add(homeItem); fileMenu.add(planItem); fileMenu.add(importItem);
        fileMenu.addSeparator(); fileMenu.add(saveItem); fileMenu.add(exitItem);
        
        JMenu toolsMenu = new JMenu(i18n.get("menu.tools"));
        JMenuItem settingsItem = new JMenuItem(i18n.get("menu.settings"));
        settingsItem.addActionListener(e -> {
            new SettingsDialog(this).setVisible(true);
            editorPanel.refreshSettings();
            fullRefresh();
        });
        toolsMenu.add(settingsItem);
        
        JMenu helpMenu = new JMenu(i18n.get("menu.help"));
        JMenuItem achievementItem = new JMenuItem(i18n.get("menu.achievements"));
        achievementItem.addActionListener(e -> new AchievementDialog(this).setVisible(true));
        JMenuItem aboutItem = new JMenuItem(i18n.get("menu.about"));
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(achievementItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu); menuBar.add(toolsMenu); menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel = new JLabel(i18n.get("app.title"));
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);
        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        progressLabel = new JLabel(""); scoreLabel = new JLabel("");
        rightTopPanel.add(scoreLabel); rightTopPanel.add(progressLabel);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplit.setResizeWeight(0.2); centerSplit.setDividerSize(5);
        questionPanel = new JTextPane(); questionPanel.setContentType("text/html"); questionPanel.setEditable(false);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JScrollPane questionScroll = new JScrollPane(questionPanel);
        questionScroll.setBorder(BorderFactory.createTitledBorder(i18n.get("question.list")));
        questionScroll.setPreferredSize(new Dimension(0, 150));
        
        JPanel editorContainer = new JPanel(new BorderLayout());
        editorPanel = new CodeEditorPanel(40, 100);
        editorPanel.setBorder(BorderFactory.createTitledBorder(i18n.get("editor.borderTitle")));
        
        questionCardLayout = new CardLayout();
        questionTypePanel = new JPanel(questionCardLayout);
        fillBlankPanel = new FillBlankPanel();
        matchPanel = new MatchPanel();
        debugPanel = new DebugPanel();
        completePanel = new CompletePanel();
        questionTypePanel.add(editorPanel, QuestionType.PROGRAM);
        questionTypePanel.add(fillBlankPanel, QuestionType.FILL);
        questionTypePanel.add(matchPanel, QuestionType.MATCH);
        questionTypePanel.add(debugPanel, QuestionType.DEBUG);
        questionTypePanel.add(completePanel, QuestionType.COMPLETE);
        
        editorContainer.add(questionTypePanel, BorderLayout.CENTER);
        centerSplit.setTopComponent(questionScroll);
        centerSplit.setBottomComponent(editorContainer);
        
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setResizeWeight(0.5); rightSplit.setDividerSize(5);
        JPanel rightControlPanel = createControlPanel();
        rightSplit.setTopComponent(rightControlPanel);
        resultPanel = new ResultPanel();
        resultPanel.setBorder(BorderFactory.createTitledBorder(i18n.get("question.check")));
        rightSplit.setBottomComponent(resultPanel);
        
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setPreferredSize(new Dimension(320, 0));
        rightWrapper.add(rightSplit, BorderLayout.CENTER);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(centerSplit); mainSplit.setRightComponent(rightWrapper);
        mainSplit.setResizeWeight(0.75); mainSplit.setDividerSize(5);
        panel.add(mainSplit, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel selectorLabel = new JLabel(i18n.get("question.list")); selectorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionListModel = new DefaultComboBoxModel<>(); questionSelector = new JComboBox<>(questionListModel);
        questionSelector.setMaximumSize(new Dimension(280,30)); questionSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionSelector.addActionListener(e -> {
            if (questionSelector.getSelectedIndex() >= 0 && currentSection != null) {
                saveCurrentCode();
                String selected = (String) questionSelector.getSelectedItem();
                Question q = questionMap.get(selected);
                if (q != null) {
                    currentQuestion = q;
                    currentQuestionIndex = questionSelector.getSelectedIndex();
                    displayQuestion(currentQuestion);
                    updateNavigationButtons();
                    updateProgress();
                    saveCurrentCardState();
                }
            }
        });
        panel.add(selectorLabel); panel.add(Box.createRigidArea(new Dimension(0,5))); panel.add(questionSelector);
        panel.add(Box.createRigidArea(new Dimension(0,15)));
        JPanel navPanel = new JPanel(new GridLayout(1,2,5,0)); navPanel.setMaximumSize(new Dimension(280,35));
        prevBtn = new JButton(i18n.get("question.prev")); prevBtn.addActionListener(e -> navigateQuestion(-1));
        nextBtn = new JButton(i18n.get("question.next")); nextBtn.addActionListener(e -> navigateQuestion(1));
        navPanel.add(prevBtn); navPanel.add(nextBtn); panel.add(navPanel); panel.add(Box.createRigidArea(new Dimension(0,15)));
        runBtn = new JButton(i18n.get("question.check")); runBtn.setMaximumSize(new Dimension(280,40));
        runBtn.addActionListener(e -> checkCodeAsync());
        submitBtn = new JButton(i18n.get("question.submit")); submitBtn.setMaximumSize(new Dimension(280,40));
        submitBtn.addActionListener(e -> submitAnswer());
        JButton resetBtn = new JButton(i18n.get("question.reset")); resetBtn.setMaximumSize(new Dimension(280,35));
        resetBtn.addActionListener(e -> resetCode());
        JButton backBtn = new JButton(i18n.get("question.back")); backBtn.setMaximumSize(new Dimension(280,35));
        backBtn.addActionListener(e -> { saveCurrentProgress(); showSectionSelector(); });
        panel.add(runBtn); panel.add(Box.createRigidArea(new Dimension(0,8))); panel.add(submitBtn);
        panel.add(Box.createRigidArea(new Dimension(0,8))); panel.add(resetBtn); panel.add(Box.createRigidArea(new Dimension(0,8)));
        panel.add(backBtn);
        return panel;
    }
    
    private void displayQuestion(Question q) {
        StringBuilder h = new StringBuilder("<html><body style='font-family: Microsoft YaHei;'>");
        h.append("<h3>").append(q.title).append("</h3>");
        if(QuestionType.PROGRAM.equals(q.type)) {
            Integer sc = questionScores.get(q.title);
            if(sc!=null) h.append("<p>").append(i18n.get("question.scoreLabel")).append(" ").append(sc).append("/").append(q.maxScore).append("</p>");
            h.append("<p><b>").append(i18n.get("question.keywordsLabel")).append("</b> ");
            for(String kw:q.keywords) h.append("<code>").append(kw).append("</code> ");
            h.append("</p>");
            editorPanel.setText(savedCodes.getOrDefault(q.title, q.template));
            runBtn.setEnabled(true);
            questionCardLayout.show(questionTypePanel, QuestionType.PROGRAM);
        } else if(QuestionType.FILL.equals(q.type)) {
            fillBlankPanel.setQuestion(q.textWithBlanks, q.blankAnswers, q.caseSensitive);
            questionCardLayout.show(questionTypePanel, QuestionType.FILL);
            runBtn.setEnabled(false);
        } else if(QuestionType.MATCH.equals(q.type)) {
            matchPanel.setItems(q.leftItems, q.rightItems, q.correctMatches);
            questionCardLayout.show(questionTypePanel, QuestionType.MATCH);
            runBtn.setEnabled(false);
        } else if(QuestionType.DEBUG.equals(q.type)) {
            debugPanel.setQuestion(q.buggyCode, q.expectedErrors, q.fixedCode);
            questionCardLayout.show(questionTypePanel, QuestionType.DEBUG);
            runBtn.setEnabled(false);
        } else if(QuestionType.COMPLETE.equals(q.type)) {
            completePanel.setQuestion(q.partialCode, q.requiredElements);
            questionCardLayout.show(questionTypePanel, QuestionType.COMPLETE);
            runBtn.setEnabled(false);
        } else {
            questionCardLayout.show(questionTypePanel, QuestionType.PROGRAM);
        }
        h.append("</body></html>");
        questionPanel.setText(h.toString());
        resultPanel.clear();
    }
    
    private void saveCurrentCode() {
        if(currentQuestion!=null && QuestionType.PROGRAM.equals(currentQuestion.type)){
            String c = editorPanel.getText();
            if(!c.equals(currentQuestion.template)) savedCodes.put(currentQuestion.title, c);
        }
    }
    
    private void navigateQuestion(int d) { saveCurrentCode(); int i=currentQuestionIndex+d; if(i>=0&&i<questionListModel.getSize()) questionSelector.setSelectedIndex(i); }
    private void updateNavigationButtons() { prevBtn.setEnabled(currentQuestionIndex>0); nextBtn.setEnabled(currentQuestionIndex<questionListModel.getSize()-1); }
    private void updateProgress() { progressLabel.setText((currentQuestionIndex+1)+" / "+currentSection.questions.size()); }
    private void updateTotalScore() { int t=0,e=0; for(Question q:currentSection.questions){ t+=q.maxScore; e+=questionScores.getOrDefault(q.title,0); } scoreLabel.setText(i18n.get("question.scoreDisplay", e, t)); }
    
    private void checkCodeAsync() {
        runBtn.setEnabled(false);
        runBtn.setText(i18n.get("question.check") + "...");
        CompletableFuture.supplyAsync(() -> {
            if (currentQuestion == null) return null;
            String code = editorPanel.getText();
            return CodeChecker.check(code, currentQuestion.keywords, currentQuestion.template);
        }).thenAcceptAsync(result -> {
            runBtn.setEnabled(true);
            runBtn.setText(i18n.get("question.check"));
            if (result != null) {
                resultPanel.clear();
                resultPanel.showCheckResult(result, currentQuestion.maxScore);
                if (result.success && result.score > 0) {
                    int s = (int)Math.round(result.score);
                    questionScores.put(currentQuestion.title, s);
                    updateTotalScore();
                    saveCurrentProgress();
                    String originalTitle = currentQuestion.title;
                    String displayName = (currentQuestionIndex + 1) + ". " + originalTitle + " ✓";
                    questionListModel.removeElementAt(currentQuestionIndex);
                    questionListModel.insertElementAt(displayName, currentQuestionIndex);
                    questionSelector.setSelectedIndex(currentQuestionIndex);
                    questionMap.put(displayName, currentQuestion);
                    
                    if (result.advanced) {
                        AchievementManager.onExcellentCode();
                    }
                }
            }
        }, SwingUtilities::invokeLater);
    }
    
    private void submitAnswer() {
        if (currentQuestion == null) { resultPanel.clear(); resultPanel.showError(i18n.get("result.errors")); return; }
        String type = currentQuestion.type;
        double score = 0;
        boolean correct = false;
        if (QuestionType.CHOICE.equals(type)) {
            int sel = ChoicePanel.showChoiceDialog(this, currentQuestion.title, currentQuestion.options);
            if (sel >= 0) {
                correct = (sel == currentQuestion.answer);
                score = correct ? currentQuestion.maxScore : 0;
                resultPanel.clear();
                resultPanel.showChoiceResult(correct, sel, currentQuestion.answer, currentQuestion.maxScore);
            }
        } else if (QuestionType.FILL.equals(type)) {
            correct = fillBlankPanel.checkAnswers();
            score = fillBlankPanel.getScore(currentQuestion.maxScore);
        } else if (QuestionType.MATCH.equals(type)) {
            correct = matchPanel.checkMatches();
            score = matchPanel.getScore(currentQuestion.maxScore);
        } else if (QuestionType.DEBUG.equals(type)) {
            score = debugPanel.getScore(currentQuestion.maxScore);
            correct = (score >= currentQuestion.maxScore * 0.8);
        } else if (QuestionType.COMPLETE.equals(type)) {
            score = completePanel.getScore(currentQuestion.maxScore);
            correct = (score >= currentQuestion.maxScore * 0.8);
        }
        
        if (score > 0) {
            questionScores.put(currentQuestion.title, (int)Math.round(score));
            updateTotalScore();
            saveCurrentProgress();
            String originalTitle = currentQuestion.title;
            String displayName = (currentQuestionIndex + 1) + ". " + originalTitle + " ✓";
            questionListModel.removeElementAt(currentQuestionIndex);
            questionListModel.insertElementAt(displayName, currentQuestionIndex);
            questionSelector.setSelectedIndex(currentQuestionIndex);
            questionMap.put(displayName, currentQuestion);
            
            checkSectionCompletion();
        }
    }
    
    private void checkSectionCompletion() {
        int completed = 0;
        for (Question q : currentSection.questions) {
            if (questionScores.containsKey(q.title)) completed++;
        }
        if (completed == currentSection.questions.size()) {
            double totalScore = 0;
            double maxScore = 0;
            for (Question q : currentSection.questions) {
                totalScore += questionScores.getOrDefault(q.title, 0);
                maxScore += q.maxScore;
            }
            double percent = maxScore > 0 ? totalScore / maxScore : 0;
            
            String planKey = currentPlan.baseName + "_" + currentPlanLevel.difficulty.name();
            UnlockManager.onSectionCompleted(planKey, percent);
            AchievementManager.onSectionCompleted(currentPlan.name, currentSection.name, percent);
        }
    }
    
    private void resetCode() { 
        if(currentQuestion!=null){ 
            if(QuestionType.PROGRAM.equals(currentQuestion.type)) {
                editorPanel.setText(currentQuestion.template); 
                savedCodes.remove(currentQuestion.title); 
            } else if(QuestionType.FILL.equals(currentQuestion.type)) {
                fillBlankPanel.clear();
            } else if(QuestionType.MATCH.equals(currentQuestion.type)) {
                matchPanel.clear();
            } else if(QuestionType.DEBUG.equals(currentQuestion.type)) {
                debugPanel.clear();
            } else if(QuestionType.COMPLETE.equals(currentQuestion.type)) {
                completePanel.clear();
            }
            resultPanel.clear(); 
        } 
    }
    
    private void saveCurrentProgress() {
        if(currentPlan!=null && currentSection!=null){ 
            saveCurrentCode(); 
            ProgressManager.saveCurrentState(currentPlan.name, currentSection.name, currentQuestionIndex, 
                currentQuestion != null ? currentQuestion.title : "", 
                savedCodes.getOrDefault(currentQuestion != null ? currentQuestion.title : "", ""),
                questionScores.getOrDefault(currentQuestion != null ? currentQuestion.title : "", -1));
        }
        saveCurrentCardState();
    }
    
    private void saveCurrentCardState() {
        String card = cardLayout.toString(); 
        String currentCard = (mainContentPanel.getComponent(0) == welcomePanel && mainContentPanel.getComponent(0).isVisible()) ? WELCOME_CARD : MAIN_CARD;
        String planName = currentPlan != null ? currentPlan.name : null;
        String difficulty = currentPlanLevel != null ? currentPlanLevel.difficulty.name() : null;
        String sectionName = currentSection != null ? currentSection.name : null;
        int qidx = currentQuestionIndex;
        ProgressManager.saveCardState(currentCard, planName, difficulty, sectionName, qidx);
    }
    
    private void loadAndRestoreProgress(ProgressManager.ProgressData p) {
        if(p == null) return;
        savedCodes.putAll(p.savedCodes);
        questionScores.putAll(p.scores);
        if(p.planName != null) {
            for(Plan pl:plans) if(pl.name.equals(p.planName)){ currentPlan=pl; break; }
        }
        if(currentPlan != null && !currentPlan.difficultyLevels.isEmpty()){
            DifficultyLevel targetLevel = DifficultyLevel.valueOf(p.currentDifficulty);
            for(PlanLevel level : currentPlan.difficultyLevels) {
                if(level.difficulty == targetLevel) {
                    currentPlanLevel = level;
                    break;
                }
            }
            if(currentPlanLevel == null) currentPlanLevel = currentPlan.difficultyLevels.get(0);
            for(Section s:currentPlanLevel.sections) if(s.name.equals(p.sectionName)){ currentSection=s; break; }
            if(currentSection != null){
                loadSection();
                if(p.questionIndex < questionListModel.getSize()) questionSelector.setSelectedIndex(p.questionIndex);
            }
        }
    }
    
    private void showPlanSelectorDialog() {
        JDialog d = new JDialog(this, i18n.get("dialog.selectPlan"), true);
        d.setSize(500, 400);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        DefaultListModel<Plan> model = new DefaultListModel<>();
        for(Plan pl:plans) model.addElement(pl);
        JList<Plan> list = new JList<>(model);
        list.setCellRenderer(new PlanListCellRenderer());
        JButton selectBtn = new JButton(i18n.get("dialog.selectPlan"));
        selectBtn.addActionListener(e -> {
            Plan selected = list.getSelectedValue();
            if(selected != null) {
                boolean hasUnlocked = selected.difficultyLevels.stream().anyMatch(l -> l.unlocked);
                if(hasUnlocked) {
                    currentPlan = selected;
                    d.dispose();
                    showLevelSelector();
                } else {
                    JOptionPane.showMessageDialog(d, i18n.get("unlock.locked"), i18n.get("unlock.locked"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JButton cancelBtn = new JButton(i18n.get("dialog.cancel"));
        cancelBtn.addActionListener(e -> d.dispose());
        p.add(new JScrollPane(list));
        JPanel bp = new JPanel(); bp.add(selectBtn); bp.add(cancelBtn);
        p.add(bp, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }
    
    private void showLevelSelector() {
        if(currentPlan == null) return;
        JDialog d = new JDialog(this, i18n.get("dialog.selectSection") + " - " + currentPlan.name, true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        DefaultListModel<PlanLevel> model = new DefaultListModel<>();
        for(PlanLevel level : currentPlan.difficultyLevels) model.addElement(level);
        JList<PlanLevel> list = new JList<>(model);
        list.setCellRenderer(new LevelListCellRenderer());
        JButton selectBtn = new JButton(i18n.get("dialog.select"));
        selectBtn.addActionListener(e -> {
            PlanLevel selected = list.getSelectedValue();
            if(selected != null) {
                if(selected.unlocked) {
                    currentPlanLevel = selected;
                    d.dispose();
                    showSectionSelector();
                } else {
                    int required = selected.difficulty.level * 15;
                    JOptionPane.showMessageDialog(d, 
                        i18n.get("unlock.required", required) + "\n" + i18n.get("unlock.current", UnlockManager.getTotalStars()), 
                        i18n.get("unlock.locked"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        p.add(new JScrollPane(list));
        JPanel bp = new JPanel(); bp.add(selectBtn);
        p.add(bp, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }
    
    private void showSectionSelector() {
        if(currentPlanLevel == null) return;
        JDialog d = new JDialog(this, i18n.get("dialog.selectSection") + " - " + currentPlan.name + " [" + currentPlanLevel.difficulty.displayName + "]", true);
        d.setSize(600, 450);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        DefaultListModel<String> m = new DefaultListModel<>();
        for(Section sec : currentPlanLevel.sections) {
            int done = 0;
            for(Question q : sec.questions) if(questionScores.containsKey(q.title)) done++;
            m.addElement(sec.name + " (" + done + "/" + sec.questions.size() + ")");
        }
        JList<String> list = new JList<>(m);
        JButton startBtn = new JButton(i18n.get("dialog.start"));
        startBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if(idx >= 0) {
                currentSection = currentPlanLevel.sections.get(idx);
                d.dispose();
                loadSection();
                saveCurrentCardState();
            }
        });
        JButton cancelBtn = new JButton(i18n.get("dialog.cancel"));
        cancelBtn.addActionListener(e -> d.dispose());
        p.add(new JScrollPane(list));
        JPanel bp = new JPanel(); bp.add(startBtn); bp.add(cancelBtn);
        p.add(bp, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }
    
    private void loadSection() {
        cardLayout.show(mainContentPanel, MAIN_CARD);
        ProgressManager.saveCardState(MAIN_CARD, currentPlan.name, currentPlanLevel.difficulty.name(), currentSection.name, 0);
        titleLabel.setText(currentPlan.name + " - " + currentPlanLevel.difficulty.displayName + " - " + currentSection.name);
        questionListModel.removeAllElements(); questionMap.clear();
        for(int i=0;i<currentSection.questions.size();i++){
            Question q=currentSection.questions.get(i);
            String st=questionScores.containsKey(q.title)?" ✓":"";
            String name=(i+1)+". "+q.title+st;
            questionListModel.addElement(name); questionMap.put(name,q);
        }
        if(questionListModel.getSize()>0){
            questionSelector.setSelectedIndex(0);
            currentQuestion=currentSection.questions.get(0);
            currentQuestionIndex=0;
            displayQuestion(currentQuestion);
        }
        updateNavigationButtons();
        updateProgress();
        updateTotalScore();
        AchievementManager.recordStudyDay();
    }
    
    private void importPlan() {
        JFileChooser fc = new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("ZIP", "zip"));
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            PlanImporter.ImportResult r = PlanImporter.importPlan(fc.getSelectedFile(), this);
            if(r.success) { try { plans = FastQuestionLoader.loadStructureOnly(); } catch(Exception ex){} }
        }
    }
    
    public void showImportDialog() { importPlan(); }
    public void showPlanSelector() { cardLayout.show(mainContentPanel, MAIN_CARD); showPlanSelectorDialog(); }
    private void showAbout() { JOptionPane.showMessageDialog(this, i18n.get("about.text")); }
    
    class PlanListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
            Plan plan = (Plan) value;
            boolean hasUnlocked = plan.difficultyLevels.stream().anyMatch(l -> l.unlocked);
            label.setText(plan.name + (hasUnlocked ? "" : i18n.get("plan.listItem.lockedSuffix")));
            if (!hasUnlocked) label.setForeground(Color.GRAY);
            return label;
        }
    }
    
    class LevelListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
            PlanLevel level = (PlanLevel) value;
            String text = i18n.get("difficulty.levelDisplay", level.difficulty.displayName, level.sections.size());
            if (!level.unlocked) {
                text += i18n.get("difficulty.starsRequired", level.difficulty.level * 15);
                label.setForeground(Color.GRAY);
            }
            label.setText(text);
            return label;
        }
    }
}
