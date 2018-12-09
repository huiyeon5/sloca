/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author Keith
 */
public class GroupLocationReport {
    
    private String locationId; 
    private SLOCADate startTime;
    private SLOCADate endTime;

    //private HashMap<String, ArrayList<String>> locationDetails; //locId, (start, end)

    /**
     * Constructor
     * @param locationId - location id of the group
     * @param startTime - start time of the group
     * @param endTime  - end time of the group
     */
    public GroupLocationReport(String locationId, SLOCADate startTime, SLOCADate endTime) {
        this.locationId = locationId;
        this.startTime = startTime;
        this.endTime = endTime;
        
    }
    
    /**
     * Retrieves the locationId from GroupLocationReport 
     * @return String locationId
     */
    public String getLocationId(){
        return locationId;
    }
    
    /**
     * Retrieves the startTime from GroupLocationReport 
     * @return SLOCADate startTime
     */
    public SLOCADate getStartTime(){
        return startTime;
    }
    
    /**
     * Retrieves the endTime from GroupLocationReport 
     * @return SLOCADate endTime
     */
    public SLOCADate getEndTime(){
        return endTime;
    }
    
    /**
     * Sets location id of the GroupLocationReport
     * @param locationId - location id to be set. 
     */
    public void setLocationId(String locationId){
        this.locationId = locationId;
    }
    
    /**
     * Sets startTime of the GroupLocationReport 
     * @param startTime - time of entry to be set.
     */
    public void setStartTime(SLOCADate startTime){
        this.startTime = startTime;
    }
    
    /**
     * Sets endTime of the GroupLocationReport
     * @param endTime - time of exit to be set. 
     */
    public void setEndTime(SLOCADate endTime){
        this.endTime = endTime;
    }

    /**
     * Check if both GroupLocationReports are the same. 
     * @param report - to be checked against. 
     * @return boolean - returns true if both are the same. 
     */
    public boolean equals(GroupLocationReport report) {
        String otherLocId = report.getLocationId();
        SLOCADate otherStartTime = report.getStartTime();
        SLOCADate otherEndTime = report.getEndTime();
        
        if(otherLocId.equals(locationId) && otherStartTime.equalsTo(startTime) && otherEndTime.equalsTo(endTime)){
            return true;
        } 
        
        return false;
    }
    
    /**
     * Checks if both GroupLocationReports are referring to the same Semantic Place.  
     * @param report - to be checked against. 
     * @return boolean - returns true if they are the same. 
     */
    public boolean compareSemPlace(GroupLocationReport report) {
        return this.locationId.equals(report.locationId);
    }
    
    /**
     * Checks if both GroupLocationReports are between the same time frame of entry and exit. 
     * @param report - report to be compared to. 
     * @return boolean - returns true if they are within the same time frame. 
     */
    public boolean compareTo(GroupLocationReport report) {
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
     * Create a new GroupLocationReport based on two GroupLocationReports which has a later start time and an earlier end time. 
     * @param report - the report to be combined with. 
     * @return GroupLocationReport - returns a the new GroupLocationReport. 
     */
    public GroupLocationReport generateGroupLocationReport(GroupLocationReport report) { //
        SLOCADate laterStartTime = null;
        SLOCADate earlierEndTime = null;
        
        //report.startTime == 23:50
        //startTime == 23:50
        //report.endTime == 00:02
        //endTime == 00:04

        if (startTime.isEqual(report.startTime)) {
            laterStartTime = startTime;
        }

        if (startTime.isAfter(report.startTime)) {
            laterStartTime = startTime;
        } else {
            laterStartTime = report.startTime;
        }
        
        if (endTime.isEqual(report.endTime)) {
            earlierEndTime = endTime;
        }

        if (endTime.isAfter(report.endTime)) {
            earlierEndTime = report.endTime;
        } else {
            earlierEndTime = endTime;
        }
        
        GroupLocationReport result = new GroupLocationReport(locationId, laterStartTime, earlierEndTime);
        
        return result;
    }
    
    /**
     * Retrieves the total duration spent in the location id. 
     * @return long duration - duration in seconds. 
     */
    public long getDuration() {
        return SLOCADate.getDuration(startTime, endTime);
    }
}
