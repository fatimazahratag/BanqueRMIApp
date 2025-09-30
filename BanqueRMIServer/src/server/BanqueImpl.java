package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BanqueImpl extends UnicastRemoteObject implements IBanque {
    private static class Compte {
        final int id;
        final String motDePasse;
        double solde;
        final List<String> operations = new ArrayList<>();
        Compte(int id, String motDePasse) {
            this.id = id;
            this.motDePasse = motDePasse;
            this.solde = 0.0;
        }
    }

    private final Map<Integer, Compte> comptes = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public BanqueImpl() throws RemoteException {
        super();
        int testId = creerCompte("test123");
        comptes.get(testId).solde = 1000.0;
        comptes.get(testId).operations.add("Compte créé avec solde initial de 1000.0");
    }

    @Override
    public synchronized int creerCompte(String motDePasse) throws RemoteException {
        int id = nextId.getAndIncrement();
        Compte c = new Compte(id, motDePasse);
        comptes.put(id, c);
        c.operations.add("Compte créé");
        return id;
    }

    @Override
    public synchronized boolean seConnecter(int compteId, String motDePasse) throws RemoteException {
        Compte c = comptes.get(compteId);
        return c != null && c.motDePasse.equals(motDePasse);
    }

    @Override
    public synchronized double consulterSolde(int compteId) throws RemoteException {
        Compte c = comptes.get(compteId);
        return (c != null) ? c.solde : 0.0;
    }

    @Override
    public synchronized void deposer(int compteId, double montant) throws RemoteException {
        Compte c = comptes.get(compteId);
        if (c != null) {
            c.solde += montant;
            c.operations.add(String.format("Dépôt de %.2f -> solde=%.2f", montant, c.solde));
        }
    }

    @Override
    public synchronized boolean retirer(int compteId, double montant) throws RemoteException {
        Compte c = comptes.get(compteId);
        if (c != null && c.solde >= montant) {
            c.solde -= montant;
            c.operations.add(String.format("Retrait de %.2f -> solde=%.2f", montant, c.solde));
            return true;
        }
        return false;
    }

    @Override
    public synchronized List<String> listerOperations(int compteId) throws RemoteException {
        Compte c = comptes.get(compteId);
        return (c != null)
             ? new ArrayList<>(c.operations)
             : Collections.emptyList();
    }

    public int getMaxCompteId() {
        return nextId.get() - 1;
    }
}
