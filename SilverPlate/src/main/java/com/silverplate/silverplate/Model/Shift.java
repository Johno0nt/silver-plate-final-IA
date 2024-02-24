package com.silverplate.silverplate.Model;

import java.sql.Date;
import java.sql.Time;

public class Shift {
    private int shiftId;
    private Time startTime;
    private Time endTime;
    private Date shiftDate;
    private Staff employee;

    public Shift(int shiftId, Time startTime, Time endTime, Date shiftDate, Staff employee) {
        this.shiftId = shiftId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftDate = shiftDate;
        this.employee = employee;
    }
}
