package com.demo.auth.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.auth.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken ,Integer> {
  
}