package bd.edu.seu.cookify.models;

import java.util.List;

/**
 * Data class for recipe mappings used in pantry management
 */
public class RecipeMapping {
    private final String id;
    private final String name;
    private final String category;
    private final String imageUrl;
    private final List<String> requiredIngredients;
    
    public RecipeMapping(String id, String name, String category, String imageUrl, List<String> requiredIngredients) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.requiredIngredients = requiredIngredients;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public List<String> getRequiredIngredients() {
        return requiredIngredients;
    }
}
