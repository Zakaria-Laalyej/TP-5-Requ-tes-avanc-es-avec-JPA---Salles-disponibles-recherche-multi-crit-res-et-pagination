package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        EntityManager em = emf.createEntityManager();

        // Initialisation des données
        em.getTransaction().begin();
        Salle s1 = new Salle("Salle Atlas", 50);
        Salle s2 = new Salle("Salle Saada", 100);
        Salle s3 = new Salle("Salle Majorelle", 20);
        Salle s4 = new Salle("Salle Gueliz", 150);
        Salle s5 = new Salle("Amphi Emsi", 300);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);
        em.persist(s4);
        em.persist(s5);

        // Une réservation pour s1 le 2026-06-01 de 09:00 à 12:00
        LocalDateTime rDebut = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime rFin = LocalDateTime.of(2026, 6, 1, 12, 0);
        Reservation res1 = new Reservation(s1, rDebut, rFin);
        em.persist(res1);

        em.getTransaction().commit();

        SalleService salleService = new SalleServiceImpl();
        ((SalleServiceImpl) salleService).setEntityManager(em);

        System.out.println("=== TEST 1 : Salles disponibles par créneau (JPQL) ===");
        LocalDateTime testDebut = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime testFin = LocalDateTime.of(2026, 6, 1, 11, 0);
        List<Salle> disponibles = salleService.getAvailableRooms(testDebut, testFin);
        System.out.println("Créneau : " + testDebut + " -> " + testFin);
        System.out.println("Nombre de salles libres : " + disponibles.size());
        disponibles.forEach(s -> System.out.println(" - " + s.getNom() + " (Capacité : " + s.getCapacite() + ")"));

        System.out.println("\n=== TEST 2 : Recherche multi-critères dynamique (Criteria API) ===");
        System.out.println("Critères : capacité >= 50 et libres sur le même créneau");
        List<Salle> critResult = salleService.searchRooms(null, 50, testDebut, testFin, 1, 10);
        critResult.forEach(s -> System.out.println(" - " + s.getNom() + " (Capacité : " + s.getCapacite() + ")"));

        System.out.println("\n=== TEST 3 : Pagination côté serveur ===");
        PaginationResult<Salle> page1 = salleService.searchRoomsPaginated(null, null, null, null, 1, 2);
        System.out.println("Page 1 (taille 2) : " + page1);
        page1.getItems().forEach(s -> System.out.println("   * " + s.getNom()));

        PaginationResult<Salle> page2 = salleService.searchRoomsPaginated(null, null, null, null, 2, 2);
        System.out.println("Page 2 (taille 2) : " + page2);
        page2.getItems().forEach(s -> System.out.println("   * " + s.getNom()));

        em.close();
        emf.close();
    }
}
