package com.tripos.pos.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the TriPOS login screen.
 *
 * <h3>Authentication note</h3>
 * <p>This is a <strong>UI prototype</strong>. The {@link #onSignInClicked()}
 * method currently simulates a successful login after a short pause and then
 * loads the Checkout screen. In production, replace the simulated block
 * with a real {@code POST /api/auth/login} call to the Spring Boot
 * authentication API. Never evaluate credentials on the client side.</p>
 *
 * <h3>PIN login note</h3>
 * <p>The {@link #onPinLoginClicked()} method is wired but navigates to a
 * placeholder for now. Connect it to the secure PIN authentication workflow
 * when that screen is ready.</p>
 *
 * <h3>Security reminders</h3>
 * <ul>
 *   <li>Never store credentials in {@code preferences} or local files.</li>
 *   <li>"Remember this terminal" must only persist a device-bound token
 *       issued by the backend, not a password.</li>
 *   <li>Authentication errors must be generic to prevent account enumeration.</li>
 * </ul>
 */
public class LoginController implements Initializable {

    // =====================================================================
    // FXML fields
    // =====================================================================
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox      rememberTerminalCheck;
    @FXML private Hyperlink     forgotPasswordLink;
    @FXML private Button        signInButton;
    @FXML private Button        pinLoginButton;
    @FXML private Label         errorLabel;
    @FXML private Label         signInBtnLabel;   // inner label for loading swap
    @FXML private VBox          formCard;
    @FXML private ProgressIndicator loadingSpinner;

    // =====================================================================
    // Initialise
    // =====================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clearError();

        // Enter key on either field triggers sign-in
        usernameField.setOnAction(e -> onSignInClicked());
        passwordField.setOnAction(e -> onSignInClicked());

        // Live validation: clear error when user edits
        usernameField.textProperty().addListener((o, old, nv) -> clearError());
        passwordField.textProperty().addListener((o, old, nv) -> clearError());

        // Loading spinner starts hidden
        if (loadingSpinner != null) {
            loadingSpinner.setVisible(false);
            loadingSpinner.setManaged(false);
        }
    }

    // =====================================================================
    // Actions
    // =====================================================================

    /**
     * Validates the form and initiates a simulated sign-in.
     *
     * <p><strong>PROTOTYPE:</strong> Replace the {@code simulateSignIn()} call
     * with a real API call to {@code POST /api/auth/login}.</p>
     */
    @FXML
    private void onSignInClicked() {
        String username = usernameField.getText().strip();
        String password = passwordField.getText();

        // --- Client-side validation (UX only; backend validates authoritatively) ---
        if (username.isEmpty()) {
            showError("Please enter your username or email.");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        // Basic email format check when @ is present (not definitive — backend validates)
        if (username.contains("@") && !username.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("That email address doesn't look valid. Please check and try again.");
            usernameField.requestFocus();
            return;
        }

        clearError();
        setLoadingState(true);

        // PROTOTYPE: simulate network call with a 1.5s pause
        simulateSignIn();
    }

    /**
     * Simulates a backend sign-in call.
     *
     * <p><strong>Replace this entire method</strong> with an actual
     * HTTP call in production, for example:</p>
     * <pre>
     * authService.login(username, password)
     *     .thenAccept(token -&gt; Platform.runLater(() -&gt; navigateToCheckout()))
     *     .exceptionally(err -&gt; { Platform.runLater(() -&gt; showAuthError()); return null; });
     * </pre>
     */
    private void simulateSignIn() {
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            setLoadingState(false);
            // In a real app, the backend decides if credentials are valid.
            // Here we always succeed to demonstrate the navigation flow.
            navigateToCheckout();
        });
        pause.play();
    }

    /**
     * Opens the PIN login workflow.
     *
     * <p><strong>PROTOTYPE:</strong> Currently shows an informational alert.
     * Replace with a navigation to the secure PIN entry screen when ready.</p>
     */
    @FXML
    private void onPinLoginClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PIN Login");
        alert.setHeaderText("PIN Authentication — Not Yet Implemented");
        alert.setContentText(
            "The PIN login screen is a planned feature.\n" +
            "Connect this button to the secure PIN authentication workflow when ready.\n\n" +
            "No mock PIN login is provided to avoid creating a false sense of security.");
        alert.showAndWait();
    }

    /**
     * Opens the password recovery workflow.
     *
     * <p><strong>PROTOTYPE:</strong> Currently shows an informational alert.
     * Replace with navigation to the configured recovery flow when ready.</p>
     */
    @FXML
    private void onForgotPasswordClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Forgot Password — Not Yet Implemented");
        alert.setContentText(
            "Connect this link to the backend password-recovery workflow.\n" +
            "Do not implement a fake recovery flow that bypasses real authentication.");
        alert.showAndWait();
    }

    // =====================================================================
    // Navigation
    // =====================================================================

    private void navigateToCheckout() {
        try {
            URL fxml = getClass().getResource("/com/tripos/pos/view/CheckoutView.fxml");
            if (fxml == null) {
                showError("Checkout screen not found. Please contact support.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 1280, 800);
            URL css = getClass().getResource("/css/pos-theme.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setTitle("TRIPOS — Ebenezer POS  |  Cashier Checkout  [PROTOTYPE]");
            stage.setScene(scene);
        } catch (Exception ex) {
            setLoadingState(false);
            showError("Could not load the checkout screen. Please restart the application.");
            ex.printStackTrace();
        }
    }

    // =====================================================================
    // UI helpers
    // =====================================================================

    private void setLoadingState(boolean loading) {
        signInButton.setDisable(loading);
        pinLoginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);

        if (loadingSpinner != null) {
            loadingSpinner.setVisible(loading);
            loadingSpinner.setManaged(loading);
        }
        signInButton.setText(loading ? "Signing in…" : "Sign in");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        // Shake/fade animation for attention
        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0); fade.setToValue(1); fade.play();
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
