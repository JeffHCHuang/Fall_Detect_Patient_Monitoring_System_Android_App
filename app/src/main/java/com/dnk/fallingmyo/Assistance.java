package com.dnk.fallingmyo;

/**
 * Created by JeffreyHao-Chan on 2015-03-14.
 */
public enum Assistance {
    NONE(0), Food(1), Washroom(2), Medication(3), Linens(4);

    private int value;

    private Assistance(int value) {
        this.value = value;
    }
}

