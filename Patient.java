import java.io.Serializable;

public class Patient implements Serializable{
    private static final long serialVersionUID = 1L;

    private String name;
    private String details;
    private int priority;

    public Patient(String name, String details, int priority) {
        this.name = name;
        this.details = details;
        this.priority = priority;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
