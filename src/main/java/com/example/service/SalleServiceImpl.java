package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleRepositoryImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class SalleServiceImpl implements SalleService {

    private SalleRepository salleRepository;

    public SalleServiceImpl() {
        this.salleRepository = new SalleRepositoryImpl();
    }

    public SalleServiceImpl(SalleRepository salleRepository) {
        this.salleRepository = salleRepository;
    }

    public void setEntityManager(EntityManager em) {
        if (this.salleRepository instanceof SalleRepositoryImpl) {
            ((SalleRepositoryImpl) this.salleRepository).setEntityManager(em);
        }
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        return salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.countRooms(nom, capaciteMin, debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRoomsPaginated(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin, int page, int size) {
        List<Salle> items = salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
        long total = salleRepository.countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(items, page, size, total);
    }
}
