package eu.gflash.notifmod.client.sound;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Simple {@link net.minecraft.client.sound.SoundInstance} that follows the player.
 * @author Alex811
 */
public class FollowingSound extends MovingSoundInstance {
    private final ClientPlayerEntity player;

    public FollowingSound(ClientPlayerEntity player, SoundEvent soundEvent, float pitch, float volume) {
        super(soundEvent, SoundCategory.MASTER);
        this.player = player;
        this.repeat = false;
        this.pitch = pitch;
        this.volume = volume;
    }

    @Override
    public void tick() {
        if(!this.player.isRemoved()){
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }else this.setDone();
    }
}
