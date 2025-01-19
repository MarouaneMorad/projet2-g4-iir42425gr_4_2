import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MaisonDomotiqueUI extends Application {

    private MaisonDomotique maisonDomotique;
    private HashMap<String, Timer> equipementTimers;
    private Map<String, Button> equipementButtons = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        maisonDomotique = new MaisonDomotique();
        equipementTimers = new HashMap<>();

        // Création du container principal
        VBox root = new VBox(15);  // Espacement vertical entre les éléments
        root.setStyle("-fx-background-color: #f4f4f9; -fx-padding: 20;");  // Fond clair et padding
        root.setPrefWidth(600);

        // Création d'un titre
        Text title = new Text("Maison Domotique");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: #333;");

        // Configuration des équipements
        VBox lampeSalonBox = createEquipementBox("Lampe Salon", "lampe_salon");
        VBox lampeCuisineBox = createEquipementBox("Lampe Cuisine", "lampe_cuisine");
        VBox chauffageBox = createEquipementBox("Chauffage", "chauffage");
        VBox ventilateurBox = createEquipementBox("Ventilateur", "ventilateur");

        // Configuration du bouton de programmation
        Button programAllButton = new Button("Programmer tous les équipements");
        programAllButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");
        programAllButton.setOnAction(e -> {
            startEquipementCountdown("lampe_salon", lampeSalonBox);
            startEquipementCountdown("lampe_cuisine", lampeCuisineBox);
            startEquipementCountdown("chauffage", chauffageBox);
            startEquipementCountdown("ventilateur", ventilateurBox);
        });

        // Bouton pour charger un scénario
        Button loadScenarioButton = new Button("Charger un scénario");
        loadScenarioButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20;");
        loadScenarioButton.setOnAction(e -> loadAndExecuteScenario());

        // Ajouter des éléments au root
        root.getChildren().addAll(title, lampeSalonBox, lampeCuisineBox, chauffageBox, ventilateurBox, programAllButton, loadScenarioButton);

        // Mise en scène
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("Maison Domotique");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createEquipementBox(String label, String equipement) {
        Text statusText = new Text("État: Éteint");
        statusText.setFill(Color.RED);

        Button equipementBtn = new Button("Allumer");
        equipementBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10;");
        equipementBtn.setOnAction(e -> toggleEquipement(equipement, equipementBtn, statusText));

        Spinner<Integer> equipementSpinner = new Spinner<>(0, 60, 0, 1);
        equipementSpinner.setStyle("-fx-padding: 5;");

        HBox equipementRow = new HBox(10);
        equipementRow.getChildren().addAll(equipementBtn, statusText, equipementSpinner);
        equipementRow.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");
        equipementRow.setPrefWidth(550);

        // Associer le bouton à l'équipement
        equipementButtons.put(equipement, equipementBtn);

        return new VBox(10, new Label(label), equipementRow);
    }

    private void toggleEquipement(String equipement, Button btn, Text statusText) {
        boolean isOn = maisonDomotique.isEquipementAllume(equipement);
        if (isOn) {
            maisonDomotique.eteindreEquipement(equipement);
            sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
            cancelCountdown(equipement);
            updateStatusText(statusText, btn, false);
        } else {
            maisonDomotique.allumerEquipement(equipement);
            sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
            updateStatusText(statusText, btn, true);
        }
    }

    private void startEquipementCountdown(String equipement, VBox equipementBox) {
        Spinner<Integer> spinner = (Spinner<Integer>) ((HBox) equipementBox.getChildren().get(1)).getChildren().get(2);
        int delayMinutes = spinner.getValue();
        if (delayMinutes > 0) {
            startCountdown(equipement, delayMinutes, equipementBox);
        }
    }

    private void startCountdown(String equipement, int delayMinutes, VBox equipementBox) {
        Timer timer = new Timer();
        equipementTimers.put(equipement, timer);

        TimerTask countdownTask = new TimerTask() {
            int remainingTime = delayMinutes * 60;

            @Override
            public void run() {
                if (remainingTime > 0) {
                    int minutes = remainingTime / 60;
                    int seconds = remainingTime % 60;

                    Platform.runLater(() -> updateStatusText((Text) ((HBox) equipementBox.getChildren().get(1)).getChildren().get(1), (Button) ((HBox) equipementBox.getChildren().get(1)).getChildren().get(0), true, minutes, seconds));
                    remainingTime--;
                } else {
                    Platform.runLater(() -> {
                        maisonDomotique.eteindreEquipement(equipement);
                        sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
                        updateStatusText((Text) ((HBox) equipementBox.getChildren().get(1)).getChildren().get(1), (Button) ((HBox) equipementBox.getChildren().get(1)).getChildren().get(0), false);
                    });
                    timer.cancel();
                }
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 1000);
    }

    private void cancelCountdown(String equipement) {
        Timer timer = equipementTimers.get(equipement);
        if (timer != null) {
            timer.cancel();
            equipementTimers.remove(equipement);
        }
    }

    private void updateStatusText(Text statusText, Button btn, boolean isOn, int minutes, int seconds) {
        if (isOn) {
            statusText.setText(String.format("État: Allumé (reste %02d:%02d)", minutes, seconds));
            statusText.setFill(Color.GREEN);
            btn.setText("Éteindre");
        } else {
            statusText.setText("État: Éteint");
            statusText.setFill(Color.RED);
            btn.setText("Allumer");
        }
    }

    private void updateStatusText(Text statusText, Button btn, boolean isOn) {
        if (isOn) {
            statusText.setText("État: Allumé");
            statusText.setFill(Color.GREEN);
            btn.setText("Éteindre");
        } else {
            statusText.setText("État: Éteint");
            statusText.setFill(Color.RED);
            btn.setText("Allumer");
        }
    }

    private void sendCommandToProcessing(String command) {
        try (Socket socket = new Socket("localhost", 5000);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {
            writer.println(command);
            System.out.println("Commande envoyée à Processing : " + command);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'envoi de la commande à Processing : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAndExecuteScenario() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un fichier de scénario");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                List<String> lines = Files.readAllLines(selectedFile.toPath());
                maisonDomotique.executerScenario(lines); // Exécuter le scénario

                // Mettre à jour les labels des équipements
                updateEquipementLabels();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void updateEquipementLabels() {
        Platform.runLater(() -> {
            for (Map.Entry<String, Button> entry : equipementButtons.entrySet()) {
                String equipement = entry.getKey();
                Button btn = entry.getValue();
                Text statusText = (Text) ((HBox) ((VBox) btn.getParent().getParent()).getChildren().get(1)).getChildren().get(1);

                boolean isOn = maisonDomotique.isEquipementAllume(equipement);
                updateStatusText(statusText, btn, isOn);

                Object timerObj = equipementTimers.get(equipement);
                if (timerObj instanceof CountdownTimer) {
                    CountdownTimer timer = (CountdownTimer) timerObj;
                    long elapsedTime = System.currentTimeMillis() - timer.getStartTime();
                    long remainingTime = timer.getTotalDuration() - elapsedTime;
                    int minutes = (int) (remainingTime / 60000);
                    int seconds = (int) ((remainingTime % 60000) / 1000);
                    updateStatusText(statusText, btn, true, minutes, seconds);
                }
            }
        });
    }
}