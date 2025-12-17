package model;

public class Infirmier extends Personnel {
    private int niveauExperience;

    public Infirmier(String id, String nom, String service, int niveauExperience) {
        super(id, nom);
        this.niveauExperience = niveauExperience;
    }

    public int getNiveauExperience() {
        return niveauExperience;
    }

    @Override
    public void examiner(Patient p) {
        System.out.println("L'infirmier " + getNom()
                + " administre les soins au patient " + p.getNom()
                + " (expérience : " + niveauExperience + " ans)");
        p.recevoirSoins();
    }
}

