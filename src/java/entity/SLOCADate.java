package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Jia Xian
 */
public class SLOCADate {

    private LocalDateTime dateTime;

    /**
     * Constructs SLOCADate using String date and String time, representation in the format of "yyyy-MM-dd HH:mm:ss"
     * @param date Provide formatted String date portion of the SLOCADate - yyyy-MM-dd  "2016-07-28"
     * @param time Provide formatted String time portion of the SLOCADate - HH:mm:ss "15:59:59"
     * @throws DateTimeParseException - if the string date or time is in the wrong format
     */
    public SLOCADate(String date, String time) throws DateTimeParseException{ // 12/12/2017
        String result = "";
        for (int i = 0; i < date.length(); i++) {
            if (date.charAt(i) == '/') {
                result += "-";
            } else {
                result += date.charAt(i);
            }
        }
        result += " " + time;
        dateTime = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Construct SLOCADate object with String formatted datetime
     * @param datetime Provide formatted String combined datetime portion of the SLOCADate - yyyy-MM-dd HH:mm:ss "2016-07-28 15:59:59"
     * @throws DateTimeParseException - if the string date or time is in the wrong format
     */
    public SLOCADate(String datetime) throws DateTimeParseException{
        LocalDateTime parsedTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dateTime = parsedTime;
    }
    
    /**
     * Constructs SLOCADate using LocalDateTime
     * @param dateTime Provide Local datetime portion of the SLOCADate - yyyy-MM-dd HH:mm:ss "2016-07-28 15:59:59"
     */
    public SLOCADate(LocalDateTime dateTime){
        this.dateTime = dateTime;
    }
    
    /**
     * Get DateTime of SLOCADate
     * @return LocalDateTime
     */
    public LocalDateTime getDateTime(){
        return dateTime;
    }
    
    /**
     * Retrieves new SLOCADate minus minutes before the current SLOCADate 
     * @param minutesBefore Number of minutes that is before current SLOCADate
     * @return SLOCADate
     */
    public SLOCADate retrieveMinutesBefore(long minutesBefore){
        LocalDateTime result = dateTime.minusMinutes(minutesBefore);
        return new SLOCADate(result);
    }
    
    /**
     * Retrieves new SLOCADate plus minutes after the current SLOCADate 
     * @param minutesAfter Number of minutes that is after current SLOCADate
     * @return SLOCADate
     */
    public SLOCADate retrieveMinutesAfter(long minutesAfter){
        LocalDateTime result = dateTime.plusMinutes(minutesAfter);
        return new SLOCADate(result);
    }
    
    /**
     * Check if the two SLOCADate dates are equal 
     * @param dateTime SLOCADate object that is used for comparison
     * @return boolean
     */
    public boolean equalsTo(SLOCADate dateTime){   
        return this.dateTime.equals(dateTime.getDateTime());
    }
    
    /**
     * Retrieves the difference in duration between two SLOCADates 
     * @param before the earlier SLOCADate object
     * @param after  the later SLOCADate object
     * @return long
     */
    public static long getDuration(SLOCADate before, SLOCADate after){
        LocalDateTime start = before.getDateTime();
        LocalDateTime end = after.getDateTime();
        long seconds = start.until(end, ChronoUnit.SECONDS);
        return seconds;
    }

    /**
     * Returns string representation of SLOCADate object in "yyyy-MM-dd HH:mm:ss" format
     * @return String
     */    
    public String toString(){
        String date = dateTime.toString();
        String result = "";
        for(int i=0;i<date.length();i++){
            if(date.charAt(i) == 'T'){
                result += " ";
            }else{
                result += date.charAt(i);
            }
        }
        
        return result;
    }
    
    /**
     * Check if input date is equal SLOCADate object
     * @param date SLOCADate used to check if is equal
     * @return boolean
     */
    public boolean isEqual(SLOCADate date){
        return this.dateTime.isEqual(date.dateTime);
    }
    
    /**
     * Check if input date is before SLOCADate object
     * @param date check if SLOCADate date if after current SLOCADate object
     * @return boolean
     */
    public boolean isAfter(SLOCADate date){
        return this.dateTime.isAfter(date.dateTime);
    }
    
    /**
     * Check if input date is after SLOCADate object
     * @param date check if SLOCADate date if before current SLOCADate object
     * @return boolean
     */
    public boolean isBefore(SLOCADate date){
        return this.dateTime.isBefore(date.dateTime);
    }
    
    /**
     * Reduce SLOCADate object by specified number of seconds
     * @param seconds minus seconds from current SLOCA object
     * @return SLOCADate result
     */
    public SLOCADate minusSeconds(long seconds){
        SLOCADate result = new SLOCADate(dateTime.minusSeconds(seconds));
        return result;
    }
}
