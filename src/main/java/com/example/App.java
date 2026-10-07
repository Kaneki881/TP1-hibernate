package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Création de l'EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-demo");

        // Insertion de produits
        insererProduits(emf);

        // Lecture des produits
        lireProduits(emf);

        // Exercice 1 : mise à jour du prix d'un produit
        Long idSmartphone = trouverIdParNom(emf, "Smartphone");
        mettreAJourPrix(emf, idSmartphone, new BigDecimal("449.99"));

        // Exercice 4 : recherche de produits par plage de prix
        rechercherParPlagePrix(emf, new BigDecimal("300"), new BigDecimal("600"));

        // Exercice 2 : suppression d'un produit
        Long idTablette = trouverIdParNom(emf, "Tablette");
        supprimerProduit(emf, idTablette);

        // Vérification après modification et suppression
        lireProduits(emf);

        // Fermeture de l'EntityManagerFactory
        emf.close();
    }

    private static void insererProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création de quelques produits
            Produit p1 = new Produit("Laptop", new BigDecimal("999.99"));
            Produit p2 = new Produit("Smartphone", new BigDecimal("499.99"));
            Produit p3 = new Produit("Tablette", new BigDecimal("299.99"));
            Produit p4 = new Produit("TV", new BigDecimal("599.99"));

            // Création des catégories et rattachement des produits
            Categorie informatique = new Categorie("Informatique");
            informatique.ajouterProduit(p1);
            informatique.ajouterProduit(p3);

            Categorie multimedia = new Categorie("Multimédia");
            multimedia.ajouterProduit(p2);
            multimedia.ajouterProduit(p4);

            // Persistance des catégories (les produits suivent grâce au cascade)
            em.persist(informatique);
            em.persist(multimedia);

            em.getTransaction().commit();
            System.out.println("Produits insérés avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // Requête JPQL pour récupérer tous les produits
            List<Produit> produits = em.createQuery("SELECT p FROM Produit p", Produit.class)
                    .getResultList();

            System.out.println("\nListe des produits :");
            for (Produit produit : produits) {
                System.out.println(produit);
            }

            // Recherche d'un produit par ID
            System.out.println("\nRecherche du produit avec ID=2 :");
            Produit produit = em.find(Produit.class, 2L);
            if (produit != null) {
                System.out.println(produit);
            } else {
                System.out.println("Produit non trouvé");
            }

            // Lecture des catégories avec leurs produits (JOIN FETCH évite le chargement paresseux)
            List<Categorie> categories = em.createQuery(
                            "SELECT DISTINCT c FROM Categorie c LEFT JOIN FETCH c.produits", Categorie.class)
                    .getResultList();

            System.out.println("\nListe des catégories et de leurs produits :");
            for (Categorie categorie : categories) {
                System.out.println(categorie);
                for (Produit p : categorie.getProduits()) {
                    System.out.println("   -> " + p);
                }
            }
        } finally {
            em.close();
        }
    }

    // Exercice 1 : mettre à jour le prix d'un produit
    private static boolean mettreAJourPrix(EntityManagerFactory emf, Long id, BigDecimal nouveauPrix) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, id);
            if (produit == null) {
                em.getTransaction().rollback();
                System.out.println("\nMise à jour impossible : produit introuvable (ID=" + id + ")");
                return false;
            }

            // Le produit est "managed" : Hibernate détecte la modification (dirty checking)
            // et génère le UPDATE au commit, sans appel explicite à merge().
            BigDecimal ancienPrix = produit.getPrix();
            produit.setPrix(nouveauPrix);

            em.getTransaction().commit();
            System.out.println("\nPrix de '" + produit.getNom() + "' mis à jour : "
                    + ancienPrix + " -> " + nouveauPrix);
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    // Exercice 2 : supprimer un produit
    private static boolean supprimerProduit(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, id);
            if (produit == null) {
                em.getTransaction().rollback();
                System.out.println("\nSuppression impossible : produit introuvable (ID=" + id + ")");
                return false;
            }

            // On retire d'abord le produit de sa catégorie pour garder la relation cohérente
            // (sinon le cascade ALL de Categorie pourrait le considérer comme encore rattaché).
            if (produit.getCategorie() != null) {
                produit.getCategorie().retirerProduit(produit);
            }
            em.remove(produit);

            em.getTransaction().commit();
            System.out.println("\nProduit supprimé : " + produit.getNom());
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    // Exercice 4 : rechercher les produits dont le prix est compris entre min et max (inclus)
    private static List<Produit> rechercherParPlagePrix(EntityManagerFactory emf, BigDecimal min, BigDecimal max) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Produit> resultats = em.createQuery(
                            "SELECT p FROM Produit p WHERE p.prix BETWEEN :min AND :max ORDER BY p.prix",
                            Produit.class)
                    .setParameter("min", min)
                    .setParameter("max", max)
                    .getResultList();

            System.out.println("\nProduits dont le prix est entre " + min + " et " + max + " :");
            if (resultats.isEmpty()) {
                System.out.println("Aucun produit trouvé");
            }
            for (Produit produit : resultats) {
                System.out.println(produit);
            }
            return resultats;
        } finally {
            em.close();
        }
    }

    // Méthode utilitaire : retrouver l'ID d'un produit à partir de son nom
    private static Long trouverIdParNom(EntityManagerFactory emf, String nom) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Long> ids = em.createQuery("SELECT p.id FROM Produit p WHERE p.nom = :nom", Long.class)
                    .setParameter("nom", nom)
                    .getResultList();
            return ids.isEmpty() ? null : ids.get(0);
        } finally {
            em.close();
        }
    }
}