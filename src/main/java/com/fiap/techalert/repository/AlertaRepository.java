package com.fiap.techalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fiap.techalert.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
}
