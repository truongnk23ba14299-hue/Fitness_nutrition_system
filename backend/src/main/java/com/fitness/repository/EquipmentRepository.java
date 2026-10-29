package main.java.com.fitness.repository;

import com.example.demo.model.Equipments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EquipmentRepository extends JpaRepository<Equipments, Long>  {
    
}
