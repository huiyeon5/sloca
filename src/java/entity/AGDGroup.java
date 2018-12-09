/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Keith
 */
public class AGDGroup {
    private ArrayList<String> members;
    //private HashMap<String, Long> locationIdMap;
    private ArrayList<GroupLocationReport> groupLocationReports;
    
    /**
     * Construct an AGDGroup with no members and group location reports.  
     */
    public AGDGroup(){
        members = new ArrayList<>();
        
        groupLocationReports = new ArrayList<GroupLocationReport>();
    }
    
    /**
     * Construct an AGDGroup with a list of member (mac address) and a list of group location report.
     * @param peopleList list of mac address of the group
     * @param groupLocationReport list of group location reports of the group
     */
    public AGDGroup(ArrayList<String> peopleList, ArrayList<GroupLocationReport> groupLocationReport){
        this.members = peopleList;
        this.groupLocationReports = groupLocationReport;
    }
    
    /**
     * Retrieves the list of member (mac address) in the AGDGroup 
     * @return ArrayList of mac address of the group 
     */
    public ArrayList<String> getMembers(){
        return members;
    }
    
    /**
     * Retrieves the list of group location reports in the AGDGroup.
     * @return ArrayList of group location reports. 
     */
    public ArrayList<GroupLocationReport> getGroupLocationReports(){
        return groupLocationReports;
    }
    
    /**
     * Sets grouplocationReports for AGDGroup object
     * @param list - setter list
     */
    public void setGroupLocationReports(ArrayList<GroupLocationReport> list){
        this.groupLocationReports = list;
    }
    
    
    //check for the same locID and create new grouplocation report after comparison 
    //if total time spent is >= 12, return true

    /**
     * Check if AGDGroup could be combined with another AGDGroup, this method compares both groups' location reports
     * and it will return true if both groups spent 12minutes or more together. 
     * @param other - other group to check
     * @return boolean - returns true if both groups spent 12minutes or more together. 
     */
    public boolean checkForCombination(AGDGroup other){ //AC 
        
        ArrayList<GroupLocationReport> otherGrpLocReport = other.groupLocationReports; //AC GroupReport
        
        long totalTime = 0; // to check if it's more than 720 secs (12mins)
        
        for(GroupLocationReport report : groupLocationReports){ //main 
            for(GroupLocationReport otherReport : otherGrpLocReport){ //other
                if(report.compareTo(otherReport)){ // new method will check for if otherReport overlaps or the same as report
                    GroupLocationReport newReport = report.generateGroupLocationReport(otherReport);
                    totalTime += newReport.getDuration();
                }
            }
        }
        
        if(totalTime >= 720){
            return true;
        }
        return false;
    }
    
    /**
     * Combine both AGDGroups into a new AGDGroup. 
     * @param other -  other group to combine with
     * @return AGDGroup - result of the combination  
     */
    public AGDGroup combine(AGDGroup other){ //ABC vs AC 
        ArrayList<String> otherMembers = other.members; //AC 
        ArrayList<String> newMembers = new ArrayList<String>();
        newMembers.addAll(members);
        
        
        for(String otherMember : otherMembers){
            if(!members.contains(otherMember)){   
                newMembers.add(otherMember);
            }
        }
        
        ArrayList<GroupLocationReport> otherGrpLocReport = other.getGroupLocationReports();
        
        
        ArrayList<GroupLocationReport> newGroupLocationReport = new ArrayList<GroupLocationReport>(); //set new grouplocationreport

        
        for(GroupLocationReport report : groupLocationReports){
            for(GroupLocationReport otherReport : otherGrpLocReport){
                if(report.compareTo(otherReport)){ // new method will check for if otherReport overlaps or the same as report
                    GroupLocationReport newReport = report.generateGroupLocationReport(otherReport); //NEW SR2.1 BASED ON TWO GROUPS 
                    newGroupLocationReport.add(newReport);
                }
            }
        }
        
        return new AGDGroup(newMembers,newGroupLocationReport);
    }
    
    //Check if both groups are the same OR subset of a larger group
    //EG. AB vs BA (SAME)
    //EG. AC vs ABC (Subset)
    //EG. AC vs AB (False)

    /**
     * Check if both AGDGroups are the same or subset of one an other. 
     * @param other - other group for equals
     * @return boolean - returns true if it is the same group or it is a subset. 
     */
    public boolean equals(AGDGroup other){ //AC 
        boolean result = false; 
                
        ArrayList<String> otherMembers = other.getMembers();
        ArrayList<GroupLocationReport> otherGrpLocReport = other.getGroupLocationReports();
        
        //check if the members are the same 
        int noOfSameMembers = 0; 
        for(String otherMember : otherMembers){ // AB (member) vs AC (otherMembers)
            for(String member : members){
                if(member.equals(otherMember)){
                   noOfSameMembers++; //1  
                }
            }   
        }
       
        //Ensure that all members have been exhausted and prevent B being added into AC in (member (AC) vs otherMembers(ABC))
        if(noOfSameMembers == otherMembers.size() || noOfSameMembers == members.size()){
            result = true;
        }
        return result;
    }
    
    /**
     * Retrieves the total duration spent as a group 
     * @return long - duration in seconds 
     */
    public long getDuration(){
        long result = 0;
        for(GroupLocationReport report : groupLocationReports){
            result += report.getDuration();
        }
        
        return result;
    }
}
