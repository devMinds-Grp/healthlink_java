package com.healthlink.Entities;

import java.util.Objects;

public class User {
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
    private int image ;
    private int imageprofile;
    private String statut;
    private int reset_code;

    public User() {}

    public User(int id, Role role, String nom, String prenom, String email, String mot_de_passe, int num_tel, String adresse, String speciality, String categorie_soin, int image, int imageprofile, String statut, int reset_code) {
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

    public User(Role role, String nom, String prenom, String email, String mot_de_passe, int num_tel, String adresse, String speciality, String categorie_soin, int image, int imageprofile, String statut, int reset_code) {
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getImageprofile() {
        return imageprofile;
    }

    public void setImageprofile(int imageprofile) {
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
        User user = (User) o;
        return id == user.id &&
                num_tel == user.num_tel &&
                image == user.image &&
                imageprofile == user.imageprofile &&
                reset_code == user.reset_code &&
                Objects.equals(role, user.role) && // Modification ici
                Objects.equals(nom, user.nom) &&
                Objects.equals(prenom, user.prenom) &&
                Objects.equals(email, user.email) &&
                Objects.equals(mot_de_passe, user.mot_de_passe) &&
                Objects.equals(adresse, user.adresse) &&
                Objects.equals(speciality, user.speciality) &&
                Objects.equals(categorie_soin, user.categorie_soin) &&
                Objects.equals(statut, user.statut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, nom, prenom, email, mot_de_passe, num_tel,
                adresse, speciality, categorie_soin, image, imageprofile,
                statut, reset_code); // Modification ici
    }
}
