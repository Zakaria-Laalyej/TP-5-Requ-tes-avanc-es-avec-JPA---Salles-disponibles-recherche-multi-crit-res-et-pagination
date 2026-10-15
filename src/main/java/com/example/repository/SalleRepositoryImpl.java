package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalleRepositoryImpl implements SalleRepository {

    private EntityManager entityManager;

    public SalleRepositoryImpl() {}

    public SalleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                      "SELECT r.salle.id FROM Reservation r WHERE " +
                      "(:debut < r.fin AND :fin > r.debut)" +
                      ")";
        TypedQuery<Salle> query = entityManager.createQuery(jpql, Salle.class);
        query.setParameter("debut", debut);
        query.setParameter("fin", fin);
        return query.getResultList();
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> predicates = buildPredicates(cb, cq, salle, nom, capaciteMin, debut, fin);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        cq.orderBy(cb.asc(salle.get("nom")));

        TypedQuery<Salle> query = entityManager.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Salle> salle = countQuery.from(Salle.class);
        countQuery.select(cb.count(salle));

        List<Predicate> predicates = buildPredicates(cb, countQuery, salle, nom, capaciteMin, debut, fin);

        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, CommonAbstractCriteria query, Root<Salle> salle,
                                            String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> predicates = new ArrayList<>();

        if (nom != null && !nom.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(salle.get("nom")), "%" + nom.toLowerCase() + "%"));
        }

        if (capaciteMin != null) {
            predicates.add(cb.ge(salle.get("capacite"), capaciteMin));
        }

        if (debut != null && fin != null) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Reservation> res = subquery.from(Reservation.class);
            subquery.select(res.get("salle").get("id"));
            Predicate overlap = cb.and(
                cb.lessThan(res.get("debut"), fin),
                cb.greaterThan(res.get("fin"), debut)
            );
            subquery.where(overlap);
            predicates.add(cb.not(salle.get("id").in(subquery)));
        }

        return predicates;
    }
}
