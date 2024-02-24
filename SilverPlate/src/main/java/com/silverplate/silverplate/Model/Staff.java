package com.silverplate.silverplate.Model;

public class Staff {
    private int staffId;
    private String staffName, staffEmail, staffPhone, staffAddress;
    public Staff(String staffName, String staffAddress, String staffEmail, String staffPhone,  int staffId) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.staffEmail = staffEmail;
        this.staffPhone = staffPhone;
        this.staffAddress = staffAddress;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }

    public String getStaffPhone() {
        return staffPhone;
    }

    public void setStaffPhone(String staffPhone) {
        this.staffPhone = staffPhone;
    }

    public String getStaffAddress() {
        return staffAddress;
    }

    public void setStaffAddress(String staffAddress) {
        this.staffAddress = staffAddress;
    }




}
