

package entity;
/**
 *
 * @author Jia Xian
 */
public class User{
    
    private String name;
    private String macAddress;
    private String password;
    private String email;
    private char gender;
    
    /**
     * Constructs a User object using the parameters
     *
     * @param macAddress        The mac-address of the device
     * @param name              The full name of the student
     * @param password          The password of the student
     * @param email             The email address of the student (2010 onwards, only for undergraduates)
     * @param gender            The gender of the student (M/F)
     * 
     */
    
    public User(String macAddress, String name, String password, String email, char gender){
        this.name = name;
        this.macAddress = macAddress;
        this.password = password;
        this.email = email;
        this.gender = gender;
    }
    
    /*
     * All the get methods for the attributes
     *
     * get methods also include the get of the year of the user and the school of the user.
     *
     */

    /**
     * Retrieves the name of an instance of a User object
     * @return String name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Retrieves the macAddress of an instance of a User object
     * @return String macAddress
     */
    public String getMacAddress(){
        return macAddress;
    }
    
    /**
     * Retrieves the password of an instance of a User object
     * @return String password
     */
    public String getPassword(){
        return password;
    }
    
    /**
     * Retrieves the email of an instance of a User object
     * @return String email
     */
    public String getEmail(){
        return email;
    }
    
    /**
     * Retrieves the gender of an instance of a User object
     * @return String gender 
     */
    public char getGender(){
        return gender;
    }
    
    /**
     * Retrieves the school of an instance of a User object
     * @return String school from email
     */
    public String getSchool(){
        return email.substring(email.indexOf('@')+1,email.indexOf('.',email.indexOf('@')));
    }
    
    /**
     * Retrieves the year that the student enrolled into SMU
     * @return numerical year of the User
     */
    public int getYear(){
        int y = 0;
        Integer[] allYear = {2013, 2014, 2015, 2016};
        for (int year : allYear) {
            if (email.contains(""+year)) {
                y = year;
                break;
            }
        }
        return y;
    }
    
    //set methods

    /**
     * Sets the name of the user to the specified parameter
     * @param name The name of the User
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Sets the mac address of the user to the specified parameter
     * @param macAddress The mac address of the User
     */
    public void setMacAddress(String macAddress){
        this.macAddress = macAddress;
    }
    
    /**
     * Sets the password of the user to the specified parameter
     * @param password The password of the User
     */
    public void setPassword(String password){
        this.password = password;
    }
    
    /**
     * Sets the email of the user to the specified parameter
     * @param email The email of the User
     */
    public void setEmail(String email){
        this.email = email;
    }
    
    /**
     * Sets the gender of the user to the specified parameter
     * @param gender The gender of the User
     */
    public void setGender(char gender){
        this.gender = gender;
    }
}
