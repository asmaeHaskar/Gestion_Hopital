package service;

import model.*;
import java.util.*;

public class Hopital {
    private static Hopital instance;

    // Utilisation d'une Map pour organiser les patients par service [cite: 8, 22, 24]
    private Map<String, ArrayList<Patient>> services;
    // Liste pour gérer l'ensemble du personnel [cite: 11]
    private ArrayList<Personnel> staff;
    // Liste globale pour un accès rapide aux patients [cite: 8]
    private ArrayList<Patient> tousLesPatients;

    private Hopital() {
        services = new HashMap<>();
        staff = new ArrayList<>();
        tousLesPatients = new ArrayList<>();

        // Initialisation automatique au démarrage
        initialiserDonneesMarocaines();
    }

    public static Hopital getInstance() {
        if (instance == null) {
            instance = new Hopital();
        }
        return instance;
    }

    /**
     * Remplit l'hôpital avec des services, du personnel et des patients
     * pour que le dashboard soit fonctionnel dès le lancement.
     */
    private void initialiserDonneesMarocaines() {
        // 1. Création des services requis [cite: 22]
        String[] nomsServices = {"Urgences", "Cardiologie", "Pédiatrie", "Neurologie", "Réanimation"};
        for (String nom : nomsServices) {
            ajouterService(nom);
        }

        // 2. Ajout du personnel par défaut [cite: 11]
        // Médecins (Polymorphisme : examiner() prescrira un diagnostic) [cite: 19]
        ajouterPersonnel(new Medecin("M01", " Salah", "Cardiologie"));
        ajouterPersonnel(new Medecin("M02", " Zakaria", "Neurologie"));

        // Infirmiers (Polymorphisme : examiner() administrera des soins)
        ajouterPersonnel(new Infirmier("I01", "Asmae", "Pédiatrie", 8));
        ajouterPersonnel(new Infirmier("I02", "Youssef", "Urgences", 5));

        // 3. Ajout des patients par défaut
        ajouterPatient(new Patient("P100", "Aicha"), "Urgences");
        ajouterPatient(new Patient("P101", "Maryam"), "Pédiatrie");
        ajouterPatient(new Patient("P102", "Ahmed"), "Cardiologie");
        ajouterPatient(new Patient("P103", "Fatima"), "Réanimation");
        ajouterPatient(new Patient("P104", "Omar"), "Neurologie");
    }

    // --- GESTION DES SERVICES ---
    public void ajouterService(String nomService) {
        if (!services.containsKey(nomService)) {
            services.put(nomService, new ArrayList<>());
        }
    }

    // --- GESTION DES PATIENTS ---
    public void ajouterPatient(Patient p, String nomService) {
        tousLesPatients.add(p);

        // Affectation au service [cite: 12, 22]
        if (services.containsKey(nomService)) {
            services.get(nomService).add(p);
        } else {
            ArrayList<Patient> nouveauService = new ArrayList<>();
            nouveauService.add(p);
            services.put(nomService, nouveauService);
        }
    }

    // --- GESTION DU PERSONNEL ---
    public void ajouterPersonnel(Personnel p) {
        staff.add(p);
    }

    // --- GETTERS POUR L'UI ---
    public Set<String> getNomsServices() { return services.keySet(); }
    public ArrayList<Patient> getPatientsDuService(String nomService) {
        return services.getOrDefault(nomService, new ArrayList<>());
    }
    public ArrayList<Patient> getTousLesPatients() { return tousLesPatients; }
    public ArrayList<Personnel> getStaff() { return staff; }

    // Statistiques pour les graphiques du Dashboard
    public int getTotalPatients() { return tousLesPatients.size(); }
    public int getTotalStaff() { return staff.size(); }
    public int getTotalServices() { return services.size(); }

    public Map<String, Integer> getStatsParService() {
        Map<String, Integer> stats = new HashMap<>();
        for (String service : services.keySet()) {
            stats.put(service, services.get(service).size());
        }
        return stats;
    }

    public Map<Object, Object> getTousLes_Patients() {
        return Map.of();
    }
}