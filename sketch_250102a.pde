import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

boolean lampeSalonOn = false;
boolean ventilateurOn = false;
boolean chauffageOn = false;
boolean lampeCuisineOn = false;

void setup() {
  size(800, 600, P3D);
  smooth(8);

  new Thread(() -> startServer()).start();  // Lancer le serveur TCP dans un thread séparé
}

void draw() {
  background(100, 150, 200);
  lights();

  // Lampe Salon
  pushMatrix();
  translate(width / 4.0 - 100, height / 2.0 - 100, 0);
  drawLamp(lampeSalonOn, "Salon");
  popMatrix();

  // Lampe Cuisine
  pushMatrix();
  translate(width / 4.0 + 100, height / 2.0 - 100, 0);
  drawLamp(lampeCuisineOn, "Cuisine");
  popMatrix();

  // Ventilateur
  pushMatrix();
  translate(3 * width / 4.0 - 100, height / 2.0 + 100, 0);
  drawFan(ventilateurOn);
  popMatrix();

  // Chauffage
  pushMatrix();
  translate(3 * width / 4.0 + 100, height / 2.0 + 100, 0);
  drawHeater(chauffageOn);
  popMatrix();
}

void drawLamp(boolean isOn, String label) {
  fill(isOn ? color(255, 255, 0) : color(100));
  sphere(40);
  fill(0);
  text(label, -20, 60);
}

void drawFan(boolean isOn) {
  for (int i = 0; i < 4; i++) {
    pushMatrix();
    rotateZ(PI / 2 * i + (isOn ? frameCount * 0.05 : 0));
    fill(200);
    box(10, 60, 10);
    popMatrix();
  }
}

void drawHeater(boolean isOn) {
  fill(isOn ? color(255, 100, 100) : color(100));
  box(50, 50, 50);
  fill(0);
  text("Chauffage", -30, 60);
}

void startServer() {
  try (ServerSocket server = new ServerSocket(5000)) {
    println("Processing Server is running...");
    while (true) {
      Socket client = server.accept();
      BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
      String command = reader.readLine();
      processCommand(command);
      client.close();
    }
  } catch (IOException e) {
    e.printStackTrace();
  }
}

void processCommand(String command) {
  println("Commande reçue : " + command); // Log de la commande reçue

  switch (command) {
    case "TOGGLE_LAMPE_SALON":
      lampeSalonOn = !lampeSalonOn; // Basculer l'état de la lampe du salon
      println("Lampe Salon : " + (lampeSalonOn ? "Allumée" : "Éteinte")); // Log de l'état
      break;

    case "TOGGLE_LAMPE_CUISINE":
      lampeCuisineOn = !lampeCuisineOn; // Basculer l'état de la lampe de la cuisine
      println("Lampe Cuisine : " + (lampeCuisineOn ? "Allumée" : "Éteinte")); // Log de l'état
      break;

    case "TOGGLE_VENTILATEUR":
      ventilateurOn = !ventilateurOn; // Basculer l'état du ventilateur
      println("Ventilateur : " + (ventilateurOn ? "Allumé" : "Éteint")); // Log de l'état
      break;

    case "TOGGLE_CHAUFFAGE":
      chauffageOn = !chauffageOn; // Basculer l'état du chauffage
      println("Chauffage : " + (chauffageOn ? "Allumé" : "Éteint")); // Log de l'état
      break;

    default:
      println("Commande non reconnue : " + command); // Log pour les commandes non reconnues
      break;
  }
}
