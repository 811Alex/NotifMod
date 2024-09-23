package eu.gflash.notifmod.client.sound;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Registers the mod's custom sound events.
 * @author Alex811
 */
public class CustomSounds {
    private static final String MOD_ID = "notifmod";

    public static void register(){
        try(
                InputStream is = CustomSounds.class.getResourceAsStream("/assets/notifmod/sounds.json");
                InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr);
                JsonReader jsonReader = new JsonReader(reader)
        ){
            Map<String, Object> soundMap = new Gson().fromJson(jsonReader, Map.class);
            soundMap.keySet().forEach(CustomSounds::register);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void register(String sound){
        Identifier id = Identifier.of(MOD_ID, sound);
        Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
