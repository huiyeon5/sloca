/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.Comparator;

/**
 * This method sort AGDgroups in descending order. It first compare them by group size, duration spent and mac address. 
 * 
 */
public class AGDGroupComparator implements Comparator<AGDGroup> {
    
    @Override
    public int compare(AGDGroup group1, AGDGroup group2) {
        
        int sizeDiff = group2.getMembers().size() - group1.getMembers().size(); 
        
        if(sizeDiff != 0){
            return sizeDiff;
        }
        
        int timeDiff = (int) (group2.getDuration() - group1.getDuration());
        
        if(timeDiff != 0){
            return timeDiff;
        }
        
        for(String member1 : group1.getMembers()){
            for(String member2 : group2.getMembers()){
                int memberDiff = member1.compareTo(member2);
                if(memberDiff != 0){
                    return -memberDiff;
                }
            }
        }
            
        return 0;
    }
    
}
