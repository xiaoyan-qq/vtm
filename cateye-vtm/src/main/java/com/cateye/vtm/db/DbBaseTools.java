package com.cateye.vtm.db;

import android.content.Context;

import org.xutils.DbManager;

/**
 * Created by xiaoxiao on 2018/12/18.
 */

public class DbBaseTools {
    protected DbManager dbManager;
    private DbBaseTools instance;

    public DbBaseTools getInstance(Context context, DbManager dbManager) {
        if (instance == null) {
            instance = new DbBaseTools(context, dbManager);
        }
        return instance;
    }

    public DbBaseTools(Context context, DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createAirPlanDBEntityTable() {
        return true;
    }
}
