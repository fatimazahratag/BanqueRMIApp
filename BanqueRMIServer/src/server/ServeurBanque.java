package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurBanque {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(2099);
            BanqueImpl banque = new BanqueImpl();
            Naming.rebind("rmi://localhost:2099/BanqueService", banque);
            System.out.println("Serveur Banque démarré sur le port 2099.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
