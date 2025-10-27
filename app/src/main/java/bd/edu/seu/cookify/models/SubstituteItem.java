package bd.edu.seu.cookify.models;

public class SubstituteItem {

    private String name;
    private String amount;
    private String imageUrl;
    private java.util.List<String> substitutes;


    public SubstituteItem() {}

    public SubstituteItem(String name, String amount, String imageUrl, java.util.List<String> substitutes) {
        this.name = name;
        this.amount = amount;
        this.imageUrl = imageUrl;
        this.substitutes = substitutes == null ? new java.util.ArrayList<>() : substitutes;

    }

    public String getName() { return name; }
    public String getAmount() { return amount; }
    public String getImageUrl() { return imageUrl; }
    public java.util.List<String> getSubstitutes() { return substitutes; }

}


