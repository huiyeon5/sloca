/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Keith
 */
public class LocationReport {

    private String macAddress;
    private String locationId;
    private SLOCADate startTime;
    private SLOCADate endTime;

    /**
     * Constructs LocationReport using String mac address, String locationId, SLOCADate startTime and SLOCADate endTime"
     * @param macAddress mac address of User
     * @param locationId location Id of User
     * @param startTime Start Time when the User at specified location Id
     * @param endTime End Time when the User at specified location Id
     */
    public LocationReport(String macAddress, String locationId, SLOCADate startTime, SLOCADate endTime) {
        this.macAddress = macAddress;
        this.locationId = locationId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Retrieves the macAddress from LocationReport object
     * @return String mac address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Retrieves the locationId from LocationReport object
     * @return String locationId
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Retrieves the startTime from LocationReport object
     * @return SLOCADate startTime
     */
    public SLOCADate getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the endTime from LocationReport object
     * @return SLOCADate endTime
     */
    public SLOCADate getEndTime() {
        return endTime;
    }

    /**
     * Retrieves the duration from LocationReport object
     * @return long duration
     */
    public long getDuration() {
        return SLOCADate.getDuration(startTime, endTime);
    }

    /**
     * Compare LocationReport using semantic place 
     * @param report Other report to be compared to the current Location report
     * @return boolean
     */
    public boolean compareSemPlace(LocationReport report) {
        return this.locationId.equals(report.locationId);
    }

    /**
     * Compare if two LocationReports have the same Location ID, Start Time and End Time
     * @param report Other report to be compared to the current Location report
     * @return boolean
     */
    public boolean compareTo(LocationReport report) {
        if (!compareSemPlace(report)) {
            return false;
        }
        SLOCADate laterStartTime = null;
        SLOCADate earlierEndTime = null;

        if (startTime.isEqual(report.startTime)) {
            laterStartTime = startTime;
        }

        if (startTime.isAfter(report.startTime)) {
            laterStartTime = startTime;
        } else {
            laterStartTime = report.startTime;
        }

        if (endTime.isAfter(report.endTime)) {
            earlierEndTime = report.endTime;
        } else {
            earlierEndTime = endTime;
        }

        if (laterStartTime.isAfter(earlierEndTime)) {
            return false;
        }

        return true;
    }

    /**
     * Retrieves the amount of time two Location Reports share with each other
     * @param report Other report to be compared to the current Location report
     * @return long 
     */
    public long getTimeTogether(LocationReport report) {
        SLOCADate laterStartTime = null;
        SLOCADate earlierEndTime = null;

        if (startTime.isAfter(report.startTime)) {
            laterStartTime = startTime;
        } else {
            laterStartTime = report.startTime;
        }

        if (endTime.isAfter(report.endTime)) {
            earlierEndTime = report.endTime;
        } else {
            earlierEndTime = endTime;
        }

        return SLOCADate.getDuration(laterStartTime, earlierEndTime);
    }

    /**
     * Create GroupLocationReport
     * @param report Location report used to generate group location report
     * @return GroupLocationReport
     */
    public GroupLocationReport generateGroupLocationReport(LocationReport report) {
        SLOCADate laterStartTime = null;
        SLOCADate earlierEndTime = null;

        if (startTime.isEqual(report.startTime)) {
            laterStartTime = startTime;
        }

        if (startTime.isAfter(report.startTime)) {
            laterStartTime = startTime;
        } else {
            laterStartTime = report.startTime;
        }

        if (endTime.isAfter(report.endTime)) {
            earlierEndTime = report.endTime;
        } else {
            earlierEndTime = endTime;
        }
        
        GroupLocationReport result = new GroupLocationReport(locationId, laterStartTime, earlierEndTime);
        
        return result;
    }

}