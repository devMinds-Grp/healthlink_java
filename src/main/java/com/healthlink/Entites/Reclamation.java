package com.healthlink.Entites;

import com.healthlink.Services.CategorieService;

public class Reclamation {
    private int id;
    private String titre;
    private String description;
    private int categoryId;  // correspond à category_id dans la DB

    private static final CategorieService categorieService = new CategorieService();

    // Constructeurs inchangés
    public Reclamation() {}

    public Reclamation(String titre, String description, int categoryId) {
        this.titre = titre;
        this.description = description;
        this.categoryId = categoryId;
    }

    public Reclamation(int id, String titre, String description, int categoryId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.categoryId = categoryId;
    }

    // Getters et Setters inchangés
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

    /**
     * Retourne la catégorie si elle existe, sinon null
     */
    public Categorie getCategorie() {
        try {
            Categorie categorie = categorieService.getCategorieById(this.categoryId);
            if (categorie == null) {
                System.err.println("Avertissement: Catégorie ID " + this.categoryId + " non trouvée");
            }
            return categorie; // Retourne null si non trouvée
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la catégorie: " + e.getMessage());
            return null;
        }
    }

    /**
     * Version alternative qui retourne toujours l'ID
     */
    public int getCategorieId() {
        return this.categoryId;
    }

    @Override
    public String toString() {
        Categorie categorie = this.getCategorie();
        String categorieInfo = (categorie != null) ? categorie.getNom() : "null";

        return "Reclamation{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", categorie=" + categorieInfo +
                '}';
    }
}