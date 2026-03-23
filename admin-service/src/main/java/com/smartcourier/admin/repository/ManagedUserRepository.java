package com.smartcourier.admin.repository;

import com.smartcourier.admin.entity.ManagedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedUserRepository extends JpaRepository<ManagedUser, Long> {
}

