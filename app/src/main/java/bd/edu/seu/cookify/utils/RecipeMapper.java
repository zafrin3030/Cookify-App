package bd.edu.seu.cookify.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bd.edu.seu.cookify.models.Recipe;
import bd.edu.seu.cookify.models.RecipeMapping;

/**
 * Utility class for mapping pantry items to suggested recipes
 */
public class RecipeMapper {
    
    /**
     * Get mapped recipes based on pantry items
     * @param pantryItems List of pantry items
     * @return List of matching recipes
     */
    public static List<Recipe> getMappedRecipes(List<String> pantryItems) {
        List<Recipe> matches = new ArrayList<>();
        
        if (pantryItems == null || pantryItems.isEmpty()) {
            return matches;
        }
        
        // Normalize pantry items for matching
        Set<String> normalizedPantrySet = new HashSet<>();
        for (String item : pantryItems) {
            normalizedPantrySet.add(normalizeString(item));
        }
        
        // Check each recipe mapping
        for (RecipeMapping mapping : getRecipeMappings()) {
            if (isPantryMatch(normalizedPantrySet, mapping.getRequiredIngredients())) {
                Recipe recipe = new Recipe(mapping.getName(), mapping.getCategory(), mapping.getImageUrl());
                recipe.setId(mapping.getId());
                matches.add(recipe);
            }
        }
        
        return matches;
    }
    
    /**
     * Check if pantry contains all required ingredients for a recipe
     * @param pantrySet Normalized set of pantry items
     * @param requiredIngredients List of required ingredients
     * @return true if all required ingredients are found in pantry
     */
    private static boolean isPantryMatch(Set<String> pantrySet, List<String> requiredIngredients) {
        for (String required : requiredIngredients) {
            boolean found = false;
            for (String pantryItem : pantrySet) {
                if (pantryItem.contains(required) || required.contains(pantryItem)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Define all recipe mappings
     * @return List of recipe mappings
     */
    private static List<RecipeMapping> getRecipeMappings() {
        List<RecipeMapping> mappings = new ArrayList<>();
        
        // Recipe 1: Grilled Chicken with Salad
        mappings.add(new RecipeMapping(
            "recipe_1",
            "Grilled Chicken with Salad",
            "Lunch",
            "https://i.imgur.com/s5rzogv.jpeg",
            Arrays.asList("garlic", "lemon juice")
        ));
        
        // Recipe 2: Haleem
        mappings.add(new RecipeMapping(
            "recipe_2", 
            "Haleem",
            "Dinner",
            "https://i.imgur.com/wEBHkJT.jpeg",
            Arrays.asList("cracked wheat", "beef")
        ));
        
        return mappings;
    }
    
    /**
     * Normalize string for better matching (lowercase, trim, remove extra spaces)
     * @param input Input string to normalize
     * @return Normalized string
     */
    private static String normalizeString(String input) {
        if (input == null) return "";
        return input.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}
