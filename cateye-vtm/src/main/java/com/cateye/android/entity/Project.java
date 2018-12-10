package com.cateye.android.entity;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/9/11.
 */

public class Project implements Serializable {

    /**
     * createTime : 2018-08-23 16:52:48.436
     * id : 1
     * modifyTime : 2018-08-23 16:52:48.436
     * name : yunnan
     */

    private String createTime;
    private int id;
    private String modifyTime;
    private String name;
    private String memo;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
