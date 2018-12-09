package entity;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Wei Ming
 */
public class Heatmap {
    /**
     * attributes
     */
    private String semanticPlace;
    private int numOfPeople;
    private int density;
    
    /**
     * constructor
     * @param semanticPlace the name of semantic place
     * @param numOfPeople the number of people in the place
     * @param density the crowd density in the place
     */
    public Heatmap(String semanticPlace, int numOfPeople, int density){
        this.semanticPlace = semanticPlace;
        this.numOfPeople = numOfPeople;
        this.density = density;
    }
    
    /**
     * get methods
     * Retrieves the semanticPlace of Heatmap object
     * @return String semanticPlace
     */
    public String getSemanticPlace(){
        return semanticPlace;
    }
    
    /**
     * Retrieves the numOfPeople in Heatmap object
     * @return numerical numOfPeople
     */
    public int getNumOfPeople(){
        return numOfPeople;
    }
    
    /**
      Retrieves the density of Heatmap object
     * @return numerical density
     */
    public int getDensity(){
        return density;
    }
}
