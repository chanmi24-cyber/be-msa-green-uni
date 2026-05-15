package com.green.academic.application.notification;

import com.green.academic.application.notification.model.NotiListReq;
import com.green.academic.application.notification.model.NotiListRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;



import java.util.List;

@Mapper
public interface NotificationMapper {
    List<NotiListRes> findNotifications(@Param("memberCode") Long memberCode,
                                        @Param("req") NotiListReq req);
}