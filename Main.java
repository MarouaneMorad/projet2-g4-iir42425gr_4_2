import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bienvenue dans le système domotique !");

        // Exemple de scénario en DSL
        String dsl = """
            scénario "Scénario Soirée"
              allumer lampe_salon à 18:00
              allumer lampe_cuisine à 18:00
              allumer chauffage à 18:00
              allumer ventilateur à 19:00
              éteindre chauffage après 30 minutes
              éteindre lampe_salon à 23:00
              éteindre lampe_cuisine à 23:00
              éteindre ventilateur à 22:00
            fin scénario
            """;

        // Parser du DSL pour obtenir les commandes
        List<String> scenarioCommands = DSLParser.parserDSL(dsl);

        // Création de l'objet MaisonDomotique
        MaisonDomotique maison = new MaisonDomotique();

        // Exécution des commandes générées par le parser
        maison.executerScenario(scenarioCommands);
    }
}
