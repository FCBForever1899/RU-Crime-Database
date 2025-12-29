package crime;
import edu.rutgers.cs112.LL.LLNode;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
/**
 * Students will be analyzing cybercrime incident data using hash tables (with separate chaining) 
 * and linked lists to organize and query crime records efficiently. The theme revolves around 
 * parsing, storing, and analyzing real-world crime logs from Rutgers University’s public 
 * safety database: Daily Crime & Fire Safety Log.
 * 
 * @author Anna Lu
 * @author Krish Lenka
 */
public class RUCrimeDatabase {
    private LLNode<Incident>[] incidentTable; // Array of LLNodes
    private int totalIncidents;
    private static final double LOAD_FACTOR_THRESHOLD = 4.0;

    /**
     * Default constructor initializes the hash table with a size of 10.
     * The total number of incidents is set to zero.
     */
    public RUCrimeDatabase() {
        incidentTable = new LLNode[10];
        totalIncidents = 0;
    }

    /**
     * Adds a new incident to the hash table
     * @param incident An incident object which we will use to add to the hash table:
     */
    public void addIncident(Incident incident) {
        // WRITE YOUR CODE HERE
        String incidentNumber = incident.getIncidentNumber();
        int incidentIndex = hashFunction(incidentNumber);
        LLNode <Incident> incidentNode = new LLNode <Incident>(incident);
        incidentNode.setNext(incidentTable[incidentIndex]);
        incidentTable[incidentIndex] = incidentNode;
        totalIncidents++;
        if ( totalIncidents / incidentTable.length >= LOAD_FACTOR_THRESHOLD){
            rehash();
        }
    }

    /**
     * Reads the csv file, creates an Incident object for each line, and calls addIncident().
     * @param filename Path to file containing incident data
     */
    public void buildIncidentTable(String inputfile) {
        // WRITE YOUR CODE HERE
        StdIn.setFile(inputfile);

        String incidentLine = StdIn.readLine();

        while (incidentLine != null){
            String index [] = incidentLine.split(",");
            
            String incidentNumber = index[0];
            String nature = index[1];
            String reportDate = index[2];
            String occuranceDate = index[3];
            String location = index[4];
            String disposition = index[5];
            String generalLocation = index[6];
            Category category = Category.fromString(nature);

            Incident incident = new Incident(incidentNumber, nature, reportDate, occuranceDate, location, disposition, generalLocation, category);
            addIncident(incident);

            incidentLine = StdIn.readLine();
        }
        
    }
 

    /**
     * Rehashes the incident groups in the hash table.
     * This is called when the load factor exceeds a certain threshold.
     */
    public void rehash() {
        // WRITE YOUR CODE HERE
        LLNode<Incident> [] firstHashTable = incidentTable;
        incidentTable = new LLNode[firstHashTable.length * 2];
        totalIncidents = 0;
        for (int i = 0; i < firstHashTable.length; i++){
            LLNode<Incident> currentIncidentNode = firstHashTable[i];
            while (currentIncidentNode != null){
                Incident incident = currentIncidentNode.getData();
                addIncident(incident);
                currentIncidentNode = currentIncidentNode.getNext();
            }
        }
    } 

    /**
     * Deletes an incident based on its incident number.
     * @param incidentNumber The incident number of the incident to delete 
     */
    public void deleteIncident(String incidentNumber) {
        // WRITE YOUR CODE HERE
        int tableIndex = hashFunction(incidentNumber);
        LLNode <Incident> headIncidentNode = incidentTable[tableIndex];
        LLNode <Incident> previousIncidentNode = null;
        LLNode <Incident> nextIncidentNode = headIncidentNode.getNext();
        while (headIncidentNode != null){
            if(incidentNumber.equals(headIncidentNode.getData().getIncidentNumber())){
                if (previousIncidentNode == null){
                    incidentTable[tableIndex] = nextIncidentNode;
                } else {
                    previousIncidentNode.setNext(headIncidentNode.getNext());
                }
                totalIncidents--;
                return;
            }
            previousIncidentNode = headIncidentNode;
            headIncidentNode = headIncidentNode.getNext();   

            
        }

        
    }

    /**
     * Iterates over another RUCrimeDatabase's incident table, and adds its incidents
     * to this table IF they do not already exist.
     * @param other RUCrimeDatabase to copy new incidents from
     */
    public void join(RUCrimeDatabase other) {
        // WRITE YOUR CODE HERE
        LLNode<Incident> [] newTable = other.getIncidentTable();
        for (int i = 0; i < newTable.length; i++){
            LLNode<Incident> currentOtherTableNode = newTable[i];
            while(currentOtherTableNode != null){
                Incident incident = currentOtherTableNode.getData();

                int index = hashFunction(incident.getIncidentNumber());
                LLNode<Incident> currentIncidentNode = incidentTable[index];
                boolean incidentExists = false;
                

                while (currentIncidentNode != null){
                    if (currentIncidentNode.getData().getIncidentNumber().equals(incident.getIncidentNumber())){
                        incidentExists = true;
                        break;
                    }
                    currentIncidentNode = currentIncidentNode.getNext();
                }
                if(!incidentExists){
                addIncident(incident);
            }

            currentOtherTableNode = currentOtherTableNode.getNext();
            }

            
        }
    } 
    
    /**
     * Returns a list of the top K locations with the most incidents 
     * If K > numLocations, return all locations
     * @return ArrayList<String> containing the top K locations
     */
    public ArrayList<String> topKLocations(int K) {
        // WRITE YOUR CODE HERE
        String [] locations = {"ACADEMIC", "CAMPUS SERVICES", "OTHER", "PARKING LOT", "RECREATION", "RESIDENTIAL", "STREET/ROADWAY"};
        int [] counts = new int [locations.length];

        for (int i = 0; i < incidentTable.length; i++){
            LLNode<Incident> currentIncidentNode = incidentTable[i];
            while (currentIncidentNode != null){
                Incident incident = currentIncidentNode.getData();
                for (int e = 0; e < locations.length; e++){
                    if (incident.getGeneralLocation().equals(locations[e])){
                        counts[e] ++;

                        break;
                    }
                }
                currentIncidentNode = currentIncidentNode.getNext();
            }
            
        } 
        ArrayList<String> resultOfLocations = new ArrayList<>();
        int displayLocations = Math.min (K, locations.length);

        for (int j = 0; j < displayLocations; j++){
            int highest = 0;

            for (int l = 0; l < counts.length; l++){
                if (counts[l] > counts[highest]){
                    highest = l;
                }
            }

            resultOfLocations.add(locations[highest]);

            counts[highest] = -1;
        }
        
        return resultOfLocations; // Replace this line, it is provided so the code compiles
    }  

    /**
     * Returns the percentage of incidents for every category.
     * Categories: Property, Violent,
     *             Mischief, Trespass, or Other
     * @return A HashMap<Category, Double> with percentage of incidents of each category
     */
    public HashMap<Category, Double> natureBreakdown() { 
        // WRITE YOUR CODE HERE
        HashMap<Category, Double> naturalBreakdownHashMap = new HashMap<>();
        naturalBreakdownHashMap.put(Category.VIOLENT, 0.0);
        naturalBreakdownHashMap.put(Category.MISCHIEF, 0.0);
        naturalBreakdownHashMap.put(Category.PROPERTY, 0.0);
        naturalBreakdownHashMap.put(Category.TRESPASS, 0.0);
        naturalBreakdownHashMap.put(Category.OTHER, 0.0);

        totalIncidents = 0;

        for (int i = 0; i < incidentTable.length; i++) {
            LLNode<Incident> currentIncidentNode = incidentTable[i];
            while (currentIncidentNode != null) {
                Incident incident = currentIncidentNode.getData();
                String nature = incident.getNature().toLowerCase();
                Category category = Category.fromString(nature);
                naturalBreakdownHashMap.put(category, naturalBreakdownHashMap.get(category) + 1.0);
                totalIncidents++;
                currentIncidentNode = currentIncidentNode.getNext();
            }
        }

        for (Category category : naturalBreakdownHashMap.keySet()) {
            double percentage = 100.0 * (naturalBreakdownHashMap.get(category) / totalIncidents);
            naturalBreakdownHashMap.put(category, percentage);
        }


        return naturalBreakdownHashMap; // Replace this line, it is provided so the code compiles
    }

    //Given methods
    /**
     * DO NOT MODIFY THIS METHOD.
     * Returns the hash table array for inspection/testing
     * @return The array of LLNode<IncidentGroup> representing the hash table
     */
    public LLNode<Incident>[] getIncidentTable() {
        return incidentTable;
    }

    public void setIncidentTable(LLNode<Incident>[] incidentTable) {
        this.incidentTable = incidentTable;
    }

    public int numberOfIncidents() {
        return totalIncidents;
    }

    /**
     * DO NOT MODIFY THIS METHOD.
     * Returns the index in the hash table for a given incident number.
     * @return The index in the hash table for the incident number
     * @param incidentNumber The incident number to hash
     */
    private int hashFunction(String incidentNumber) {
        String last5Digits = incidentNumber.substring(Math.max(0, incidentNumber.length() - 5));
        int val = Integer.parseInt(last5Digits) % incidentTable.length;
        //System.out.println("Hashing incident number: " + last5Digits + " val: " + val);
        return val;
    }

}