import java.io.Serializable;

public class Patient implements Serializable{
    private static final long serialVersionUID = 2L;

    private String name;
    private String details;
    private int priority;
    private String patientID;

    public Patient(String name, String details, int priority, String patientID) {
        this.name = name;
        this.details = details;
        this.priority = priority;
        this.patientID = patientID;
    }

    public String getName() {
        return this.name;
    }

    public String getDetails() {
        return this.details;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getID() {
        return this.patientID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setID(String patientID) {
        this.patientID = patientID;
    }

}
