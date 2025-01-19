import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSLParser {
    public static List<String> parserDSL(String dsl) {
        List<String> scenarioCommands = new ArrayList<>();

        // Expression régulière pour matcher les commandes dans le DSL
        String regex = "(allumer|éteindre) (\\w+) (à (\\d{2}:\\d{2}))?(après (\\d+) minutes)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dsl);

        while (matcher.find()) {
            String action = matcher.group(1);  // "allumer" ou "éteindre"
            String equipement = matcher.group(2);  // nom de l'équipement (ex: lampe_salon)
            String heure = matcher.group(4);  // heure d'exécution
            String minutes = matcher.group(6);  // minutes après lesquelles éteindre

            if ("allumer".equals(action)) {
                if (heure != null) {
                    scenarioCommands.add("allumer " + equipement + " à " + heure);
                } else {
                    scenarioCommands.add("allumer " + equipement);
                }
            } else if ("éteindre".equals(action)) {
                if (minutes != null) {
                    // S'assurer que les minutes sont un nombre valide
                    try {
                        int minutesValue = Integer.parseInt(minutes);
                        scenarioCommands.add("éteindre " + equipement + " après " + minutesValue + " minutes");
                    } catch (NumberFormatException e) {
                        System.out.println("Erreur : le format des minutes est incorrect.");
                    }
                } else if (heure != null) {
                    scenarioCommands.add("éteindre " + equipement + " à " + heure);
                } else {
                    scenarioCommands.add("éteindre " + equipement);
                }
            }
        }

        return scenarioCommands;
    }
}
