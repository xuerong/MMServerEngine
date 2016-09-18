package com.mm.engine.framework.data.entity.account;

import com.mm.engine.framework.data.entity.AbAccount;
import com.mm.engine.framework.data.persistence.orm.annotation.DBEntity;

/**
 * Created by a on 2016/9/18.
 */
@DBEntity(tableName = "account",pks = {"id"})
public class Account extends AbAccount {


    @Override
    public void destroySession() {
        // 下线通知
    }

    public void logout(LogoutReason reason){
        switch (reason){
            case userLogout:
                break;
            case replaceLogout: // 通知前端
                break;
            case netErrorLogout: // 通知前端
                break;
        }
    }
}
