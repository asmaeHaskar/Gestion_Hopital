package model;

public class Patient implements Soignable {
    private String id;
    private String nom;
    private String diagnostic;

    public Patient(String id, String nom) {
        this.id = id;
        this.nom = nom;
        this.diagnostic = "Aucun diagnostic";
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getDiagnostic() { return diagnostic; }

    // Indispensable pour que le médecin puisse modifier le diagnostic [cite: 19, 25]
    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    @Override
    public void recevoirSoins() {
        System.out.println("Le patient " + nom + " reçoit des soins.");
    }
}