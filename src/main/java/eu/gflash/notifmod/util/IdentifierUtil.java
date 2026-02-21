package eu.gflash.notifmod.util;

import net.minecraft.resources.Identifier;

/**
 * Identifier related utility functions.
 * @author Alex811
 */
public class IdentifierUtil {
    public static boolean isValid(String namespace, String path){
        return Identifier.isValidPath(path) && Identifier.isValidNamespace(namespace);
    }

    public static boolean isValid(String id){
        int i = id.indexOf(':');
        if(!Identifier.isValidPath(i < 0 ? id : id.substring(i + 1))) return false;
        return i <= 0 || Identifier.isValidNamespace(id.substring(0, i));
    }
}
