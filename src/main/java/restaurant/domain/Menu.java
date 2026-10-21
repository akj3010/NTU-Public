package restaurant.domain;

import java.util.ArrayList;
import java.util.List;

/** The restaurant's menu (Stage 1: the three single dishes). */
public class Menu {
    private final List<MenuItem> items = new ArrayList<>();

    public Menu(List<MenuItem> initialItems) {
        items.addAll(initialItems);
    }

    public List<MenuItem> all() {
        return List.copyOf(items);
    }

    public MenuItem find(String name) {
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No menu item named " + name));
    }

    public void add(MenuItem item) {
        items.add(item);
    }
}
