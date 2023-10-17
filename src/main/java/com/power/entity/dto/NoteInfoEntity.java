package com.power.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoteInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNum;
    private String note;
}
