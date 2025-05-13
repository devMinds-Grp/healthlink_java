# 🏥 Application de Service Médical

> Une application médicale développée avec **Java et JavaFX** qui permet de gérer différents services médicaux tels que les utilisateurs, les rendez-vous, les prescriptions, les soins, les dons du sang, les réclamations et un forum d'échange.

## 📋 Description

Cette application a été conçue pour aider les professionnels de santé ainsi que les patients à mieux gérer leurs interactions et services au sein d’un établissement médical. Elle inclut 6 modules principaux :

### 🧩 Modules Principaux

1. **Gestion des Utilisateurs**  
   - Création, modification et suppression des utilisateurs (patients, médecins, infirmiers, kinésithérapeutes, administrateurs...).
   - Gestion des rôles et permissions.

2. **Gestion du Forum**  
   - Forums thématiques.
   - Création de sujets et participation aux discussions.
   - Modération par les admins.

3. **Gestion des Réclamations**  
   - Soumission de réclamations par les patients.
   - Suivi et réponse par les admins.

4. **Gestion des Rendez-vous et Prescriptions**  
   - Prise de rendez-vous en ligne.
   - Génération et gestion des ordonnances/prescriptions.
   - Confirmation, annulation et rappel automatique.

5. **Gestion des Soins**  
   - Enregistrement des soins prodigués par les kinésithérapeutes ou infirmiers.
   - Historique des soins par patient.

6. **Gestion des Dons du Sang**  
   - Inscription aux campagnes de don du sang.
   - Suivi des donneurs et historique des dons.
   - Recherche selon les groupes sanguins.

---

## 🛠️ Technologies Utilisées

- **Java 17**
- **JavaFX**
- **FXML** pour l’interface graphique
- **MySQL** comme base de données
- **JDBC** pour la connexion à la base de données
- **Maven** comme outil de build
- **OpenCV** si reconnaissance d'image

---

## 📦 Installation

### Étape 1 : Cloner le projet

```bash
git clone https://github.com/devMinds-Grp/healthlink_java.git
cd healthlink_java
```

### Étape 2 : Installer les dépendances

Assurez-vous d’avoir :
- **Java 17**
- **JavaFX SDK**
- **IntelliJ IDEA** 

Puis exécutez :

```bash
mvn install
```

### Étape 3 : Configurer la base de données

Modifiez le fichier de configuration (`config.properties` ou directement dans le code) pour configurer vos identifiants de base de données :

```properties
db.url=jdbc:mysql://localhost:3306/nom_de_la_base
db.user=nom_utilisateur
db.password=mot_de_passe
```

### Étape 4 : Exécuter l'application

```bash
mvn clean javafx:run
```

---

## 🧪 Tests

Vous pouvez exécuter les tests unitaires avec JUnit :

```bash
mvn test
```

---


## 🤝 Contribution

Les contributions sont les bienvenues ! Si vous souhaitez participer :

1. Fork le repo
2. Crée une branche (`git checkout -b feature/feature-name`)
3. Commit tes changements (`git commit -m 'feat: add new feature'`)
4. Push sur la branche (`git push origin feature/feature-name`)
5. Ouvre une Pull Request

---


## 📞 Contact

Pour toute question ou suggestion, contactez-nous à :
📧 healthlink@devminds.com  
💻 GitHub: [github.com/healthlink_java](https://github.com/devMinds-Grp/healthlink_java)
```
