
package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IBanque extends Remote {
    int creerCompte(String motDePasse) throws RemoteException;

    boolean seConnecter(int compteId, String motDePasse) throws RemoteException;

    double consulterSolde(int compteId) throws RemoteException;

    void deposer(int compteId, double montant) throws RemoteException;

    boolean retirer(int compteId, double montant) throws RemoteException;

    List<String> listerOperations(int compteId) throws RemoteException;
}
