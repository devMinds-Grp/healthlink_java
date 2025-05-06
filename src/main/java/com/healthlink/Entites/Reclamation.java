package com.healthlink.Entites;

import com.healthlink.Services.CategorieService;

public class Reclamation {
    private int id;
    private String titre;
    private String description;
    private int categoryId;  // correspond à category_id dans la DB
    private String image;
    private String statut;
    public Reclamation() {}
    public Reclamation(int id, String titre, String description, int categoryId, String image, String statut) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.categoryId = categoryId;
        this.image = image;
        this.statut = statut;
    }

    public Reclamation(int id, String titre, String description, int categoryId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.categoryId = categoryId;
    }
    public Reclamation( String titre, String description, int categoryId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public CategorieService categorieService = new CategorieService();
    public Categorie getCategorie() {
        return categorieService.getCategorieById(categoryId);
    }
}