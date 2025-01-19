import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MaisonDomotique {
    // Équipements dans la maison
    private boolean lampeSalon = false;
    private boolean lampeCuisine = false;
    private boolean chauffage = false;
    private boolean ventilateur = false;

    private HashMap<String, Timer> equipementTimers;

    public MaisonDomotique() {
        equipementTimers = new HashMap<>();
    }

    // Méthode pour allumer un équipement
    public void allumerEquipement(String equipement) {
        switch (equipement) {
            case "lampe_salon":
                lampeSalon = true;
                break;
            case "lampe_cuisine":
                lampeCuisine = true;
                break;
            case "chauffage":
                chauffage = true;
                break;
            case "ventilateur":
                ventilateur = true;
                break;
            default:
                System.out.println("Équipement non trouvé : " + equipement);
                break;
        }
        System.out.println("Allumer " + equipement);
    }

    // Méthode pour éteindre un équipement
    public void eteindreEquipement(String equipement) {
        switch (equipement) {
            case "lampe_salon":
                lampeSalon = false;
                break;
            case "lampe_cuisine":
                lampeCuisine = false;
                break;
            case "chauffage":
                chauffage = false;
                break;
            case "ventilateur":
                ventilateur = false;
                break;
            default:
                System.out.println("Équipement non trouvé : " + equipement);
                break;
        }
        System.out.println("Éteindre " + equipement);
    }

    // Méthode pour vérifier si un équipement est allumé
    public boolean isEquipementAllume(String equipement) {
        switch (equipement) {
            case "lampe_salon":
                return lampeSalon;
            case "lampe_cuisine":
                return lampeCuisine;
            case "chauffage":
                return chauffage;
            case "ventilateur":
                return ventilateur;
            default:
                System.out.println("Équipement non trouvé : " + equipement);
                return false;
        }
    }

    // Méthode pour exécuter les commandes du scénario
    public void executerScenario(List<String> scenario) {
        for (String commande : scenario) {
            if (commande.startsWith("allumer")) {
                String equipement = commande.split(" ")[1];
                allumerEquipement(equipement);
                sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
            } else if (commande.startsWith("éteindre")) {
                String equipement = commande.split(" ")[1];
                if (commande.contains("après")) {
                    int minutes = Integer.parseInt(commande.split(" ")[3]); // Récupérer le délai en minutes
                    System.out.println("Éteindre " + equipement + " après " + minutes + " minutes");
                    startCountdown(equipement, minutes * 60000); // Lancer le compte à rebours
                } else if (commande.contains("à")) {
                    String heure = commande.split(" ")[2]; // Récupérer l'heure spécifique
                    System.out.println("Éteindre " + equipement + " à " + heure);
                    startCountdownAtTime(equipement, heure); // Lancer le compte à rebours à une heure spécifique
                } else {
                    eteindreEquipement(equipement); // Éteindre immédiatement
                    sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
                }
            }
        }
    }

    private void startCountdown(String equipement, long delayMillis) {
        Timer timer = new Timer();
        equipementTimers.put(equipement, timer);

        TimerTask countdownTask = new TimerTask() {
            @Override
            public void run() {
                eteindreEquipement(equipement); // Éteindre l'équipement après le délai
                sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
                timer.cancel(); // Arrêter le timer
                equipementTimers.remove(equipement); // Supprimer le timer de la liste
            }
        };

        timer.schedule(countdownTask, delayMillis);
    }

    private void startCountdownAtTime(String equipement, String heure) {
        Timer timer = new Timer();
        equipementTimers.put(equipement, timer);

        TimerTask countdownTask = new TimerTask() {
            @Override
            public void run() {
                java.time.LocalTime currentTime = java.time.LocalTime.now();
                String currentHour = currentTime.toString().substring(0, 5); // Format HH:MM
                if (currentHour.equals(heure)) {
                    eteindreEquipement(equipement); // Éteindre l'équipement à l'heure spécifiée
                    sendCommandToProcessing("TOGGLE_" + equipement.toUpperCase());
                    timer.cancel(); // Arrêter le timer
                    equipementTimers.remove(equipement); // Supprimer le timer de la liste
                }
            }
        };

        timer.scheduleAtFixedRate(countdownTask, 0, 1000); // Vérifier toutes les secondes
    }

    private void cancelCountdown(String equipement) {
        Timer timer = equipementTimers.get(equipement);
        if (timer != null) {
            timer.cancel();
            equipementTimers.remove(equipement);
        }
    }

    // Méthode pour envoyer une commande à Processing
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
}
