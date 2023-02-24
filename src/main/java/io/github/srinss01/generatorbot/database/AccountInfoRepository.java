package io.github.srinss01.generatorbot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Integer> {
    @Transactional
    @Modifying
    @Query("delete from AccountInfo a where a.id = ?1 and a.accountId = ?2")
    int deleteByIdAndAccountId(int id, long accountId);
    List<AccountInfo> findByAccountId(long accountId);

    @Transactional
    @Modifying
    @Query("delete from AccountInfo a where a.accountId = ?1")
    int deleteByAccountId(long accountId);
    @Query(value = "select * from account_info_db a where a.account_id = ? limit 1", nativeQuery = true)
    Optional<AccountInfo> findFirst(long accountId);
}