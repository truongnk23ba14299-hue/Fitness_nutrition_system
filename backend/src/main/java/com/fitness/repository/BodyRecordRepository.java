package com.fitness.repository;

import com.fitness.model.BodyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyRecordRepository extends JpaRepository<BodyRecord, Long> {

    // 1. Lịch sử chỉ số InBody của 1 học viên (mới nhất xếp lên đầu)
    List<BodyRecord> findByUser_IdOrderByRecordDateDescIdDesc(Long userId);

    // 2. Tìm bản ghi theo ID và UserID (bảo mật: không cho xem/xóa trộm bản ghi của người khác)
    Optional<BodyRecord> findByIdAndUser_Id(Long id, Long userId);

    // 3. Bản ghi mới nhất của 1 học viên
    Optional<BodyRecord> findFirstByUser_IdOrderByRecordDateDescIdDesc(Long userId);

    // 4. Lấy toàn bộ bản ghi của nhiều học viên trong 1 lần truy vấn (tránh N+1)
    List<BodyRecord> findByUser_IdInOrderByRecordDateDescIdDesc(Collection<Long> userIds);
}
