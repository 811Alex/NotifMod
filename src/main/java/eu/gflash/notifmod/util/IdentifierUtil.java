package eu.gflash.notifmod.util;

import net.minecraft.util.Identifier;

/**
 * Identifier related utility functions.
 * @author Alex811
 */
public class IdentifierUtil {
    public static boolean isValid(String namespace, String path){
        return Identifier.isPathValid(path) && Identifier.isNamespaceValid(namespace);
    }

    public static boolean isValid(String id){
        int i = id.indexOf(':');
        if(!Identifier.isPathValid(i < 0 ? id : id.substring(i + 1))) return false;
        return i <= 0 || Identifier.isNamespaceValid(id.substring(0, i));
    }
}
