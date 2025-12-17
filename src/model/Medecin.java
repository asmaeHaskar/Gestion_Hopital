package model;

public class Medecin extends Personnel{
    private String specialite;

    public Medecin(String id, String nom,  String specialite) {
        super(id, nom);
        this.specialite=specialite;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    @Override
    public void examiner(Patient p) {
        System.out.println("Le médecin " + getNom() + " examine le patient " + p.getNom());
        p.setDiagnostic("Diagnostic prescrit par le Dr " + getNom() + " (" + specialite + ")");
    }
}
