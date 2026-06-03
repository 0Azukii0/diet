package com.azukii.diet.system;

import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Heuristics for determining food categories based on tags and name patterns.
 * Used as fallback when recipe analysis is not available.
 */
public class ModHeuristics {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModHeuristics.class);

    private static final List<TagKey<Item>> MEAT_TAGS = List.of(
            ItemTags.MEAT,
            ItemTags.FISHES,
            tag("c", "meat"),
            tag("c", "cooked_meat"),
            tag("c", "raw_meat"),
            tag("c", "foods/meat"),
            tag("forge", "raw_meat"),
            tag("forge", "cooked_meat"),
            tag("forge", "meat"),
            tag("minecraft", "meat")
    );

    private static final List<TagKey<Item>> VEGETABLE_TAGS = List.of(
            tag("c", "vegetables"),
            tag("c", "foods/vegetable"),
            tag("forge", "vegetables"),
            tag("forge", "vegetable"),
            tag("minecraft", "vegetables")
    );

    private static final List<TagKey<Item>> FRUIT_TAGS = List.of(
            tag("c", "fruits"),
            tag("c", "foods/fruit"),
            tag("forge", "fruits"),
            tag("forge", "fruit"),
            tag("minecraft", "fruits")
    );

    private static final List<TagKey<Item>> GRAIN_TAGS = List.of(
            tag("c", "grains"),
            tag("c", "bread"),
            tag("c", "foods/grain"),
            tag("c", "crops"),
            tag("forge", "bread"),
            tag("forge", "grain"),
            tag("forge", "grains"),
            tag("forge", "crops/wheat"),
            tag("forge", "crops/rice"),
            tag("minecraft", "grains")
    );

    private static final List<TagKey<Item>> DRINK_TAGS = List.of(
            tag("c", "drinks"),
            tag("c", "foods/drink"),
            tag("c", "soups"),
            tag("c", "stews"),
            tag("c", "beverages"),
            tag("forge", "drinks"),
            tag("forge", "soups"),
            tag("forge", "beverages"),
            tag("minecraft", "drinks")
    );

    private static final String[] MEAT_KEYWORDS = {
            "meat", "beef", "pork", "chicken", "mutton", "steak",
            "bacon", "sausage", "ham", "turkey", "duck", "fish", "salmon", "cod",
            "lamb", "veal", "venison", "rabbit", "goat", "quail", "pheasant",
            "anchovy", "tuna", "trout", "halibut", "sardine", "mackerel",
            "shrimp", "crab", "lobster", "scallop", "clam", "oyster", "mussel",
            "jerky", "pepperoni", "salami", "prosciutto", "chorizo", "carnitas",
            "ribs", "chop", "cutlet", "fillet", "drumstick", "wing", "thigh"
    };

    private static final String[] VEGETABLE_KEYWORDS = {
            "carrot", "potato", "tomato", "lettuce", "cabbage", "onion",
            "garlic", "pepper", "mushroom", "salad", "veg", "bean",
            "broccoli", "cauliflower", "celery", "cucumber", "zucchini", "squash",
            "pumpkin", "eggplant", "asparagus", "spinach", "kale", "chard",
            "radish", "turnip", "beet", "pea", "lentil", "chickpea",
            "okra", "artichoke", "leek", "shallot", "ginger", "horseradish",
            "brussels", "sprout", "endive", "arugula", "bok", "choy", "daikon"
    };

    private static final String[] FRUIT_KEYWORDS = {
            "berry", "berries", "apple", "melon", "orange", "banana", "grape",
            "peach", "pear", "fruit", "cherry", "citrus", "coco",
            "strawberry", "blueberry", "raspberry", "blackberry", "cranberry",
            "sweet_berries", "glow_berries", "chorus_fruit",
            "watermelon", "cantaloupe", "honeydew", "kiwi", "pineapple", "mango",
            "papaya", "apricot", "plum", "nectarine", "fig", "date", "pomegranate",
            "lemon", "lime", "grapefruit", "tangerine", "clementine", "coconut",
            "avocado", "olive", "persimmon", "lychee", "dragonfruit", "passion"
    };

    private static final String[] GRAIN_KEYWORDS = {
            "bread", "wheat", "grain", "flour", "pasta", "rice",
            "noodle", "dough", "cereal", "oat", "bun", "cake", "cookie",
            "barley", "rye", "corn", "millet", "quinoa", "amaranth", "spelt",
            "bagel", "muffin", "croissant", "biscuit", "roll", "tortilla", "pita",
            "macaroni", "spaghetti", "linguine", "fettuccine", "penne", "rotini",
            "pancake", "waffle", "crepe", "biscotti", "pretzel", "cracker", "crisp",
            "bran", "germ", "grits", "polenta", "couscous", "bulgur", "farro"
    };

    private static final Map<String, FinalHeuristics> MOD_HEURISTICS = new HashMap<>();

    static {
        // Farmer's Delight - balanced meals with good variety
        MOD_HEURISTICS.put("farmersdelight", new FinalHeuristics(1.2f, 1.3f, 1.4f, 1.3f, 1.1f, 1.0f));

        // Croptopia - lots of crops and fruits
        MOD_HEURISTICS.put("croptopia", new FinalHeuristics(1.1f, 1.1f, 1.5f, 1.4f, 1.2f, 1.0f));

        // Let's Do series - bakery, brewery, candlelight, vinery, etc.
        MOD_HEURISTICS.put("bakery", new FinalHeuristics(1.4f, 1.0f, 1.0f, 1.1f, 1.3f, 0.8f));
        MOD_HEURISTICS.put("brewery", new FinalHeuristics(1.0f, 1.0f, 1.0f, 1.2f, 1.2f, 1.4f));
        MOD_HEURISTICS.put("candlelight", new FinalHeuristics(1.3f, 1.3f, 1.2f, 1.1f, 1.2f, 1.0f));
        MOD_HEURISTICS.put("vinery", new FinalHeuristics(1.0f, 1.0f, 1.0f, 1.4f, 1.2f, 1.3f));
        MOD_HEURISTICS.put("meadow", new FinalHeuristics(1.1f, 1.1f, 1.2f, 1.3f, 1.2f, 1.0f));
        MOD_HEURISTICS.put("beachparty", new FinalHeuristics(1.0f, 1.2f, 1.1f, 1.3f, 1.2f, 1.1f));
        MOD_HEURISTICS.put("herbalbrews", new FinalHeuristics(1.0f, 1.0f, 1.3f, 1.2f, 1.1f, 1.4f));
        MOD_HEURISTICS.put("farm_and_charm", new FinalHeuristics(1.2f, 1.1f, 1.3f, 1.2f, 1.1f, 1.0f));

        // Cultural/Regional food mods
        MOD_HEURISTICS.put("culturaldelights", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("cuisinedelight", new FinalHeuristics(1.3f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("rusticdelight", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));

        // Delight addons
        MOD_HEURISTICS.put("cratedelight", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("croptopiadelight", new FinalHeuristics(1.2f, 1.1f, 1.4f, 1.3f, 1.2f, 1.0f));
        MOD_HEURISTICS.put("expandeddelight", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("extradelight", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("dumplings_delight", new FinalHeuristics(1.3f, 1.2f, 1.2f, 1.1f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("seeddelight", new FinalHeuristics(1.2f, 1.1f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("storagedelight", new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));

        // Other food mods
        MOD_HEURISTICS.put("butchery", new FinalHeuristics(1.0f, 1.5f, 1.0f, 1.0f, 1.0f, 0.9f));
        MOD_HEURISTICS.put("pamhc2foodcore", new FinalHeuristics(1.1f, 1.1f, 1.3f, 1.3f, 1.2f, 1.0f));
        MOD_HEURISTICS.put("cuisine", new FinalHeuristics(1.3f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("brew", new FinalHeuristics(1.0f, 1.0f, 1.0f, 1.0f, 1.2f, 1.3f));
        MOD_HEURISTICS.put("quark", new FinalHeuristics(1.1f, 1.0f, 1.0f, 1.0f, 1.1f, 1.0f));
        MOD_HEURISTICS.put("botania", new FinalHeuristics(1.0f, 1.0f, 1.2f, 1.3f, 1.1f, 1.1f));
    }

    public static boolean isMeatLike(Item item) {
        if (matchesAnyTag(item, MEAT_TAGS)) {
            return true;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String fullId = id.toString();
        return containsKeyword(path, MEAT_KEYWORDS) || containsKeyword(fullId, MEAT_KEYWORDS);
    }

    public static boolean hasVegetableHints(Item item) {
        if (matchesAnyTag(item, VEGETABLE_TAGS)) {
            return true;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String fullId = id.toString();
        return containsKeyword(path, VEGETABLE_KEYWORDS) || containsKeyword(fullId, VEGETABLE_KEYWORDS);
    }

    public static boolean hasFruitHints(Item item) {
        if (matchesAnyTag(item, FRUIT_TAGS)) {
            return true;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String fullId = id.toString();
        return containsKeyword(path, FRUIT_KEYWORDS) || containsKeyword(fullId, FRUIT_KEYWORDS);
    }

    public static boolean hasGrainHints(Item item) {
        if (matchesAnyTag(item, GRAIN_TAGS)) {
            return true;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String fullId = id.toString();
        return containsKeyword(path, GRAIN_KEYWORDS) || containsKeyword(fullId, GRAIN_KEYWORDS);
    }

    public static FoodProfile applyModHeuristics(Identifier itemId, FoodProfile profile) {
        String modId = itemId.getNamespace();

        if ("minecraft".equals(modId)) {
            return profile;
        }

        FinalHeuristics heuristics = MOD_HEURISTICS.get(modId);
        if (heuristics != null) {
            LOGGER.debug("[DIET] Applying {} mod heuristics to {}", modId, itemId);
            return applyHeuristics(profile, heuristics);
        }

        for (Map.Entry<String, FinalHeuristics> entry : MOD_HEURISTICS.entrySet()) {
            if (modId.contains(entry.getKey())) {
                LOGGER.debug("[DIET] Applying {} mod heuristics (partial match) to {}", entry.getKey(), itemId);
                return applyHeuristics(profile, entry.getValue());
            }
        }

        // Let's Do series pattern matching
        if (modId.contains("doapi") || modId.contains("wildernature")) {
            LOGGER.debug("[DIET] Applying Let's Do heuristics to {}", itemId);
            FinalHeuristics letsDoHeuristics = new FinalHeuristics(1.2f, 1.2f, 1.2f, 1.2f, 1.1f, 1.0f);
            return applyHeuristics(profile, letsDoHeuristics);
        }

        // Delight series pattern matching
        if (modId.contains("delight")) {
            LOGGER.debug("[DIET] Applying delight heuristics to {}", itemId);
            FinalHeuristics delightHeuristics = new FinalHeuristics(1.2f, 1.2f, 1.3f, 1.2f, 1.1f, 1.0f);
            return applyHeuristics(profile, delightHeuristics);
        }

        if (modId.contains("farm") || modId.contains("harvest") || modId.contains("grow") || modId.contains("agricraft")) {
            LOGGER.debug("[DIET] Applying farm heuristics to {}", itemId);
            FinalHeuristics farmHeuristics = new FinalHeuristics(1.2f, 1.1f, 1.3f, 1.2f, 1.1f, 1.0f);
            return applyHeuristics(profile, farmHeuristics);
        }

        if (modId.contains("food") || modId.contains("cook") || modId.contains("kitchen") ||
                modId.contains("culinary") || modId.contains("cuisine")) {
            LOGGER.debug("[DIET] Applying food heuristics to {}", itemId);
            FinalHeuristics foodHeuristics = new FinalHeuristics(1.1f, 1.1f, 1.2f, 1.2f, 1.1f, 1.0f);
            return applyHeuristics(profile, foodHeuristics);
        }

        return profile;
    }

    private static FoodProfile applyHeuristics(FoodProfile profile, FinalHeuristics heuristics) {
        return FoodProfile.of(
                profile.get(FoodCategories.GRAIN) * heuristics.grainMultiplier,
                profile.get(FoodCategories.PROTEIN) * heuristics.proteinMultiplier,
                profile.get(FoodCategories.VEGETABLE) * heuristics.vegetableMultiplier,
                profile.get(FoodCategories.FRUIT) * heuristics.fruitMultiplier,
                profile.get(FoodCategories.SUGAR) * heuristics.sugarMultiplier
        );
    }

    private static boolean matchesAnyTag(Item item, List<TagKey<Item>> tags) {
        var holder = item.builtInRegistryHolder();
        for (TagKey<Item> tag : tags) {
            if (holder.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsKeyword(String value, String[] keywords) {
        String lower = value.toLowerCase();
        for (String keyword : keywords) {
            if (containsAsWord(lower, keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAsWord(String text, String keyword) {
        int index = text.indexOf(keyword);
        if (index == -1) {
            return false;
        }

        while (index >= 0) {
            boolean validStart = index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1));
            boolean validEnd = (index + keyword.length() >= text.length()) ||
                    !Character.isLetterOrDigit(text.charAt(index + keyword.length()));

            if (index > 0 && text.charAt(index - 1) == '_') {
                validStart = true;
            }
            if (index + keyword.length() < text.length() && text.charAt(index + keyword.length()) == '_') {
                validEnd = true;
            }

            if (validStart && validEnd) {
                return true;
            }

            index = text.indexOf(keyword, index + 1);
        }

        return false;
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private record FinalHeuristics(
            float grainMultiplier,
            float proteinMultiplier,
            float vegetableMultiplier,
            float fruitMultiplier,
            float sugarMultiplier,
            float waterMultiplier
    ) {}
}