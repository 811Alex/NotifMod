package eu.gflash.notifmod.config.types;

import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import eu.gflash.notifmod.config.ProviderBase;
import joptsimple.internal.Strings;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Item list config entry type.
 * @author Alex811
 */
@JsonAdapter(ItemList.Adapter.class)
public class ItemList {
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(";");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private final String itemListStr;
    private final List<String> itemList;
    private String error = "";
    private String errorId = "";

    public ItemList(String... itemList){
        this(Strings.join(itemList, "; "));
    }

    public ItemList(String itemList){
        this.itemListStr = itemList;
        String noSpaceItemList = SPACE_PATTERN.matcher(itemList).replaceAll("");
        if(!noSpaceItemList.isEmpty()){
            String[] entrySplit = DELIMITER_PATTERN.split(noSpaceItemList);
            ArrayList<String> itemIds = Lists.newArrayList(entrySplit);
            if(itemIds.stream().noneMatch(id -> {    // validity check
                if(Identifier.isValid(id)){
                    if(Registry.ITEM.containsId(new Identifier(id)))
                        return false;   // success
                    else setError("doesNotExist", id);
                }else setError("invalidIdentifier", id);
                return true;    // fail
            })){
                this.itemList = itemIds;
                return;
            }
        }
        this.itemList = new ArrayList<>();  // on fail, set empty
    }

    public static ItemList getDefault(){
        return new ItemList("");
    }

    /**
     * Sets the error message and its parameter.
     * @param err the partial LangKey to use for the error
     * @param id the LangKey parameter
     */
    private void setError(String err, String id){
        error = err;
        errorId = id;
    }

    /**
     * Get error during construction.
     * @return {@link Optional} containing the error, or empty if none
     */
    public Optional<Text> getError() {
        return error.isEmpty() ? Optional.empty() : Optional.of(Text.translatable("error.config.notifmod.itemList." + error, errorId));
    }

    /**
     * Validates an item list string.
     * @param itemList string to validate
     * @return empty if valid, otherwise contains the error
     */
    public static Optional<Text> validate(String itemList){
        return new ItemList(itemList).getError();
    }

    public boolean contains(Item item){
        return itemList.contains(Registry.ITEM.getId(item).toString());
    }

    public boolean contains(ItemStack itemStack){
        return contains(itemStack.getItem());
    }

    @Override
    public String toString() {
        return itemListStr;
    }

    public static class Adapter extends TypeAdapter<ItemList> { // JSON adapter
        @Override
        public void write(JsonWriter out, ItemList value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public ItemList read(JsonReader in) throws IOException {
            return new ItemList(in.nextString());
        }
    }

    public static class Provider extends ProviderBase<String> { // GUI provider
        @Override
        public AbstractConfigListEntry<String> getEntry(String i13n, Field field, Object config, Object defaults, GuiRegistryAccess registry) {
            return ENTRY_BUILDER.startStrField(Text.translatable(i13n), Utils.getUnsafely(field, config, ItemList.getDefault()).toString())
                    .setDefaultValue(() -> Utils.getUnsafely(field, defaults).toString())
                    .setSaveConsumer(newValue -> Utils.setUnsafely(field, config, new ItemList(newValue)))
                    .setErrorSupplier(ItemList::validate)
                    .build();
        }
    }
}
