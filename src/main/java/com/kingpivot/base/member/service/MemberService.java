package com.kingpivot.base.member.service;

import com.kingpivot.base.member.model.Member;
import com.kingpivot.common.service.BaseService;

public interface MemberService extends BaseService<Member, String> {

    Member getMemberByLoginNameAndApplicationId(String loginName,String applicationID);

    String getCurRecommandCode(String applicationId);

    String getMemberIdByPhoneAndApplicationId(String phone,String applicationID);

    String getMemberApplicationID(String memberID);
}
