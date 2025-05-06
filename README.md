# ♻️ Module Recyclage et Collecte des Déchets – DataFarm

Ce module fait partie de l’application **DataFarm**. Il permet de gérer les déchets agricoles, leur collecte, ainsi que les opérations de recyclage et la production d’énergie qui en résulte.

---

## 🎯 Objectifs

- Collecter les déchets issus des activités agricoles
- Organiser et suivre les opérations de recyclage
- Quantifier la production d’énergie issue du recyclage
- Analyser l’utilisation de cette énergie

---

## 🧩 Fonctionnalités

### 🚛 Gestion des collectes de déchets
- Ajout d’une nouvelle collecte avec les informations suivantes :
  - Type de déchet (fumier, paille, déchets végétaux, etc.)
  - Quantité collectée
  - Date de début et de fin de collecte
- Modification et suppression d’une collecte existante
- Affichage d’une liste de toutes les collectes enregistrées
- Filtrage et tri des collectes

### 🔄 Gestion du recyclage
- Création d’une opération de recyclage
- Saisie des informations de recyclage :
  - Date de recyclage
  - Quantité recyclée
  - Énergie produite (en kWh)
  - Type d'utilisation de l’énergie (irrigation, chauffage, etc.)
- Mise à jour et suppression des opérations de recyclage
- Consultation des opérations passées

### 📊 Suivi et analyse
- Statistiques sur :
  - Les quantités de déchets recyclés
  - La production totale d’énergie
  - La répartition de l’utilisation de cette énergie



---

## 👥 Rôles utilisateurs

- **Employé** : ajoute les collectes
- **Administrateur** : gère les opérations de recyclage et consulte les statistiques

---

## ⚙️ Technologies utilisées

- **Java** – Langage principal
- **JavaFX** – Interface graphique
- **FXML** – Fichiers de description des vues
- **MySQL / MariaDB** – Base de données
- **JavaFX Charts** – Affichage des statistiques

---
