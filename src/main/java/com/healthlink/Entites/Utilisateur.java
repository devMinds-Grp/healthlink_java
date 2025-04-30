package com.healthlink.Entites;

import java.util.Objects;

public class Utilisateur {
    private int id;
    private Role role;
    private String nom;
    private String prenom;
    private String email;
    private String mot_de_passe;
    private int num_tel;
    private String adresse;
    private String speciality;
    private String categorie_soin;
    private String image ;
    private String imageprofile;
    private String statut;
    private int reset_code;

    public Utilisateur() {}

    public Utilisateur(int id, Role role, String nom, String prenom, String email, String mot_de_passe, int num_tel, String adresse, String speciality, String categorie_soin, String image, String imageprofile, String statut, int reset_code) {
        this.id = id;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mot_de_passe = mot_de_passe;
        this.num_tel = num_tel;
        this.adresse = adresse;
        this.speciality = speciality;
        this.categorie_soin = categorie_soin;
        this.image = image;
        this.imageprofile = imageprofile;
        this.statut = statut;
        this.reset_code = reset_code;
    }

    public Utilisateur(int id, String nom, String prenom, String email, String mot_de_passe, int num_tel) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mot_de_passe = mot_de_passe;
        this.num_tel = num_tel;
    }
    public Utilisateur(String nom, String prenom, String email, String mot_de_passe, int num_tel, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mot_de_passe = mot_de_passe;
        this.num_tel = num_tel;
        this.role = role;
    }

    public Utilisateur(Role role, String nom, String prenom, String email, String mot_de_passe, int num_tel, String adresse, String speciality, String categorie_soin, String image, String imageprofile, String statut, int reset_code) {
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mot_de_passe = mot_de_passe;
        this.num_tel = num_tel;
        this.adresse = adresse;
        this.speciality = speciality;
        this.categorie_soin = categorie_soin;
        this.image = image;
        this.imageprofile = imageprofile;
        this.statut = statut;
        this.reset_code = reset_code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public void setMot_de_passe(String mot_de_passe) {
        this.mot_de_passe = mot_de_passe;
    }

    public int getNum_tel() {
        return num_tel;
    }

    public void setNum_tel(int num_tel) {
        this.num_tel = num_tel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getCategorie_soin() {
        return categorie_soin;
    }

    public void setCategorie_soin(String categorie_soin) {
        this.categorie_soin = categorie_soin;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageprofile() {
        return imageprofile;
    }

    public void setImageprofile(String imageprofile) {
        this.imageprofile = imageprofile;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getReset_code() {
        return reset_code;
    }

    public void setReset_code(int reset_code) {
        this.reset_code = reset_code;
    }

    @Override
    public String toString() {
        return "user{" +
                "id=" + id +
                ", role=" + (role != null ? role.getNom() : "null") +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", mot_de_passe='" + mot_de_passe + '\'' +
                ", num_tel=" + num_tel +
                ", adresse='" + adresse + '\'' +
                ", speciality='" + speciality + '\'' +
                ", categorie_soin='" + categorie_soin + '\'' +
                ", image=" + image +
                ", imageprofile=" + imageprofile +
                ", statut='" + statut + '\'' +
                ", reset_code=" + reset_code +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur utilisateur = (Utilisateur) o;
        return id == utilisateur.id &&
                num_tel == utilisateur.num_tel &&
                image == utilisateur.image &&
                imageprofile == utilisateur.imageprofile &&
                reset_code == utilisateur.reset_code &&
                Objects.equals(role, utilisateur.role) && // Modification ici
                Objects.equals(nom, utilisateur.nom) &&
                Objects.equals(prenom, utilisateur.prenom) &&
                Objects.equals(email, utilisateur.email) &&
                Objects.equals(mot_de_passe, utilisateur.mot_de_passe) &&
                Objects.equals(adresse, utilisateur.adresse) &&
                Objects.equals(speciality, utilisateur.speciality) &&
                Objects.equals(categorie_soin, utilisateur.categorie_soin) &&
                Objects.equals(statut, utilisateur.statut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, nom, prenom, email, mot_de_passe, num_tel,
                adresse, speciality, categorie_soin, image, imageprofile,
                statut, reset_code); // Modification ici
    }
}
