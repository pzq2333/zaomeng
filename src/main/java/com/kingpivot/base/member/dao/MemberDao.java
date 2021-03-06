package com.kingpivot.base.member.dao;

import com.kingpivot.base.member.model.Member;
import com.kingpivot.common.dao.BaseDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "member")
@Qualifier("memberDao")
public interface MemberDao extends BaseDao<Member, String> {
    @Query(value = "SELECT * FROM member WHERE loginName=?1 and applicationID=?2 and isValid=1 and isLock=0 limit 1", nativeQuery = true)
    Member getMemberByLoginNameAndApplicationId(String loginName, String applicationID);

    @Query(value = "SELECT MAX(CAST(recommandCode AS SIGNED)) FROM member WHERE\n" +
            "                 applicationID=?1\n" +
            "                AND isValid=1 and isLock=0", nativeQuery = true)
    String getCurRecommandCode(String applicationId);

    @Query(value = "SELECT id FROM member WHERE phone=?1 and applicationID=?2 AND isValid=1 AND isLock=0 LIMIT 1", nativeQuery = true)
    String getMemberIdByPhoneAndApplicationId(String phone,String applicationId);

    @Query(value = "SELECT applicationID FROM member WHERE id=?1 AND isValid=1 AND isLock=0 LIMIT 1", nativeQuery = true)
    String getMemberApplicationID(String memberID);
}
