package model;

public abstract class Personnel {
    private String id;
    private String nom;
    private String service;

    public Personnel(String id, String nom) {
        this.id = id;
        this.nom = nom;
        this.service = "Non affecté";
    }


    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service=service;
    }

    public abstract void examiner(Patient p);

}
