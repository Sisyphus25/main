package seedu.address.ui;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.ClassListChangedEvent;
import seedu.address.commons.events.model.StudentListChangedEvent;
import seedu.address.commons.events.ui.ExitAppRequestEvent;
import seedu.address.commons.events.ui.ShowHelpRequestEvent;
import seedu.address.commons.events.ui.ThemeChangeEvent;
import seedu.address.commons.events.ui.ToggleListEvent;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private static final String EXTENSIONS_STYLESHEET = "view/Extensions.css";

    private static final String TAG_COLOUR_STYLESHEET = "view/TagColour.css";

    private static final ThemeList THEME_LIST = new ThemeList();

    private static final String DEFAULT_THEME = "light";

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private AppointmentListPanel appointmentListPanel;
    private TaskListPanel taskListPanel;
    private Config config;
    private UserPrefs prefs;
    private CalendarPanel calendarPanel;
    private ShortcutListPanel shortcutListPanel;
    private ClassListPanel classListPanel;

    private String theme;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane listPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private StackPane calendarPlaceholder;

    public MainWindow(Stage primaryStage, Config config, UserPrefs prefs, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;

        // Configure the UI
        setTheme();
        setTitle(config.getAppTitle());
        setWindowDefaultSize(prefs);

        setAccelerators();
        registerAsAnEventHandler(this);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    //@@author Sisyphus25
    private void setTheme() {
        setTheme(DEFAULT_THEME);
    }

    private void setTheme(String theme) {
        primaryStage.getScene().getStylesheets().add(EXTENSIONS_STYLESHEET);
        primaryStage.getScene().getStylesheets().add(TAG_COLOUR_STYLESHEET);
        primaryStage.getScene().getStylesheets().add(THEME_LIST.getThemeStyleSheet(theme));
    }

    @Subscribe
    private void handleThemeChangeEvent(ThemeChangeEvent event) {
        theme = event.theme;
        Platform.runLater(
                this::changeTheme
        );
    }

    private void changeTheme() {
        primaryStage.getScene().getStylesheets().clear();
        setTheme(theme);
    }
    //@@author

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        listPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        ResultDisplay resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(prefs.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(logic);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        calendarPanel = new CalendarPanel(logic.getFilteredAppointmentList());
        calendarPlaceholder.getChildren().add(calendarPanel.getRoot());

        appointmentListPanel = new AppointmentListPanel(logic.getFilteredAppointmentList());
        taskListPanel = new TaskListPanel(logic.getFilteredTaskList());

        shortcutListPanel = new ShortcutListPanel(logic.getFilteredShortcutList());

        classListPanel = new ClassListPanel(logic.getFilteredClassList());

    }

    void hide() {
        primaryStage.hide();
    }

    private void setTitle(String appTitle) {
        primaryStage.setTitle(appTitle);
    }

    /**
     * Sets the default size based on user preferences.
     */
    private void setWindowDefaultSize(UserPrefs prefs) {
        primaryStage.setHeight(prefs.getGuiSettings().getWindowHeight());
        primaryStage.setWidth(prefs.getGuiSettings().getWindowWidth());
        if (prefs.getGuiSettings().getWindowCoordinates() != null) {
            primaryStage.setX(prefs.getGuiSettings().getWindowCoordinates().getX());
            primaryStage.setY(prefs.getGuiSettings().getWindowCoordinates().getY());
        }
    }

    /**
     * Returns the current size and the position of the main Window.
     */
    GuiSettings getCurrentGuiSetting() {
        return new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
    }

    /**
     * Opens the help window.
     */
    @FXML
    public void handleHelp() {
        HelpWindow helpWindow = new HelpWindow();
        helpWindow.show();
    }

    /**
     * Toggles list
     */
    @FXML
    public void toggleList(String list) {
        listPanelPlaceholder.getChildren().clear();
        switch(list) {
        case "appointments":
            listPanelPlaceholder.getChildren().add(appointmentListPanel.getRoot());
            break;
        case "tasks":
            listPanelPlaceholder.getChildren().add(taskListPanel.getRoot());
            break;
        case "contacts":
            listPanelPlaceholder.getChildren().add(personListPanel.getRoot());
            break;
        case "shortcuts":
            listPanelPlaceholder.getChildren().add(shortcutListPanel.getRoot());
            break;
        case "classes":
            listPanelPlaceholder.getChildren().add(classListPanel.getRoot());
            break;

        default:
            listPanelPlaceholder.getChildren().add(personListPanel.getRoot());
        }

    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        raise(new ExitAppRequestEvent());
    }

    public PersonListPanel getPersonListPanel() {
        return this.personListPanel;
    }

    @Subscribe
    private void handleShowHelpEvent(ShowHelpRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        handleHelp();
    }

    @Subscribe
    private void handleToggleListEvent(ToggleListEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        toggleList(event.list);
    }

    @Subscribe
    private void handleStudentListChangedEvent(StudentListChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
    }

    @Subscribe
    private void handleClassListChangedEvent(ClassListChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        classListPanel = new ClassListPanel(logic.getFilteredClassList());    }
}
