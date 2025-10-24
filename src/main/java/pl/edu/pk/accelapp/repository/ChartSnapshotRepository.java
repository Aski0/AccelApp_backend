package pl.edu.pk.accelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.pk.accelapp.model.ChartSnapshot;

import java.util.List;
import java.util.Optional;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, Long> {
    List<ChartSnapshot> findByUploadedFileId(Long fileId);
    Optional<ChartSnapshot> findByIdAndUploadedFileId(Long id, Long fileId);
    @Query("select cs from ChartSnapshot cs " +
            "join fetch cs.uploadedFile uf " +
            "join fetch uf.user u " +
            "where cs.id in :ids and u.email = :email")
    List<ChartSnapshot> findAllByIdInAndUserEmail(@Param("ids") List<Long> ids,
                                                  @Param("email") String email);
}
