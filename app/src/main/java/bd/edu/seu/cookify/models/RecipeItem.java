package bd.edu.seu.cookify.models;

public class RecipeItem {
    private String id;
    private String name;
    private String imageUrl;
    private String culture;

    public RecipeItem() {}

    public RecipeItem(String id, String name, String imageUrl, String culture) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.culture = culture;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }

    public String getCulture() { return culture; }

}


